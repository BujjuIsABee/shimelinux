/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

use std::{
    cmp,
    sync::mpsc::{Sender, channel},
    thread,
};

use jni::{
    EnvUnowned,
    elements::ReleaseMode,
    errors::{Error, ThrowRuntimeExAndDefault},
    objects::{JClass, JIntArray, JObject},
    sys::{jboolean, jint, jlong},
};
use smithay_client_toolkit::{
    compositor::CompositorState,
    output::OutputState,
    registry::RegistryState,
    seat::SeatState,
    shell::{
        WaylandSurface,
        wlr_layer::{Anchor, Layer, LayerShell},
    },
    shm::{Shm, slot::SlotPool},
};
use wayland_client::{Connection, globals::registry_queue_init};

use crate::layer::{CursorState, LayerState, get_screen_rect};

mod layer;

#[derive(Default, Clone)]
pub struct Point {
    pub x: i32,
    pub y: i32,
}

#[derive(Default, Clone)]
pub struct Rect {
    pub x: i32,
    pub y: i32,
    pub width: i32,
    pub height: i32,
}

enum Event {
    SetBounds(Rect),
    SetImage(Vec<i32>),
    SetCursor(bool),
    Dispose(),
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createLayer<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    object: JObject<'caller>,
) -> jlong {
    unowned_env
        .with_env(|env| -> Result<jlong, Error> {
            let (sender, receiver) = channel::<Event>();

            let connection = Connection::connect_to_env().expect("Failed to get compositor state");
            let (globals, mut event_queue) = registry_queue_init(&connection).expect("Failed to initialize event queue");
            let qh = event_queue.handle();

            let compositor_state = CompositorState::bind(&globals, &qh)
                .expect("Failed to get compositor state");
            let layer_shell: LayerShell = LayerShell::bind(&globals, &qh)
                .expect("Failed to get layer shell");
            let shm = Shm::bind(&globals, &qh)
                .expect("Failed to get shm");
            let pool = SlotPool::new(256 * 256 * 4, &shm)
                .expect("Failed to create pool");

            let surface = compositor_state.create_surface(&qh);
            let layer = layer_shell.create_layer_surface(
                &qh,
                surface,
                Layer::Overlay,
                Some("shimelinux"),
                None,
            );

            layer.set_exclusive_zone(-1);
            layer.set_anchor(Anchor::TOP | Anchor::LEFT);
            layer.set_size(1, 1);
            layer.commit();

            let mut layer_state = LayerState {
                object: env.new_global_ref(object).expect("Failed to get global reference to object"),

                compositor_state,
                registry_state: RegistryState::new(&globals),
                output_state: OutputState::new(&globals, &qh),
                seat_state: SeatState::new(&globals, &qh),
                cursor_state: CursorState::default(),
                shm,
                pool,

                layer,
                layer_mask: Vec::new(),
                configured: false,
                image_rgb: Vec::new(),
                image_bounds: Rect::default(),
            };

            thread::spawn(move || {
                loop {
                    let _ = event_queue.blocking_dispatch(&mut layer_state);

                    // Handle events
                    while let Ok(event) = receiver.try_recv() {
                        match event {
                            Event::SetBounds(bounds) => {
                                layer_state.set_bounds(bounds);
                            }
                            Event::SetImage(rgb) => {
                                layer_state.set_image(rgb);
                            }
                            Event::SetCursor(use_hand) => {
                                layer_state.set_cursor(&connection, &qh, use_hand);
                            }
                            Event::Dispose() => {
                                layer_state.dispose();
                            }
                        }
                    }
                }
            });

            Ok(Box::into_raw(Box::new(sender)) as jlong) // Return a raw pointer to the sender
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_setBounds<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_ptr: jlong,
    x: jint,
    y: jint,
    width: jint,
    height: jint,
) {
    unowned_env
        .with_env(|_env| -> Result<(), Error> {
            let sender = unsafe { &*(sender_ptr as *const Sender<Event>) };
            sender.send(Event::SetBounds(Rect {
                x: cmp::max(-width + 1, x),
                y: cmp::max(-height + 1, y),
                width: cmp::max(1, width),
                height: cmp::max(1, height),
            })).expect("Failed to send SetBounds event");

            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_setImage<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_ptr: jlong,
    rgb: JIntArray,
) {
    unowned_env
        .with_env(|env| -> Result<(), Error> {
            let rgb = unsafe {
                rgb.get_elements(env, ReleaseMode::NoCopyBack).expect("Failed to get array elements")
            };

            let sender = unsafe { &*(sender_ptr as *const Sender<Event>) };
            sender.send(Event::SetImage(rgb.to_vec())).expect("Failed to send SetImage event");

            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_setCursor<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_ptr: jlong,
    use_hand: jboolean,
) {
    unowned_env
        .with_env(|_env| -> Result<(), Error> {
            let sender = unsafe { &*(sender_ptr as *const Sender<Event>) };
            sender.send(Event::SetCursor(use_hand)).expect("Failed to send SetCursor event");

            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_dispose<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_ptr: jlong,
) {
    unowned_env
        .with_env(|_env| -> Result<(), Error> {
            let sender = unsafe { &*(sender_ptr as *const Sender<Event>) };
            sender.send(Event::Dispose()).expect("Failed to send dispose event");

            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getScreenRect<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> JIntArray<'caller> {
    unowned_env
        .with_env(|env| -> Result<_, Error> {
            let screen_rect = get_screen_rect();

            let array = JIntArray::new(env, 4).expect("Failed to create array");
            array
                .set_region(
                    env,
                    0,
                    &[
                        screen_rect.x,
                        screen_rect.y,
                        screen_rect.width,
                        screen_rect.height,
                    ],
                )
                .expect("Failed to set array");

            Ok(array)
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

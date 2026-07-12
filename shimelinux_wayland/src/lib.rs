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

mod mascot;

use std::{
    cmp, mem, sync::{
        Mutex,
        mpsc::{Sender, channel},
    }, thread,
};

use jni::{
    EnvUnowned,
    elements::ReleaseMode,
    errors::{Error, ThrowRuntimeExAndDefault},
    objects::{JBooleanArray, JClass, JIntArray},
};
use smithay_client_toolkit::{
    compositor::CompositorState,
    shell::{
        WaylandSurface,
        wlr_layer::{Anchor, KeyboardInteractivity, Layer, LayerShell},
    },
};
use wayland_client::{Connection, globals::registry_queue_init};

use crate::mascot::MouseState;

enum Event {
    SetBounds(i32, i32, i32, i32),
    UpdateImage(Vec<i32>),
    Dispose(),
}

static SENDERS: Mutex<Vec<Sender<Event>>> = Mutex::new(Vec::new());

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createMascot<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> i32 {
    let (sender, receiver) = channel::<Event>();
    let mut senders = SENDERS.lock().unwrap();
    senders.push(sender);
    let sender_index = (senders.len() - 1) as i32;

    let connection = Connection::connect_to_env().unwrap();
    let (globals, mut event_queue) = registry_queue_init(&connection).unwrap();
    let qh = event_queue.handle();

    let compositor_state = CompositorState::bind(&globals, &qh).expect("Failed to get compositor state");
    let layer_shell = LayerShell::bind(&globals, &qh).expect("Failed to create layer shell");

    let surface = compositor_state.create_surface(&qh);
    let layer = layer_shell.create_layer_surface(&qh, surface, Layer::Top, Some("shimelinux"), None);
    layer.set_exclusive_zone(-1);
    layer.set_anchor(Anchor::TOP | Anchor::LEFT);
    layer.set_size(1, 1);
    layer.set_keyboard_interactivity(KeyboardInteractivity::None);
    layer.commit();

    let mut mascot = mascot::Mascot::new(compositor_state, layer, sender_index, &globals, &qh);

    thread::spawn(move || {
        loop {
            _ = event_queue.blocking_dispatch(&mut mascot);

            // Handle events
            while let Ok(event) = receiver.try_recv() {
                match event {
                    Event::SetBounds(x, y, width, height) => {
                        mascot.layer.set_size(cmp::max(1, width as u32), cmp::max(1, height as u32));
                        mascot.layer.set_margin(cmp::max(-height + 1, y), 0, 0, x);

                        mascot.image_width = width;
                    },
                    Event::UpdateImage(rgb) => {
                        mascot.mask = get_mask(&rgb, mascot.image_width, mascot.height as i32);
                        mascot.rgb = rgb;
                    },
                    Event::Dispose() => {
                        mascot.layer.wl_surface().destroy();
                    },
                }
            }
        }
    });

    sender_index
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_setBounds<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
    x: i32,
    y: i32,
    width: i32,
    height: i32,
) {
    let senders = SENDERS.lock().unwrap();
    if let Some(sender) = senders.get(sender_index as usize) {
        let _ = sender.send(Event::SetBounds(x, y, cmp::max(1, width), cmp::max(1, height)));
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_updateImage<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
    rgb: JIntArray,
) {
    let senders = SENDERS.lock().unwrap();
    if let Some(sender) = senders.get(sender_index as usize) {
        let outcome = unowned_env.with_env(|env| -> Result<_, Error> {
            let rgb = unsafe {
                rgb.get_elements(env, ReleaseMode::NoCopyBack).unwrap().to_vec()
            };

            _ = sender.send(Event::UpdateImage(rgb));
            Ok(())
        });

        outcome.resolve::<ThrowRuntimeExAndDefault>();
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getMouseState<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
) -> JBooleanArray<'caller> {
    let result = unowned_env.with_env(|env| -> Result<JBooleanArray, Error> {
        let array = JBooleanArray::new(env, 4).expect("Failed to get array");
        MouseState::get_mouse_state(sender_index, |mouse_state| {
            array.set_region(env, 0, &[
                mem::replace(&mut mouse_state.left_pressed, false),
                mem::replace(&mut mouse_state.right_pressed, false),
                mem::replace(&mut mouse_state.left_released, false),
                mem::replace(&mut mouse_state.right_released, false),
            ]).expect("Failed to set array");
        });

        Ok(array)
    });

    result.resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getMousePosition<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
) -> JIntArray<'caller> {
    let result = unowned_env.with_env(|env| -> Result<JIntArray, Error> {
        let array = JIntArray::new(env, 2).expect("Failed to get array");
        MouseState::get_mouse_state(sender_index, |mouse_state| {
            array.set_region(env, 0, &[
                mouse_state.position_x,
                mouse_state.position_y
            ]).expect("Failed to set array");
        });

        Ok(array)
    });

    result.resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_dispose<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
) {
    let senders = SENDERS.lock().unwrap();
    if let Some(sender) = senders.get(sender_index as usize) {
        _ = sender.send(Event::Dispose());
    }
}

fn get_mask(rgb: &Vec<i32>, width: i32, height: i32) -> Vec<(i32, i32, i32, i32)> {
    let mut rects: Vec<(i32, i32, i32, i32)> = Vec::new();

    for y in 0..height {
        let mut start: Option<i32> = None;
        for x in 0..width {
            let index = ((y * width) + x) as usize;
            let alpha = (rgb[index] >> 24) & 0xFF;
            if alpha > 0 && start.is_none() {
                start = Some(x);
            } else if alpha == 0 && start.is_some() {
                rects.push((start.unwrap(), y, x - start.unwrap(), 1));
                start = None;
            }
        }
    }

    rects
}

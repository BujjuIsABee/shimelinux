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
    sync::{Mutex, mpsc},
    thread,
};

use jni::{
    EnvUnowned, Outcome,
    elements::ReleaseMode,
    errors::{Error, ThrowRuntimeExAndDefault},
    objects::{JClass, JIntArray, JObject},
    sys::jboolean,
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

use crate::mascot::{CursorState, Mascot, get_cursor_position, get_screen_rect};

mod mascot;

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

static SENDERS: Mutex<Vec<mpsc::Sender<Event>>> = Mutex::new(Vec::new());

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createMascot<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    object: JObject,
) -> i32 {
    let mut senders = SENDERS.lock().unwrap();
    let (sender, receiver) = mpsc::channel::<Event>();
    senders.push(sender);

    let conn = Connection::connect_to_env().unwrap();
    let (globals, mut event_queue) = registry_queue_init(&conn).unwrap();
    let qh = event_queue.handle();

    let compositor_state = CompositorState::bind(&globals, &qh).expect("Failed to get compositor state");
    let layer_shell = LayerShell::bind(&globals, &qh).expect("Failed to get layer shell");
    let shm = Shm::bind(&globals, &qh).expect("Failed to get shm");
    let pool = SlotPool::new(256 * 256 * 4, &shm).expect("Failed to get pool");

    let surface = compositor_state.create_surface(&qh);
    let layer = layer_shell.create_layer_surface(
        &qh,
        surface,
        Layer::Overlay,
        Some("shimelinux"),
        None
    );

    layer.set_exclusive_zone(-1);
    layer.set_anchor(Anchor::TOP | Anchor::LEFT);
    layer.set_size(1, 1);
    layer.commit();

    let (jvm, object) = match unowned_env
        .with_env(|env| -> Result<_, Error> {
            Ok((
                env.get_java_vm().unwrap(),
                env.new_global_ref(object).unwrap(),
            ))
        })
        .into_outcome()
    {
        Outcome::Ok((jvm, object)) => (jvm, object),
        _ => panic!("Failed to get JVM"),
    };

    let mut mascot = Mascot {
        jvm,
        object,
        compositor_state,
        registry_state: RegistryState::new(&globals),
        output_state: OutputState::new(&globals, &qh),
        seat_state: SeatState::new(&globals, &qh),
        cursor_state: CursorState::default(),
        shm,
        pool,
        layer,
        layer_width: 0,
        layer_height: 0,
        layer_mask: Vec::new(),
        configured: false,
        image_rgb: Vec::new(),
        image_bounds: Rect::default(),
    };

    thread::spawn(move || {
        loop {
            _ = event_queue.blocking_dispatch(&mut mascot);

            // Handle events
            while let Ok(event) = receiver.try_recv() {
                match event {
                    Event::SetBounds(bounds) => {
                        mascot.set_bounds(bounds);
                    }
                    Event::SetImage(rgb) => {
                        mascot.set_image(rgb);
                    }
                    Event::SetCursor(use_hand) => {
                        mascot.set_cursor(&conn, &qh, use_hand);
                    }
                    Event::Dispose() => {
                        mascot.dispose();
                    }
                }
            }
        }
    });

    senders.len() as i32 - 1 // return the sender index; it is used as an identifier for the mascot
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
        let _ = sender.send(Event::SetBounds(Rect {
            x: cmp::max(-width + 1, x),
            y: cmp::max(-height + 1, y),
            width: cmp::max(1, width),
            height: cmp::max(1, height),
        }));
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_setImage<'caller>(
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

            Ok(rgb)
        });

        _ = sender.send(Event::SetImage(outcome.resolve::<ThrowRuntimeExAndDefault>()));
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_setCursor<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
    use_hand: jboolean,
) {
    let senders = SENDERS.lock().unwrap();
    if let Some(sender) = senders.get(sender_index as usize) {
        _ = sender.send(Event::SetCursor(use_hand));
    }
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

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getScreenRect<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> JIntArray<'caller> {
    let outcome = unowned_env.with_env(|env| -> Result<_, Error> {
        let array = JIntArray::new(env, 4).unwrap();
        let screen_rect = get_screen_rect();
        array.set_region(env, 0, &[
            screen_rect.x,
            screen_rect.y,
            screen_rect.width,
            screen_rect.height,
        ]).expect("Failed to set array");

        Ok(array)
    });

    outcome.resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getCursorPosition<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> JIntArray<'caller> {
    let outcome = unowned_env.with_env(|env| -> Result<_, Error> {
        let array = JIntArray::new(env, 2).unwrap();
        let cursor_position = get_cursor_position();
        array.set_region(env, 0, &[
            cursor_position.x,
            cursor_position.y,
        ]).expect("Failed to set array");

        Ok(array)
    });

    outcome.resolve::<ThrowRuntimeExAndDefault>()
}

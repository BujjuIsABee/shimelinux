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
    cmp, mem,
    sync::{
        Mutex,
        mpsc::{Sender, channel},
    },
    thread,
};

use jni::{
    EnvUnowned,
    elements::ReleaseMode,
    errors::{Error, ThrowRuntimeExAndDefault},
    objects::{JBooleanArray, JClass, JIntArray},
    sys::jboolean,
};
use smithay_client_toolkit::{
    compositor::CompositorState,
    shell::{
        WaylandSurface,
        wlr_layer::{Anchor, Layer, LayerShell},
    },
};
use wayland_client::{Connection, globals::registry_queue_init};
use wayland_cursor::CursorTheme;

use crate::mascot::MouseState;
use crate::mascot::{Mascot, Rectangle};

enum Event {
    SetBounds(i32, i32, i32, i32),
    UpdateImage(Vec<i32>),
    SetCursor(bool),
    Dispose(),
}

static SENDERS: Mutex<Vec<Sender<Event>>> = Mutex::new(Vec::new());

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createMascot<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> i32 {
    // Get the sender and receiver
    let mut senders = SENDERS.lock().unwrap();
    let (sender, receiver) = channel::<Event>();
    let sender_index = senders.len() as i32; // used as an identifier for the mascot
    senders.push(sender);

    // Connect to the Wayland server
    let connection = Connection::connect_to_env().unwrap();
    let (globals, mut event_queue) = registry_queue_init(&connection).unwrap();
    let qh = event_queue.handle();

    let compositor_state = CompositorState::bind(&globals, &qh)
        .expect("Failed to get compositor state");
    let layer_shell = LayerShell::bind(&globals, &qh)
        .expect("Failed to create layer shell");

    // Create the layer
    let surface = compositor_state.create_surface(&qh);
    let layer = layer_shell.create_layer_surface(&qh, surface, Layer::Top, Some("shimelinux"), None);
    layer.set_exclusive_zone(-1);
    layer.set_anchor(Anchor::TOP | Anchor::LEFT);
    layer.set_size(1, 1);
    layer.commit();

    let mut mascot = Mascot::new(&globals, &qh, compositor_state, layer, sender_index);

    thread::spawn(move || {
        loop {
            _ = event_queue.blocking_dispatch(&mut mascot);

            // Handle events
            while let Ok(event) = receiver.try_recv() {
                match event {
                    Event::SetBounds(x, y, width, height) => {
                        // Set the dimensions
                        // cmp::max is used to avoid setting values below 1
                        mascot.layer.set_size(
                            cmp::max(1, width as u32),
                            cmp::max(1, height as u32),
                        );

                        // Set the position
                        mascot.layer.set_margin(
                            y - mascot.offset_y.unwrap_or_default(),
                            0,
                            0,
                            x - mascot.offset_x.unwrap_or_default(),
                        );

                        // Store the requested dimensions
                        mascot.image_width = width as u32;
                        mascot.image_height = height as u32;
                    }
                    Event::UpdateImage(rgb) => {
                        mascot.mask = get_mask(&rgb, mascot.image_width, mascot.image_height);
                        mascot.rgb = rgb;
                    }
                    Event::SetCursor(use_hand) => {
                        let mut cursor_theme = CursorTheme::load(&connection, mascot.shm.wl_shm().clone(), 24)
                            .expect("Failed to get cursor theme");

                        if let Some(cursor) = cursor_theme.get_cursor(if use_hand { "pointer" } else { "left_ptr" }) {
                            let cursor_surface = mascot.cursor_surface.get_or_insert(mascot.compositor_state.create_surface(&qh));

                            // Attach None to remove the previous cursor image buffer
                            cursor_surface.attach(None, 0, 0);
                            cursor_surface.commit();

                            // Attach the new cursor image buffer
                            cursor_surface.attach(Some(&cursor[0]), 0, 0);
                            cursor_surface.commit();

                            mascot.cursor_surface = Some(cursor_surface.clone());
                        }
                    }
                    Event::Dispose() => {
                        mascot.layer.wl_surface().destroy();
                    }
                }
            }
        }
    });

    sender_index // return the sender index/identifier to kotlin
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
        // cmp::max is used on the position to prevent the mascot from going fully offscreen, which causes visual issues on niri
        // it is used on the dimensions to prevent the width and height from going below 1, which is not allowed
        let _ = sender.send(Event::SetBounds(
            cmp::max(-width + 1, x),
            cmp::max(-height + 1, y),
            cmp::max(1, width),
            cmp::max(1, height),
        ));
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
            // Convert the JIntArray to a Vec<i32>
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
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getScreen<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> JIntArray<'caller> {
    let outcome = unowned_env.with_env(|env| -> Result<JIntArray, Error> {
        let array = JIntArray::new(env, 4).expect("Failed to get array");
        Mascot::get_screen(|screen| {
            array.set_region(env, 0, &[
                screen.x,
                screen.y,
                screen.width,
                screen.height,
            ]).expect("Failed to set array");
        });

        Ok(array)
    });

    outcome.resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getMouseState<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
) -> JBooleanArray<'caller> {
    let outcome = unowned_env.with_env(|env| -> Result<JBooleanArray, Error> {
        let array = JBooleanArray::new(env, 4).expect("Failed to get array");
        MouseState::get(sender_index, |mouse_state| {
            // mem::replace is used to get the value of the bool, then set it to false so the press/release is only reported once
            array.set_region(env, 0, &[
                mem::replace(&mut mouse_state.left_pressed, false),
                mem::replace(&mut mouse_state.right_pressed, false),
                mem::replace(&mut mouse_state.left_released, false),
                mem::replace(&mut mouse_state.right_released, false),
            ]).expect("Failed to set array");
        });

        Ok(array)
    });

    outcome.resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getMousePosition<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
) -> JIntArray<'caller> {
    let outcome = unowned_env.with_env(|env| -> Result<JIntArray, Error> {
        let array = JIntArray::new(env, 2).expect("Failed to get array");
        MouseState::get(sender_index, |mouse_state| {
            array.set_region(env, 0, &[
                mouse_state.position_x,
                mouse_state.position_y
            ]).expect("Failed to set array");
        });

        Ok(array)
    });

    outcome.resolve::<ThrowRuntimeExAndDefault>()
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

fn get_mask(rgb: &Vec<i32>, width: u32, height: u32) -> Vec<Rectangle> {
    let mut rects: Vec<Rectangle> = Vec::new();

    for y in 0..height {
        let mut start: Option<u32> = None; // the mask is divided into rows of non-transparent pixels to reduce memory usage for larger images
        for x in 0..width {
            let index = cmp::min(((y * width) + x) as usize, rgb.len() - 1); // cmp::min is used to prevent an index out of bounds error
            let alpha = (rgb[index] >> 24) & 0xFF;
            if alpha > 0 && start.is_none() {
                // Start a new row
                start = Some(x);
            } else if alpha == 0 && start.is_some() {
                // End the current row and add it to rects
                rects.push(Rectangle {
                    x: start.unwrap() as i32,
                    y: y as i32,
                    width: (x - start.unwrap()) as i32,
                    height: 1,
                });
                start = None;
            }
        }
    }

    rects
}

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

#![allow(deprecated, clippy::too_many_arguments)]

use core::slice;
use std::{
    collections::HashMap,
    sync::{LazyLock, mpsc},
    thread,
};

use jni::{
    Env, bind_java_type,
    elements::ReleaseMode,
    objects::JIntArray,
    refs::Global,
    sys::{jboolean, jint},
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
use wayland_cursor::CursorTheme;

use crate::state::{CursorState, LayerState, Rect, SCREEN_RECT, State};

static EVENT_SENDER: LazyLock<mpsc::Sender<Event>> = LazyLock::new(|| {
    // Get the event sender and receiver
    let (sender, receiver) = mpsc::channel::<Event>();

    // Connect to the Wayland server
    let conn = Connection::connect_to_env().expect("Failed to connect to Wayland server");
    let (globals, mut event_queue) = registry_queue_init(&conn).unwrap();
    let qh = event_queue.handle();

    let compositor = CompositorState::bind(&globals, &qh).expect("wl_compositor not available");
    let layer_shell = LayerShell::bind(&globals, &qh).expect("wlr_layer_shell not available");
    let shm = Shm::bind(&globals, &qh).expect("wl_shm not available");
    let pool = SlotPool::new(128 * 128 * 4, &shm).expect("Failed to create pool");
    let mut state = State {
        compositor_state: compositor,
        layer_shell,
        registry_state: RegistryState::new(&globals),
        output_state: OutputState::new(&globals, &qh),
        seat_state: SeatState::new(&globals, &qh),
        shm,
        pool,
        output: None,
        pointer: None,
        pointer_surface: None,
        serial: None,
        layers: HashMap::new(),
    };

    // Spawn a thread for the event loop
    thread::spawn(move || {
        loop {
            let _ = event_queue.blocking_dispatch(&mut state);

            // Handle events
            while let Ok(event) = receiver.try_recv() {
                match event {
                    Event::CreateLayer(
                        id,
                        mascot_layer,
                        mouse_event_receiver,
                        use_input_region,
                    ) => {
                        let surface = state.compositor_state.create_surface(&qh);
                        let output = state.output.as_ref();
                        let layer_surface = state.layer_shell.create_layer_surface(
                            &qh,
                            surface,
                            match mascot_layer {
                                0 => Layer::Background,
                                1 => Layer::Bottom,
                                2 => Layer::Top,
                                _ => Layer::Overlay,
                            },
                            Some("shimelinux"),
                            output,
                        );

                        layer_surface.set_exclusive_zone(-1);
                        layer_surface.set_anchor(Anchor::TOP | Anchor::LEFT);
                        layer_surface.set_size(1, 1);
                        layer_surface.commit();

                        state.layers.insert(
                            id,
                            LayerState {
                                layer_surface,
                                configured: false,
                                rgb: &[],
                                bounds: Rect::default(),
                                image_changed: true,
                                cursor_state: CursorState::default(),
                                mouse_event_receiver,
                                use_input_region,
                                input_region: Vec::new(),
                            },
                        );
                    }
                    Event::SetBounds(id, x, y, width, height) => {
                        if let Some(layer) = state.layers.get_mut(&id) {
                            layer.bounds = Rect {
                                x,
                                y,
                                width,
                                height,
                            };

                            layer.layer_surface.set_margin(y, 0, 0, x);
                        }
                    }
                    Event::SetImage(id, rgb) => {
                        if let Some(layer) = state.layers.get_mut(&id) {
                            layer.rgb = rgb;
                            layer.image_changed = true;

                            if layer.use_input_region {
                                layer.update_input_region();
                            }
                        }
                    }
                    Event::SetCursor(use_hand) => {
                        let shm = state.shm.wl_shm().clone();
                        let Ok(mut theme) = CursorTheme::load(&conn, shm, 24) else {
                            continue;
                        };

                        let name = if use_hand { "pointer" } else { "left_ptr" };
                        let Some(cursor) = theme.get_cursor(name) else {
                            continue;
                        };

                        let cursor_surface = state
                            .pointer_surface
                            .get_or_insert(state.compositor_state.create_surface(&qh));

                        // Attach None to clear the previous buffer
                        cursor_surface.attach(None, 0, 0);
                        cursor_surface.commit();

                        // Attach the new buffer
                        cursor_surface.attach(Some(&cursor[0]), 0, 0);
                        cursor_surface.commit();
                    }
                    Event::Dispose(id) => {
                        state.layers.remove(&id);
                    }
                }
            }
        }
    });

    sender
});

enum Event {
    CreateLayer(i32, i32, Global<MouseEventReceiver<'static>>, bool),
    SetBounds(i32, i32, i32, i32, i32),
    SetImage(i32, &'static [i32]),
    SetCursor(bool),
    Dispose(i32),
}

bind_java_type! {
    pub MouseEventReceiver => io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver,
    methods {
        fn update_cursor(
            left_pressed: jboolean,
            right_pressed: jboolean,
            left_released: jboolean,
            right_released: jboolean,
            position_x: jint,
            position_y: jint,
        )
    }
}

bind_java_type! {
    WaylandLib => io.github.bujjuisabee.shimelinux.wayland.lib.WaylandLib,
    type_map {
        MouseEventReceiver => io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver,
    },
    native_methods {
        extern fn create_layer(mascot_layer: jint, id: jint, mouse_event_receiver: MouseEventReceiver, use_input_region: jboolean),
        extern fn set_bounds(id: jint, x: jint, y: jint, width: jint, height: jint),
        extern fn set_image(id: jint, rgb: [jint]),
        extern fn set_cursor(use_hand: jboolean),
        extern fn get_screen_rect() -> [jint],
        extern fn dispose(id: jint),
    },
}

impl WaylandLibNativeInterface for WaylandLibAPI {
    type Error = jni::errors::Error;

    fn create_layer<'local>(
        env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        id: jint,
        mascot_layer: jint,
        mouse_event_receiver: MouseEventReceiver<'local>,
        use_input_region: jboolean,
    ) -> jni::errors::Result<()> {
        EVENT_SENDER
            .send(Event::CreateLayer(
                id,
                mascot_layer,
                env.new_global_ref(mouse_event_receiver).unwrap(),
                use_input_region,
            ))
            .expect("Failed to send CreateLayer event");

        Ok(())
    }

    fn set_bounds<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        id: jint,
        x: jint,
        y: jint,
        width: jint,
        height: jint,
    ) -> jni::errors::Result<()> {
        EVENT_SENDER
            .send(Event::SetBounds(
                id,
                x.max(-width + 1),
                y.max(-height + 1),
                width.max(1),
                height.max(1),
            ))
            .expect("Failed to send SetBounds event");

        Ok(())
    }

    fn set_image<'local>(
        env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        id: jint,
        rgb: JIntArray,
    ) -> jni::errors::Result<()> {
        let elements = unsafe { rgb.get_elements_critical(env, ReleaseMode::NoCopyBack)? };
        let slice = unsafe { slice::from_raw_parts(elements.as_ptr(), elements.len()) };

        EVENT_SENDER
            .send(Event::SetImage(id, slice))
            .expect("Failed to send SetImage event");

        Ok(())
    }

    fn set_cursor<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        use_hand: jboolean,
    ) -> jni::errors::Result<()> {
        EVENT_SENDER
            .send(Event::SetCursor(use_hand))
            .expect("Failed to send SetCursor event");

        Ok(())
    }

    fn get_screen_rect<'local>(
        env: &mut Env<'local>,
        _this: WaylandLib<'local>,
    ) -> jni::errors::Result<JIntArray<'local>> {
        let array = env.new_int_array(4)?;
        let screen_rect = SCREEN_RECT.lock().unwrap();
        array.set_region(
            env,
            0,
            &[
                screen_rect.x,
                screen_rect.y,
                screen_rect.width,
                screen_rect.height,
            ],
        )?;

        Ok(array)
    }

    fn dispose<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        id: jint,
    ) -> jni::errors::Result<()> {
        EVENT_SENDER
            .send(Event::Dispose(id))
            .expect("Failed to send Dispose event");

        Ok(())
    }
}

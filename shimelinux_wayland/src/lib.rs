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

#![allow(deprecated)]

use std::{sync::mpsc, thread};

use jni::{
    Env, bind_java_type,
    elements::ReleaseMode,
    objects::JIntArray,
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

use crate::layer::LayerState;

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
    SetImage(Vec<i32>, bool),
    SetCursor(bool),
    Dispose(),
}

bind_java_type! {
    MouseEventReceiver => "io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver",
    methods {
        #[allow(clippy::too_many_arguments)]
        fn update_cursor(
            left_pressed: jboolean,
            right_pressed: jboolean,
            left_released: jboolean,
            right_released: jboolean,
            position_x: jint,
            position_y: jint,
        )
    },
}

bind_java_type! {
    WaylandLib => "io.github.bujjuisabee.shimelinux.wayland.lib.WaylandLib",
    type_map {
        MouseEventReceiver => "io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver",
    },
    native_methods {
        extern fn create_layer(mascot_layer: jint, mouse_event_receiver: MouseEventReceiver) -> jlong,
        extern fn set_bounds(sender_ptr: jlong, x: jint, y: jint, width: jint, height: jint),
        extern fn set_image(sender_ptr: jlong, rgb: [jint], update_input_region: jboolean),
        extern fn set_cursor(sender_ptr: jlong, use_hand: jboolean),
        extern fn dispose(sender_ptr: jlong),
    },
}

impl WaylandLibNativeInterface for WaylandLibAPI {
    type Error = jni::errors::Error;

    fn create_layer<'local>(
        env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        mascot_layer: jint,
        mouse_event_receiver: MouseEventReceiver<'local>,
    ) -> jni::errors::Result<jlong> {
        // Get the event sender and receiver
        let (sender, receiver) = mpsc::channel::<Event>();

        // Create the layer
        let conn = Connection::connect_to_env().expect("Failed to connect to Wayland server");
        let (globals, mut event_queue) = registry_queue_init(&conn).unwrap();
        let qh = event_queue.handle();

        let compositor = CompositorState::bind(&globals, &qh).expect("wl_compositor not available");
        let surface = compositor.create_surface(&qh);
        let layer_shell = LayerShell::bind(&globals, &qh).expect("wlr_layer_shell not available");
        let layer = layer_shell.create_layer_surface(
            &qh,
            surface,
            match mascot_layer {
                0 => Layer::Background,
                1 => Layer::Bottom,
                2 => Layer::Top,
                _ => Layer::Overlay,
            },
            Some("shimelinux"),
            None,
        );

        layer.set_exclusive_zone(-1);
        layer.set_anchor(Anchor::TOP | Anchor::LEFT);
        layer.set_size(1, 1);
        layer.commit();

        let shm = Shm::bind(&globals, &qh).expect("wl_shm not available");
        let pool = SlotPool::new(128 * 128 * 4, &shm).expect("Failed to create pool");

        let mut layer_state = LayerState {
            compositor_state: compositor,
            registry_state: RegistryState::new(&globals),
            output_state: OutputState::new(&globals, &qh),
            seat_state: SeatState::new(&globals, &qh),
            shm,

            layer,
            configured: false,
            pool,
            image_rgb: Vec::new(),
            image_bounds: Rect::default(),
            image_changed: true,
            layer_mask: Vec::new(),
            mouse_event_receiver: env.new_global_ref(mouse_event_receiver).unwrap(),
            pointer: None,
            pointer_surface: None,
            serial: None,
        };

        // Get a JavaVM so it can be attached to the thread. This ensures that the global reference
        // to mouse_event_receiver can be freed without pausing to attach the JVM to the thread.
        let jvm = env.get_java_vm().unwrap();

        // Spawn a thread for the event loop
        thread::spawn(move || {
            let _ = jvm.attach_current_thread(|_env| -> jni::errors::Result<_> {
                'outer: loop {
                    let _ = event_queue.blocking_dispatch(&mut layer_state);

                    // Handle events
                    while let Ok(event) = receiver.try_recv() {
                        match event {
                            Event::SetBounds(bounds) => {
                                layer_state.set_bounds(bounds);
                            }
                            Event::SetImage(rgb, update_input_region) => {
                                layer_state.set_image(rgb, update_input_region);
                            }
                            Event::SetCursor(use_hand) => {
                                layer_state.set_cursor(&conn, &qh, use_hand);
                            }
                            Event::Dispose() => {
                                layer_state.dispose();
                                layer_state.mouse_event_receiver.into_raw();
                                break 'outer;
                            }
                        }
                    }
                }

                Ok(())
            });
        });

        // Return a raw pointer to the event sender
        Ok(Box::into_raw(Box::new(sender)) as jlong)
    }

    fn set_bounds<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        x: jint,
        y: jint,
        width: jint,
        height: jint,
    ) -> jni::errors::Result<()> {
        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::SetBounds(Rect {
                x: x.max(-width + 1),
                y: y.max(-height + 1),
                width: width.max(1),
                height: height.max(1),
            }))
            .expect("Failed to send SetBounds event");

        Ok(())
    }

    fn set_image<'local>(
        env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        rgb: JIntArray,
        update_input_region: jboolean,
    ) -> jni::errors::Result<()> {
        let elements = unsafe { rgb.get_elements(env, ReleaseMode::NoCopyBack)? };

        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::SetImage(elements.to_vec(), update_input_region))
            .expect("Failed to send SetImage event");

        Ok(())
    }

    fn set_cursor<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        use_hand: jboolean,
    ) -> jni::errors::Result<()> {
        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::SetCursor(use_hand))
            .expect("Failed to send SetCursor event");

        Ok(())
    }

    fn dispose<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
    ) -> jni::errors::Result<()> {
        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::Dispose())
            .expect("Failed to send dispose event");

        // Free sender
        unsafe { drop(Box::from_raw(sender_ptr as *mut mpsc::Sender<Event>)) };

        Ok(())
    }
}

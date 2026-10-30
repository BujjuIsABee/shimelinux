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
use std::{sync::mpsc, thread};

use jni::{
    Env, bind_java_type,
    elements::ReleaseMode,
    objects::JIntArray,
    refs::Global,
    sys::{jboolean, jint, jlong},
    vm::JavaVM,
};
use smithay_client_toolkit::{
    compositor::{CompositorHandler, CompositorState, FrameCallbackData},
    delegate_dispatch2, delegate_registry,
    output::{OutputHandler, OutputState},
    registry::{ProvidesRegistryState, RegistryState},
    registry_handlers,
    seat::{
        Capability, SeatHandler, SeatState,
        pointer::{BTN_LEFT, BTN_RIGHT, PointerEvent, PointerEventKind, PointerHandler},
    },
    shell::{
        WaylandSurface,
        wlr_layer::{
            Anchor, Layer, LayerShell, LayerShellHandler, LayerSurface, LayerSurfaceConfigure,
        },
    },
    shm::{Shm, ShmHandler, slot::SlotPool},
};
use tokio::sync::oneshot;
use wayland_client::{
    Connection, QueueHandle, delegate_noop,
    globals::registry_queue_init,
    protocol::{
        wl_output::{Transform, WlOutput},
        wl_pointer::WlPointer,
        wl_region::WlRegion,
        wl_seat::WlSeat,
        wl_shm::Format,
        wl_surface::WlSurface,
    },
};
use wayland_cursor::CursorTheme;

#[derive(Default)]
struct Rect {
    x: i32,
    y: i32,
    width: i32,
    height: i32,
}

#[derive(Default)]
struct CursorState {
    left_pressed: bool,
    right_pressed: bool,
    left_released: bool,
    right_released: bool,
}

struct State {
    compositor_state: CompositorState,
    layer_shell: LayerShell,
    registry_state: RegistryState,
    output_state: OutputState,
    seat_state: SeatState,
    shm: Shm,
    pool: SlotPool,
    layers: Vec<LayerState>,
    output: Option<WlOutput>,
    pointer: Option<WlPointer>,
    pointer_surface: Option<WlSurface>,
    serial: Option<u32>,
}

struct LayerState {
    layer_surface: LayerSurface,
    configured: bool,
    rgb: &'static [i32],
    bounds: Rect,
    image_changed: bool,
    cursor_state: CursorState,
    mouse_event_receiver: Global<MouseEventReceiver<'static>>,
    use_input_region: bool,
    input_region: Vec<Rect>,
}

enum Event {
    CreateLayer(
        oneshot::Sender<i32>,
        Global<MouseEventReceiver<'static>>,
        bool,
    ),
    SetBounds(i32, i32, i32, i32, i32),
    SetImage(i32, &'static [i32]),
    SetCursor(bool),
    Dispose(i32),
}

impl CompositorHandler for State {
    fn scale_factor_changed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &WlSurface,
        _new_factor: i32,
    ) {
    }

    fn transform_changed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &WlSurface,
        _new_transform: Transform,
    ) {
    }

    fn frame(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        surface: &WlSurface,
        _time: u32,
    ) {
        if let Some(layer) = self
            .layers
            .iter_mut()
            .find(|l| l.layer_surface.wl_surface() == surface)
        {
            layer.draw(&mut self.pool, &self.compositor_state, qh);
        }
    }

    fn surface_enter(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &WlSurface,
        output: &WlOutput,
    ) {
        self.output.get_or_insert(output.clone());
    }

    fn surface_leave(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &WlSurface,
        _output: &WlOutput,
    ) {
    }
}

impl OutputHandler for State {
    fn output_state(&mut self) -> &mut OutputState {
        &mut self.output_state
    }

    fn new_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {}

    fn update_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {}

    fn output_destroyed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {
    }
}

impl LayerShellHandler for State {
    fn closed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _layer: &LayerSurface) {}

    fn configure(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        layer: &LayerSurface,
        _configure: LayerSurfaceConfigure,
        _serial: u32,
    ) {
        if let Some(layer) = self.layers.iter_mut().find(|l| l.layer_surface == *layer)
            && !layer.configured
        {
            layer.configured = true;
            layer.draw(&mut self.pool, &self.compositor_state, qh);
        }
    }
}

impl SeatHandler for State {
    fn seat_state(&mut self) -> &mut SeatState {
        &mut self.seat_state
    }

    fn new_seat(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _seat: WlSeat) {}

    fn new_capability(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        seat: WlSeat,
        capability: Capability,
    ) {
        if capability == Capability::Pointer && self.pointer.is_none() {
            self.pointer = self.seat_state.get_pointer(qh, &seat).ok();
        }
    }

    fn remove_capability(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _seat: WlSeat,
        capability: Capability,
    ) {
        if capability == Capability::Pointer && self.pointer.is_some() {
            self.pointer.take().unwrap().release();
        }
    }

    fn remove_seat(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _seat: WlSeat) {}
}

impl PointerHandler for State {
    fn pointer_frame(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _pointer: &WlPointer,
        events: &[PointerEvent],
    ) {
        use PointerEventKind::*;

        let mut layers = Vec::<*mut LayerState>::new();
        let (mut position_x, mut position_y) = (0.0, 0.0);

        for event in events {
            if let Some(layer) = self
                .layers
                .iter_mut()
                .find(|l| *l.layer_surface.wl_surface() == event.surface)
            {
                (position_x, position_y) = event.position;

                match event.kind {
                    Enter { serial } => {
                        self.serial = Some(serial);
                    }
                    Leave { serial } => {
                        self.serial = Some(serial);
                    }
                    Motion { .. } => {
                        if let (Some(pointer), Some(serial), Some(surface)) =
                            (&self.pointer, self.serial, &self.pointer_surface)
                        {
                            pointer.set_cursor(serial, Some(surface), 0, 0);
                        }
                    }
                    Press { button, .. } => {
                        layer.cursor_state.left_pressed = button == BTN_LEFT;
                        layer.cursor_state.right_pressed = button == BTN_RIGHT;
                    }
                    Release { button, .. } => {
                        layer.cursor_state.left_released = button == BTN_LEFT;
                        layer.cursor_state.right_released = button == BTN_RIGHT;
                    }
                    Axis { .. } => {}
                }

                layers.push(layer as *mut LayerState);
            }
        }

        if let Ok(jvm) = JavaVM::singleton() {
            let _ = jvm.attach_current_thread(|env| -> jni::errors::Result<()> {
                for layer_ptr in layers {
                    let layer = unsafe { &mut *layer_ptr };
                    layer.mouse_event_receiver.update_cursor(
                        env,
                        std::mem::replace(&mut layer.cursor_state.left_pressed, false),
                        std::mem::replace(&mut layer.cursor_state.right_pressed, false),
                        std::mem::replace(&mut layer.cursor_state.left_released, false),
                        std::mem::replace(&mut layer.cursor_state.right_released, false),
                        position_x as i32,
                        position_y as i32,
                    )?;
                }

                Ok(())
            });
        }
    }
}

impl ShmHandler for State {
    fn shm_state(&mut self) -> &mut Shm {
        &mut self.shm
    }
}

delegate_noop!(State: ignore WlRegion);

impl LayerState {
    fn draw(&mut self, pool: &mut SlotPool, compositor: &CompositorState, qh: &QueueHandle<State>) {
        let width = self.bounds.width.max(1);
        let height = self.bounds.height.max(1);
        let stride = width * 4;

        self.layer_surface.set_size(width as u32, height as u32);

        let surface = self.layer_surface.wl_surface();

        if self.image_changed {
            let (buffer, canvas) = pool
                .create_buffer(width, height, stride, Format::Argb8888)
                .expect("Failed to create buffer");

            if !self.rgb.is_empty() {
                for y in 0..height {
                    for x in 0..width {
                        let canvas_index = (((y * width + x) * 4) as usize).min(canvas.len() - 1);
                        let image_index = ((y * width + x) as usize).min(self.rgb.len() - 1);
                        let slice = &self.rgb[image_index].to_le_bytes();
                        canvas[canvas_index..canvas_index + 4].copy_from_slice(slice);
                    }
                }

                if !self.input_region.is_empty() {
                    let region = compositor.wl_compositor().create_region(qh, ());
                    for rect in &self.input_region {
                        region.add(rect.x, rect.y, rect.width, rect.height);
                    }
                    self.layer_surface.set_input_region(Some(&region));
                }
            }

            surface.damage_buffer(0, 0, width, height);
            let _ = buffer.attach_to(surface);
            self.image_changed = false;
        }

        surface.frame(qh, FrameCallbackData(surface.clone()));
        self.layer_surface.commit();
    }

    fn update_input_region(&mut self) {
        let mut rects: Vec<Rect> = Vec::new();
        let width = self.bounds.width;
        let height = self.bounds.height;

        for y in 0..height as u32 {
            let mut start: Option<u32> = None;
            for x in 0..width as u32 {
                let index = ((y * width as u32 + x) as usize).min(self.rgb.len() - 1);
                let alpha = (self.rgb[index] >> 24) & 0xFF;
                if alpha > 0 && start.is_none() {
                    start = Some(x);
                } else if alpha == 0 && start.is_some() {
                    let start = start.take().unwrap();
                    rects.push(Rect {
                        x: start as i32,
                        y: y as i32,
                        width: (x - start) as i32,
                        height: 1,
                    });
                }
            }
        }

        self.input_region = rects;
    }
}

delegate_registry!(State);

impl ProvidesRegistryState for State {
    fn registry(&mut self) -> &mut RegistryState {
        &mut self.registry_state
    }

    registry_handlers![];
}

delegate_dispatch2!(State);

bind_java_type! {
    MouseEventReceiver => io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver,
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
        extern fn init(mascot_layer: jint) -> jlong,
        extern fn create_layer(sender_ptr: jlong, mouse_event_receiver: MouseEventReceiver, use_input_region: jboolean) -> jint,
        extern fn set_bounds(sender_ptr: jlong, layer_index: jint, x: jint, y: jint, width: jint, height: jint),
        extern fn set_image(sender_ptr: jlong, layer_index: jint, rgb: [jint]),
        extern fn set_cursor(sender_ptr: jlong, use_hand: jboolean),
        extern fn dispose(sender_ptr: jlong, layer_index: jint),
    },
}

impl WaylandLibNativeInterface for WaylandLibAPI {
    type Error = jni::errors::Error;

    fn init<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        mascot_layer: jint,
    ) -> jni::errors::Result<jlong> {
        let (sender, receiver) = mpsc::channel::<Event>();

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
            layers: Vec::new(),
            output: None,
            pointer: None,
            pointer_surface: None,
            serial: None,
        };

        thread::spawn(move || {
            loop {
                event_queue.blocking_dispatch(&mut state).unwrap();

                // Handle events
                while let Ok(event) = receiver.try_recv() {
                    match event {
                        Event::CreateLayer(sender, mouse_event_receiver, use_input_region) => {
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

                            let index = state.layers.len();
                            state.layers.push(LayerState {
                                layer_surface,
                                configured: false,
                                rgb: &[],
                                bounds: Rect::default(),
                                image_changed: false,
                                cursor_state: CursorState::default(),
                                mouse_event_receiver,
                                use_input_region,
                                input_region: Vec::new(),
                            });

                            sender
                                .send(index as i32)
                                .expect("Failed to send layer index");
                        }
                        Event::SetBounds(layer_index, x, y, width, height) => {
                            let layer = &mut state.layers[layer_index as usize];
                            layer.bounds = Rect {
                                x: x.max(-width + 1),
                                y: y.max(-height + 1),
                                width: width.max(1),
                                height: height.max(1),
                            };
                            layer
                                .layer_surface
                                .set_margin(layer.bounds.y, 0, 0, layer.bounds.x);
                        }
                        Event::SetImage(layer_index, rgb) => {
                            let layer = &mut state.layers[layer_index as usize];
                            layer.image_changed = true;
                            layer.rgb = rgb;

                            if layer.use_input_region {
                                layer.update_input_region();
                            }
                        }
                        Event::SetCursor(use_hand) => {
                            if let Ok(mut theme) =
                                CursorTheme::load(&conn, state.shm.wl_shm().clone(), 24)
                                && let Some(cursor) =
                                    theme.get_cursor(if use_hand { "pointer" } else { "left_ptr" })
                            {
                                let surface = state
                                    .pointer_surface
                                    .get_or_insert(state.compositor_state.create_surface(&qh));
                                surface.attach(None, 0, 0);
                                surface.commit();
                                surface.attach(Some(&cursor[0]), 0, 0);
                                surface.commit();
                                state.pointer_surface = Some(surface.clone());
                            }
                        }
                        Event::Dispose(layer_index) => {
                            let layer = &mut state.layers[layer_index as usize];
                            layer.layer_surface.wl_surface().destroy();
                        }
                    }
                }
            }
        });

        Ok(Box::into_raw(Box::new(sender)) as jlong)
    }

    fn create_layer<'local>(
        env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        mouse_event_receiver: MouseEventReceiver<'local>,
        use_input_region: jboolean,
    ) -> jni::errors::Result<jint> {
        let (tx, rx) = oneshot::channel::<i32>();
        let global = env.new_global_ref(mouse_event_receiver).unwrap();

        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::CreateLayer(tx, global, use_input_region))
            .expect("Failed to send CreateLayer event");

        rx.blocking_recv()
            .map_err(|_| jni::errors::Error::JavaException)
    }

    fn set_bounds<'local>(
        _env: &mut Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        layer_index: jint,
        x: jint,
        y: jint,
        width: jint,
        height: jint,
    ) -> jni::errors::Result<()> {
        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::SetBounds(layer_index, x, y, width, height))
            .expect("Failed to send SetBounds event");

        Ok(())
    }

    fn set_image<'local>(
        env: &mut ::jni::Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        layer_index: jint,
        rgb: JIntArray,
    ) -> jni::errors::Result<()> {
        let elements = unsafe { rgb.get_elements_critical(env, ReleaseMode::NoCopyBack)? };
        let slice = unsafe { slice::from_raw_parts(elements.as_ptr(), elements.len()) };

        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::SetImage(layer_index, slice))
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
        _env: &mut ::jni::Env<'local>,
        _this: WaylandLib<'local>,
        sender_ptr: jlong,
        layer_index: jint,
    ) -> jni::errors::Result<()> {
        let sender = unsafe { &*(sender_ptr as *const mpsc::Sender<Event>) };
        sender
            .send(Event::Dispose(layer_index))
            .expect("Failed to send Dispose event");

        Ok(())
    }
}

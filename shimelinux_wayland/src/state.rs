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
    collections::HashMap,
    sync::{LazyLock, Mutex},
};

use jni::{refs::Global, vm::JavaVM};
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
        wlr_layer::{LayerShell, LayerShellHandler, LayerSurface, LayerSurfaceConfigure},
    },
    shm::{Shm, ShmHandler, slot::SlotPool},
};
use wayland_client::{
    Connection, QueueHandle, delegate_noop,
    protocol::{
        wl_output::{Transform, WlOutput},
        wl_pointer::WlPointer,
        wl_region::WlRegion,
        wl_seat::WlSeat,
        wl_shm::Format,
        wl_surface::WlSurface,
    },
};

use crate::ffi::MouseEventReceiver;

pub static SCREEN_RECT: LazyLock<Mutex<Rect>> = LazyLock::new(|| Mutex::new(Rect::default()));

#[derive(Default)]
pub struct Rect {
    pub x: i32,
    pub y: i32,
    pub width: i32,
    pub height: i32,
}

#[derive(Default)]
pub struct CursorState {
    left_pressed: bool,
    right_pressed: bool,
    left_released: bool,
    right_released: bool,
    position_x: i32,
    position_y: i32,
}

pub struct State {
    pub compositor_state: CompositorState,
    pub layer_shell: LayerShell,
    pub registry_state: RegistryState,
    pub output_state: OutputState,
    pub seat_state: SeatState,
    pub shm: Shm,
    pub pool: SlotPool,
    pub output: Option<WlOutput>,
    pub pointer: Option<WlPointer>,
    pub pointer_surface: Option<WlSurface>,
    pub serial: Option<u32>,
    pub layers: HashMap<i32, LayerState>,
}

pub struct LayerState {
    pub layer_surface: LayerSurface,
    pub configured: bool,
    pub rgb: &'static [i32],
    pub bounds: Rect,
    pub image_changed: bool,
    pub cursor_state: CursorState,
    pub mouse_event_receiver: Global<MouseEventReceiver<'static>>,
    pub use_input_region: bool,
    pub input_region: Vec<Rect>,
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
            .values_mut()
            .find(|l| l.layer_surface.wl_surface() == surface)
            && layer.configured
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
        self.output.get_or_insert_with(|| {
            if let Some(info) = self.output_state.info(output) {
                let (x, y) = info.logical_position.unwrap_or_default();
                let (width, height) = info.logical_size.unwrap_or_default();
                let mut screen_rect = SCREEN_RECT.lock().unwrap();
                *screen_rect = Rect {
                    x,
                    y,
                    width,
                    height,
                };
            }

            output.clone()
        });
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

    fn update_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, output: WlOutput) {
        if let Some(old_output) = &self.output
            && let Some(old_info) = self.output_state.info(old_output)
            && let Some(new_info) = self.output_state.info(&output)
            && old_info.id == new_info.id
        {
            let (x, y) = new_info.logical_position.unwrap_or_default();
            let (width, height) = new_info.logical_size.unwrap_or_default();
            let mut screen_rect = SCREEN_RECT.lock().unwrap();
            *screen_rect = Rect {
                x,
                y,
                width,
                height,
            };
        }
    }

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
        if let Some(layer) = self.layers.values_mut().find(|l| l.layer_surface == *layer)
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

        for event in events {
            if let Some(layer) = self
                .layers
                .values_mut()
                .find(|l| *l.layer_surface.wl_surface() == event.surface)
            {
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

                // Update cursor position
                let (position_x, position_y) = event.position;
                layer.cursor_state.position_x = position_x as i32;
                layer.cursor_state.position_y = position_y as i32;

                layers.push(layer as *mut LayerState);
            }
        }

        // Send events to mouse event receiver
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
                        layer.cursor_state.position_x,
                        layer.cursor_state.position_y,
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
                // Update image
                for y in 0..height {
                    for x in 0..width {
                        let canvas_index = (((y * width + x) * 4) as usize).min(canvas.len() - 1);
                        let image_index = ((y * width + x) as usize).min(self.rgb.len() - 1);
                        let slice = &self.rgb[image_index].to_le_bytes();
                        canvas[canvas_index..canvas_index + 4].copy_from_slice(slice);
                    }
                }

                // Update input region
                if !self.input_region.is_empty() {
                    let region = compositor.wl_compositor().create_region(qh, ());
                    for rect in &self.input_region {
                        region.add(rect.x, rect.y, rect.width, rect.height);
                    }
                    surface.set_input_region(Some(&region));
                }
            } else {
                canvas.fill(0);
            }

            surface.damage_buffer(0, 0, width, height);
            let _ = buffer.attach_to(surface);
            self.image_changed = false;
        }

        surface.frame(qh, FrameCallbackData(surface.clone()));
        surface.commit();
    }

    pub fn update_input_region(&mut self) {
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

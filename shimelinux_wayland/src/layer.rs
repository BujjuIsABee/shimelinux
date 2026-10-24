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
        wlr_layer::{LayerShellHandler, LayerSurface, LayerSurfaceConfigure},
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
use wayland_cursor::CursorTheme;

use crate::{MouseEventReceiver, Rect};

pub struct LayerState {
    pub compositor_state: CompositorState,
    pub registry_state: RegistryState,
    pub output_state: OutputState,
    pub seat_state: SeatState,
    pub shm: Shm,

    pub layer: LayerSurface,
    pub configured: bool,
    pub pool: SlotPool,
    pub image_rgb: Vec<i32>,
    pub image_bounds: Rect,
    pub image_changed: bool,
    pub layer_mask: Vec<Rect>,
    pub mouse_event_receiver: Global<MouseEventReceiver<'static>>,
    pub pointer: Option<WlPointer>,
    pub pointer_surface: Option<WlSurface>,
    pub serial: Option<u32>,
}

impl CompositorHandler for LayerState {
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
        _surface: &WlSurface,
        _time: u32,
    ) {
        self.draw(qh);
    }

    fn surface_enter(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &WlSurface,
        _output: &WlOutput,
    ) {
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

impl OutputHandler for LayerState {
    fn output_state(&mut self) -> &mut OutputState {
        &mut self.output_state
    }

    fn new_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {}

    fn update_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {}

    fn output_destroyed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {
    }
}

impl LayerShellHandler for LayerState {
    fn closed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _layer: &LayerSurface) {}

    fn configure(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        _layer: &LayerSurface,
        _configure: LayerSurfaceConfigure,
        _serial: u32,
    ) {
        if !self.configured {
            self.configured = true;
            self.draw(qh);
        }
    }
}

impl SeatHandler for LayerState {
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

impl PointerHandler for LayerState {
    fn pointer_frame(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _pointer: &WlPointer,
        events: &[PointerEvent],
    ) {
        use PointerEventKind::*;

        let mut left_pressed = false;
        let mut right_pressed = false;
        let mut left_released = false;
        let mut right_released = false;
        let mut position_x = 0;
        let mut position_y = 0;

        for event in events {
            // Update cursor position
            let (x, y) = event.position;
            position_x = x as i32;
            position_y = y as i32;

            match event.kind {
                Enter { serial } => {
                    self.serial = Some(serial);
                }
                Leave { serial } => {
                    self.serial = Some(serial);
                }
                Motion { .. } => {
                    // Update cursor surface
                    if let (Some(pointer), Some(serial), Some(surface)) =
                        (&self.pointer, self.serial, &self.pointer_surface)
                    {
                        pointer.set_cursor(serial, Some(surface), 0, 0);
                    }
                }
                Press { button, .. } => {
                    left_pressed = button == BTN_LEFT;
                    right_pressed = button == BTN_RIGHT;
                }
                Release { button, .. } => {
                    left_released = button == BTN_LEFT;
                    right_released = button == BTN_RIGHT;
                }
                Axis { .. } => {}
            }
        }

        // Send events to mouse event receiver
        if let Ok(jvm) = JavaVM::singleton() {
            let _ = jvm.attach_current_thread(|env| -> jni::errors::Result<_> {
                self.mouse_event_receiver.update_cursor(
                    env,
                    left_pressed,
                    right_pressed,
                    left_released,
                    right_released,
                    position_x,
                    position_y,
                )
            });
        }
    }
}

impl ShmHandler for LayerState {
    fn shm_state(&mut self) -> &mut Shm {
        &mut self.shm
    }
}

delegate_noop!(LayerState: ignore WlRegion);

impl LayerState {
    pub fn set_bounds(&mut self, bounds: Rect) {
        self.image_bounds = bounds.clone();
        self.layer.set_margin(bounds.y, 0, 0, bounds.x);
    }

    pub fn set_image(&mut self, rgb: Vec<i32>, update_mask: bool) {
        self.image_changed = true;
        self.image_rgb = rgb;

        if update_mask {
            self.update_layer_mask();
        }
    }

    pub fn set_cursor(&mut self, conn: &Connection, qh: &QueueHandle<Self>, use_hand: bool) {
        if let Ok(mut theme) = CursorTheme::load(conn, self.shm.wl_shm().clone(), 24)
            && let Some(cursor) = theme.get_cursor(if use_hand { "pointer" } else { "left_ptr" })
        {
            let surface = self
                .pointer_surface
                .get_or_insert(self.compositor_state.create_surface(qh));

            // Attach None to clear the previous buffer
            surface.attach(None, 0, 0);
            surface.commit();

            // Attach the new buffer
            surface.attach(Some(&cursor[0]), 0, 0);
            surface.commit();

            self.pointer_surface = Some(surface.clone());
        }
    }

    pub fn dispose(&mut self) {
        self.layer.wl_surface().destroy();
    }

    fn draw(&mut self, qh: &QueueHandle<Self>) {
        let width = self.image_bounds.width.max(1);
        let height = self.image_bounds.height.max(1);
        let stride = width * 4;

        self.layer.set_size(width as u32, height as u32);

        let surface = self.layer.wl_surface();

        if self.image_changed {
            let (buffer, canvas) = self
                .pool
                .create_buffer(width, height, stride, Format::Argb8888)
                .expect("Failed to create buffer");

            if !self.image_rgb.is_empty() {
                // Draw the image to the canvas
                for y in 0..height {
                    for x in 0..width {
                        let canvas_index = (((y * width + x) * 4) as usize).min(canvas.len() - 1);
                        let image_index = ((y * width + x) as usize).min(self.image_rgb.len() - 1);
                        let slice = &self.image_rgb[image_index].to_le_bytes();
                        canvas[canvas_index..canvas_index + 4].copy_from_slice(slice);
                    }
                }

                // Set the mask shape
                if !self.layer_mask.is_empty() {
                    let region = self.compositor_state.wl_compositor().create_region(qh, ());
                    for rect in &self.layer_mask {
                        region.add(rect.x, rect.y, rect.width, rect.height);
                    }
                    self.layer.set_input_region(Some(&region));
                }
            }

            surface.damage_buffer(0, 0, width, height);
            let _ = buffer.attach_to(surface);

            self.image_changed = false;
        }

        surface.frame(qh, FrameCallbackData(surface.clone()));

        self.layer.commit();
    }

    fn update_layer_mask(&mut self) {
        let mut rects: Vec<Rect> = Vec::new();
        let width = self.image_bounds.width;
        let height = self.image_bounds.height;

        for y in 0..height as u32 {
            let mut start: Option<u32> = None;
            for x in 0..width as u32 {
                let index = ((y * width as u32 + x) as usize).min(self.image_rgb.len() - 1);
                let alpha = (self.image_rgb[index] >> 24) & 0xFF;
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

        self.layer_mask = rects;
    }
}

delegate_registry!(LayerState);

impl ProvidesRegistryState for LayerState {
    fn registry(&mut self) -> &mut RegistryState {
        &mut self.registry_state
    }

    registry_handlers![];
}

delegate_dispatch2!(LayerState);

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
    panic::{AssertUnwindSafe, catch_unwind},
    sync::{LazyLock, Mutex, OnceLock},
};

use jni::{JValue, errors::Error, jni_sig, jni_str, objects::JObject, refs::Global, vm::JavaVM};
use smithay_client_toolkit::{
    compositor::{CompositorHandler, CompositorState},
    delegate_compositor, delegate_layer, delegate_output, delegate_pointer, delegate_registry,
    delegate_seat, delegate_shm,
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

use crate::{Point, Rect};

#[derive(Default)]
pub struct CursorState {
    pub pointer: Option<WlPointer>,
    pub surface: Option<WlSurface>,
    pub serial: Option<u32>,

    pub left_pressed: bool,
    pub right_pressed: bool,
    pub left_released: bool,
    pub right_released: bool,
    pub position: Point,
}

pub struct Mascot {
    pub object: Global<JObject<'static>>,

    pub compositor_state: CompositorState,
    pub registry_state: RegistryState,
    pub output_state: OutputState,
    pub seat_state: SeatState,
    pub cursor_state: CursorState,
    pub shm: Shm,
    pub pool: SlotPool,

    pub layer: LayerSurface,
    pub layer_mask: Vec<Rect>,
    pub configured: bool,
    pub image_rgb: Vec<i32>,
    pub image_bounds: Rect,
}

delegate_compositor!(Mascot);
impl CompositorHandler for Mascot {
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
        output: &WlOutput,
    ) {
        set_screen_rect(&self.output_state, output);
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

delegate_output!(Mascot);
impl OutputHandler for Mascot {
    fn output_state(&mut self) -> &mut OutputState {
        &mut self.output_state
    }

    fn new_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, output: WlOutput) {
        set_screen_rect(&self.output_state, &output);
    }

    fn update_output(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, output: WlOutput) {
        set_screen_rect(&self.output_state, &output);
    }

    fn output_destroyed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _output: WlOutput) {}
}

delegate_layer!(Mascot);
impl LayerShellHandler for Mascot {
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

delegate_seat!(Mascot);
impl SeatHandler for Mascot {
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
        if capability == Capability::Pointer && self.cursor_state.pointer.is_none() {
            self.cursor_state.pointer = self.seat_state.get_pointer(qh, &seat).ok();
        }
    }

    fn remove_capability(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _seat: WlSeat,
        capability: Capability,
    ) {
        if capability == Capability::Pointer && self.cursor_state.pointer.is_some() {
            self.cursor_state.pointer.take().unwrap().release();
        }
    }

    fn remove_seat(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _seat: WlSeat) {}
}

delegate_pointer!(Mascot);
impl PointerHandler for Mascot {
    fn pointer_frame(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _pointer: &WlPointer,
        events: &[PointerEvent],
    ) {
        use PointerEventKind::*;
        for event in events {
            // Skip events for other mascots
            if &event.surface != self.layer.wl_surface() {
                continue;
            }

            match event.kind {
                Enter { serial } => {
                    self.cursor_state.serial = Some(serial);
                }
                Leave { serial } => {
                    self.cursor_state.serial = Some(serial);
                }
                Motion { .. } => {
                    if let Some(pointer) = &self.cursor_state.pointer
                        && let Some(serial) = self.cursor_state.serial
                        && let Some(surface) = &self.cursor_state.surface
                    {
                        pointer.set_cursor(serial, Some(surface), 0, 0);
                    }

                    self.cursor_state.position.x = event.position.0 as i32;
                    self.cursor_state.position.y = event.position.1 as i32;
                }
                Press { button, .. } => {
                    if button == BTN_LEFT {
                        self.cursor_state.left_pressed = true;
                    } else if button == BTN_RIGHT {
                        self.cursor_state.right_pressed = true;
                    }
                }
                Release { button, .. } => {
                    if button == BTN_LEFT {
                        self.cursor_state.left_released = true;
                    } else if button == BTN_RIGHT {
                        self.cursor_state.right_released = true;
                    }
                }
                Axis { .. } => {}
            }
        }

        if let Ok(jvm) = JavaVM::singleton() {
            let _ = jvm.attach_current_thread(|env| -> Result<(), Error> {
                env.call_method(
                    &self.object,
                    jni_str!("updateCursor"),
                    jni_sig!((bool, bool, bool, bool, i32, i32)),
                    &[
                        JValue::from(self.cursor_state.left_pressed),
                        JValue::from(self.cursor_state.right_pressed),
                        JValue::from(self.cursor_state.left_released),
                        JValue::from(self.cursor_state.right_released),
                        JValue::from(self.cursor_state.position.x),
                        JValue::from(self.cursor_state.position.y),
                    ],
                )?;

                Ok(())
            });
        }

        self.cursor_state.left_pressed = false;
        self.cursor_state.right_pressed = false;
        self.cursor_state.left_released = false;
        self.cursor_state.right_released = false;
    }
}

delegate_shm!(Mascot);
impl ShmHandler for Mascot {
    fn shm_state(&mut self) -> &mut Shm {
        &mut self.shm
    }
}

delegate_registry!(Mascot);
impl ProvidesRegistryState for Mascot {
    fn registry(&mut self) -> &mut RegistryState {
        &mut self.registry_state
    }

    registry_handlers![];
}

delegate_noop!(Mascot: ignore WlRegion);
impl Mascot {
    pub fn set_bounds(&mut self, bounds: Rect) {
        self.image_bounds = bounds.clone();
        self.layer.set_margin(bounds.y, 0, 0, bounds.x);
    }

    pub fn set_image(&mut self, rgb: Vec<i32>) {
        self.image_rgb = rgb;
        self.update_layer_mask();
    }

    pub fn set_cursor(&mut self, connection: &Connection, qh: &QueueHandle<Self>, use_hand: bool) {
        let Ok(mut theme) = CursorTheme::load(connection, self.shm.wl_shm().clone(), 24) else { return; };
        let name = if use_hand { "pointer" } else { "left_ptr" };
        if let Some(cursor) = theme.get_cursor(name)
        {
            let surface = self
                .cursor_state
                .surface
                .get_or_insert(self.compositor_state.create_surface(qh));

            // Attach None to clear the previous buffer
            surface.attach(None, 0, 0);
            surface.commit();

            // Attach the new buffer
            surface.attach(Some(&cursor[0]), 0, 0);
            surface.commit();

            self.cursor_state.surface = Some(surface.clone());
        }
    }

    pub fn dispose(&mut self) {
        self.layer.wl_surface().destroy();
    }

    fn draw(&mut self, qh: &QueueHandle<Self>) {
        let _ = catch_unwind(AssertUnwindSafe(|| {
            let width = cmp::max(1, self.image_bounds.width);
            let height = cmp::max(1, self.image_bounds.height);
            let stride = width * 4;

            self.layer.set_size(width as u32, height as u32);

            let (buffer, canvas) = self
                .pool
                .create_buffer(width, height, stride, Format::Argb8888)
                .unwrap();

            if !self.image_rgb.is_empty() {
                // Draw the image to the canvas
                for y in 0..height {
                    for x in 0..width {
                        let canvas_index = cmp::min(((y * width + x) * 4) as usize, canvas.len() - 1);
                        let image_index = cmp::min((y * width + x) as usize, self.image_rgb.len() - 1);
                        canvas[canvas_index..canvas_index + 4].copy_from_slice(&self.image_rgb[image_index].to_le_bytes());
                    }
                }

                // Set the mask shape
                let region = self.compositor_state.wl_compositor().create_region(&qh, ());
                for rect in &self.layer_mask {
                    region.add(rect.x, rect.y, rect.width, rect.height);
                }
                self.layer.set_input_region(Some(&region));
            }

            // Update the layer
            self.layer.wl_surface().damage_buffer(0, 0, width, height);
            self.layer.wl_surface().frame(qh, self.layer.wl_surface().clone());
            buffer.attach_to(self.layer.wl_surface()).unwrap();
            self.layer.commit();
        }));
    }

    fn update_layer_mask(&mut self) {
        let mut rects: Vec<Rect> = Vec::new();
        let width = self.image_bounds.width;
        let height = self.image_bounds.height;

        for y in 0..height as u32 {
            let mut start: Option<u32> = None;
            for x in 0..width as u32 {
                let index = cmp::min((y * width as u32 + x) as usize, self.image_rgb.len() - 1);
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

static OUTPUT_ID: OnceLock<u32> = OnceLock::new();
static SCREEN_RECT: LazyLock<Mutex<Rect>> = LazyLock::new(|| Mutex::new(Rect::default()));

pub fn get_screen_rect() -> Rect {
    let screen_rect = SCREEN_RECT.lock().unwrap();
    screen_rect.clone()
}

fn set_screen_rect(output_state: &OutputState, output: &WlOutput) {
    if let Some(info) = output_state.info(output) {
        if *OUTPUT_ID.get_or_init(|| info.id) == info.id {
            let (width, height) = info.logical_size.unwrap_or_default();
            let mut screen_rect = SCREEN_RECT.lock().unwrap();
            *screen_rect = Rect {
                x: 0,
                y: 0,
                width,
                height,
            };
        }
    }
}

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
    process::Command,
    sync::{LazyLock, Mutex, OnceLock},
};

use jni::{JNIVersion, JValue, errors::Error, jni_sig, jni_str, objects::JObject, refs::Global, vm::{InitArgsBuilder, JavaVM}};
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
    pub grab_start: Point,
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
    pub layer_width: u32,
    pub layer_height: u32,
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
        self.set_screen_rect(output);
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

    fn new_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        output: WlOutput,
    ) {
        self.set_screen_rect(&output);
    }

    fn update_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        output: WlOutput,
    ) {
        self.set_screen_rect(&output);
    }

    fn output_destroyed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _output: WlOutput,
    ) {
    }
}

delegate_layer!(Mascot);
impl LayerShellHandler for Mascot {
    fn closed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _layer: &LayerSurface
    ) {
    }

    fn configure(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        _layer: &LayerSurface,
        configure: LayerSurfaceConfigure,
        _serial: u32,
    ) {
        let (width, height) = configure.new_size;
        self.layer_width = width;
        self.layer_height = height;

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

    fn new_seat(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _seat: WlSeat
    ) {
    }

    fn new_capability(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        seat: WlSeat,
        capability: Capability,
    ) {
        if capability == Capability::Pointer && self.cursor_state.pointer.is_none() {
            let pointer = self.seat_state.get_pointer(qh, &seat).unwrap();
            self.cursor_state.pointer = Some(pointer);
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

    fn remove_seat(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _seat: WlSeat
    ) {
    }
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

        self.set_cursor_position();

        let jvm = JVM.get_or_init(|| {
            let jvm_args = InitArgsBuilder::new()
                .version(JNIVersion::V1_8)
                .option("-Xcheck:jni")
                .build()
                .unwrap();

            JavaVM::new(jvm_args).unwrap()
        });

        let _ = jvm.attach_current_thread(|env| -> Result<_, Error> {
            let _ = env.call_method(
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
            );

            Ok(())
        });

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
        self.layer.set_size(
            cmp::max(1, bounds.width as u32),
            cmp::max(1, bounds.height as u32),
        );

        self.layer.set_margin(
            bounds.y,
            0,
            0,
            bounds.x,
        );

        self.layer.commit();
    }

    pub fn set_image(&mut self, rgb: Vec<i32>) {
        self.image_rgb = rgb;
        self.update_layer_mask();
    }

    pub fn set_cursor(&mut self, connection: &Connection, qh: &QueueHandle<Self>, use_hand: bool) {
        let mut theme = CursorTheme::load(connection, self.shm.wl_shm().clone(), 24).expect("Failed to get cursor theme");
        let name = if use_hand { "pointer" } else { "left_ptr" };
        if let Some(cursor) = theme.get_cursor(name) {
            let surface = self.cursor_state.surface.get_or_insert(self.compositor_state.create_surface(qh));

            // Attach None to clear the previous buffer
            surface.attach(None, 0, 0);
            surface.commit();

            // Attach the new buffer
            surface.attach(Some(&cursor[0]), 0, 0);
            surface.commit();
        }
    }

    pub fn dispose(&mut self) {
        self.layer.wl_surface().destroy();
    }

    fn draw(&mut self, qh: &QueueHandle<Self>) {
        let width = self.layer_width as i32;
        let height = self.layer_height as i32;
        let stride = width * 4;

        let (buffer, canvas) = self
            .pool
            .create_buffer(width, height, stride, Format::Argb8888)
            .expect("Failed to create buffer");

        if !self.image_rgb.is_empty() {
            // Draw the image to the canvas
            for i in 0..width * height {
                let (x, y) = (i % width, i / width);
                let canvas_index = ((y * width + x) * 4) as usize;
                let image_index = (y * self.image_bounds.width as i32 + x) as usize;

                canvas[canvas_index..canvas_index + 4].copy_from_slice(&self.image_rgb[image_index].to_le_bytes());
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
        buffer.attach_to(self.layer.wl_surface()).expect("Failed to attach buffer");
        self.layer.commit();
    }

    fn update_layer_mask(&mut self) {
        let mut rects: Vec<Rect> = Vec::new();
        for y in 0..self.image_bounds.height as u32 {
            let mut section_start: Option<u32> = None;
            for x in 0..self.image_bounds.width as u32 {
                let index: usize = (y * self.image_bounds.width as u32 + x) as usize;
                let alpha = (self.image_rgb[index] >> 24) & 0xFF;
                if alpha > 0 && section_start.is_none() {
                    section_start = Some(x);
                } else if alpha == 0 && let Some(start) = section_start {
                    section_start = None;
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

    fn set_screen_rect(&mut self, output: &WlOutput) {
        if let Some(info) = self.output_state.info(output) {
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

    fn set_cursor_position(&mut self) {
        let desktop = DESKTOP_TYPE.unwrap_or_else(|| "other");
        let mut cursor_position = CURSOR_POSITION.lock().unwrap();

        if desktop == NIRI {
            if self.cursor_state.left_pressed {
                self.cursor_state.grab_start = Point {
                    x: self.image_bounds.x,
                    y: self.image_bounds.y,
                };
            }

            *cursor_position = Point {
                x: self.cursor_state.position.x + self.cursor_state.grab_start.x,
                y: self.cursor_state.position.y + self.cursor_state.grab_start.y,
            };
        } else if desktop == HYPRLAND {
            let output = Command::new("hyprctl")
                .arg("cursorpos")
                .output()
                .expect("Failed to get cursor position");

            if let Ok(output_text) = String::from_utf8(output.stdout) 
                && let Some((x, y)) = output_text.split_once(", ")
            {
                *cursor_position = Point {
                    x: x.trim().parse().unwrap_or_default(),
                    y: y.trim().parse().unwrap_or_default(),
                }
            }
        }
    }
}

pub fn get_screen_rect() -> Rect {
    let screen_rect = SCREEN_RECT.lock().unwrap();
    screen_rect.clone()
}

pub fn get_cursor_position() -> Point {
    let cursor_position = CURSOR_POSITION.lock().unwrap();
    cursor_position.clone()
}

static JVM: OnceLock<JavaVM> = OnceLock::new();
static DESKTOP_TYPE: Option<&str> = option_env!("XDG_CURRENT_DESKTOP");
static OUTPUT_ID: OnceLock<u32> = OnceLock::new();
static SCREEN_RECT: LazyLock<Mutex<Rect>> = LazyLock::new(|| Mutex::new(Rect::default()));
static CURSOR_POSITION: LazyLock<Mutex<Point>> = LazyLock::new(|| Mutex::new(Point::default()));

const NIRI: &str = "niri";
const HYPRLAND: &str = "Hyprland";

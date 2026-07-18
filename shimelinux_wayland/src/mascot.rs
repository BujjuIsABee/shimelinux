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
    collections::HashMap,
    sync::{LazyLock, Mutex},
};

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
    globals::GlobalList,
    protocol::{
        wl_output::{self, WlOutput},
        wl_pointer::WlPointer,
        wl_region::WlRegion,
        wl_seat::WlSeat,
        wl_shm::Format,
        wl_surface::WlSurface,
    },
};

pub struct Mascot {
    pub compositor_state: CompositorState,
    pub registry_state: RegistryState,
    pub output_state: OutputState,
    pub seat_state: SeatState,
    pub shm: Shm,
    pub pool: SlotPool,
    pub layer: LayerSurface,
    pub pointer: Option<WlPointer>,
    pub cursor_surface: Option<WlSurface>,
    pub serial: Option<u32>,
    pub width: u32,
    pub height: u32,
    pub image_width: u32,
    pub image_height: u32,
    pub rgb: Vec<i32>,
    pub mask: Vec<Rectangle>,
    pub first_configure: bool,
    pub sender_index: i32,
}

#[derive(Default)]
pub struct Rectangle {
    pub x: i32,
    pub y: i32,
    pub width: i32,
    pub height: i32,
}

#[derive(Default)]
pub struct MouseState {
    pub left_pressed: bool,
    pub right_pressed: bool,
    pub left_released: bool,
    pub right_released: bool,
    pub position_x: i32,
    pub position_y: i32,
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
        _new_transform: wl_output::Transform,
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
        self.set_screen(&output);
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
        output: WlOutput
    ) {
        self.set_screen(&output);
    }

    fn update_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        output: WlOutput
    ) {
        self.set_screen(&output);
    }

    fn output_destroyed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _output: WlOutput
    ) {
    }
}

delegate_layer!(Mascot);
impl LayerShellHandler for Mascot {
    fn closed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _layer: &LayerSurface) {}

    fn configure(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        _layer: &LayerSurface,
        configure: LayerSurfaceConfigure,
        _serial: u32,
    ) {
        let (width, height) = configure.new_size;
        self.width = cmp::max(1, width);
        self.height = cmp::max(1, height);

        // Draw the mascot for the first time if this is the first configure
        if self.first_configure {
            self.first_configure = false;
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
        if capability == Capability::Pointer && self.pointer.is_none() {
            let pointer = self.seat_state.get_pointer(qh, &seat).expect("Failed to get pointer");
            self.pointer = Some(pointer);
        }
    }

    fn remove_capability(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _seat: WlSeat,
        capability: Capability,
    ) {
        if capability == Capability::Pointer && self.pointer.is_none() {
            self.pointer.take().unwrap().release();
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
                Press { button, .. } => {
                    MouseState::get(self.sender_index, |mouse_state| {
                        mouse_state.left_pressed = button == BTN_LEFT;
                        mouse_state.right_pressed = button == BTN_RIGHT;
                    });
                }
                Release { button, .. } => {
                    MouseState::get(self.sender_index, |mouse_state| {
                        mouse_state.left_released = button == BTN_LEFT;
                        mouse_state.right_released = button == BTN_RIGHT;
                    });
                }
                Motion { .. } => {
                    MouseState::get(self.sender_index, |mouse_state| {
                        let (position_x, position_y) = event.position;
                        mouse_state.position_x = position_x as i32;
                        mouse_state.position_y = position_y as i32;
                    });

                    // Set the cursor
                    if let Some(cursor_surface) = &self.cursor_surface
                        && let Some(serial) = self.serial
                        && let Some(pointer) = &self.pointer
                    {
                        pointer.set_cursor(serial, Some(&cursor_surface), 0, 0);
                    }
                }
                Enter { serial } => {
                    self.serial = Some(serial);
                }
                Leave { serial } => {
                    self.serial = Some(serial);
                }

                _ => {}
            }
        }
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
    pub fn new(
        globals: &GlobalList,
        qh: &QueueHandle<Mascot>,
        compositor_state: CompositorState,
        layer: LayerSurface,
        sender_index: i32,
    ) -> Self {
        let shm = Shm::bind(globals, qh).expect("Failed to get shm");
        let pool = SlotPool::new(256 * 256 * 4, &shm).expect("Failed to get pool");
        Mascot {
            compositor_state: compositor_state,
            registry_state: RegistryState::new(&globals),
            output_state: OutputState::new(&globals, &qh),
            seat_state: SeatState::new(&globals, &qh),
            shm,
            pool,
            layer,
            pointer: None,
            cursor_surface: None,
            serial: None,
            width: 0,
            height: 0,
            image_width: 0,
            image_height: 0,
            rgb: Vec::new(),
            mask: Vec::new(),
            first_configure: true,
            sender_index,
        }
    }

    pub fn update_mask(&mut self, rgb: &Vec<i32>) {
        let mut rects = Vec::new();

        for y in 0..self.image_height {
            let mut start: Option<u32> = None; 
            for x in 0..self.image_width {
                let index = cmp::min(((y * self.image_width) + x) as usize, rgb.len() - 1);
                let alpha = (rgb[index] >> 24) & 0xFF;
                if alpha > 0 && start.is_none() {
                    start = Some(x);
                } else if alpha == 0 && start.is_some() {
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

        self.mask = rects;
    }

    fn draw(&mut self, qh: &QueueHandle<Self>) {
        let width = self.width as i32;
        let height = self.height as i32;
        let stride = width * 4;

        let (buffer, canvas) = self
            .pool
            .create_buffer(width, height, stride, Format::Argb8888)
            .expect("Failed to create buffer");

        if self.rgb.len() > 0 {
            // Draw the image to the canvas
            for y in 0..height {
                for x in 0..width {
                    let canvas_index = cmp::min(
                        ((y * width + x) * 4) as usize,
                        canvas.len() - 1,
                    );
                    let color_index = cmp::min(
                        (y * self.image_width as i32 + x) as usize,
                        self.rgb.len() - 1,
                    );

                    canvas[canvas_index..canvas_index + 4].copy_from_slice(&self.rgb[color_index].to_le_bytes());
                }
            }

            // Set the mask shape
            let shape = self.compositor_state.wl_compositor().create_region(&qh, ());
            for rect in &self.mask {
                shape.add(rect.x, rect.y, rect.width, rect.height);
            }
            self.layer.set_input_region(Some(&shape));
        }

        self.layer.wl_surface().damage_buffer(0, 0, width, height);
        self.layer.wl_surface().frame(qh, self.layer.wl_surface().clone());
        buffer.attach_to(self.layer.wl_surface()).expect("Failed to attach buffer");
        self.layer.commit();
    }
    
    pub fn get_screen<T: FnMut(&mut Rectangle)>(mut action: T) {
        let mut screen = SCREEN.lock().unwrap();
        action(screen.get_or_insert_default());
    }

    fn set_screen(&mut self, output: &WlOutput) {
        if let Some(info) = self.output_state.info(output) {
            let id = info.id as i32;
            let (width, height) = info.logical_size.unwrap_or_default();

            if Mascot::get_output_id() == None {
                Mascot::set_output_id(id);
            }
            if Some(id) == Mascot::get_output_id() {
                Mascot::get_screen(|screen| {
                    screen.x = 0;
                    screen.y = 0;
                    screen.width = width;
                    screen.height = height;
                });
            }
        }
    }

    fn get_output_id() -> Option<i32> {
        let output_id = OUTPUT_ID.lock().unwrap();
        *output_id
    }

    fn set_output_id(id: i32) {
        let mut output_id = OUTPUT_ID.lock().unwrap();
        *output_id = Some(id);
    }
}

impl MouseState {
    pub fn get<T: FnMut(&mut MouseState)>(sender_index: i32, mut action: T) {
        let mut mouse_states = MOUSE_STATES.lock().unwrap();
        let mouse_state = mouse_states.entry(sender_index).or_default();
        action(mouse_state);
    }
}

static OUTPUT_ID: LazyLock<Mutex<Option<i32>>> = LazyLock::new(|| { Mutex::new(None) });
static SCREEN: LazyLock<Mutex<Option<Rectangle>>> = LazyLock::new(|| { Mutex::new(None) });
static MOUSE_STATES: LazyLock<Mutex<HashMap<i32, MouseState>>> = LazyLock::new(|| { Mutex::new(HashMap::new()) });

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
    cmp::max,
    collections::HashMap,
    sync::{
        LazyLock, Mutex,
        mpsc::{Sender, channel},
    },
};

use jni::{
    EnvUnowned,
    elements::ReleaseMode,
    errors::{Error, ThrowRuntimeExAndDefault},
    objects::{JClass, JIntArray},
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
        pointer::{BTN_LEFT, BTN_RIGHT, PointerEventKind, PointerHandler},
    },
    shell::{
        WaylandSurface,
        wlr_layer::{
            Anchor, KeyboardInteractivity, Layer, LayerShell, LayerShellHandler, LayerSurface,
            LayerSurfaceConfigure,
        },
    },
    shm::{Shm, ShmHandler, slot::SlotPool},
};
use wayland_client::{
    Connection, QueueHandle, delegate_noop,
    globals::registry_queue_init,
    protocol::{
        wl_output::{Transform, WlOutput},
        wl_pointer::WlPointer,
        wl_region,
        wl_seat::WlSeat,
        wl_shm::Format,
        wl_surface::WlSurface,
    },
};

enum Event {
    SetBounds(i32, i32, i32, i32),
    UpdateImage(Vec<i32>),
    Dispose(),
}

struct Mascot {
    compositor_state: CompositorState,
    registry_state: RegistryState,
    output_state: OutputState,
    seat_state: SeatState,
    shm: Shm,
    pool: SlotPool,
    layer: LayerSurface,
    width: u32,
    height: u32,
    first_configure: bool,
    sender_index: i32,
    pointer: Option<WlPointer>,
    logical_x: i32,
    logical_y: i32,
    rgb: Vec<i32>,
}

struct MouseState {
    left_pressed: bool,
    right_pressed: bool,
    left_released: bool,
    right_released: bool,
    position_x: i32,
    position_y: i32,
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
        if let Some(info) = self.output_state.info(&output) {
            let logical_pos = info.logical_position.expect("Failed to get logical position");
            self.logical_x = logical_pos.0;
            self.logical_y = logical_pos.1;
        }
    }

    fn update_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        output: WlOutput
    ) {
        if let Some(info) = self.output_state.info(&output) {
            let logical_pos = info.logical_position.expect("Failed to get logical position");
            self.logical_x = logical_pos.0;
            self.logical_y = logical_pos.1;
        }
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
        self.width = max(1, width);
        self.height = max(1, height);

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
        events: &[smithay_client_toolkit::seat::pointer::PointerEvent],
    ) {
        use PointerEventKind::*;
        for event in events {
            if &event.surface != self.layer.wl_surface() {
                continue;
            }
            match event.kind {
                Press { button, .. } => {
                    let left_pressed = button == BTN_LEFT;
                    let right_pressed = button == BTN_RIGHT;
                    let mut mouse_states = MOUSE_STATES.lock().unwrap();
                    mouse_states
                        .entry(self.sender_index)
                        .and_modify(|m| {
                            m.left_pressed = left_pressed;
                            m.right_pressed = right_pressed;
                        })
                        .or_insert(MouseState {
                            left_pressed: left_pressed,
                            right_pressed: right_pressed,
                            left_released: false,
                            right_released: false,
                            position_x: 0,
                            position_y: 0,
                        });
                },
                Release { button, .. } => {
                    let left_released = button == BTN_LEFT;
                    let right_released = button == BTN_RIGHT;
                    let mut mouse_states = MOUSE_STATES.lock().unwrap();
                    mouse_states
                        .entry(self.sender_index)
                        .and_modify(|m| {
                            m.left_released = left_released;
                            m.right_released = right_released;
                        })
                        .or_insert(MouseState {
                            left_pressed: false,
                            right_pressed: false,
                            left_released: left_released,
                            right_released: right_released,
                            position_x: 0,
                            position_y: 0,
                        });
                },

                Motion { .. } => {
                    let position_x = (event.position.0 as i32) + self.logical_x;
                    let position_y = (event.position.1 as i32) + self.logical_y;
                    let mut mouse_states = MOUSE_STATES.lock().unwrap();
                    mouse_states
                        .entry(self.sender_index)
                        .and_modify(|m| {
                            m.position_x = position_x;
                            m.position_y = position_y;
                        })
                        .or_insert(MouseState {
                            left_pressed: false,
                            right_pressed: false,
                            left_released: false,
                            right_released: false,
                            position_x: position_x,
                            position_y: position_y,
                        });
                },

                // unused
                Enter { .. } => {},
                Leave { .. } => {},
                Axis { .. } => {},
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

delegate_noop!(Mascot: ignore wl_region::WlRegion);
impl Mascot {
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
            for (chunk, rgb) in canvas.chunks_exact_mut(4).zip(self.rgb.iter()) {
                chunk[3] = (rgb >> 24) as u8;
                chunk[2] = (rgb >> 16) as u8;
                chunk[1] = (rgb >> 8) as u8;
                chunk[0] = (rgb >> 0) as u8;
            }

            // Set the mask shape
            let shape = self.compositor_state.wl_compositor().create_region(&qh, ());
            for (x, y, width, height) in get_mask(&self.rgb, self.width, self.height) {
                shape.add(x, y, width, height);
            }
            self.layer.set_input_region(Some(&shape));
        }

        self.layer.wl_surface().damage_buffer(0, 0, width, height);
        self.layer.wl_surface().frame(qh, self.layer.wl_surface().clone());
        buffer.attach_to(self.layer.wl_surface()).expect("Failed to attach buffer");
        self.layer.commit();
    }
}

static SENDERS: Mutex<Vec<Sender<Event>>> = Mutex::new(Vec::new());
static MOUSE_STATES: LazyLock<Mutex<HashMap<i32, MouseState>>> = LazyLock::new(|| {
    Mutex::new(HashMap::new())
});

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createMascot<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> i32 {
    let (sender, receiver) = channel::<Event>();
    let mut senders = SENDERS.lock().unwrap();
    senders.push(sender);
    let sender_index = (senders.len() - 1) as i32;

    let connection = Connection::connect_to_env().unwrap();
    let (globals, mut event_queue) = registry_queue_init(&connection).unwrap();
    let qh = event_queue.handle();

    let compositor_state =CompositorState::bind(&globals, &qh).expect("Failed to get compositor state");
    let layer_shell = LayerShell::bind(&globals, &qh).expect("Failed to create layer shell");

    let shm = Shm::bind(&globals, &qh).expect("Failed to create shm");
    let pool = SlotPool::new(256 * 256 * 4, &shm).expect("Failed to create pool");
    let surface = compositor_state.create_surface(&qh);
    let layer = layer_shell.create_layer_surface(&qh, surface, Layer::Top, Some("shimelinux"), None);

    layer.set_exclusive_zone(-1);
    layer.set_anchor(Anchor::TOP | Anchor::LEFT);
    layer.set_size(128, 128);
    layer.set_keyboard_interactivity(KeyboardInteractivity::None);
    layer.commit();

    let mut mascot = Mascot {
        compositor_state: compositor_state,
        registry_state: RegistryState::new(&globals),
        output_state: OutputState::new(&globals, &qh),
        seat_state: SeatState::new(&globals, &qh),
        shm,
        pool,
        layer: layer,
        width: 128,
        height: 128,
        first_configure: true,
        sender_index: sender_index,
        pointer: None,
        logical_x: 0,
        logical_y: 0,
        rgb: Vec::new(),
    };

    std::thread::spawn(move || {
        loop {
            _ = event_queue.blocking_dispatch(&mut mascot);

            // Handle events
            while let Ok(event) = receiver.try_recv() {
                match event {
                    Event::SetBounds(x, y, width, height) => {
                        mascot.layer.set_size(max(1, width as u32), max(1, height as u32));
                        mascot.layer.set_margin(y, 0, 0, x);
                    },
                    Event::UpdateImage(rgb) => {
                        mascot.rgb = rgb;
                    },
                    Event::Dispose() => {
                        mascot.layer.wl_surface().destroy();
                    },
                }
            }
        }
    });

    sender_index
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
        let _ = sender.send(Event::SetBounds(x, y, max(1, width), max(1, height)));
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
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_getMouseState<'caller>(
    mut unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
    sender_index: i32,
) -> JIntArray<'caller> {
    let mut mouse_states = MOUSE_STATES.lock().unwrap();
    let result = unowned_env.with_env(|env| -> Result<JIntArray, Error> {
        let array = JIntArray::new(env, 6).expect("Failed to get array");
        if let Some(mouse_state) = mouse_states.get_mut(&sender_index) {
            array.set_region(env, 0, &[
                mouse_state.left_pressed as i32,
                mouse_state.right_pressed as i32,
                mouse_state.left_released as i32,
                mouse_state.right_released as i32,
                mouse_state.position_x,
                mouse_state.position_y,
            ]).expect("Failed to set array");

            mouse_state.left_pressed = false;
            mouse_state.right_pressed = false;
            mouse_state.left_released = false;
            mouse_state.right_released = false;
        } else {
            array.set_region(env, 0, &[0, 0, 0, 0, 0, 0]).expect("Failed to set array");
        }

        Ok(array)
    });

    result.resolve::<ThrowRuntimeExAndDefault>()
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

fn get_mask(rgb: &Vec<i32>, width: u32, height: u32) -> Vec<(i32, i32, i32, i32)> {
    let mut rects: Vec<(i32, i32, i32, i32)> = Vec::new();

    for y in 0..height {
        for x in 0..width {
            let index = ((y * width) + x) as usize;
            if (rgb[index] >> 24) & 0xFF > 0 {
                rects.push((x as i32, y as i32, 1, 1));
            }
        }
    }

    rects
}

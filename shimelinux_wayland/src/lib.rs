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
    sync::{
        Mutex,
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
    delegate_compositor, delegate_layer, delegate_output, delegate_registry, delegate_shm,
    output::{OutputHandler, OutputState},
    registry::{ProvidesRegistryState, RegistryState},
    registry_handlers,
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
    Connection, QueueHandle,
    globals::registry_queue_init,
    protocol::{
        wl_output::{Transform, WlOutput},
        wl_shm::Format,
        wl_surface::WlSurface,
    },
};

enum Event {
    SetBounds(i32, i32, i32, i32),
    UpdateImage(Vec<i32>),
}

struct Mascot {
    registry_state: RegistryState,
    output_state: OutputState,
    shm: Shm,
    pool: SlotPool,
    layer: LayerSurface,
    width: u32,
    height: u32,
    first_configure: bool,
    rgb: Vec<i32>,
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
        _output: WlOutput
    ) {
    }

    fn update_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _output: WlOutput
    ) {
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
        self.width = width;
        self.height = height;

        // Draw the mascot for the first time if this is the first configure
        if self.first_configure {
            self.first_configure = false;
            self.draw(qh);
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

static SENDERS: Mutex<Vec<Sender<Event>>> = Mutex::new(Vec::new());

impl Mascot {
    fn draw(&mut self, qh: &QueueHandle<Self>) {
        let width = self.width as i32;
        let height = self.height as i32;
        let stride = width * 4;

        let (buffer, canvas) = self
            .pool
            .create_buffer(width, height, stride, Format::Argb8888)
            .expect("Failed to create buffer");

        // Copy the RGB data to the canvas
        unsafe {
            std::ptr::copy(
                self.rgb.as_ptr() as *const u8,
                canvas.as_mut_ptr(),
                self.rgb.len() * 4,
            );
        }

        self.layer.wl_surface().damage_buffer(0, 0, width, height);
        self.layer.wl_surface().frame(qh, self.layer.wl_surface().clone());
        buffer.attach_to(self.layer.wl_surface()).expect("Failed to attach buffer");
        self.layer.commit();
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createMascot<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) -> i32 {
    let (sender, receiver) = channel::<Event>();
    let mut senders = SENDERS.lock().unwrap();
    senders.push(sender);
    let sender_index = senders.len() - 1;

    let connection = Connection::connect_to_env().unwrap();
    let (globals, mut event_queue) = registry_queue_init(&connection).unwrap();
    let qh = event_queue.handle();

    let compositor_state =CompositorState::bind(&globals, &qh).expect("Failed to get compositor state");
    let layer_shell = LayerShell::bind(&globals, &qh).expect("Failed to create layer shell");

    let shm = Shm::bind(&globals, &qh).expect("Failed to create shm");
    let pool = SlotPool::new(256 * 256 * 4, &shm).expect("Failed to create pool");
    let surface = compositor_state.create_surface(&qh);
    let layer = layer_shell.create_layer_surface(&qh, surface, Layer::Top, Some("shimeji"), None);

    layer.set_anchor(Anchor::TOP | Anchor::LEFT);
    layer.set_size(128, 128);
    layer.set_keyboard_interactivity(KeyboardInteractivity::None);
    layer.commit();

    let mut mascot = Mascot {
        registry_state: RegistryState::new(&globals),
        output_state: OutputState::new(&globals, &qh),
        shm,
        pool,
        layer: layer,
        width: 128,
        height: 128,
        first_configure: true,
        rgb: Vec::new(),
    };

    std::thread::spawn(move || {
        loop {
            // Handle events
            while let Ok(event) = receiver.try_recv() {
                match event {
                    Event::SetBounds(x, y, width, height) => {
                        mascot.layer.set_size(width as u32, height as u32);
                        mascot.layer.set_margin(y, 0, 0, x);
                    }
                    Event::UpdateImage(rgb) => {
                        mascot.rgb = rgb;
                    }
                }
            }

            event_queue.blocking_dispatch(&mut mascot).expect("Failed to dispatch");
        }
    });

    sender_index as i32
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

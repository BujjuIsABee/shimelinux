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

use std::num::NonZeroU32;

use jni::{EnvUnowned, objects::JClass};
use smithay_client_toolkit::{compositor::{CompositorHandler, CompositorState}, delegate_compositor, delegate_layer, delegate_output, delegate_registry, delegate_shm, output::{self, OutputHandler, OutputState}, registry::{ProvidesRegistryState, RegistryState}, registry_handlers, shell::{WaylandSurface, wlr_layer::{self, Anchor, Layer, LayerShell, LayerShellHandler, LayerSurface}}, shm::{Shm, ShmHandler, slot::SlotPool}};
use wayland_client::{Connection, QueueHandle, globals::registry_queue_init, protocol::{wl_output, wl_shm::Format, wl_surface}};

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_test<'caller>(
    mut _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) {
    let connection = Connection::connect_to_env().unwrap();
    let (globals, mut event_queue) = registry_queue_init(&connection).unwrap();
    let qh = event_queue.handle();

    let layer_shell = LayerShell::bind(&globals, &qh).expect("Failed to create layer shell");
    let shm = Shm::bind(&globals, &qh).expect("Failed to create shm");
    let pool = SlotPool::new(256 * 256 * 4, &shm).expect("Failed to create pool");

    let compositor_state = CompositorState::bind(&globals, &qh).expect("Failed to get compositor state");
    let surface = compositor_state.create_surface(&qh);
    let layer_surface = layer_shell.create_layer_surface(&qh, surface, Layer::Top, Some("shimelinux"), None);

    layer_surface.set_anchor(Anchor::TOP | Anchor::LEFT);
    layer_surface.set_size(128, 128);
    layer_surface.set_keyboard_interactivity(wlr_layer::KeyboardInteractivity::None);
    layer_surface.commit();

    let mut layer = ShimejiiLayer {
        registry_state: RegistryState::new(&globals),
        output_state: OutputState::new(&globals, &qh),
        layer: layer_surface,
        exit: false,
        width: 128,
        height: 128,
        shm: shm,
        pool: pool,
        first_configure: true,
    };

    loop {
        event_queue.blocking_dispatch(&mut layer).expect("Failed to dispatch");

        if layer.exit {
            break;
        }
    }
}

struct ShimejiiLayer {
    registry_state: RegistryState,
    output_state: OutputState,
    layer: LayerSurface,
    exit: bool,
    width: u32,
    height: u32,
    shm: Shm,
    pool: SlotPool,
    first_configure: bool,
}

impl ShmHandler for ShimejiiLayer {
    fn shm_state(&mut self) -> &mut Shm {
        &mut self.shm
    }
}

impl CompositorHandler for ShimejiiLayer {
    fn scale_factor_changed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &wl_surface::WlSurface,
        _new_factor: i32,
    ) {
    }

    fn transform_changed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &wl_surface::WlSurface,
        _new_transform: wl_output::Transform,
    ) {
    }

    fn frame(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        _surface: &wl_surface::WlSurface,
        _time: u32,
    ) {
        self.draw(qh);
    }

    fn surface_enter(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &wl_surface::WlSurface,
        _output: &wl_output::WlOutput,
    ) {
    }

    fn surface_leave(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _surface: &wl_surface::WlSurface,
        _output: &wl_output::WlOutput,
    ) {
    }
}

impl OutputHandler for ShimejiiLayer {
    fn output_state(&mut self) -> &mut output::OutputState {
        &mut self.output_state
    }

    fn new_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _output: wl_output::WlOutput,
    ) {
    }

    fn update_output(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _output: wl_output::WlOutput,
    ) {
    }

    fn output_destroyed(
        &mut self,
        _conn: &Connection,
        _qh: &QueueHandle<Self>,
        _output: wl_output::WlOutput,
    ) {
    }
}

impl LayerShellHandler for ShimejiiLayer {
    fn closed(&mut self, _conn: &Connection, _qh: &QueueHandle<Self>, _layer: &wlr_layer::LayerSurface) {
        self.exit = true;
    }

    fn configure(
        &mut self,
        _conn: &Connection,
        qh: &QueueHandle<Self>,
        _layer: &wlr_layer::LayerSurface,
        configure: wlr_layer::LayerSurfaceConfigure,
        _serial: u32,
    ) {
        self.width = NonZeroU32::new(configure.new_size.0).map_or(128, NonZeroU32::get);
        self.height = NonZeroU32::new(configure.new_size.1).map_or(128, NonZeroU32::get);

        if self.first_configure {
            self.first_configure = false;
            self.draw(qh);
        }
    }
}

impl ProvidesRegistryState for ShimejiiLayer {
    fn registry(&mut self) -> &mut RegistryState {
        &mut self.registry_state
    }
    registry_handlers![];
}

impl ShimejiiLayer {
    pub fn draw(&mut self, qh: &QueueHandle<Self>) {
        let width = self.width;
        let height = self.height;
        let stride = self.width as i32 * 4;

        let (buffer, canvas) = self
            .pool
            .create_buffer(width as i32, height as i32, stride, Format::Argb8888)
            .expect("Failed to create buffer");

        canvas.chunks_exact_mut(4).enumerate().for_each(|(_index, chunk)| {
            let a = 0xFF as u32;
            let r = 0xFF as u32;
            let g = 0xFF as u32;
            let b = 0xFF as u32;
            let color = (a << 24) + (r << 16) + (g << 8) + b;

            let array: &mut [u8; 4] = chunk.try_into().unwrap();
            *array = color.to_le_bytes();
        });

        self.layer.wl_surface().damage_buffer(0, 0, width as i32, height as i32);

        self.layer.wl_surface().frame(qh, self.layer.wl_surface().clone());

        buffer.attach_to(self.layer.wl_surface()).expect("Failed to attach buffer");

        self.layer.commit();
    }
}

delegate_registry!(ShimejiiLayer);
delegate_layer!(ShimejiiLayer);
delegate_compositor!(ShimejiiLayer);
delegate_output!(ShimejiiLayer);
delegate_shm!(ShimejiiLayer);

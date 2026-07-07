use std::env;

use gtk::{prelude::*, Application};
use gtk4_layer_shell::LayerShell;
use jni::{EnvUnowned, objects::JClass};

#[unsafe(no_mangle)]
pub extern "system" fn Java_io_github_bujjuisabee_shimelinux_linux_WaylandLib_createLayer<'caller>(
    _unowned_env: EnvUnowned<'caller>,
    _class: JClass<'caller>,
) {
    unsafe {
        env::set_var("GDK_BACKEND", "wayland");
    }

    gtk::init().expect("Failed to init gtk");

    let application = Application::builder()
        .application_id("io.github.bujjuisabee.shimelinux")
        .build();

    application.connect_activate(|app| {
        let window = gtk::ApplicationWindow::builder()
            .application(app)
            .default_width(128)
            .default_height(128)
            .build();

        let test = gtk::Label::new(Some("test"));

        window.init_layer_shell();

        window.set_child(Some(&test));

        window.present();
    });

    application.run_with_args(&[""]);
}

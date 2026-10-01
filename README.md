<div align="center">

<img width="96" src="icon.svg" alt="Logo"/>

# ShimeLinux

An unofficial port of Shimeji-ee desktop pet for Linux and BSD. Any Shimeji made for the latest version of Shimeji-ee should work. See the supported operating systems, desktop environments, and tiling window managers [here](https://github.com/BujjuIsABee/shimelinux#compatibility).

[![Release](https://img.shields.io/github/v/release/BujjuIsABee/shimelinux?style=for-the-badge&logo=github&color=b7bdf8&labelColor=363a4f)](https://github.com/BujjuIsABee/shimelinux/releases)
[![Issues](https://img.shields.io/github/issues/BujjuIsABee/shimelinux?style=for-the-badge&logo=github&color=f5c2e7&labelColor=363a4f)](https://github.com/BujjuIsABee/shimelinux/issues)
[![License](https://img.shields.io/github/license/BujjuIsABee/shimelinux?style=for-the-badge&color=a6da95&labelColor=363a4f)](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE)

![Screenshot](.github/images/screenshot.png)

</div>

## Installation

### Debian-based distributions

If you are using **Debian** or a Debian-based distribution, you can download the `.deb` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### RPM-based distributions

If you are using an RPM-based distribution, such as **Fedora**, you can download the `.rpm` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### Arch-based distributions

If you are using **Arch** or an Arch-based distribution, you can install ShimeLinux from the Arch User Repository.

```
git clone https://aur.archlinux.org/shimelinux.git
cd shimelinux
makepkg -si
```

With an AUR helper:

```
paru -S shimelinux
```

### Nix and NixOS

If you are using **NixOS** or are using the Nix package manager, you can install ShimeLinux from the Nix User Repository.

First, set up the NUR by following its [documentation](https://nur.nix-community.org/documentation/).

You can then install it with the Home Manager module:

```nix
{
  imports = [
    inputs.nur.repos.claymorwan.homeModules.shimelinux
  ];

  shimelinux = {
    enable = true;
    # If you want ShimeLinux to launch on boot (off by default)
    autostart = true;
  };
}
```

Alternatively, you can also add ShimeLinux to your packages:

```nix
{
  # System-wide install
  environment.systemPackages = with pkgs; [
    nur.repos.claymorwan.shimelinux
  ];

  # User-side / Home Manager install
  home.packages = with pkgs; [
    nur.repos.claymorwan.shimelinux
  ];
}
```

### Other

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases) and run it with Java. You will also need to install libappindicator or libayatana-appindicator for the system tray icon to work.

## Usage

When you open ShimeLinux, a Shimeji will appear. You can right-click on a Shimeji to open a menu with options for that Shimeji, or right-click on the system tray icon for general options. To close the program, open either of these menus and select "Dismiss All."

To add more Shimeji, right-click on the system tray icon and select "Choose Shimeji...." Then, click on the "More..." button to open the `img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and choose Shimeji from the list.

## Compatibility

ShimeLinux has been tested on the following operating systems and desktop environments:

| Operating system | Desktop environment(s)                                                                                                                                                                                      | Install                                                                        |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| Arch Linux       | • GNOME 50 <br/> • Hyprland v0.56.2 <br/> • KDE Plasma 6.6 and 6.7 <br/> • niri v26.04 <br/> • sway 1.12 <br/> • Awesome 4.3 <br/> • Cinnamon 6.6.9 <br/> • dwm 6.8 <br/> • i3 4.25.1 <br/> • xmonad 0.18.1 | [![AUR]](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)   |
| Debian 13.7      | • LXQt 2.1.0 <br/> • MATE 1.26                                                                                                                                                                              | [![DEB]](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions) |                                                                             |
| Fedora Linux 44  | • KDE Plasma 6.7                                                                                                                                                                                            | [![RPM]](https://github.com/BujjuIsABee/shimelinux#rpm-based-distributions)    |
| FreeBSD 15.1     | • KDE Plasma 6.7 <br/> • Xfce 4.20                                                                                                                                                                          | [![JAR]](https://github.com/BujjuIsABee/shimelinux#other)                      |
| Linux Mint 22.3  | • Cinnamon 6.6.9                                                                                                                                                                                            | [![DEB]](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions) |
| NixOS 26.05      | • KDE Plasma 6.7 <br/> • niri v26.04                                                                                                                                                                        | [![NUR]](https://github.com/BujjuIsABee/shimelinux#nix-and-nixos)              |
| Pop!_OS 24.04    | • COSMIC 1.8                                                                                                                                                                                                | [![DEB]](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions) |
| Ubuntu 26.04     | • GNOME 50                                                                                                                                                                                                  | [![DEB]](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions) |
| Void Linux       | • Xfce 4.20                                                                                                                                                                                                 | [![JAR]](https://github.com/BujjuIsABee/shimelinux#other)                      |

[AUR]: https://img.shields.io/badge/AUR-rgba(0,0,0,0)?style=flat-square&logo=archlinux
[DEB]: https://img.shields.io/badge/.deb-rgba(0,0,0,0)?style=flat-square&logo=debian&logoColor=red
[RPM]: https://img.shields.io/badge/.rpm-rgba(0,0,0,0)?style=flat-square&logo=redhat&logoColor=red
[JAR]: https://custom-icon-badges.demolab.com/badge/.jar-rgba(0,0,0,0)?style=flat-square&logo=java&logoColor=orange
[NUR]: https://img.shields.io/badge/NUR-rgba(0,0,0,0)?style=flat-square&logo=nixos

### Graphical issues on Wayland

When running ShimeLinux on Wayland, Shimeji may be displayed through XWayland instead of on native Wayland surfaces. If you notice graphical issues, such as a black background appearing behind Shimeji, try enabling the Wayland environment by right-clicking on the system tray icon, selecting "Settings," going to the "Environment" tab, and choosing "Wayland."

> [!NOTE]
> This will not work on GNOME, Cinnamon's Wayland session, or any other desktop environment or compositor that does not implement the `wlr_layer_shell` protocol.

## Building

First, install the following build dependencies:

- Java Development Kit (version 21 or later)
- Cargo
- libxkbcommon
- pkg-config.

For Arch:

```
sudo pacman -S jdk21-openjdk cargo libxkbcommon pkgconf
```

Then, clone the repository with Git:

```
git clone https://github.com/BujjuIsABee/shimelinux
cd shimelinux
```

Then, use Gradle to build:

```
./gradlew build
```

The `.jar` file will be in `build/libs`.

## Licenses

This project incorporates work from [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji), [SystemTray by dorkbox](https://github.com/dorkbox/SystemTray), [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf), [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java), [dbus-java by hypfvieh](https://github.com/hypfvieh/dbus-java), and [Smithay's Client Toolkit](https://github.com/smithay/client-toolkit). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

<div align="center">

# ShimeLinux

An unofficial Linux port of Shimeji-ee desktop pet. Any Shimeji made for the latest version of Shimeji-ee should work. See the supported operating systems, desktop environments, and tiling window managers [here](https://github.com/BujjuIsABee/shimelinux#compatibility).

[![Release]](https://github.com/BujjuIsABee/shimelinux/releases)
[![Issues]](https://github.com/BujjuIsABee/shimelinux/issues)
[![License]](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE)

</div>

![Screenshot]

## Installation

### Debian-based distributions

If you are on **Debian** or a Debian-based distribution, you can download the `.deb` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### RPM-based distributions

If you are on an RPM-based distribution, such as **Fedora**, you can download the `.rpm` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### Arch-based distributions

If you are on **Arch** or an Arch-based distribution, you can install ShimeLinux from the Arch User Repository.

`git clone https://aur.archlinux.org/shimelinux.git`

`cd shimelinux`

`makepkg -si`

You can also use an AUR helper.

`paru -S shimelinux` or `yay -S shimelinux`

### Nix and NixOS

If you are on **NixOS** or are using the Nix package manager, you can install ShimeLinux from the Nix User Repository.

First, set up the NUR by following its [documentation](https://nur.nix-community.org/documentation/).

You can then install it with the Home Manager module:

```nix
{
  imports = [
    inputs.nur.repos.claymorwan.homeModules.shimelinux
  ];

  shimelinux = {
    enable = true;
    # If you want shimelinux to launch on boot (off by default)
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

### Other distributions and operating systems

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator or libayatana-appindicator

## Compatibility

ShimeLinux has been tested on the following operating systems and desktop environments:

| Operating system                                                                                                                    | Desktop environment | Installation                                                                                                |
|-------------------------------------------------------------------------------------------------------------------------------------|---------------------|-------------------------------------------------------------------------------------------------------------|
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=archLinux"> Arch Linux          | GNOME               | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=archLinux"> Arch Linux          | Hyprland            | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=archLinux"> Arch Linux          | KDE Plasma          | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=archLinux"> Arch Linux          | niri                | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=archLinux"> Arch Linux          | sway                | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=fedora"> Fedora Linux           | KDE Plasma          | [Download `.rpm` file](https://github.com/BujjuIsABee/shimelinux#rpm-based-distributions)                   |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=freebsd&logoColor=red"> FreeBSD | KDE Plasma          | [Download `.jar` file](https://github.com/BujjuIsABee/shimelinux#other-distributions-and-operating-systems) |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=freebsd&logoColor=red"> FreeBSD | Xfce                | [Download `.jar` file](https://github.com/BujjuIsABee/shimelinux#other-distributions-and-operating-systems) |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=linuxmint"> Linux Mint          | Cinnamon            | [Download `.deb` file](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions)                |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=nixos"> NixOS                   | KDE Plasma          | [Install via NUR](https://github.com/BujjuIsABee/shimelinux#nix-and-nixos)                                  |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=nixos"> NixOS                   | niri                | [Install via NUR](https://github.com/BujjuIsABee/shimelinux#nix-and-nixos)                                  |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=popos"> Pop!_OS                 | COSMIC              | [Download `.deb` file](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions)                |
| <img width="32" align="top" src="https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=ubuntu"> Ubuntu                 | GNOME               | [Download `.deb` file](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions)                |

### Graphical issues on Wayland

On certain desktop environments/compositors, Shimeji will be displayed using XWayland, which may cause graphical issues. This can be fixed by enabling the Wayland environment:

1. Right-click on the system tray icon and select "Settings"
2. Select the "Environment" tab
3. Choose "Wayland" from the dropdown

> [!NOTE]
> This will not work on GNOME, Cinnamon's Wayland session, or any other desktop environment/compositor that does not implement the `wlr_layer_shell` protocol.

## Usage

When you open ShimeLinux, a Shimeji will appear. You can right-click on a Shimeji to open a menu with options for that Shimeji, or right-click on the system tray icon for general options. To close the program, open one of these menus and select "Dismiss All."

To add more Shimeji, click on the system tray icon and select "Choose Shimeji...." Then, click on the "More..." button to open the `img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and select the Shimeji you want to use.

## Licenses

This project incorporates work from [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji), [SystemTray by dorkbox](https://github.com/dorkbox/SystemTray), [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf), [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java), [dbus-java by hypfvieh](https://github.com/hypfvieh/dbus-java), and [Smithay's Client Toolkit](https://github.com/smithay/client-toolkit). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

[Screenshot]: .github/images/screenshot.png
[Release]: https://img.shields.io/github/v/release/BujjuIsABee/shimelinux?style=for-the-badge&logo=github&color=b7bdf8&labelColor=363a4f
[Issues]: https://img.shields.io/github/issues/BujjuIsABee/ShimeLinux?style=for-the-badge&logo=github&color=f5c2e7&labelColor=363a4f
[License]: https://img.shields.io/github/license/BujjuIsABee/shimelinux?style=for-the-badge&color=a6da95&labelColor=363a4f

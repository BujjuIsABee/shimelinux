<div align="center">

# ShimeLinux

An unofficial Linux port of Shimeji-ee desktop pet. Any Shimeji made for the latest version of Shimeji-ee should work. See the supported operating systems, desktop environments, and tiling window managers [here](https://github.com/BujjuIsABee/shimelinux#compatibility).

[![Release]](https://github.com/BujjuIsABee/shimelinux/releases)
[![Issues]](https://github.com/BujjuIsABee/shimelinux/issues)
[![License]](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE)

[![Arch]![Fedora]![FreeBSD]![Mint]![NixOS]![Pop!_OS]![Ubuntu]![Cinnamon]![GNOME]![Hyprland]![Plasma]![niri]](https://github.com/BujjuIsABee/shimelinux#compatibility)

![Screenshot]

</div>

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

### Other distributions and operating systems

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator or libayatana-appindicator

## Compatibility

ShimeLinux has been tested on the following operating systems and desktop environments:

| Operating system | Desktop environment | Installation                                                                                                |
|------------------|---------------------|-------------------------------------------------------------------------------------------------------------|
| Arch Linux       | GNOME               | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| Arch Linux       | Hyprland            | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| Arch Linux       | KDE Plasma          | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| Arch Linux       | niri                | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| Arch Linux       | sway                | [Install via AUR](https://github.com/BujjuIsABee/shimelinux#arch-based-distributions)                       |
| Fedora Linux     | KDE Plasma          | [Download `.rpm` file](https://github.com/BujjuIsABee/shimelinux#rpm-based-distributions)                   |
| FreeBSD          | KDE Plasma          | [Download `.jar` file](https://github.com/BujjuIsABee/shimelinux#other-distributions-and-operating-systems) |
| FreeBSD          | Xfce                | [Download `.jar` file](https://github.com/BujjuIsABee/shimelinux#other-distributions-and-operating-systems) |
| Linux Mint       | Cinnamon            | [Download `.deb` file](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions)                |
| NixOS            | KDE Plasma          | [Install via NUR](https://github.com/BujjuIsABee/shimelinux#nix-and-nixos)                                  |
| NixOS            | niri                | [Install via NUR](https://github.com/BujjuIsABee/shimelinux#nix-and-nixos)                                  |
| Pop!_OS          | COSMIC              | [Download `.deb` file](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions)                |
| Ubuntu           | GNOME               | [Download `.deb` file](https://github.com/BujjuIsABee/shimelinux#debian-based-distributions)                |

### Graphical issues on Wayland

When running ShimeLinux on Wayland, Shimeji may be displayed through an X11 compatibility layer like XWayland instead of native Wayland surfaces. If you notice graphical issues, such as a black background appearing behind Shimeji, try enabling the Wayland environment:

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
[Arch]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=archlinux
[Fedora]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=fedora
[FreeBSD]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=freebsd&logoColor=red
[Mint]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=linuxmint
[NixOS]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=nixos
[Pop!_OS]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=popos
[Ubuntu]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=ubuntu
[Cinnamon]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=cinnamon
[GNOME]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=gnome
[Hyprland]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=hyprland
[Plasma]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=kdeplasma&logoColor=white
[niri]: https://img.shields.io/badge/-rgba(0,0,0,0)?style=flat-square&logo=niri

# ShimeLinux

<img width="1280" height="720" alt="Screenshot" src="https://github.com/user-attachments/assets/eb7c5939-7cd2-4fab-8891-eab648211d64" />

An unofficial Linux port of Shimeji-ee desktop pet. Any Shimeji made for the latest version of Shimeji-ee should work. Some tiling window managers are also supported (see [Compatibility](https://github.com/BujjuIsABee/shimelinux#compatibility)).

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

First, set up the NUR by following its [documentation](https://nur.nix-community.org/documentation/)

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

### Other distributions

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator or libayatana-appindicator

## How to use

When you open ShimeLinux, a Shimeji will appear. You can right-click on a Shimeji to open a menu with options for that Shimeji, or right-click on the system tray icon for general options. To close the program, open one of these menus and select "Dismiss All."

To add more Shimeji, click the system tray icon and select "Choose Shimeji...." Then, click the "More..." button to open the `img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and select the Shimeji you want to use.

> [!WARNING]
> Make sure not to select too many Shimeji at once, as ShimeLinux can use a lot of your computer's memory. You can disable Shimeji in the Shimeji chooser or move some that are not in use to the `img/unused` folder.

## Compatibility

ShimeLinux has been tested on the following Linux distributions and desktop environments:

| Distro          | Desktop Environment(s)                                                    |
|-----------------|---------------------------------------------------------------------------|
| Arch Linux      | KDE Plasma 6.6/6.7, GNOME 50, Cinnamon 6.6, Hyprland v0.55.4, niri v26.04 |
| Fedora 44       | KDE Plasma 6.7                                                            |
| Linux Mint 22.3 | Cinnamon 6.6                                                              |
| Ubuntu 26.04    | GNOME 50                                                                  |
| NixOS 26.05     | KDE Plasma 6.7, niri v26.04                                               |

### Tiling Window Managers

ShimeLinux supports some tiling Wayland compositors, though you may need to make some changes to configuration files for it to work properly.

#### Hyprland

Add this to your Hyprland configuration file (`~/.config/hypr/hyprland.lua`):

```lua
hl.env("_JAVA_AWT_WM_NONREPARENTING", "1")
```

#### Niri

Add this to your niri configuration file (`~/.config/niri/config.kdl`):

```kdl
environment {
    _JAVA_AWT_WM_NONREPARENTING "1"
}

window-rule {
    match app-id="com-group_finity-mascot"
    open-floating true
}
```

## Licenses

This project incorporates work from [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji), [SystemTray by dorkbox](https://github.com/dorkbox/SystemTray), [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf), [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java), [dbus-java by hypfvieh](https://github.com/hypfvieh/dbus-java), and [Smithay's Client Toolkit](https://github.com/smithay/client-toolkit). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

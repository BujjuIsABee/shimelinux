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

`yay -S shimelinux` or `paru -S shimelinux`

### Other distributions

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator or libayatana-appindicator

## How to use

When you open ShimeLinux, a Shimeji will appear. You can right-click on a Shimeji to open a menu with options for that Shimeji, or right-click on the system tray icon for general options. To close the program, open one of these menus and select "Dismiss All."

To add more Shimeji, click the system tray icon and select "Choose Shimeji...." Then, click the "More..." button to open the `img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and select the Shimeji you want to use.

> [!WARNING]
> ShimeLinux can use a lot of your computer's memory, so you should make sure not to select too many Shimeji at once. You can disable Shimeji in the Shimeji chooser or move some that are not in use to the `img/unused` folder.

## Compatibility

ShimeLinux has been tested on the following Linux distributions and desktop environments:

| Distro          | Desktop Environment(s)                                                    |
|-----------------|---------------------------------------------------------------------------|
| Arch Linux      | KDE Plasma 6.6/6.7, GNOME 50, Cinnamon 6.6, Hyprland v0.55.4, niri v26.04 |
| Fedora 44       | KDE Plasma 6.7                                                            |
| Linux Mint 22.3 | Cinnamon 6.6                                                              |
| Ubuntu 26.04    | GNOME 50                                                                  |
| NixOS 26.05     | KDE Plasma 6.7, niri v26.04                                               |

### NixOS

The system tray icon does not work on NixOS. To access the settings menu and Shimeji chooser, you will need to right-click on ShimeLinux in your application launcher and select "Settings" or "Choose Shimeji," or launch the application from the terminal with the `--settings` or `--chooser` flags.

### Tiling Window Managers

ShimeLinux supports **Hyprland** and **niri**. On these compositors, Shimeji are displayed using Wayland layers instead of windows. Some menus may be buggy, as ShimeLinux uses an outdated UI library with limited Wayland support.

> [!TIP]
> On niri, the settings menu, Shimeji chooser, and right-click popup menus will be displayed as full windows by default. To make them display as floating windows, add this to your niri config file:
> 
> ```kdl
> window-rule {
>   match app-id="com-group_finity-mascot-MainKt"
>   open-floating true
> }
> ```

## Licenses

This project incorporates work from [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji), [SystemTray by dorkbox](https://github.com/dorkbox/SystemTray), [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf), [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java), [dbus-java by hypfvieh](https://github.com/hypfvieh/dbus-java), and [Smithay's Client Toolkit](https://github.com/smithay/client-toolkit). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

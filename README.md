# ShimeLinux <img width="32" height="32" src="icon.svg" />

**Shimeji desktop pet for Linux**

<img width="1280" height="720" alt="Shimeji" src="https://github.com/user-attachments/assets/eb7c5939-7cd2-4fab-8891-eab648211d64" />

ShimeLinux is an unofficial Linux port/Kotlin rewrite of [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji). This project also incorporates work from [SystemTray by dorkbox](https://github.com/dorkbox/SystemTray), [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf), [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java), [dbus-java by hypfvieh](https://github.com/hypfvieh/dbus-java), and [Smithay's Client Toolkit](https://github.com/smithay/client-toolkit). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

## How to install

### Debian-based distributions

If you are on **Debian** or a Debian-based distribution, you can download the `.deb` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### RPM-based distributions

If you are on an RPM-based distribution like **Fedora**, you can download the `.rpm` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### Arch-based distributions

If you are on **Arch** or an Arch-based distribution, you can install ShimeLinux from the Arch User Repository.

`git clone https://aur.archlinux.org/shimelinux.git`

`cd shimelinux`

`makepkg -si`

You can also use an AUR helper.

`yay -S shimelinux` or `paru -S shimelinux`

### Other distributions

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to make sure that you have the following dependencies installed:

- Java Runtime Environment (version 21 or later)
- libappindicator or libayatana-appindicator

## How to use

When you open ShimeLinux, a Shimeji will appear. To add more Shimeji, click the system tray icon and select "Choose Shimeji...." Then, click the "More..." button to open the `/img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and select the Shimeji you want. Shimeji in the `/unused` folder will be ignored.

## Compatibility

> [!IMPORTANT]
> #### Tiling Window Managers
>
> Support for tiling window managers is an **experimental feature**! You may encounter some bugs. Currently, only Hyprland and niri are supported.
>
> #### NixOS
>
> The system tray icon does not work properly on NixOS. To access the settings menu and Shimeji chooser, you will need to right-click on ShimeLinux in your application launcher and select "Settings" or "Choose Shimeji." If this does not work, you can use these commands:
>
> `java -jar shimelinux.jar --settings`
>
> `java -jar shimelinux.jar --chooser`

ShimeLinux has been tested on the following distributions/desktop environments:

| Distro          | Desktop Environment(s)                                                    |
|-----------------|---------------------------------------------------------------------------|
| Arch Linux      | KDE Plasma 6.6/6.7, GNOME 50, Cinnamon 6.6, Hyprland v0.55.4, niri v26.04 |
| Fedora 44       | KDE Plasma 6.7                                                            |
| Linux Mint 22.3 | Cinnamon 6.6                                                              |
| Ubuntu 26.04    | GNOME 50                                                                  |
| NixOS 26.05     | KDE Plasma 6.7                                                            |

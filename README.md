# ShimeLinux <img width="24" height="24" src="icon.svg" />

**Shimeji desktop pet for Linux**

<img width="1280" height="720" alt="Shimeji" src="https://github.com/user-attachments/assets/eb7c5939-7cd2-4fab-8891-eab648211d64" />

> [!NOTE]
> ShimeLinux is an unofficial Linux port/Kotlin rewrite of [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji). This project also incorporates work from [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java) and [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

## How to install

### Debian-based distributions

If you are on **Debian** or a Debian-based distro like **Ubuntu** or **Linux Mint**, you can download the `.deb` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### RPM-based distributions

If you are on an RPM-based distro like **Fedora**, you can download the `.rpm` file [here](https://github.com/BujjuIsABee/shimelinux/releases).

### Arch-based distributions

If you are on **Arch** or an Arch-based distro, you can install ShimeLinux from the Arch User Repository.

`git clone https://aur.archlinux.org/shimelinux.git`

`cd shimelinux`

`makepkg -si`

You can also use an AUR helper like `yay` or `paru`.

### Other distributions

If none of these options work for you, you can download the `.jar` file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator or libayatana-appindicator

## How to use

When you open ShimeLinux, a Shimeji will appear. To add more Shimeji, click the tray icon and select "Choose Shimeji...." Then, click the "More..." button to open the `/img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and select the Shimeji you want. Shimeji in the `/img/unused` folder will be ignored.
# ShimeLinux <img width="24" height="24" src="src/main/resources/icon.png" />

**Shimeji desktop pet for Linux**

<img width="1280" height="720" src="https://github.com/user-attachments/assets/eb7c5939-7cd2-4fab-8891-eab648211d64" />

ShimeLinux is an unofficial Linux port/Kotlin rewrite of [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji). View the original license [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

## How to install

### Any distribution

Download the JAR file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator

### Arch Linux (via the AUR)

If you are using Arch Linux, you can install ShimeLinux from the Arch User Repository:

`git clone https://aur.archlinux.org/shimelinux.git`

`cd shimelinux`

`makepkg -si`

You can also use an AUR helper like yay or paru.

## How to use

When you open ShimeLinux, a Shimeji will appear. To add more Shimeji, click on the tray icon and select "Choose Shimeji...." Then, click on the "Add" button to open the `/img` folder. Once you've added Shimeji to this folder, you can reopen the image set chooser and select the Shimeji you want. Shimeji in the `/img/unused` folder will be ignored.
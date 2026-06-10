# ShimeLinux ![icon](src/main/resources/img/icon.png)

**Shimeji desktop pet for Linux**

> [!NOTE]
ShimeLinux is an **unofficial** Linux port/Kotlin rewrite of [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji). View the original license [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

## How to install

Currently, there are two ways to install ShimeLinux. I plan to provide more options in the future.

### Any distribution

To install ShimeLinux on any Linux distribution, you can download the JAR file [here](https://github.com/BujjuIsABee/shimelinux/releases). You will also need to install the following dependencies:

- Java Runtime Environment (version 21 or later)
- libappindicator (for the tray icon)

### Arch Linux (via the AUR)

If you are using Arch Linux, you can also install ShimeLinux from the Arch User Repository.

#### With an AUR helper

`yay -S shimelinux` or `paru -S shimelinux`

#### Manually

`git clone https://aur.archlinux.org/shimelinux.git`

`cd shimelinux`

`makepkg -si`

## How to use

If you downloaded a JAR file, open it with Java. If you used the AUR, use your desktop environment's application launcher. When you open ShimeLinux, a Shimeji will appear. To add more Shimeji, click on the tray icon and select "Choose Shimeji...." Then, click on the + button to open the `/img` folder. Once you've added Shimeji to this folder, you can reopen the image set chooser and select the Shimeji you want. Shimeji in the `/img/unused` folder will be ignored.
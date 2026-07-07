# ShimeLinux <img width="24" height="24" src="icon.svg" />

**Shimeji desktop pet for Linux**

<img width="1280" height="720" alt="Shimeji" src="https://github.com/user-attachments/assets/eb7c5939-7cd2-4fab-8891-eab648211d64" />

ShimeLinux is an unofficial Linux port/Kotlin rewrite of [Shimeji-ee by Kilkakon](https://kilkakon.com/shimeji). This project also incorporates work from [SystemTray by dorkbox](https://github.com/dorkbox/SystemTray), [FlatLaf by FormDev](https://github.com/JFormDesigner/FlatLaf), [hqx-java by Arcnor](https://github.com/Arcnor/hqx-java), and [dbus-java by hypfvieh](https://github.com/hypfvieh/dbus-java). You can view the licenses for these projects [here](https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL).

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

#### Create a desktop entry:

To create a desktop entry, follow these steps:

**Step 1:** Move the `.jar` file to `/usr/share/java` and rename it to `shimelinux.jar`

**Step 2:** Download `shimelinux.sh` from [here](https://github.com/BujjuIsABee/shimelinux/blob/master/shimelinux.sh), move it to `/usr/bin`, rename it to `shimelinux`, and use the following command to give it executable permissions: `chmod +x shimelinux`

**Step 3:** Download `shimelinux.desktop` from [here](https://github.com/BujjuIsABee/shimelinux/blob/master/shimelinux.desktop) and move it to `/usr/share/applications`

## How to use

When you open ShimeLinux, a Shimeji will appear. To add more Shimeji, click the tray icon and select "Choose Shimeji...." Then, click the "More..." button to open the `/img` folder. Once you've added Shimeji to this folder, you can reopen the Shimeji chooser and select the Shimeji you want. Shimeji in the `/unused` folder will be ignored.

## Compatibility

> [!NOTE]
> ShimeLinux will not work properly on tiling window managers. Try [wl_shimeji](https://github.com/CluelessCatBurger/wl_shimeji).

ShimeLinux has been tested on the following distributions/desktop environments:

| Distro          | Desktop Environment(s)                     |
|-----------------|--------------------------------------------|
| Arch Linux      | KDE Plasma 6.6/6.7, GNOME 50, Cinnamon 6.6 |
| Fedora 44       | KDE Plasma 6.7                             |
| Linux Mint 22.3 | Cinnamon 6.6                               |
| Ubuntu 26.04    | GNOME 50                                   |
| NixOS 26.05*    | KDE Plasma 6.7                             |

### NixOS

The system tray icon does not work properly on NixOS. When you first launch ShimeLinux, you will get an error message saying that the system tray icon could not be created. You can still choose a language and access the Shimeji chooser and settings menus.

#### Option 1: From the desktop entry

If you [created a desktop entry](https://github.com/BujjuIsABee/shimelinux/edit/master/README.md#create-a-desktop-entry), you can right-click on it and use the "Choose Language", "Choose Shimeji", or "Settings" options.

#### Option 2: From the terminal

If you did not create a desktop entry, you can use these commands. Make sure to replace `shimelinux.jar` with the path to the `.jar` file.

`java -jar shimelinux.jar --language-chooser`

`java -jar shimelinux.jar --shimeji-chooser`

`java -jar shimelinux.jar --settings`

#### Option 3: By editing the configuration files

You can also manually edit the configuration files if necessary. The main configuration file is located at `~/.config/shimelinux/conf/settings.properties`. Create it if it does not exist.

**Example:**

```properties
# Add a Shimeji
ActiveShimeji=Shimeji/Other Shimeji

# Choose a language
Language=en-US

# Whitelist/Blacklist Interactive Windows
InteractiveWindows=Whitelisted Window/Other Whitelisted Window
InteractiveWindowsBlacklist=Blacklisted Window/Other Blacklisted Window

# Enable/Disable Behaviors
Breeding=true
Transients=true
Transformation=true
ThrowingWindows=true
SoundEffects=true
Multiscreen=true

# Configure Windowed Mode
Environment=linux # Change to "virtual" to enable windowed mode
Background=\#00FF00
WindowSize=600x500

# Other Settings
AlwaysShowShimejiChooser=false
AlwaysShowInformationScreen=false
Filter=Nearest # Nearest, Bicubic, Hqx
MenuScaling=1
Opacity=1.0
Scaling=1.0
Theme=FlatDark # FlatDark, FlatLight, or GTK
```

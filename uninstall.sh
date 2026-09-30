#!/usr/bin/env bash

uninstall() {
  echo "Uninstalling!"

  rm ~/.local/share/java/shimelinux.jar
  rm ~/.local/bin/shimelinux
  rm ~/.local/share/icons/hicolor/scalable/apps/shimelinux.svg
  rm ~/.local/share/applications/shimelinux.desktop

  echo "Done!"
}

if [ "$(id -u)" -eq 0 ]; then
  echo "Please do not run as root or with sudo."
  exit 1
fi

read -p "Uninstall ShimeLinux? [y/N] "
case $REPLY in
  [Yy])
    uninstall
    break
    ;;
  *)
    exit 0
    ;;
esac

read -p "Permanently delete Shimeji image sets and configuration files? [y/N] "
case $REPLY in
  [Yy])
    rm -rf ~/.config/shimelinux
    echo "Done!"
    ;;
  *)
    exit 0
    ;;
esac

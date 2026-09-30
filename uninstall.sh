#!/usr/bin/env bash

uninstall() {
  echo "Uninstalling!"

  rm ~/.local/share/java/shimelinux.jar
  rm ~/.local/bin/shimelinux
  rm ~/.local/share/icons/hicolor/scalable/apps/shimelinux.svg
  rm ~/.local/share/applications/shimelinux.desktop
  rm -rf ~/.config/shimelinux

  echo "Done!"
}

if [ "$(id -u)" -eq 0 ]; then
  echo "Please do not run as root or with sudo."
  exit 1
fi

echo "WARNING: All Shimeji image sets will be permanently deleted!"
read -p "Uninstall ShimeLinux? [y/N] "
case $REPLY in
  [Yy])
    uninstall
    ;;
  *)
    exit 0
    ;;
esac

#!/usr/bin/env bash

install() {
  echo "Installing!"

  ./gradlew build

  mkdir -p ~/.local/share/java
  cp build/libs/shimelinux-*.jar ~/.local/share/java/shimelinux.jar

  mkdir -p ~/.local/bin
  cat > ~/.local/bin/shimelinux << 'EOF'
#!/usr/bin/env bash

if [[ ";$XDG_CURRENT_DESKTOP;" == *";sway;"* ]]; then
  export XDG_CURRENT_DESKTOP="sway"
fi

case "$XDG_CURRENT_DESKTOP" in
  "Hyprland" | "niri" | "sway")
  export _JAVA_AWT_WM_NONREPARENTING=1
  ;;
esac

exec java -jar "$HOME/.local/share/java/shimelinux.jar" "$@"
EOF
  chmod +x ~/.local/bin/shimelinux

  mkdir -p ~/.local/share/icons/hicolor/scalable/apps
  cp icon.svg ~/.local/share/icons/hicolor/scalable/apps/shimelinux.svg

  mkdir -p ~/.local/share/applications
  cat > ~/.local/share/applications/shimelinux.desktop << 'EOF'
[Desktop Entry]
Name=ShimeLinux
Comment=Shimeji desktop pet
Icon=shimelinux
Type=Application
Categories=Graphics;
Exec=sh -c '"$HOME/.local/bin/shimelinux"'
EOF

  echo "Done!"
}

if [ "$(id -u)" -eq 0 ]; then
  echo "Please do not run as root or with sudo."
  exit 1
fi

read -p "Install ShimeLinux? [y/N] "
case $REPLY in
  [Yy])
    install
    ;;
  *)
    exit 0
    ;;
esac

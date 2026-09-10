#!/usr/bin/env bash
if [[ ";$XDG_CURRENT_DESKTOP;" == *";sway;"* ]]; then
  export XDG_CURRENT_DESKTOP="sway"
fi
case "$XDG_CURRENT_DESKTOP" in
  "Hyprland" | "niri" | "sway")
    export _JAVA_AWT_WM_NONREPARENTING=1
    ;;
esac
exec java -jar "/usr/share/java/shimelinux.jar" "$@"

#!/usr/bin/env bash

if [[ ";$XDG_CURRENT_DESKTOP;" == *";sway;"* ]]; then
  export XDG_CURRENT_DESKTOP="sway"
fi

case "$XDG_CURRENT_DESKTOP" in
  "Hyprland" | "niri" | "sway")
    export _JAVA_AWT_WM_NONREPARENTING=1
    ;;
esac

exec /app/jre/bin/java -Duser.home="$HOME/.var/app/io.github.bujjuisabee.shimelinux/config" -Djava.io.tmpdir="$HOME/.var/app/io.github.bujjuisabee.shimelinux/cache/tmp" -jar "/app/share/java/shimelinux.jar" "$@"

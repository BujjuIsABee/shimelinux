#!/usr/bin/env bash

if [[ ";$XDG_CURRENT_DESKTOP;" == *";sway;"* ]]; then
  export XDG_CURRENT_DESKTOP="sway"
fi

case "$XDG_CURRENT_DESKTOP" in
  "Hyprland" | "niri" | "sway")
    export _JAVA_AWT_WM_NONREPARENTING=1
    ;;
esac

JVM_ARGS="-XX:+UseSerialGC \
-XX:-ShrinkHeapInSteps \
-XX:MinHeapFreeRatio=10 \
-XX:MaxHeapFreeRatio=40"

exec /app/jre/bin/java $JVM_ARGS -Duser.home="$HOME/.var/app/io.github.bujjuisabee.shimelinux/config" -Djava.io.tmpdir="$HOME/.var/app/io.github.bujjuisabee.shimelinux/cache/tmp" -jar "/app/share/java/shimelinux.jar" "$@"

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

exec java $JVM_ARGS -jar "/usr/share/java/shimelinux.jar" "$@"

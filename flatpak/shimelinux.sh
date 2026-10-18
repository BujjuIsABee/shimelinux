#!/usr/bin/env bash

case "${XDG_CURRENT_DESKTOP:-$DESKTOP_SESSION}" in
  *Hyprland* | *niri* | *river* | *sway*)
    export _JAVA_AWT_WM_NONREPARENTING=1
    ;;
esac

JVM_ARGS="-XX:+UseSerialGC \
-XX:-ShrinkHeapInSteps \
-XX:MinHeapFreeRatio=10 \
-XX:MaxHeapFreeRatio=40"

export IS_FLATPAK=true

exec /app/jre/bin/java $JVM_ARGS -Dflatpak="true" -Duser.home="$HOME/.var/app/io.github.bujjuisabee.shimelinux/config" -Djava.io.tmpdir="$HOME/.var/app/io.github.bujjuisabee.shimelinux/cache/tmp" -jar "/app/share/java/shimelinux.jar" "$@"

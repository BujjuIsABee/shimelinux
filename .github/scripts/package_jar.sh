#!/usr/bin/env bash

./gradlew build -PmultiArch
cp build/libs/*.jar .

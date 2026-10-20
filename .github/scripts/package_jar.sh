#!/usr/bin/env bash

NAME='shimelinux'
VERSION='1.4.0'

./gradlew build -PappendArch
mv build/libs/*.jar .

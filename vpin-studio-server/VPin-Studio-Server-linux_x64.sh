#!/bin/sh
# Starts the VPin Studio server on Linux, see LINUX-SERVER.md
cd "$(dirname "$(readlink -f -- "$0")")"

JRE=zulu25.34.17-ca-fx-jre25.0.3-linux_x64

if [ ! -d "$JRE" ];
then
	tar -xf "$JRE.tar.gz"
fi

# The overlay and pause menu need a display. Without one, start with -Djava.awt.headless=true.
export DISPLAY="${DISPLAY:-:0}"

exec "./$JRE/bin/java" \
  --add-exports=javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.iio.common=ALL-UNNAMED \
  --enable-native-access=javafx.graphics \
  --enable-native-access=javafx.media \
  --enable-native-access=ALL-UNNAMED \
  -Dfile.encoding=UTF-8 \
  "$@" \
  -jar vpin-studio-server.jar

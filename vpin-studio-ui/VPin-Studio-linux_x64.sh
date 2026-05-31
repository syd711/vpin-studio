#!/bin/sh
cd "$(dirname "$(readlink -f -- "$0")")"

if [ ! -d zulu25.34.17-ca-fx-jre25.0.3-linux_x64 ];
then
	tar -xvf zulu25.34.17-ca-fx-jre25.0.3-linux_x64.tar.gz
fi

./zulu25.34.17-ca-fx-jre25.0.3-linux_x64/bin/java \
  --add-exports=javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.iio.common=ALL-UNNAMED \
  --enable-native-access=javafx.graphics \
  --enable-native-access=javafx.web \
  -jar vpin-studio-ui.jar

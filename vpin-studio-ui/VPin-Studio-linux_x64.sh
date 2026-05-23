#!/bin/sh
cd "$(dirname "$(readlink -f -- "$0")")"

if [ ! -d zulu25.34.17-ca-fx-jre25.0.3-linux_x64 ];
then
	tar -xvf zulu25.34.17-ca-fx-jre25.0.3-linux_x64.tar.gz
fi

./zulu25.34.17-ca-fx-jre25.0.3-linux_x64/bin/java -jar vpin-studio-ui.jar

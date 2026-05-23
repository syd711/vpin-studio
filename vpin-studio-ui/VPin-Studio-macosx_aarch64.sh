#!/bin/sh
if [[ ! -d zulu25.34.17-ca-fx-jre25.0.3-macosx_aarch64 ]];
then
	tar -xvf zulu25.34.17-ca-fx-jre25.0.3-macosx_aarch64.tar.gz
fi

./zulu25.34.17-ca-fx-jre25.0.3-macosx_aarch64/zulu-25.jre/Contents/Home/bin/java -jar vpin-studio-ui.jar

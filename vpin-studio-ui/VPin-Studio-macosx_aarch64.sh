#!/bin/sh
if [[ ! -d zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64 ]];
then
	tar -xvf zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64.tar.gz
fi

./zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64/zulu-11.jre/Contents/Home/bin/java -jar vpin-studio-ui.jar

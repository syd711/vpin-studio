#!/bin/sh
if [[ ! -d zulu11.72.19-ca-fx-jre11.0.23-linux_x64 ]];
then
	tar -xvf zulu11.72.19-ca-fx-jre11.0.23-linux_x64.tar.gz
fi

./zulu11.72.19-ca-fx-jre11.0.23-linux_x64/bin/java -jar vpin-studio-ui.jar

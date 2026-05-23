#!/bin/sh
sleep 4
echo "Unzipping jar..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
unzip -o '{{MAC_WRITE_PATH}}/vpin-studio-ui-jar.zip' -d '{{MAC_WRITE_PATH}}_updatefolder' >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
echo "Removing Zip..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
rm vpin-studio-ui-jar.zip >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
echo "Closing App..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
killall VPin-Studio >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
echo "Moving Jar..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
cp -vf '{{MAC_WRITE_PATH}}_updatefolder/vpin-studio-ui.jar' '{{MAC_JAR_PATH}}' >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
echo "Removing _updatefolder..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
rm -rf '{{MAC_WRITE_PATH}}_updatefolder' >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
echo "Restarting client..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
open -n {{MAC_APP_PATH}} >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1

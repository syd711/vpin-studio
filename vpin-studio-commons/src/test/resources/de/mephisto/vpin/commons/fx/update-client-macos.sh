#!/bin/sh
sleep 4

ARCH=$(uname -m)
if [ "$ARCH" = "arm64" ]; then
    ZULU_BUNDLE="zulu25.34.17-ca-fx-jre25.0.3-macosx_aarch64.tar.gz"
else
    ZULU_BUNDLE="zulu25.34.17-ca-fx-jre25.0.3-macosx_x64.tar.gz"
fi
ZULU_URL="https://cdn.azul.com/zulu/bin/${ZULU_BUNDLE}"
JAVA_RELEASE="java-runtime/Contents/Home/release"

if ! [ -f "${JAVA_RELEASE}" ] || ! grep -q "Zulu25.34" "${JAVA_RELEASE}"; then
    echo "Downloading Java runtime..." >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
    rm -rf java-runtime
    curl -L -o "${ZULU_BUNDLE}" "${ZULU_URL}" >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
    mkdir -p java-runtime
    tar -xzf "${ZULU_BUNDLE}" --strip-components=1 -C java-runtime >> '{{MAC_WRITE_PATH}}Logs/vpin-studio-ui.log' 2>&1
    rm "${ZULU_BUNDLE}"
fi

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

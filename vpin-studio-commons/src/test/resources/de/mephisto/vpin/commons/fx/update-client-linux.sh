#!/bin/bash
sleep 4
cd "$(dirname "$0")"

ZULU_BUNDLE="zulu25.34.17-ca-fx-jre25.0.3-linux_x64.tar.gz"
ZULU_URL="https://cdn.azul.com/zulu/bin/${ZULU_BUNDLE}"

if ! [ -f "java-runtime/release" ] || ! grep -q "Zulu25.34" "java-runtime/release"; then
    echo "Downloading Java runtime..."
    rm -rf java-runtime
    curl -L -o "${ZULU_BUNDLE}" "${ZULU_URL}"
    mkdir -p java-runtime
    tar -xzf "${ZULU_BUNDLE}" --strip-components=1 -C java-runtime
    rm "${ZULU_BUNDLE}"
fi

unzip -o vpin-studio-ui-jar.zip
rm vpin-studio-ui-jar.zip
./VPin-Studio.sh &

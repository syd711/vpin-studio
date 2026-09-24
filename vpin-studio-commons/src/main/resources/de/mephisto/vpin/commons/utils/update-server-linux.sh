#!/bin/bash
sleep 8
cd "$(dirname "$0")"

retries=0
while pgrep -f "vpin-studio-server.jar" > /dev/null 2>&1; do
  retries=$((retries + 1))
  if [ "$retries" -ge 15 ]; then
    echo "$(date) ERROR: Failed to stop the running server after 15 retries" >> vpin-studio-server.log
    exit 1
  fi
  sleep 2
done

sleep 5

retries=0
until unzip -o VPin-Studio-Server.zip; do
  retries=$((retries + 1))
  if [ "$retries" -ge 3 ]; then
    echo "$(date) ERROR: Extraction failed after $retries attempts - the downloaded archive is corrupt, not locked, so retrying extraction cannot fix it." >> vpin-studio-server.log
    rm -f VPin-Studio-Server.zip
    echo
    echo "Update failed: the downloaded update archive is corrupt."
    echo "The corrupt file has been deleted - please retry the update from VPin Studio so it can be re-downloaded."
    exit 1
  fi
  sleep 3
done

sleep 4
rm -f VPin-Studio-Server.zip
nohup java -jar vpin-studio-server.jar >> vpin-studio-server.log 2>&1 &
disown
exit 0

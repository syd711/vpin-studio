@echo off
timeout /T 4 /nobreak
cd /d %~dp0

:update
resources\7z.exe -aoa x "VPin-Studio.zip"
timeout /T 4 /nobreak
del VPin-Studio.zip
VPin-Studio.exe
exit

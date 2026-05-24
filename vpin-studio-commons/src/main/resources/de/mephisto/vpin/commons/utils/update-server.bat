@echo off
timeout /T 8 /nobreak
cd /d %~dp0

:update
del VPin-Studio-Server.exe
resources\7z.exe -aoa x "VPin-Studio-Server.zip"
timeout /T 4 /nobreak
del VPin-Studio-Server.zip
wscript server.vbs
exit

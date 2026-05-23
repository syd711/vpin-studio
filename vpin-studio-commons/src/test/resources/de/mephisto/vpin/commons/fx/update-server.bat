@echo off
timeout /T 8 /nobreak
cd /d %~dp0

set ZULU_BUNDLE=zulu25.34.17-ca-fx-jre25.0.3-win_x64.zip
set ZULU_URL=https://cdn.azul.com/zulu/bin/%ZULU_BUNDLE%

if not exist "java-runtime\release" goto :download_jre
findstr /C:"Zulu25.34" "java-runtime\release" >nul 2>&1
if errorlevel 1 goto :download_jre
goto :update

:download_jre
echo Downloading Java runtime...
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path 'java-runtime') { Remove-Item 'java-runtime' -Recurse -Force }; Invoke-WebRequest -Uri '%ZULU_URL%' -OutFile '%ZULU_BUNDLE%'; Expand-Archive '%ZULU_BUNDLE%' '_jre_temp' -Force; $d = (Get-ChildItem '_jre_temp' | Select-Object -First 1).FullName; Move-Item $d 'java-runtime'; Remove-Item '_jre_temp' -Recurse -Force -ErrorAction SilentlyContinue; Remove-Item '%ZULU_BUNDLE%' -Force"

:update
del VPin-Studio-Server.exe
resources\7z.exe -aoa x "VPin-Studio-Server.zip"
timeout /T 4 /nobreak
del VPin-Studio-Server.zip
wscript server.vbs
exit

@echo off
timeout /T 4 /nobreak
cd /d %~dp0

set ZULU_BUNDLE=zulu25.34.17-ca-fx-jre25.0.3-win_x64.zip
set ZULU_URL=https://cdn.azul.com/zulu/bin/%ZULU_BUNDLE%

if not exist "win32\java\release" goto :download_jre
findstr /C:"Zulu25.34" "win32\java\release" >nul 2>&1
if errorlevel 1 goto :download_jre
goto :update

:download_jre
echo Downloading Java runtime...
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path 'win32\java') { Remove-Item 'win32\java' -Recurse -Force }; Invoke-WebRequest -Uri '%ZULU_URL%' -OutFile '%ZULU_BUNDLE%'; Expand-Archive '%ZULU_BUNDLE%' '_jre_temp' -Force; $d = (Get-ChildItem '_jre_temp' | Select-Object -First 1).FullName; New-Item -ItemType Directory -Force 'win32' | Out-Null; Move-Item $d 'win32\java'; Remove-Item '_jre_temp' -Recurse -Force -ErrorAction SilentlyContinue; Remove-Item '%ZULU_BUNDLE%' -Force"

:update
resources\7z.exe -aoa x "VPin-Studio.zip"
timeout /T 4 /nobreak
del VPin-Studio.zip
VPin-Studio.exe
exit

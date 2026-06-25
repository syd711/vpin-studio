@echo off
timeout /T 8 /nobreak
cd /d %~dp0

set retries=0

:waitforfile
del VPin-Studio-Server.exe >nul 2>&1
if exist VPin-Studio-Server.exe (
    set /a retries+=1
    if %retries% geq 15 (
        echo %date% %time% ERROR: Failed to delete VPin-Studio-Server.exe after 15 retries >> vpin-studio-server.log
        pause
        exit /b 1
    )
    timeout /T 2 /nobreak
    goto waitforfile
)

timeout /T 5 /nobreak

set retries=0

:update
resources\7z.exe -aoa x "VPin-Studio-Server.zip"
if errorlevel 1 (
    set /a retries+=1
    if %retries% geq 20 (
        echo %date% %time% ERROR: 7-Zip extraction failed after 20 retries >> vpin-studio-server.log
        pause
        exit /b 1
    )
    timeout /T 3 /nobreak
    goto update
)
timeout /T 4 /nobreak
del VPin-Studio-Server.zip
wscript server.vbs
exit
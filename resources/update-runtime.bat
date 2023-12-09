@echo off
setlocal enabledelayedexpansion

REM Set the URL of the zip file you want to download
set "zipUrl=https://example.com/path/to/your/file.zip"

REM Set the path where you want to download and extract the zip file
set "downloadPath=C:\Path\To\Download"
set "extractPath=C:\Path\To\Extract"

REM Create the directories if they don't exist
if not exist "!downloadPath!" mkdir "!downloadPath!"
if not exist "!extractPath!" mkdir "!extractPath!"

REM Download the zip file using PowerShell
powershell -Command "& { Invoke-WebRequest -Uri '%zipUrl%' -OutFile '!downloadPath!\file.zip' }"

REM Check if the download was successful
if %errorlevel% neq 0 (
    echo Download failed.
    exit /b %errorlevel%
)

REM Extract the contents of the zip file
powershell -Command "& { Expand-Archive -Path '!downloadPath!\file.zip' -DestinationPath '!extractPath!' }"

echo Extraction complete.

endlocal

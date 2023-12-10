@echo off
setlocal enabledelayedexpansion

if exist "java-runtime.zip" (
	del "java-runtime.zip"
	echo Deleted existing runtime archive.
)

REM Set the URL of the zip file you want to download
set "zipUrl=https://github.com/syd711/vpin-studio/blob/main/java-runtime.zip?raw=true"

REM Set the path where you want to download and extract the zip file
set "downloadPath=.\"
set "extractPath=.\..\win32\java\"

REM Create the directories if they don't exist
if not exist "!downloadPath!" mkdir "!downloadPath!"
if not exist "!extractPath!" mkdir "!extractPath!"

REM Download the zip file using PowerShell
powershell -Command "& { Invoke-WebRequest -Uri '%zipUrl%' -OutFile '!downloadPath!\java-runtime.zip' }"

REM Check if the download was successful
if %errorlevel% neq 0 (
    echo Download failed.
    exit /b %errorlevel%
)

echo Download completed.

if exist "%extractPath%" (
    pushd "%extractPath%"
    del /q *.*
    popd
    echo All files in %extractPath% have been deleted.
) else (
    echo Folder not found: %extractPath%
)

REM Extract the contents of the zip file
powershell -Command "& { Expand-Archive -force -Path '!downloadPath!java-runtime.zip' -DestinationPath '!extractPath!' }"

echo Runtime extraction completed, runtime update finished.

endlocal

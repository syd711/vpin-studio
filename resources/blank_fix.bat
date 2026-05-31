@echo off
setlocal enabledelayedexpansion

set "SEARCH_DIR=%~dp0POPMedia"
set "LOG_FILE=%~dp0blank_fix.log"
set "TARGET_SIZE=665"
set "BLANK_URL=https://github.com/syd711/vpin-studio/raw/refs/heads/main/resources/blank.mp4"
set "BLANK_TMP=%TEMP%\blank_fix_replacement.mp4"

echo Downloading replacement blank.mp4...
powershell -Command "Invoke-WebRequest -Uri $env:BLANK_URL -OutFile $env:BLANK_TMP"
if not exist "%BLANK_TMP%" (
    echo ERROR: Failed to download blank.mp4
    exit /b 1
)

echo Scanning %SEARCH_DIR% for blank .mp4 files (%TARGET_SIZE% bytes^)...
echo Scan started: %DATE% %TIME% > "%LOG_FILE%"
echo Target size: %TARGET_SIZE% bytes >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

set "COUNT=0"
for /r "%SEARCH_DIR%" %%F in (*.mp4) do (
    if %%~zF==%TARGET_SIZE% (
        echo %%F
        echo %%F >> "%LOG_FILE%"
        set /a COUNT+=1
        rename "%%F" "%%~nxF.deleteme"
        copy /y "%BLANK_TMP%" "%%F" >nul
        if !errorlevel!==0 (
            del "%%F.deleteme"
        ) else (
            echo ERROR replacing %%F >> "%LOG_FILE%"
        )
    )
)

del "%BLANK_TMP%"

echo. >> "%LOG_FILE%"
echo Total found: !COUNT! >> "%LOG_FILE%"
echo.
echo Done. !COUNT! file(s) found. Log written to %LOG_FILE%
pause

@echo off
setlocal enabledelayedexpansion

set "SEARCH_DIR=%~dp0POPMedia"
set "LOG_FILE=%~dp0blank_fix.log"
set "TARGET_SIZE=665"
set "BLANK=w"

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
        set "CURRENT_FILE=%%F"
        powershell -Command "[System.IO.File]::WriteAllBytes($env:CURRENT_FILE, [System.Convert]::FromBase64String($env:BLANK))"
        if !errorlevel!==0 (
            del "%%F.deleteme"
        ) else (
            echo ERROR replacing %%F >> "%LOG_FILE%"
        )
    )
)

echo. >> "%LOG_FILE%"
echo Total found: !COUNT! >> "%LOG_FILE%"
echo.
echo Done. !COUNT! file(s) found. Log written to %LOG_FILE%

@echo off
where wt.exe >nul 2>&1
if %errorlevel% == 0 (
    start "" wt.exe -d "C:\workspace\vpin-studio" -- powershell -NoExit -Command "claude"
) else (
    start "" powershell -NoExit -Command "Set-Location 'C:\workspace\vpin-studio'; claude"
)

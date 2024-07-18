@echo off
echo %0 %* > "%~dp0B2SWindowPunch.log"
setlocal EnableDelayedExpansion

set "resfile=%cd%\%~1.res"
set "GameName=%~2"

REM If started from explorer, quit
if ["%GameName%"] == [""] echo %0 Exit when started from explorer >> "%~dp0B2SWindowPunch.log"&goto :EOF

REM Cut holes in the destination "B2S Backglass Server" & "B2S DMD" forms
set "B2SWindowPunch=^B2S Backglass$|^B2S Backglass Server$|^B2S DMD$"

REM using "Virtual DMD", "Virtual Alphanumeric" and all "PUPSCREEN" forms as regular expressions
set "cutter=^Virtual DMD$|^Virtual Alphanumeric Display$|^PUPSCREEN[0-9]+$|^VPinMAME:|^PROC:"

REM Check if there is any table specific settings for B2SWindowPunch
if exist "%resfile%" for /f "usebackq eol=# tokens=1,2 delims==" %%G in (`findstr /R "^[A-Za-z0-9][A-Za-z0-9]*=" "%resfile%"`) do set "%%G=%%H"

echo B2SWindowPunch = "!B2SWindowPunch!" >> "%~dp0B2SWindowPunch.log"
echo cutter = "!cutter!" >> "%~dp0B2SWindowPunch.log"

if ["!B2SWindowPunch!"] == ["off"] echo %0 B2SWindowPunch turned off>> "%~dp0B2SWindowPunch.log"&goto :EOF
REM Quit If no destination set
if ["!B2SWindowPunch!"] == [""] echo %0 No destination set>> "%~dp0B2SWindowPunch.log"&goto :EOF

REM Wait 10 seconds
timeout /t 10 /nobreak > NUL

REM Punch the holes
echo "%~dp0B2SWindowPunch.exe" "!B2SWindowPunch!" "!cutter!"  >> "%~dp0B2SWindowPunch.log"
"%~dp0B2SWindowPunch.exe" "!B2SWindowPunch!" "!cutter!"  >> "%~dp0B2SWindowPunch.log"

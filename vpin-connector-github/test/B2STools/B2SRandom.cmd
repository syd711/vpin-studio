@echo off
REM  ___ ___ ___ 
REM | _ )_  ) __|
REM | _ \/ /\__ \
REM |___/___|___/ Random
REM 
REM This script can be used to randomize different backglasses before starting Visual Pinball
REM 
REM To activate it, add the following line in the Popper Launch script
REM CALL C:\vPinball\B2SServer\B2STools\B2SRandom.cmd "[GAMEFULLNAME]"
REM
REM If you start a table "table.vpx" in the folder "D:\vPinball\VisualPinball\Tables\" it will try to find
REM D:\vPinball\VisualPinball\Tables\table-#.directb2s where # is a random number.
REM If it is found, the file D:\vPinball\VisualPinball\Tables\table-#.directb2s is copied over D:\vPinball\VisualPinball\Tables\table.directb2s

REM Extract path + name without extension
set b2sfullname=%~dpn1

REM Uncomment following line to log how this script was called.
REM echo %0 %* >> %0.log

REM Count the amount of b2sfullname-?.directb2s alt directb2s files there is. 
for /F %%i in ('dir /A:-D /B "%b2sfullname%-?.directb2s" 2^>nul ^| find /c /v ""') do set COUNT=%%i

REM Step out if there is no alt directb2s files available
if %COUNT% == 0 goto :EOF

REM Generate random number between 1 and the %COUNT%
set /a num=%random% %%%COUNT% + 1 

if not exist "%b2sfullname%-%num%.directb2s" goto :EOF

copy /Y "%b2sfullname%-%num%.directb2s" "%b2sfullname%.directb2s"

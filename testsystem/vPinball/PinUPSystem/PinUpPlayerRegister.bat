@echo off
NET SESSION
IF %ERRORLEVEL% NEQ 0 GOTO ELEVATE
GOTO ADMINTASKS

:ELEVATE
CD /d %~dp0
MSHTA "javascript: var shell = new ActiveXObject('shell.application'); shell.ShellExecute('%~nx0', '', '', 'runas', 1);close();"
EXIT

:ADMINTASKS
CD /d %~dp0
"%~dp0PinUpPlayer.exe" /regserver
"%~dp0PinUpDisplay.exe" /config
EXIT

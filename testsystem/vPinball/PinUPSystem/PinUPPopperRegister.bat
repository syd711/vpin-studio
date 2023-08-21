@echo off
NET SESSION
IF %ERRORLEVEL% NEQ 0 GOTO ELEVATE
GOTO ADMINTASKS

:ELEVATE
CD /d %~dp0
MSHTA "javascript: var shell = new ActiveXObject('shell.application'); shell.ShellExecute('%~nx0', '', '', 'runas', 1);close();"
EXIT

:ADMINTASKS
REG ADD "HKCU\Control Panel\Desktop" /v "ForegroundLockTimeout" /t REG_DWORD /d 0 /f
CD /d %~dp0
"%~dp0PinUpDOF.exe" /regserver
"%~dp0PuPServer.exe" /regserver
"%~dp0PinUpPlayer.exe" /regserver
"%~dp0PinUpMenuSetup.exe" -setfolders
rem "%~dp0PinUpDisplay.exe" /config
EXIT

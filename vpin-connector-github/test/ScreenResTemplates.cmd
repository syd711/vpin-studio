@echo off
set "restemplate=%~1"
set "resfile=%~dpn2.res"

copy "%~dp0%restemplate%" "%resfile%"
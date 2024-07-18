@echo off
REM directb2sReelSoundsONOFF.cmd call xslt transformation to add or remove empty attributes for directb2s files
setlocal

set "xsltFilePath=%~dpn0.xsl"

:loop
call :transform "%~1"

shift
if not "%~1"=="" goto loop

pause
goto :EOF
:transform
set "xmlFilePath=%~1"
set "outputFilePath=%~dpn1-updated.directb2s"

echo Transforming "%xmlFilePath%" 
echo           to "%outputFilePath%"
if not "%~x1" == ".directb2s" echo This only support directb2s files not "%1"&goto :EOF

powershell -Command ^
$xmlFilePath = '%xmlFilePath%'; ^
$xsltFilePath = '%xsltFilePath%'; ^
$outputFilePath = '%outputFilePath%'; ^
$xslt = New-Object System.Xml.Xsl.XslCompiledTransform; ^
$xslt.Load($xsltFilePath); ^
$xslt.Transform($xmlFilePath, $outputFilePath);

if %ERRORLEVEL% GEQ 1 echo Transformation error!&goto :EOF
echo Transformation complete.


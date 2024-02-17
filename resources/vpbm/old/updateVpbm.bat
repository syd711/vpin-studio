timeout /T 5 /nobreak
echo D | xcopy * old /y /q 
echo D | xcopy Help old\Help /y /q /s 
echo D | xcopy Resources old\Resources /y /q /s 
echo D | xcopy runtimes old\runtimes /y /q /s 
rmdir /s /q old\Logs
echo F | xcopy vPinBackupManager.json vPinBackupManager.json.old /y /q 
echo F | xcopy Resources\7z.exe . /y /q 
start 7z.exe -aoa x "C:\Users\matth\AppData\Local\Temp\vpinBackupManager\vPinBackupManager-update-2.0.zip"
timeout /T 1 /nobreak
del 7z.exe /q
(goto) 2>nul & del "%~f0"

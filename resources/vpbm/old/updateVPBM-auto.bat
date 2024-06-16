timeout /T 5 /nobreak

rem copy existing application content to .\old
echo VPBM Updater: Creating backup of existing application in .\old
rmdir /s /q old
mkdir old
mkdir old\Help
mkdir old\Resources
mkdir old\runtimes
echo VPBM Updater: Backing up base files
xcopy /y /q * old
echo VPBM Updater: Backing up Help files
xcopy /y /q /s Help old\Help
echo VPBM Updater: Backing up Resources
xcopy /y /q /s Resources old\Resources
echo VPBM Updater: Backing up runtimes
xcopy /y /q /s runtimes old\runtimes

rem delete old files
for %%f in (*.*) do (
    if not "%%f" == "updateVPBM-auto.bat" (
        del /f /q "%%f"
    )
)

rem restore key files used in install
echo VPBM Updater: Backing up vPinBackupManager.json to vPinBackupManager-old.json
xcopy /y /q old\vPinBackupManager.json vPinBackupManager-old.json*
xcopy /y /q Resources\7z.exe .
rmdir /s /q Help Resources runtimes

rem perform install
echo VPBM Updater: Installing new version
start 7z.exe -aoa x "C:\Users\syd71\AppData\Local\Temp\vpinBackupManager\vPinBackupManager-update-3.2.zip"
timeout /t 5 /nobreak
del /q 7z.exe
(goto) 2>nul & del "%~f0"

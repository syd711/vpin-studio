@echo off

set updateZip="vPinBackupManager-update.zip"

echo VPBM Updater: Launched

rem check zip to install exists and 7Zip exe is present in the correct location
if not exist %updateZip% (
    echo VPBM Updater: ERROR: The file '.\%updateZip%' was not found, cannot perform update
    timeout /t 5 /nobreak
    exit
)
if not exist Resources\7z.exe (
    echo VPBM Updater: ERROR: The file '.\Resources\7z.exe' was not found, cannot perform update
    timeout /t 5 /nobreak
    exit
)


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
    if not "%%f" == "updateVPBM.bat" (
		if not "%%f" == "vPinBackupManager-update.zip" (
            del /f /q "%%f"
		)
    )
)

rem restore key files used in install
echo VPBM Updater: Backing up vPinBackupManager.json to vPinBackupManager-old.json
xcopy /y /q old\vPinBackupManager.json vPinBackupManager-old.json*
rmdir /s /q Help Resources runtimes

rem perform install
echo VPBM Updater: Installing new version
start old\Resources\7z.exe -aoa x ".\vPinBackupManager-update.zip"
timeout /t 5 /nobreak

rem restart VPBM
timeout /T 1 /nobreak
start vPinBackupManager.exe

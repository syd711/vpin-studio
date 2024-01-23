mvn versions:set -DnewVersion="%1" -DgenerateBackupPoms=false -DprocessAllModules
cd vpin-studio-server
mvn versions:set -DnewVersion="%1" -DgenerateBackupPoms=false -DprocessAllModules
cd ..
cd vpin-studio-ui
mvn versions:set -DnewVersion="%1" -DgenerateBackupPoms=false -DprocessAllModules
cd ..
cd vps-bot
mvn versions:set -DnewVersion="%1" -DgenerateBackupPoms=false -DprocessAllModules
cd ..
mvn license:aggregate-download-licenses
generateLicenses.bat

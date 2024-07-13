## Release Notes 2.21.6
 
### Bugfixes

- **Highscore Scanning**: Improved highscore detection for VPReg.stg based scores (e.g. "Aloha", "Pabst Can Crusher").
- **Highscore Scanning**: Improved highscore detection "Mountain Climbing" (text file based).
- **Table Overview / Table Deletion**: Fixed issue that the emulator folder is deleted together with the last game of it. 

Some technical stuff:
- Improved highscore support by checking the file scoringdb.json (located in the resources folder) which contains some manual lookup/correction for ROM names and highscore files. The file is updated on server start an allows to fix some highscore checks without updating the whole Studio all the time.

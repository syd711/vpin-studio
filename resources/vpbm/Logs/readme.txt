This directory holds application logs.

Logs launched from the GUI version of VPBM will be of the form: vPinBackupManager_<PID>_YYYYMMDD.log
Logs launched from the CLI version of VPBM will be of the form: vPinBackupManagerCLI_<PID>_YYYYMMDD.log

Individual logs are rotated daily and when they reach 20Mb in size.
Logs that are rotated out are of the form where XXX is a counter starting at 001:
  vPinBackupManager_<PID>_YYYYMMDD_XXX.log
  vPinBackupManagerCLI_<PID>_YYYYMMDD_XXX.log
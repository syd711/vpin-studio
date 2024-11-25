## Required Software

- JDK + FX 11 Zulu (https://www.azul.com/core-post-download/?endpoint=zulu&uuid=53d3549c-3cb6-44bc-bc7d-3618746dada5)
- Inno Installer 6 (the installer must be added to the windows PATH).


## Required Repositories

- https://github.com/syd711/vpin-studio
- https://github.com/syd711/vpin-mania-rest-client (private)
- https://github.com/syd711/vpin-studio-popper-client (private)


## Installation Steps

- Clone all 3x repositories
- Inside the **vpin-studio** project, run the maven build for the module **vpin-connector-assets**.
- Maven build the **vpin-studio-popper-client** project.
- Maven build the **vpin-mania-rest-client** project **with skip tests!!!**.

The **vpin-studio** project should build now, even without a maven build.

Edit the file **resources/system.properties** and paste the following entries there.
Adapt the pinupSystem path.

```
The file#Sat Apr 08 12:27:44 CEST 2023
pinupSystem.installationDir=C\:\\vPinball\\PinUPSystem
#visualPinball.installationDir=C\:\\vPinball\\VisualPinball
#pinballX.installationDir=C\:\\PinballX
``` 

## Execution

Assuming you use IDEA:

- Execute the Studio Server DEV runtime config.
- Start the Studio via the provided runtime config.

## How to update pinemhi

- Check the method PINEmHiService#checkForUpdates method
- Download and extract the new pinemhi into the resources folder.
- Update the version in the file **scoringdb.json**.
- Upload everything on main.

## How to release

- Use the _setVersion_ script to apply the new version, e.g. "setVersion.bat 1.3.5".
- Push everything
- Go to github actions and trigger the workflow job, this will build a release draft to review.
- Manually publish the drafted release.
- Clear the RELEASE_NOTES.md afterwards to start with the next version.


## How to update nvrams

- Copy new resetted nvram files to **resources/nvrams**.
- Open the NVRamSynchronizer from the tools module and adapter author and target folder for the nvrams project. You need to clone this separately. The nvram project is supposed to be cloned next to the vpin-studio folder.
- Run the synchronizer.
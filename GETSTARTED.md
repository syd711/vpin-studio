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
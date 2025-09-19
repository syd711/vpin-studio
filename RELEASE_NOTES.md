## Release Notes 4.3.0

## Changes

- **Table Backups**
  - The "Default Backups Folder" is not mandatory anymore and can be re-configured with another name and folder.
  - Added backup indicator for the tables overview so that you can immediately see which tables are backed up already. 
    
    <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/cards/asdfsaf.png?raw=true" width="700" />

- **Folder Selections**
  - Added new dialog to select folders from the remote system. This allows the configurations at a lot of places now, where it only had been possible when the Studio was used on the cabinet itself.
  
     <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/cards/asdfsaf.png?raw=true" width="700" />

- **Tables Overview**
  - Support of next/previous navigation in DmdPosition tool when opened from the tables overview.
  - Support of next/previous navigation in the Table Data dialog.
  - Added "Save" shortcut (Ctrl+S) in Table Data dialog.

- **BackglassManager**
  - Added a frame generator directly in studio, on an idea and python script provided by Himura95. Four different generators are provided: 
    - Ambilight: Uses the pixels on the edge of the backglass to generate the frame.
    - Blurred: Creates a blurred zoom of the backglass.
    - Mirror: Creates a blurred mirror reflection with perspective.
    - Gradient: Calculates the dominant color of the image and use it to draw a gradient to black.

- **Assets and Medias** 
  - In the Table Media tab of the tables sidebar, videos are now previewed as one frame vs. the full video that is consuming lots of CPU. On mouse over, a play button is shown to start the video playback. A stop icon button stops the video.
  - In Table Asset Manager, added "Set A Default" button to choose default asset in the list of assets.
  - Improved streaming of table assets and frontend media.

- **PinVol Preferences** 
  - Added possibility to change the installation folder of PinVol and switch between PinVol provided as part of Studio to an installed version
  - Improved monitoring of PinVol process


## Bugfixes

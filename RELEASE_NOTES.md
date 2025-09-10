## Release Notes 4.3.0

## Changes

- **Table Backups**
  - The "Default Backups Folder" is not mandatory anymore and can be re-configured with another name and folder.
- **Folder Selections**
  - Added new dialog to select folders from the remote system. This allows the configurations at a lot of places now, where it only had been possible when the Studio was used on the cabinet itself.
  
     <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/cards/cards.png?raw=true" width="700" />

- **BackglassManager**: Added a frame generator directly in studio, on an idea and python script provided by Himura95. 4 different generators are provided: 
  - Ambilight, Uses the pixels on the edge of the backglass to generate the frame.
  - Blurred, Creates a blurred zoom of the backglass.
  - Mirror, Creates a blurred mirror reflection with perspective.
  - Gradient, Calculate the dominant color of the image and use it to draw a gradient to black

- **Assets and Medias** : 
  - In the Table Media tab of the tables sidebar, videos are now previewed as one frame, vs the full video that is consuming lots of CPU. A play icon to start the video appears when mouse is over the media. A stop icon stops the vidéo.
  - In Asset Manager, Added 'Set A Default' button to choose default asset in the list of assets
  - Improved streaming of Table Assets and Frontend Medias

## Bugfixes

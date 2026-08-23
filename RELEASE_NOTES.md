## Release Notes 5.3.0

### Changes

  <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/release-notes/move-clone.png?raw=true" width="400" />


### Bugfixes

- **Notifications**
  - Fixed notification tests.
- **Media Recorder**
  - Added NVIDIA detection so that the ffmpeg command can use the GPU for the video encoding. This was a huge gap between the Studio and Popper and should improve the recording performance.
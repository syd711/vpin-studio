## Release Notes 4.10.0

- **Future Pinball**
  - With long overdue, VPin Studio 4.10.0 supports Future Pinball highscores now too. The highscore support reflects in the Designer section too, which allows to design highscore cards for Future Pinball now. Note that because of the limited test data a lot of highscore lists might still look broken. Please submit your fpRAM file in that case on our Discord so that we can improve the parsing.
- **Statistics**
    - Analytics not updated for PinballY in pauseMenu
- **DMD SCreen Capture**
    - Fixed the DMD capture when dmddevice is set to double or scale2X scaler mode. Used to transfer DMD score in WoVP

### Bugfixes

- **Highscores**: Fixed VPReg.stg file lookup.
- **VPU/VPF**: Fixed login tests.
- **discord** player name inconsistent


### Breaking Changes

- Player comp. name (iscored) for discord too
- iScored notification settings
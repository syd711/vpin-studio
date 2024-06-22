## Release Notes 2.21.1

- **Table Overview / Popper Media Preview**: Added border to the screen that is selected for the highscore card generation. An additional tooltip on the screen indicats why this border is shown.
- **Highscore Card Editor**: Added reload button in case table assets have been updated. 

### Bugfixes

- **Highscore Card Editor**: Fixed overlay mode only showing videos but no images.
- **Competitions / TournamentId**: The id that is generated for the Popper field "Tournament Id" contains now the information segment **remote/local** to indicate if the the user is the competition owner or not. E.g. when you subscribed to another ones table subscription, the value will be **remote**. You can use the keywords to build custom SQL playlists with these keywords.
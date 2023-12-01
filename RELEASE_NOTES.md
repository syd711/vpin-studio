# Hotfix 2.3.1

### Bugfixes

- Added missing highscore card section again. Sorry for that! I accidently disabled the feature with another one I didn't want to release yet.
- Fixed broken highscore card editor: Some users had the problem that the editor wasn't working. I finally found the cause for it and fixed it.
- Added .ini file which allows to configure some runtime settings, like the locale or file encoding the server is executed with. Right now this file is only created for fresh installations because I'm not entirely sure if it's really required. But might help in the future.
- Encoding problems: If your highscore parsing fails and the characters look broken, take a look on https://github.com/syd711/vpin-studio/wiki/FAQ. 
# Pause Menu Theme

This folder lets you re-skin the persistent chrome of the in-game pause menu
(the overlay shown when you pause a running table) without rebuilding VPin
Studio.

## What you can customize

| File        | Used for                                                              | Default size |
|-------------|------------------------------------------------------------------------|--------------|
| `row.png`   | The diagonal background bar behind the scrolling menu item row         | 4400 x 600   |
| `screen.png`| The screen/photo frame decoration at the top of the menu                | 1106 x 700   |
| `footer.png`| The footer bar behind the table name and navigation arrows              | 1200 x 150   |
| `blue.png`  | The round selector graphic (used both for the highlighted item and the "archive table" tile) | 688 x 688 |
| `wheel.png` | The static decorative wheel graphic shown behind the menu                | 1000 x 1000  |

The `default/` subfolder contains copies of the original images shipped with
the app, so you always have something to restore from — it is **not** read
by the application itself.

Note: the actual per-table artwork you see in the pause menu (the real wheel
image, table screenshots, highscore cards, tutorial videos, etc.) is pulled
from your table/frontend media and is not themeable via this folder — only
the five static chrome images above are.

## How to apply a custom theme

1. Create your replacement image(s), keeping the same pixel dimensions and
   aspect ratio as the defaults above (PNG, transparency supported).
2. Copy the file(s) directly into **this folder**
   (`resources/themes/pause-menu/`) — not into `default/`:
   - `resources/themes/pause-menu/row.png`
   - `resources/themes/pause-menu/screen.png`
   - `resources/themes/pause-menu/footer.png`
   - `resources/themes/pause-menu/blue.png`
   - `resources/themes/pause-menu/wheel.png`
3. **Restart the VPin Studio client / UI process.** Unlike the notification
   theme, the pause menu is built once when the client starts up, so theme
   files are only read at startup — dropping files in while the app is
   already running won't affect an already-open pause menu.

You don't have to replace all five files — any file you don't provide falls
back to the built-in default.

## Restoring the default theme

Copy the file(s) you want to restore from `default/` back into this folder
(or just delete your custom files from this folder), then restart the
client.
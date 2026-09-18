# Notification Theme

This folder lets you re-skin the on-screen notification popup (the overlay
shown for events like a new highscore, table added, etc.) without rebuilding
VPin Studio. This theming mechanism is **implemented and working**.

## What you can customize

| File      | Used for                                                        | Default size |
|-----------|------------------------------------------------------------------|--------------|
| `row.png` | The background bar/ribbon behind the notification text           | 1200 x 600   |
| `logo.png`| The icon shown next to the text (left of the message)            | 800 x 800    |

The `default/` subfolder contains copies of the original images shipped with
the app, so you always have something to restore from — it is **not** read
by the application itself.

Note: the picture that sometimes appears at the far right of a notification
(e.g. a table's wheel image) is supplied per-event and is not themeable via
this folder.

## How to apply a custom theme

1. Create your replacement image(s), keeping the same pixel dimensions and
   aspect ratio as the defaults above (PNG, transparency supported).
2. Copy the file(s) directly into **this folder**
   (`resources/themes/notifications/`) — not into `default/`:
   - `resources/themes/notifications/row.png`
   - `resources/themes/notifications/logo.png`
3. That's it — no restart is required. Each notification popup loads these
   files fresh from disk when it is shown, so the very next notification
   will use your custom images.

You don't have to replace both files — if only one exists, only that one is
themed and the other falls back to the built-in default.

## Restoring the default theme

Copy `row.png` and/or `logo.png` from `default/` back into this folder (or
just delete your custom files from this folder).

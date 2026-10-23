# Discord Integration – Ideas & TODO

Collected ideas for extending the Discord integration: "Now Playing" announcements, personal Rich Presence,
a community bot hosted on VPin Mania, and Discord-driven Mania challenges.

---

## 1. Current State (what already exists)

| Capability | Where |
|---|---|
| Table launch/exit detection (VPX, FP, via frontend or direct launch) | `TableStatusChangeListener#tableLaunched` / `#tableExited`, e.g. `DefaultTableAndFrontendStatusChangeListenerImpl` |
| Bot presence "Playing <table>" (bot status, **not** the user's status) | `DiscordClient#setActivity`, called from `DefaultTableAndFrontendStatusChangeListenerImpl#tableLaunched` |
| Posting messages incl. images to channels | `DiscordClient#sendMessage(..., byte[] image, ...)` |
| Discord webhook calls (content + embeds, no editing) | `vpin-connector-discord/.../DiscordWebhook` |
| Generic HTTP webhooks (game/score/player/pause/unpause; create/update/delete) | `WebhooksService`, `WebhooksRestClient`, `restclient/webhooks/*` |
| Mania authentication per installation (API key + cabinet UUID) | `ManiaService#refreshDefaultCabinet` |
| Mania "played count" on table launch (VPS table + version id) | `ManiaService#tableLaunched` |
| Cabinet status with `status` (online/offline) and `activeGame` field | `ManiaService#setOnline` / offline handling; `activeGame` is currently only ever set to `null` |
| Highscore sync to Mania per VPS table/version for local players with Mania accounts | `ManiaService#synchronizeHighscore` (requires `submitAllScores`) |
| Competition type `MANIA` already known in UI widgets / pause menu | `CompetitionType.MANIA`, `WidgetCompetitionSummaryController`, `PauseMenuItemsFactory` |

### Design decisions

- **No public bot token inside Studio.** A community bot has exactly one token; it must never be shipped with
  every installation (extractable, abusable, ToS). A public bot is a hosted *service*, not a Studio feature.
- **No second bot token per user.** Posting and editing a channel message can be done with a webhook or the
  existing Studio bot.
- **No selfbots.** Changing a user's status via their user token violates Discord ToS. Personal status only via
  local Rich Presence (IPC).
- **Logos:** use wheel icons from frontend media locally; for anything hosted, key images by **VPS id**, not by
  exact table name (names vary between setups).
- **Play duration:** use Discord timestamps (`<t:UNIX:R>`) for "started X minutes ago" – Discord renders it live,
  no repeated message edits needed. Edit once on exit with the final duration.

---

## 2. Option A – "Now Playing" posted directly by Studio (small, local)

On table launch Studio posts an embed into a configured channel; on exit the message is updated.

- [ ] Settings: enable "Now Playing", target = existing Studio bot + channel **or** a Discord webhook URL.
- [ ] Embed: table name, emulator, wheel icon (thumbnail), VPS link, start time as `<t:…:R>`.
- [ ] On `tableExited`: edit the message ("played 23 min") or delete it (configurable).
- [ ] Remember message id per active session so the right message gets edited.
- [ ] Extend `DiscordWebhook`:
  - [ ] support image attachments / embed images,
  - [ ] support `?wait=true` to get the created message id,
  - [ ] support editing own messages (`PATCH /webhooks/{id}/{token}/messages/{message_id}`) and deleting them.

---

## 3. Option B – Personal Discord Rich Presence (optional)

Show the currently played table in the **user's own** Discord profile.

- Works only via the local Discord desktop client IPC (named pipe `\\.\pipe\discord-ipc-0`): handshake (opcode 0),
  then `SET_ACTIVITY` frames (opcode 1) with JSON.
- Needs a Discord application **client id** only (not secret, safe to ship).
- Limitation: Discord desktop client must run and be logged in **on the cabinet** (where the Studio server and the
  table run). Many cabinets don't run Discord.
- Limitation: images must be public HTTPS URLs or uploaded application assets (limited count) – local wheel
  files can't be used directly. A hosted logo database keyed by VPS id would solve this.

TODO:
- [ ] Setting "Show current table in my Discord status".
- [ ] Implement IPC client (own small implementation or library like jagrosh DiscordIPC).
- [ ] On launch: details = table name, state = emulator, `timestamps.start`, large image = logo URL,
      optional button "View on VPS".
- [ ] On exit / frontend exit / server shutdown: clear activity.

---

## 4. Option C – Generic webhooks: "Table Status" events

Let any external service (community bot, Home Assistant, stream overlays, stats) receive table start/exit events.

> **Not required for the Mania-based community bot (option D).** Studio talks to Mania through the existing,
> authenticated Mania client (cabinet status update), so no webhook is involved. This section is only relevant
> for third-party integrations outside of Mania and can be done independently (or not at all).

### Current limitations of `WebhooksService`

- Payload only contains `id` + user-defined parameters → useless for external services, since they can't call
  back into the Studio REST API (Studio is not reachable from the internet).
- No table launch/exit events (only create/update/delete of games, players, scores; pause/unpause).
- `WebhooksRestClient` can't send custom HTTP headers (e.g. `Authorization: Bearer …`).
- Possible bug in `WebhooksService#handleWebhookSet` (delete case): `/<id>` is appended only if the URL does
  **not** end with `/` – condition looks inverted. Also appending `/<id>` breaks URLs with a query string.

### TODO

- [ ] New `WebhookType` "tableStatus" with events `table_started` / `table_exited`, fired from
      `tableLaunched` / `tableExited`.
- [ ] Send a full JSON payload (POST), e.g.:
  ```json
  {
    "event": "table_started",
    "installationId": "…",
    "player": "…",
    "table": "Attack from Mars",
    "emulator": "VPX",
    "vpsTableId": "…",
    "vpsVersionId": "…",
    "startedAt": "2026-10-22T20:15:00Z",
    "wheelUrl": "https://…"
  }
  ```
  `table_exited` additionally contains `durationSeconds`.
- [ ] Wheel image: either send inline (base64/multipart) or only the VPS id and let the receiver resolve logos.
- [ ] Support custom headers in `WebhooksRestClient` (auth key as `Authorization` header).
- [ ] UI for the new webhook type in the webhook settings.
- [ ] Fix/verify the delete URL handling mentioned above.

### Recommended hook URL format (for receivers)

```
https://<domain>/api/v1/vpin-studio/events
```

- HTTPS only; one endpoint for all events (event type in body); version in path.
- Auth key preferably as webhook parameter (JSON body) or header – avoid putting it into the URL (logs, proxies,
  and the delete-append issue above).

---

## 5. Option D (preferred) – Community bot hosted on VPin Mania (app.vpin-mania.net, PHP)

Mania already authenticates every Studio installation, so no separate key handling or registration is needed.
The community bot becomes the **Mania bot**, run on existing infrastructure.

### Flow

```
Studio (cabinet)              app.vpin-mania.net (PHP)                    Discord
────────────────              ────────────────────────                    ───────
tableLaunched
  └─ cabinet status:   ───►   store activeGame + start time
     activeGame = VPS id      look up linked Discord user + channels
                              POST /channels/{id}/messages ─────────────►  "Now Playing" embed
tableExited
  └─ activeGame = null ───►   PATCH /channels/{id}/messages/{msg} ──────►  "played 23 min"
```

### Bot can be pure PHP (no long-running process)

- Sending/editing messages: Discord REST API with `Authorization: Bot <token>` (curl/Guzzle). Token stays on the
  Mania server.
- Slash commands: configure an **Interactions Endpoint URL** (e.g. `https://app.vpin-mania.net/discord/interactions`);
  verify the Ed25519 signature (`sodium_crypto_sign_verify_detached`).
- Not possible without gateway: reacting to normal chat messages, setting bot presence – not needed here.
- Timed tasks (challenge start/end, reminders) via cron job on the web server.

### Studio TODO

- [ ] In `ManiaService#tableLaunched` set `cabinet.getStatus().activeGame` and call cabinet update;
      reset it in `tableExited` (implement `tableExited` handling in `ManiaService`).
- [ ] Check the type of `activeGame` in the Mania connector library; extend if needed.
      Mania expects in the cabinet status: `activeGame` (table name, `null` when nothing is played),
      `activeGameVpsTableId`, `activeGameVpsVersionId`. A change of these values starts/ends the Discord session.
- [ ] New Mania setting "Share now playing on Discord" (opt-in, like `submitPlayed`).
- [ ] Button "Connect Discord" in Mania preferences → opens `https://app.vpin-mania.net/settings`.

### Mania (PHP) TODO

- [x] Discord application with bot + OAuth2 redirect + interactions endpoint (`POST api/discord/interactions`).
      Env: `DISCORD_BOT_TOKEN`, `DISCORD_PUBLIC_KEY`; register commands with `php artisan discord:register-commands`.
- [x] Account linking (see section 6).
- [x] `/nowplaying setup` / `/nowplaying disable` – stored in `mania_discord_channels`.
- [x] On `activeGame` change: post/update the embed in all configured channels of guilds the user is a member of
      (`NowPlayingService`, membership cached in `mania_discord_members`).
- [x] Table `mania_discord_now_playing`.
- [x] Embed images: uploaded wheel (`/wheels/<vpsTableId>.png`) or VPS image.
- [x] Respect the cabinet's opt-in (`shareDiscordActivity` in the cabinet status, nothing is posted unless `true`).

---

## 6. Linking Mania accounts with Discord accounts

### Preferred: OAuth2 "Connect with Discord"

1. Discord Developer Portal: add redirect `https://app.vpin-mania.net/discord/callback`, keep client secret
   server-side only.
2. `/discord/connect` (user logged into Mania): generate random `state`, store in session, redirect to
   `https://discord.com/oauth2/authorize` with `client_id`, `redirect_uri`, `response_type=code`,
   `scope=identify`, `state`.
3. `/discord/callback`: verify `state` (CSRF!), exchange `code` at `POST /api/v10/oauth2/token`, fetch
   `GET /api/v10/users/@me`, store `discord_id` (+ username) on the Mania account.

Notes:
- [x] Scope `identify` is enough; the access token doesn't need to be stored – only the `discord_id` (+ username).
- [x] Settings page "Connect Discord" (`POST api/user/discord/connect`, reuses the login callback with a `state`).
- [x] `UNIQUE` constraint on `discord_id`.
- [x] "Disconnect Discord" → set `discord_id` to `NULL`.

### Alternative: link code via bot

- Mania profile → "Generate code" (e.g. `K7Q-4MZ`, valid 10 min, single use).
- User runs `/mania link K7Q-4MZ` in Discord; the interaction contains the signed `member.user.id`.
- PHP stores `discord_id`, deletes the code, replies ephemeral (flag `64`).
- Reverse direction also possible: bot creates code, user enters it in Studio (Studio is already authenticated
  via API key).

---

## 7. Discord-driven Mania challenges (e.g. "Monthly Challenge")

A Discord server admin creates a challenge for a table and duration. Every linked Mania user on that server who
plays the table automatically participates.

### Flow

```
Discord admin                    app.vpin-mania.net (PHP)                 Studio (cabinets)
─────────────                    ────────────────────────                 ─────────────────
/challenge create
  table: Attack from Mars  ───►  store challenge
  duration: 4w                   (guild, channel, vps table, version,
                                 start, end)
                                 post announcement + empty leaderboard
                                                                          player plays table
                                                                          new highscore
                                 ◄──────────────────────────────────────  synchronizeHighscore
                                 score matches a running challenge?
                                 player is guild member?
                                 → edit leaderboard (PATCH)
cron (every 5 min)
                                 challenge expired?
                                 → post final results, close challenge
```

### Mania (PHP) TODO

- [x] Slash commands: `/challenge create table:<autocomplete> days:<n> version:<autocomplete>`, `/challenge list`,
      `/challenge cancel`, `/challenge leaderboard`.
- [x] Table autocomplete against VPS data (`VpsDataService`, index refreshed daily by `discord:process`).
- [x] Admin only: `create`/`cancel` check the Manage Server permission of the member.
- [x] Tables `mania_discord_challenges`, `mania_discord_challenge_scores`.
- [x] On incoming score: match VPS id + time window (score `creationDate`) + linked account + guild membership → update leaderboard.
- [x] Cron: `php artisan schedule:run` every minute → `discord:process` every 5 min (reminders, final results).
- [x] Opt-out for users who don't want automatic participation (`participateInDiscordChallenges` Mania setting).

### Pitfalls

1. **Only scores on the local highscore list are visible.** Studio reads NVRAM/text highscores (often top 4 +
   GC). A new score that doesn't beat the local list never shows up. Existing Discord competitions solve this by
   resetting highscores at start → challenges need the same (see Studio TODO).
2. **Same table version.** Scores of different VPS versions/ROMs are often not comparable → bind the challenge
   to a `vps_version_id` (at least the ROM).
3. **Score date.** Only scores achieved after the challenge start may count. Verify whether Mania stores the
   score's creation date or only the upload time; ideally the time Studio first detected the score. Initial
   syncs must not bring in old scores.
4. **Fairness.** Table settings (balls per game, difficulty) can't be verified – trust-based, as with existing
   competitions.

### Studio TODO

- [ ] Nothing mandatory for basic score flow (scores are already synced when `submitAllScores` is enabled).
- [ ] Fetch active challenges for the cabinet from Mania on startup / periodically.
- [ ] Show challenges in pause menu and widgets (`CompetitionType.MANIA` already exists).
- [ ] On first launch of a challenge table: offer to back up and reset highscores (pitfall 1).
- [ ] Warn when the installed table version differs from the challenge version.
- [ ] Notify when the challenge table is not installed.

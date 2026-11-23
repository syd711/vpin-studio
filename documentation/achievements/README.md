# Achievements

The **Achievements** section replaces the former dashboard. It is a port of the
[VPX Achievement Watcher](https://github.com/Mizzlsolti/vpx-achievement-watcher) and tracks achievements for every
table whose ROM has an NVRAM map ([pinmame-nvram-maps](https://github.com/tomlogic/pinmame-nvram-maps)).

The achievements of a table come with VPin Studio: every release contains the achievement rules for all ROMs with an
NVRAM map, so all cabinets have the same achievements for a table. Updates of the NVRAM maps never change existing
achievements; tables whose map was added after the release get their achievements generated on the cabinet.

## How it works

- When a table is launched from the frontend, VPin Studio reads the audits of the table's `.nv` file.
  When the table is exited (and the game lasted at least 60 seconds), the audits are read again and the
  achievement rules are evaluated.
- Every game is credited to **all players with "Track achievements" enabled** (player settings, or
  VPin Mania settings > Achievements). Games with more than one player only count for the global achievements.
- Per-table achievements are created the first time the `.nv` file of a ROM can be read (same rules as the
  Achievement Watcher). There are also 122 global achievements (totals, manufacturers, played tables).
- Players get levels (10 levels, prestige stars every 2,000 achievements) and 31 badges. The playtime badges use the
  play time of the frontend statistics.
- Achievement, badge and level-up notifications are shown on the cabinet (can be switched off).

## Views

| Tab | Content |
|---|---|
| Overview | Level, unlocked achievements, cloud rank, recent unlocks, latest scores, cabinet/cloud ranking, badges, closest to unlock |
| Tables | Progress per table (filters: in progress, completed, not started, no NVRAM map), achievements of a table with rarity, cloud top 5 |
| Badges | Level table, prestige, all badges with progress; choose the badge shown next to your name on the leaderboards |
| Leaderboards | Global top 50 (different achievements) and top 25 per table (VPin Mania) |

## VPin Mania

Achievements of players linked to a VPin Mania account are uploaded after every game (and with *Sync with cloud*).
VPin Mania computes the rarity of every achievement and the leaderboards. Players without a VPin Mania account are
tracked locally only.

## Migration from the Achievement Watcher

VPin Mania settings > Achievements > *Migrate Achievement Watcher data...*:

- **Achievement Watcher cloud**: enter the Player ID and the Recovery Key (Watcher: System > General, or the backup
  ZIP). VPin Studio verifies the ownership once (the Watcher's "claim") and reads the data. Afterwards the Watcher can
  no longer upload for this Player ID. Nothing else is written to the Watcher's cloud.
- **Local installation**: select the Watcher folder on the cabinet. The data is imported locally; it is uploaded to
  VPin Mania only if the ownership can be verified with the keys of the installation.

Each Player ID can only be imported into VPin Mania once.

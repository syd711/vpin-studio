# Achievements Integration — Progress Log

Tracks implementation of `ACHIEVEMENTS-INTEGRATION-PLAN.md`. Updated after each step so a cancelled session
can resume. Status: `[ ]` open, `[~]` in progress, `[x]` done.

## Phase 0 — Groundwork
- [x] 0.1 Golden fixtures from Python AW (20 ROMs, `vpin-studio-server/src/test/resources/achievements/golden`,
      generator script copied to `vpin-studio-server/src/test/resources/achievements/gen_fixtures.py`)
- [x] 0.2 NvramAuditReader + RomMapResolver + MapFieldExtractor (golden: fields identical for 20 ROMs;
      values: same field set, ~90 % equal values vs AW autofix decoder)
- [x] 0.3 Rule model (AW JSON compatible), bundled `achievements/global_achievements.json`, RomRuleGenerator
      (golden: identical rule files for 20 ROMs), RuleEvaluator
- [x] 0.4 LevelCalculator (golden), Badge/BadgeEvaluator (+progress texts), RarityTier, ManufacturerUtil
- [x] 0.5 Unit tests: RomRuleGeneratorGoldenTest, NvramAuditReaderTest, ProgressionTest, RuleEvaluatorTest (all green)

Code: `vpin-studio-server/.../server/achievements/{nvram,rules,progression}`, DTOs in
`vpin-studio-rest-client/.../restclient/achievements`, `PreferenceNames.ACHIEVEMENTS_SETTINGS`.

## Phase 1 — Local tracking + UI
- [x] 1.1 JPA entities/repos (AchievementUnlocks, AchievementSessions, AchievementPlayerStates),
      `Preferences.achievementsSettings`, `Player.achievementsEnabled` (default true)
- [x] 1.2 AchievementsService (TableStatusChangeListener, nv flush wait, all tracked players, multiplayer skip,
      badges, ALX playtime, player deletion cleanup), AchievementRuleStore, AchievementGameIndex, AchievementsQueryService
- [x] 1.3 REST `AchievementsResource` (/api/v1/achievements/...) + `AchievementsServiceClient` (registered in VPinStudioClient)
- Integration test `AchievementsServiceIntegrationTest` (Spring context, real twst_405.nv) green.
  NOTE: Spring tests modify `testsystem/*.db` — restored with git checkout after running.
- [x] 1.4 UI: `NavigationItem.Achievements` replaces Dashboard (DashboardController + scene-dashboard.fxml removed),
      `ui/achievements/*` (AchievementsController + Overview/Tables/Badges/Leaderboards panes, AchievementsUI helper)
- [x] 1.5 Player dialog checkbox "Track achievements", i18n (en + de; es/fr/it/pt fall back to en). Docs: `documentation/achievements/README.md`

## Phase 2 — Notifications
- [x] 2.1 Achievement/badge/level-up notifications via NotificationService (in AchievementsService.notifyPlayer,
      setting `notificationsEnabled`)

## Phase 3 — Mania cloud + migration
- [x] 3.1 Mania backend (`C:\workspace-php\vpin-mania`): schema block at end of `server/sql/create_schema.sql`,
      `app/Services/AchievementService.php`, `app/Http/Controllers/AchievementController.php`, routes, GET whitelist
      in `RequestValidator`, cascades in Account/CabinetController (user delete goes through deleteCabinet).
      Tested against local XAMPP dev DB (tables created there): sync idempotent, ownership 403, one-time import 409,
      progress clamped to stored unlocks, leaderboards/rarity/search/account. Test rows removed again.
- [x] 3.2 vpin-mania-rest-client 2.1.0: `AchievementClient` + models; installed locally; Studio poms bumped to 2.1.0
- [x] 3.3 AchievementsCloudService (sync queue, restore from Mania, rarity cache, import upload); cloud UI parts
      (cloud rank, cloud ranking, Cloud Top 5, Leaderboards tab read Mania directly via Studio.maniaClient)
- [x] 3.4 Migration (FirebaseLegacyClient claim+read, LocalWatcherImporter with signature check,
      AchievementsMigrationService; tests) + Studio prefs page `preferences/preference-achievements.fxml` /
      `AchievementsPreferencesController` + wizard `AchievementsMigrationDialog`
- [x] 3.5 Ran dev server + UI against the real dev cabinet and the local Mania backend; all four tabs, Mania
      settings page and migration wizard checked with screenshots. Fixed: Mania date format (PHP must return
      `yyyy-MM-dd HH:mm:ss` UTC like all Mania endpoints), table pre-selection/sorting, "no data yet" for ROMs without
      rules, duplicate widget titles, badge tooltips. Test data removed from both dev DBs afterwards.

## Status: all phases implemented. Nothing is committed (Studio, vpin-mania-rest-client, vpin-mania).

## TODOs / follow-ups (instead of questions)
- Deploy: run the achievement block at the end of `vpin-mania/server/sql/create_schema.sql` on production BEFORE
  deploying the PHP code; deploy vpin-mania-rest-client 2.1.0 to the GitLab package registry (only installed locally).
- NVRAM map updates change the fields of a map. Rule files are generated once per ROM and then kept, but a cabinet that
  generates a ROM's rules after a map update gets a different rule set than one that generated them earlier, and the
  Watcher pinned the maps to one commit (eb0d7cf). Consider pinning the map version for rule generation, or keeping
  generated rule sets centrally in VPin Mania, so totals are the same on all cabinets.
- [x] Golden tests no longer read the gitignored `resources/maps`: the maps of the 20 test ROMs are copied to
  `src/test/resources/achievements/maps` (refresh with `copy_test_maps.py`, see the README there).
- Like the Watcher, the per-ROM rules include adjustment fields (e.g. "Replay Type: 1", "Extra Ball Limit: 3") that can
  never be reached in a game. Kept for migration compatibility; could be filtered (would change titles/totals).
- The "Achievements" entry in the Mania settings menu is hidden when the cabinet is not registered for VPin Mania
  (whole menu hidden); the page is reachable via the gear button in the Achievements view.
- Server-side texts (condition tooltips, achievement titles) are English only. UI texts incl. badge names/descriptions
  and progress texts are translated for en/de/es/fr/it/pt.
- Mania leaderboards/rarity are computed per request; add caching if load becomes an issue. No rate limit on sync yet.
- Speed Demon: achievements are evaluated at the end of a game, so "3 unlocks within 5 minutes" counts games of at most
  5 minutes with 3+ unlocks.
- [x] Library bug in java-pinmame-nvmaps `NVRamMapping.unpackOffset` fixed in C:\workspace\java-pinmame-nvmaps
  (uncommitted, installed locally as 1.0-SNAPSHOT). Affects 135 maps with `offset` (e.g. System 11 player count stored
  as players-1, so the multiplayer check was wrong there). Library test expectation for bcats_l5 player count 0 -> 1.
  Pre-existing, unrelated failures: library PinemhiRamParserTest (4) + NVRamParserCompareTest, Studio
  NvRamOutputToScoreTextTest (3).
- Phase 4 (not done, optional): post-game overlay/highlights, VPX 10.8.1 score-server multiplayer/live unlocks,
  AWEditor custom achievements, Mania web views.
- Firebase "claim" of a Player ID that was never claimed succeeds with any key (same as in the Watcher).

## Notes
- Golden fixtures were regenerated after the dev-server run updated `resources/maps`.
- Running the Spring tests modifies `testsystem/vpin-studio.db` and `testsystem/vPinball/PinUPSystem/PUPDatabase.db`
  (restore with `git checkout --`).

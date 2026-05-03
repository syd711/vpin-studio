# Unit Test Generation Progress

Generated with Claude Code. Resume in any session by reading this file.
Last updated: 2026-05-03

## Summary
- Total `@Service` classes: 131
- Tests generated total: 76 (4 session 1 + 6 session 2 + 9 session 3 + 3 session 4 + 6 session 5 + 6 session 6 + 4 session 7 + 4 session 8 + 5 session 9 + 4 session 10 + 9 session 11 + 5 session 12 + 11 session 13)
- Tests already existed: ~27
- Tests remaining: ~28

## Test Style
- New tests use `@ExtendWith(MockitoExtension.class)` (pure unit tests with Mockito)
- Existing tests use `@SpringBootTest` (integration tests requiring `testsystem/`)
- Test files live in `vpin-studio-server/src/test/java/de/mephisto/vpin/server/`

## Services — Status

| # | Class | Package | Test File | Status |
|---|-------|---------|-----------|--------|
| 1 | VRService | vr/ | VRServiceTest.java | ✅ GENERATED |
| 2 | FrontendService | frontend/ | FrontendServiceTest.java | ✅ EXISTS |
| 3 | GameDetailsRepositoryService | games/ | GameDetailsRepositoryServiceTest.java | ✅ GENERATED |
| 4 | VPinMameService | vpinmame/ | - | ❌ TODO |
| 5 | GameValidationService | games/ | GameValidationServiceTest.java | ✅ GENERATED |
| 6 | GameCachingService | games/ | - | ❌ TODO |
| 7 | PinUPConnector | frontend/popper/ | - | ❌ TODO |
| 8 | CompetitionChangeListenerImpl | listeners/ | CompetitionChangeListenerImplTest.java | ✅ GENERATED |
| 9 | IScoredService | iscored/ | - | ❌ TODO |
| 10 | DOFLinxComponent | components/facades/ | - | ❌ TODO |
| 11 | BackglassService | directb2s/ | BackglassServiceTest.java | ✅ EXISTS |
| 12 | FrontendStatusService | frontend/ | FrontendStatusServiceTest.java | ✅ GENERATED |
| 13 | GameService | games/ | GameServiceTest.java | ✅ EXISTS |
| 14 | HighscoreResolver | highscores/ | HighscoreResolverTest.java | ✅ GENERATED |
| 15 | HighscoreService | highscores/ | HighscoreServiceTest.java | ✅ EXISTS |
| 16 | SubscriptionCompetitionChangeListenerImpl | listeners/ | SubscriptionCompetitionChangeListenerImplTest.java | ✅ GENERATED |
| 17 | SystemService | system/ | SystemTest.java | ✅ EXISTS |
| 18 | RecorderService | recorder/ | RecorderTest.java | ✅ EXISTS |
| 19 | ScreenshotService | recorder/ | ScreenshotServiceTest.java | ✅ GENERATED |
| 20 | EmulatorService | emulators/ | EmulatorServiceTest.java | ✅ EXISTS |
| 21 | DefaultPictureService | system/ | DefaultPictureServiceTest.java | ✅ EXISTS |
| 22 | SystemBackupService | system/ | SystemBackupServiceTest.java | ✅ GENERATED |
| 23 | PlayerService | players/ | PlayerServiceTest.java | ✅ GENERATED |
| 24 | AlxService | alx/ | AlxServiceTest.java | ✅ GENERATED |
| 25 | VpsService | vps/ | VpsServiceTest.java | ✅ EXISTS |
| 26 | PINemHiService | pinemhi/ | PINemHiServiceTest.java | ✅ GENERATED |
| 27 | PreferencesService | preferences/ | PreferencesServiceTest.java | ✅ EXISTS |
| 28 | MusicService | music/ | MusicServiceTest.java | ✅ GENERATED |
| 29 | AltColorService | altcolor/ | AltColorServiceTest.java | ✅ EXISTS |
| 30 | CardService | highscores/cards/ | CardServiceTest.java | ✅ EXISTS |
| 31 | UniversalUploadService | games/ | UniversalUploadServiceTest.java | ✅ GENERATED |
| 32 | MameService | mame/ | MameServiceTest.java | ✅ GENERATED |
| 33 | WOVPCompetitionSynchronizer | competitions/wovp/ | WOVPCompetitionSynchronizerTest.java | ✅ GENERATED |
| 34 | TaggingService | tagging/ | TaggingServiceTest.java | ✅ GENERATED |
| 35 | PinVolService | pinvol/ | PinVolServiceTest.java | ✅ GENERATED |
| 36 | InputEventService | inputs/ | - | ❌ TODO (JavaFX dependency) |
| 37 | EmulatorFactory | emulators/ | EmulatorFactoryTest.java | ✅ GENERATED |
| 38 | EmulatorDetailsService | emulators/ | EmulatorDetailsServiceTest.java | ✅ GENERATED |
| 39 | DMDDeviceIniService | dmd/ | DMDDeviceIniServiceTest.java | ✅ GENERATED |
| 40 | FolderLookupService | vpx/ | FolderLookupServiceTest.java | ✅ GENERATED |
| 41 | GameMediaService | games/ | GameMediaServiceTest.java | ✅ EXISTS |
| 42 | VpaService | backups/adapters/vpa/ | TableBackupVpaServiceTest.java | ✅ EXISTS |
| 43 | CardTemplatesService | highscores/cards/ | CardTemplatesServiceTest.java | ✅ GENERATED |
| 44 | DefaultTableAndFrontendStatusChangeListenerImpl | listeners/ | - | ❌ TODO |
| 45 | IScoredCompetitionSynchronizer | competitions/iscored/ | IScoredCompetitionSynchronizerTest.java | ✅ GENERATED |
| 46 | ManiaService | mania/ | - | ❌ TODO |
| 47 | WovpService | wovp/ | WovpServiceTest.java | ✅ GENERATED |
| 48 | VPAuthenticationService | vpauthenticators/ | VPAuthenticationServiceTest.java | ✅ GENERATED |
| 49 | PinballYConnector | frontend/pinbally/ | - | ❌ TODO |
| 50 | ScreenPreviewService | recorder/ | - | ❌ TODO |
| 51 | VPXCommandLineService | vpx/ | - | ❌ TODO (skip — ApplicationContext dependency) |
| 52 | VPXMonitoringService | vpx/ | VPXMonitoringServiceTest.java | ✅ GENERATED |
| 53 | SteamService | steam/ | SteamServiceTest.java | ✅ GENERATED |
| 54 | RomService | roms/ | RomServiceTest.java | ✅ EXISTS |
| 55 | PupPacksService | puppack/ | PupPacksServiceTest.java | ✅ EXISTS |
| 56 | NotificationService | notifications/ | NotificationServiceTest.java | ✅ GENERATED |
| 57 | NVRamService | nvrams/ | NVRamServiceTest.java | ✅ GENERATED |
| 58 | ManiaServiceCache | mania/ | ManiaServiceCacheTest.java | ✅ GENERATED |
| 59 | OfflineCompetitionChangeListenerImpl | listeners/ | OfflineCompetitionChangeListenerImplTest.java | ✅ GENERATED |
| 60 | HighscoreChangeListenerImpl | listeners/ | HighscoreChangeListenerImplTest.java | ✅ GENERATED |
| 61 | DiscordCompetitionChangeListenerImpl | listeners/ | DiscordCompetitionChangeListenerImplTest.java | ✅ GENERATED |
| 62 | IScoredHighscoreChangeListener | iscored/ | IScoredHighscoreChangeListenerTest.java | ✅ GENERATED |
| 63 | IniService | ini/ | IniServiceTest.java | ✅ GENERATED |
| 64 | TextHighscoreAdapters | highscores/parsing/text/ | - | ❌ TODO |
| 65 | IniHighscoreAdapters | highscores/parsing/ini/ | - | ❌ TODO |
| 66 | HighscoreBackupService | highscores/ | HighscoreBackupServiceTest.java | ✅ EXISTS |
| 67 | GameStatusService | games/ | GameStatusServiceTest.java | ✅ GENERATED |
| 68 | StandaloneConnector | frontend/standalone/ | - | ❌ TODO (extends BaseConnector, complex wiring) |
| 69 | PinballXConnector | frontend/pinballx/ | - | ❌ TODO |
| 70 | PinballXAssetsIndexer | frontend/pinballx/ | - | ❌ TODO |
| 71 | PinballXAssetsIndexAdapter | frontend/pinballx/ | PinballXAssetsIndexAdapterTest.java | ✅ EXISTS |
| 72 | PinballXAssetsAdapter | frontend/pinballx/ | PinballXAssetsAdapterTest.java | ✅ EXISTS |
| 73 | VPinScreenService | frontend/ | - | ❌ TODO |
| 74 | FPCommandLineService | fp/ | - | ❌ TODO |
| 75 | TableExporterService | exporter/ | - | ❌ TODO |
| 76 | HighscoreExportService | exporter/ | - | ❌ TODO |
| 77 | MediaExportService | exporter/ | - | ❌ TODO |
| 78 | BackglassExportService | exporter/ | - | ❌ TODO |
| 79 | DOFService | dof/ | DOFTest.java | ✅ EXISTS |
| 80 | DiscordService | discord/ | - | ❌ TODO |
| 81 | DiscordCompetitionService | discord/ | - | ❌ TODO (heavy JDA dependencies) |
| 82 | MediaConverterService | converter/ | MediaConverterServiceTest.java | ✅ GENERATED |
| 83 | ComponentService | components/ | ComponentServiceTest.java | ✅ GENERATED |
| 84 | CompetitionService | competitions/ | CompetitionServiceTest.java | ✅ EXISTS |
| 85 | CompetitionIdUpdater | competitions/ | CompetitionIdUpdaterTest.java | ✅ GENERATED |
| 86 | BackupService | backups/ | BackupServiceTest.java | ✅ GENERATED |
| 87 | AssetService | assets/ | AssetServiceTest.java | ✅ EXISTS |
| 88 | AltSoundBackupService | altsound/ | AltSoundBackupServiceTest.java | ✅ GENERATED |
| 89 | VPXService | vpx/ | VPXServiceTest.java | ✅ GENERATED |
| 90 | VPXZService | vpxz/ | VPXZServiceTest.java | ✅ GENERATED |
| 91 | VPinMameRomAliasService | vpinmame/ | VPinMameRomAliasServiceTest.java | ✅ GENERATED |
| 92 | DMDService | dmd/ | DMDServiceTest.java | ✅ GENERATED |
| 93 | TextEditService | textedit/ | TextEditServiceTest.java | ✅ GENERATED |
| 94 | DMDPositionService | dmd/ | DMDPositionServiceTest.java | ✅ EXISTS |
| 95 | AltSoundService | altsound/ | AltSoundServiceTest.java | ✅ EXISTS |
| 96 | FlexDMDComponent | components/facades/ | FlexDMDComponentTest.java | ✅ GENERATED |
| 97 | SerumComponent | components/facades/ | SerumComponentTest.java | ✅ GENERATED |
| 98 | VPinMAMEComponent | components/facades/ | - | ❌ TODO |
| 99 | FreezyComponent | components/facades/ | FreezyComponentTest.java | ✅ GENERATED |
| 100 | BackglassValidationService | directb2s/ | BackglassValidationServiceTest.java | ✅ GENERATED |
| 101 | VPXZFileService | vpxz/ | VPXZFileServiceTest.java | ✅ GENERATED |
| 102 | VpxScriptOptionsService | vpx/ | VpxScriptOptionsServiceTest.java | ✅ GENERATED |
| 103 | PlaylistMediaService | playlists/ | - | ❌ TODO |
| 104 | JobQueue | jobs/ | JobQueueTest.java | ✅ GENERATED |
| 105 | VPRegService | highscores/parsing/vpreg/ | VPRegTest.java | ✅ EXISTS |
| 106 | GameEmulatorValidationService | games/ | GameEmulatorValidationServiceTest.java | ✅ GENERATED |
| 107 | FuturePinballService | fp/ | FuturePinballServiceTest.java | ✅ GENERATED |
| 108 | BackglassComponent | components/facades/ | BackglassComponentTest.java | ✅ GENERATED |
| 109 | CompetitionNotificationsListener | competitions/ | CompetitionNotificationsListenerTest.java | ✅ GENERATED |
| 110 | TableBackupAdapterFactory | backups/adapters/ | TableBackupAdapterFactoryTest.java | ✅ GENERATED |
| 111 | TableAssetsService | assets/ | TableAssetsServiceTest.java | ✅ GENERATED |
| 112 | CompetitionLifecycleService | competitions/ | CompetitionLifecycleServiceTest.java | ✅ GENERATED |
| 113 | ResService | res/ | ResServiceTest.java | ✅ GENERATED |
| 114 | GameLifecycleService | games/ | GameLifecycleServiceTest.java | ✅ GENERATED |
| 115 | TableAssetAdapterFactory | assets/ | TableAssetAdapterFactoryTest.java | ✅ GENERATED |
| 116 | TableAssetSourcesService | assets/ | - | ❌ TODO |
| 117 | JobService | jobs/ | JobServiceTest.java | ✅ GENERATED |
| 118 | TemplateMerger | highscores/cards/ | - | ❌ TODO |
| 119 | WebhooksService | webhooks/ | WebhooksServiceTest.java | ✅ GENERATED |
| 120 | DiscordBotResponseService | discord/ | DiscordBotResponseServiceTest.java | ✅ GENERATED |
| 121 | VpsEntryService | vpsdb/ | VpsEntryServiceTest.java | ✅ GENERATED |
| 122 | VpxComponent | components/facades/ | VpxComponentTest.java | ✅ GENERATED |
| 123 | DOFComponent | components/facades/ | DOFComponentTest.java | ✅ GENERATED |
| 124 | PatchingService | patcher/ | PatchingServiceTest.java | ✅ GENERATED |
| 125 | HooksService | hooks/ | HooksServiceTest.java | ✅ GENERATED |
| 126 | DiscordSubscriptionMessageFactory | discord/ | DiscordSubscriptionMessageFactoryTest.java | ✅ GENERATED |
| 127 | DiscordChannelMessageFactory | discord/ | DiscordChannelMessageFactoryTest.java | ✅ GENERATED |
| 128 | DiscoveryBroadcaster | discovery/ | DiscoveryBroadcasterTest.java | ✅ GENERATED |
| 129 | HighscoreParsingService | highscores/parsing/ | HighscoreParserTest.java | ✅ EXISTS |
| 130 | PinballXFtpClient | frontend/pinballx/ | - | ❌ TODO |
| 131 | PlaylistService | playlists/ | PlaylistServiceTest.java | ✅ EXISTS |

## Sessions

### Session 1 — 2026-04-29
Generated: MusicService, GameStatusService, VPinMameRomAliasService, VpsEntryService

### Session 2 — 2026-04-29
Generated: AlxService, FrontendStatusService, GameLifecycleService, JobQueue, JobService, TaggingService

### Session 3 — 2026-04-29
Generated: VRService, GameDetailsRepositoryService, HighscoreResolver, NotificationService, TextEditService, VPXZFileService, HooksService, DiscoveryBroadcaster, WebhooksService

### Session 4 — 2026-04-29
Generated: GameValidationService, PlayerService, MameService

### Session 5 — 2026-04-29
Generated: NVRamService, EmulatorDetailsService, IniService, PINemHiService, SystemBackupService, ScreenshotService

### Session 6 — 2026-04-29
Generated: CompetitionChangeListenerImpl, UniversalUploadService, PinVolService, DMDDeviceIniService, FolderLookupService, VPXMonitoringService

### Session 7 — 2026-04-30
Generated: VPXService, BackupService, CardTemplatesService, EmulatorFactory

### Session 8 — 2026-04-30
Generated: CompetitionLifecycleService, GameEmulatorValidationService, ComponentService, VPXZService

### Session 9 — 2026-04-30
Generated: CompetitionIdFactory (utility, not @Service), CompetitionIdUpdater, CompetitionNotificationsListener, ResService, WOVPCompetitionSynchronizer

### Session 10 — 2026-04-30
Generated: AltSoundBackupService, TableBackupAdapterFactory, DMDService, VpxScriptOptionsService

### Session 11 — 2026-05-03
Generated: PatchingService, DiscordChannelMessageFactory, DiscordSubscriptionMessageFactory, IScoredHighscoreChangeListener, HighscoreChangeListenerImpl, OfflineCompetitionChangeListenerImpl, DiscordCompetitionChangeListenerImpl, SubscriptionCompetitionChangeListenerImpl, MediaConverterService
Also fixed pre-existing NvRamOutputToScoreTextTest compilation error (NVRamPinemhiParser API changed)

### Session 12 — 2026-05-03
Generated: BackglassValidationService, FuturePinballService, TableAssetsService, SteamService (mockStatic), IScoredCompetitionSynchronizer (mockStatic)

### Session 13 — 2026-05-03
Generated: VPAuthenticationService, ManiaServiceCache, TableAssetAdapterFactory, WovpService, VpxComponent, FlexDMDComponent, SerumComponent, FreezyComponent, BackglassComponent, DOFComponent, DiscordBotResponseService

### Next batch (pick up here)
Remaining TODO (easiest first):
- VPinMAMEComponent (98) — similar to other component facades, depends on VPinMameService
- ManiaService (46) — heavy: VPinManiaClient, many listeners (may skip)
- DiscordService (80) — heavy JDA dep (may skip)
- DiscordCompetitionService (81) — heavy (may skip)
- PlaylistMediaService (103) — extends MediaService, file-based
- TableAssetSourcesService (116) — asset sources management
- TableAssetAdapterFactory (115) — done ✅
- TemplateMerger (118) — highscore card template merging
- StandaloneConnector (68) — complex BaseConnector
- PinballXConnector (69) / PinballXAssetsIndexer (70) — PinballX connectors
- VPinScreenService (73) — frontend service
- FPCommandLineService (74) — FP command line
- TableExporterService (75) / HighscoreExportService (76) / MediaExportService (77) / BackglassExportService (78) — exporters
- PinballYConnector (49) / ScreenPreviewService (50) — remaining services
- PinballXFtpClient (130) — FTP client

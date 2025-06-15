package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.restclient.system.FeaturesInfo;

/**
 * The different frontend supported.
 * Use feature control to activate or not components in the tool
 */
public enum FrontendType {
  Standalone(SupportType.NONE, SupportType.NONE, SupportType.NONE,false, SupportType.NONE, false, false, false, false, false, false, false, false),
  Popper    (SupportType.FULL, SupportType.FULL, SupportType.FULL, true, SupportType.FULL, true, false, true, true, true, true, true, true),
  PinballX  (SupportType.MINI, SupportType.MINI, SupportType.MINI, true, SupportType.MINI, true, true, false, true, false, false, true, false),
  PinballY  (SupportType.MINI, SupportType.NONE, SupportType.MINI, true, SupportType.MINI, true, true, false, true, false, false, true, false);

  enum SupportType {NONE, MINI, FULL}

  FrontendType(SupportType supportFields, SupportType supportEmulators, SupportType supportPlaylists, boolean supportPlaylistsCrud,
          SupportType supportStatuses, boolean supportMedias, boolean supportMediaCache,
          boolean supportPupPacks, boolean supportStatistics, boolean supportArchive,
          boolean supportControls, boolean supportRating, boolean supportCompetitions) {
    this.supportFields = supportFields;
    this.supportEmulators = supportEmulators;
    this.supportPlaylists = supportPlaylists;
    this.supportPlaylistsCrud = supportPlaylistsCrud;
    this.supportStatuses = supportStatuses;
    this.supportMedias = supportMedias;
    this.supportMediaCache = supportMediaCache;
    this.supportPupPacks = supportPupPacks;
    this.supportStatistics = supportStatistics;
    this.supportArchive = supportArchive;
    this.supportControls = supportControls;
    this.supportRating = supportRating;
    this.supportCompetitions = supportCompetitions;
  }

  /** Whether stantard vpin metadata are supported (gametype, year, manufacturer, nbPlayers, rating author, theme, IPDB,
   * or Whether extended set of fields are supported (cf all popper fields) */
  private SupportType supportFields;

  /**
   * Whether Playlists are supported or not
   * MINI ie pinballX way, one level of favorites managed globally
   * FULL is the pinup way of managing favorites : local and global favorites support + management at playlist level
   */
  private SupportType supportPlaylists;
  /** Whether Playlists creation / update / deletion are supported or not */
  private boolean supportPlaylistsCrud;


  /**
   * Whether Emulator configs are supported or not
   * MINI ie pinballX way: no delete or create options
   */
  private SupportType supportEmulators;

  /** Whether frontend support statuses or not
   * MINI : just active / inactive
   * FILL : MINI + additional popper status (MATURE + WIP) */
  private SupportType supportStatuses;

  /** Whether medias are supported by the frontend */
  private boolean supportMedias;
  /** Whether a media cache is supported by the frontend */
  private boolean supportMediaCache;
  /** Whether puppacks are supported by the frontend */
  private boolean supportPupPacks;
  /** Whether statistics are recorded by the frontend */
  private boolean supportStatistics;
  /** Whether archive and VPBM are supported by the frontend */
  private boolean supportArchive;
  /** Whether controls are supported by the frontend */
  private boolean supportControls;
  /** Whether ratings are supported by the frontend */
  private boolean supportRating;
  /** Whether competitions are supported by the frontend */
  private boolean supportCompetitions;

  //----------

  public void apply(FeaturesInfo features) {
    features.FIELDS_STANDARD &= !supportFields.equals(SupportType.NONE);
    features.FIELDS_EXTENDED &= supportFields.equals(SupportType.FULL);
    features.PLAYLIST_ENABLED &= !supportPlaylists.equals(SupportType.NONE);
    features.PLAYLIST_EXTENDED &= supportPlaylists.equals(SupportType.FULL);
    features.PLAYLIST_CRUD &= supportPlaylistsCrud;
    features.STATUSES &= !supportStatuses.equals(SupportType.NONE);
    features.STATUS_EXTENDED &= supportStatuses.equals(SupportType.FULL);
    features.MEDIA_ENABLED &= supportMedias;
    features.PUPPACKS_ENABLED &= supportPupPacks;
    features.STATISTICS_ENABLED &= supportStatistics;
    features.ARCHIVE_ENABLED &= supportArchive;
    features.CONTROLS_ENABLED &= supportControls;
    features.RATINGS &= supportRating;
    features.COMPETITIONS_ENABLED &= supportCompetitions;
    features.EMULATORS_ENABLED &= !supportEmulators.equals(SupportType.NONE);
    features.EMULATORS_CRUD &= supportEmulators.equals(SupportType.FULL);
    features.MEDIA_CACHE &= supportMediaCache;
    features.IS_STANDALONE = this.equals(Standalone);
  }

}

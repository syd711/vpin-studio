package de.mephisto.vpin.restclient.frontend;

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

  public boolean supportStandardFields() {
    return !supportFields.equals(SupportType.NONE);
  }
  public boolean supportExtendedFields() {
    return supportFields.equals(SupportType.FULL);
  }

  public boolean supportPlaylists() {
    return !supportPlaylists.equals(SupportType.NONE);
  }
  public boolean supportExtendedPlaylists() {
    return supportPlaylists.equals(SupportType.FULL);
  }
  public boolean supportPlaylistsCrud() {
    return supportPlaylistsCrud;
  }

  public boolean supportStatuses() {
    return !supportStatuses.equals(SupportType.NONE);
  }
  public boolean supportExtendedStatuses() {
    return supportStatuses.equals(SupportType.FULL);
  }

  public boolean supportMedias() {
    return supportMedias;
  }
  public boolean supportPupPacks() {
    return supportPupPacks;
  }
  public boolean supportStatistics() {
    return supportStatistics;
  }
  public boolean supportArchive() {
    return supportArchive;
  }
  public boolean supportControls() {
    return supportControls;
  }
  public boolean supportRating() { return supportRating; }
  public boolean supportCompetitions() { return supportCompetitions; }
  public boolean isSupportMediaCache() {
    return supportMediaCache;
  }
  public boolean supportEmulators() {return !supportEmulators.equals(SupportType.NONE);}
  public boolean supportEmulatorCreateDelete() {return supportEmulators.equals(SupportType.FULL);}

  //----------

  public boolean isStandalone() {
    return this.equals(Standalone);
  }
  public boolean isNotStandalone() {
    return !isStandalone();
  }
}

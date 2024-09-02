package de.mephisto.vpin.restclient.frontend;

/**
 * The different frontend supported.
 * Use feature control to activate or not components in the tool 
 */
public enum FrontendType {

  Standalone(false, false, false, false, false, false, false, false, false, false, false),
  Popper(true, true, true, true, false, true, false, true, true, true, true),

  // playlist not activated yet but will be supported
  PinballX(true, false, true, false, true, true, true, false, true, false, false);

  FrontendType(boolean supportStandardFields, boolean supportExtendedFields, 
          boolean supportPlaylists, boolean supportExtendedPlaylists, boolean supportPlaylistsCrud, 
          boolean supportMedias, boolean supportMediaCache,
          boolean supportPupPacks, boolean supportStatistics, boolean supportArchive,
          boolean supportControls) {
    this.supportStandardFields = supportStandardFields;
    this.supportExtendedFields = supportExtendedFields;
    this.supportPlaylists = supportPlaylists;
    this.supportExtendedPlaylists = supportExtendedPlaylists;
    this.supportPlaylistsCrud = supportPlaylistsCrud;
    this.supportMedias = supportMedias;
    this.supportMediaCache = supportMediaCache;
    this.supportPupPacks = supportPupPacks;
    this.supportStatistics = supportStatistics;
    this.supportArchive = supportArchive;
    this.supportControls = supportControls;
  }

  /** Whether stantard vpin metadata are supported (gametype, year, manufacturer, nbPlayers, rating author, theme, IPDB */
  private boolean supportStandardFields;
  /** Whther extended set of fields are supported (cf all popper fields) */
  private boolean supportExtendedFields;
  /** Whether Playlists are supported or not */
  private boolean supportPlaylists;
  /** Whether Playlists creation / update / deletion are supported or not */
  private boolean supportPlaylistsCrud;
  /** pinup way of managing favorites : local and global favorites support + management at playlist level
   * If false, ie pinballX way, one level of favorites managed globally */
  private boolean supportExtendedPlaylists;
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
  
  //----------

  public boolean supportStandardFields() {
    return supportStandardFields;
  }
  public boolean supportExtendedFields() {
    return supportExtendedFields;
  }
  public boolean supportPlaylists() {
    return supportPlaylists;
  }
  public boolean supportPlaylistsCrud() {
    return supportPlaylistsCrud;
  }
  public boolean supportExtendedPlaylists() {
    return supportExtendedPlaylists;
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
  public boolean isSupportMediaCache() {
    return supportMediaCache;
  }

  //----------

  public boolean isStandalone() {
    return this.equals(Standalone);
  }
  public boolean isNotStandalone() {
    return !isStandalone();
  }
}

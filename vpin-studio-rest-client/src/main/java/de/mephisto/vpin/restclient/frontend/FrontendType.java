package de.mephisto.vpin.restclient.frontend;

/**
 * The different frontend supported.
 * Use feature control to activate or not components in the tool 
 */
public enum FrontendType {

  Standalone(false, false, false, false, false, false, false, false, false),
  Popper(true, true, true, true, false, true, true, true, true),
  // playlist and statistics not activated yet but will be supported
  PinballX(true, false, false, true, true, false, false, false, false);

  FrontendType(boolean supportStandardFields, boolean supportExtendedFields, boolean supportPlaylists,
          boolean supportMedias,
               boolean supportMediaCache,
               boolean supportPupPacks, boolean supportStatistics, boolean supportArchive,
          boolean supportControls) {
    this.supportStandardFields = supportStandardFields;
    this.supportExtendedFields = supportExtendedFields;
    this.supportPlaylists = supportPlaylists;
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

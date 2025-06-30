package de.mephisto.vpin.restclient.system;

public class FeaturesInfo {

  public boolean MANIA_ENABLED = true;
  public boolean MANIA_SOCIAL_ENABLED = true;
  public boolean ISCORED_ENABLED = true;
  /** Whether competitions are supported by the frontend */
  public boolean COMPETITIONS_ENABLED = true;

  public boolean NOTIFICATIONS_ENABLED = true;
  public boolean VPX_MONITORING = false;

  public boolean VP_UNIVERSE = false;
  public boolean VP_FORUMS = false;
  public boolean AUTO_INSTALLER = false;

  public boolean DROP_IN_FOLDER = true;

  public boolean RECORDER = true;
  public boolean RES_EDITOR = true;
  public boolean SCREEN_VALIDATOR = false;

  /** Whether ratings are supported */
  public boolean RATINGS = true;

  public boolean PLAYLIST_ENABLED = true;
  public boolean PLAYLIST_MANAGER = true;
  public boolean PLAYLIST_EXTENDED = true;
  public boolean PLAYLIST_CRUD = true;

  public boolean BACKUPS_ENABLED = true;

  public boolean SCREEN_MANAGER_ENABLED = true;

  /* Whether Emulator configs are supported or not */
  public boolean EMULATORS_ENABLED = true;
  /* Not only supporting multiple emulators, also possible to create / delete emulators */
  public boolean EMULATORS_CRUD = true;

  public boolean WEBHOOKS_ENABLED = true;

  /** Whether medias are supported by the frontend */
  public boolean MEDIA_ENABLED = true;
  /** Whether a media cache is supported by the frontend */
  public boolean MEDIA_CACHE = true;

  /** Whether puppacks are supported by the frontend */
  public boolean PUPPACKS_ENABLED = true;

  /** Whether statistics are recorded by the frontend */
  public boolean STATISTICS_ENABLED = true;

  /** Whether controls are supported by the frontend */
  public boolean CONTROLS_ENABLED = true;

  public boolean FIELDS_STANDARD = true;
  public boolean FIELDS_EXTENDED = true;

  /** Whether frontend support statuses active / inactive */
  public boolean STATUSES = true;
  /** Whether frontend support STATUSES + additional popper status (MATURE + WIP) */
  public boolean STATUS_EXTENDED= true;

    /** Specific flag for standalone mode */
  public boolean IS_STANDALONE = false;

}

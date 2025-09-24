package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.restclient.system.FeaturesInfo;

/**
 * The different frontend supported.
 * Use feature control to activate or not components in the tool
 */
public enum FrontendType {

  Standalone {
    @Override
    public void apply(FeaturesInfo features) {
      features.FIELDS_STANDARD &= false;
      features.FIELDS_EXTENDED &= false;
      features.PLAYLIST_ENABLED &= false;
      features.PLAYLIST_EXTENDED &= false;
      features.PLAYLIST_CRUD &= false;
      features.STATUSES &= false;
      features.STATUS_EXTENDED &= false;
      features.MEDIA_ENABLED &= false;
      features.MEDIA_CACHE &= false;
      features.PUPPACKS_ENABLED &= false;
      features.STATISTICS_ENABLED &= false;
      features.BACKUPS_ENABLED &= true;
      features.CONTROLS_ENABLED &= false;
      features.RATINGS &= false;
      features.COMPETITIONS_ENABLED &= false;
      features.EMULATORS_ENABLED &= false;
      features.EMULATORS_CRUD &= false;
      features.IS_STANDALONE = true;
    }
  },

  Popper{
    @Override
    public void apply(FeaturesInfo features) {
      features.FIELDS_STANDARD &= true;
      features.FIELDS_EXTENDED &= true;
      features.PLAYLIST_ENABLED &= true;
      features.PLAYLIST_EXTENDED &= true;
      features.PLAYLIST_CRUD &= true;
      features.STATUSES &= true;
      features.STATUS_EXTENDED &= true;
      features.MEDIA_ENABLED &= true;
      features.MEDIA_CACHE &= false;
      features.PUPPACKS_ENABLED &= true;
      features.STATISTICS_ENABLED &= true;
      features.BACKUPS_ENABLED &= true;
      features.CONTROLS_ENABLED &= true;
      features.RATINGS &= true;
      features.COMPETITIONS_ENABLED &= true;
      features.EMULATORS_ENABLED &= true;
      features.EMULATORS_CRUD &= true;
      features.IS_STANDALONE = false;
    }
  },

  PinballX {
    @Override
    public void apply(FeaturesInfo features) {
      features.FIELDS_STANDARD &= true;
      features.FIELDS_EXTENDED &= false;
      features.PLAYLIST_ENABLED &= true;
      features.PLAYLIST_EXTENDED &= false;
      features.PLAYLIST_CRUD &= true;
      features.STATUSES &= true;
      features.STATUS_EXTENDED &= false;
      features.MEDIA_ENABLED &= true;
      features.MEDIA_CACHE &= true;
      features.PUPPACKS_ENABLED &= true;
      features.STATISTICS_ENABLED &= true;
      features.BACKUPS_ENABLED &= true;
      features.CONTROLS_ENABLED &= true;
      features.RATINGS &= true;
      features.COMPETITIONS_ENABLED &= false;
      features.EMULATORS_ENABLED &= true;
      features.EMULATORS_CRUD &= true;
      features.IS_STANDALONE = false;
    }
  },

  PinballY {
    @Override
    public void apply(FeaturesInfo features) {
      features.FIELDS_STANDARD &= true;
      features.FIELDS_EXTENDED &= false;
      features.PLAYLIST_ENABLED &= true;
      features.PLAYLIST_EXTENDED &= true;
      features.PLAYLIST_CRUD &= true;
      features.STATUSES &= true;
      features.STATUS_EXTENDED &= false;
      features.MEDIA_ENABLED &= true;
      features.MEDIA_CACHE &= true;
      features.PUPPACKS_ENABLED &= true;
      features.STATISTICS_ENABLED &= true;
      features.BACKUPS_ENABLED &= true;
      features.CONTROLS_ENABLED &= true;
      features.RATINGS &= true;
      features.COMPETITIONS_ENABLED &= false;
      features.EMULATORS_ENABLED &= false;
      features.EMULATORS_CRUD &= false;
      features.IS_STANDALONE = false;
    }
  };

  public abstract void apply(FeaturesInfo features);
}

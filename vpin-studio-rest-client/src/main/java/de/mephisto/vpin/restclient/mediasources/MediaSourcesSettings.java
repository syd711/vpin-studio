package de.mephisto.vpin.restclient.mediasources;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MediaSourcesSettings extends JsonSettings {
  private List<MediaSource> mediaSources = new ArrayList<>();


  public List<MediaSource> getMediaSources() {
    return mediaSources;
  }

  public void setMediaSources(List<MediaSource> mediaSources) {
    this.mediaSources = mediaSources;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.MEDIA_SOURCES_SETTINGS;
  }
}

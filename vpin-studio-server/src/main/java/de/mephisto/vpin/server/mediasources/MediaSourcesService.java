package de.mephisto.vpin.server.mediasources;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mediasources.MediaSource;
import de.mephisto.vpin.restclient.mediasources.MediaSourcesSettings;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MediaSourcesService implements PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MediaSourcesService.class);

  @Autowired
  private PreferencesService preferencesService;

  private MediaSourcesSettings mediaSourcesSettings;

  public MediaSource getMediaSource(String sourceId) {
    Optional<MediaSource> first = mediaSourcesSettings.getMediaSources().stream().filter(m -> m.getId().equalsIgnoreCase(sourceId)).findFirst();
    return first.orElse(null);
  }

  public List<MediaSource> getMediaSources() {
    return mediaSourcesSettings.getMediaSources();
  }

  public boolean deleteMediaSource(String id) throws Exception {
    List<MediaSource> filtered = mediaSourcesSettings.getMediaSources().stream().filter(m -> !m.getId().equalsIgnoreCase(id)).collect(Collectors.toList());
    mediaSourcesSettings.setMediaSources(filtered);
    preferencesService.savePreference(mediaSourcesSettings);
    return true;
  }

  public MediaSource save(MediaSource mediaSource) throws Exception {
    List<MediaSource> filtered = new ArrayList<>(mediaSourcesSettings.getMediaSources().stream().filter(m -> !m.getId().equalsIgnoreCase(mediaSource.getId())).collect(Collectors.toList()));
    filtered.add(mediaSource);
    mediaSourcesSettings.setMediaSources(filtered);
    preferencesService.savePreference(mediaSourcesSettings);
    return mediaSource;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.MEDIA_SOURCES_SETTINGS.equalsIgnoreCase(propertyName)) {
      this.mediaSourcesSettings = preferencesService.getJsonPreference(PreferenceNames.MEDIA_SOURCES_SETTINGS, MediaSourcesSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferenceChanged(PreferenceNames.MEDIA_SOURCES_SETTINGS, null, null);
    preferencesService.addChangeListener(this);
  }
}

package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.wovp.WOVPCompetitionSynchronizer;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WovpService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WovpService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private WOVPCompetitionSynchronizer wovpCompetitionSynchronizer;

  private WOVPSettings wovpSettings;


  public String validateKey() {
    String apiKey = wovpSettings.getApiKey();
    Wovp wovp = Wovp.create(apiKey);
    return wovp.validateKey();
  }

  /**
   * The WOVP service does not need to return a specific value for the sync result.
   * The weekly competitions should remain abstract from the specific competition organizer.
   */
  public boolean synchronize() {
    return wovpCompetitionSynchronizer.synchronizeWovp();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.WOVP_SETTINGS.equals(propertyName)) {
      wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      synchronize();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    wovpSettings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    preferencesService.addChangeListener(this);
    synchronize();
    LOG.info("Initialized {}", this.getClass().getSimpleName());
  }
}

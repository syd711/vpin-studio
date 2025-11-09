package de.mephisto.vpin.server.competitions.wovp;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class WOVPCompetitionSynchronizer implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPCompetitionSynchronizer.class);

  @Autowired
  private PreferencesService preferencesService;

  private void synchronizeWovp() {

  }


  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    WOVPSettings settings = preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    if (settings.isEnabled()) {
      new Thread(() -> {
        long start = System.currentTimeMillis();
        Thread.currentThread().setName("Wovp Initial Sync");
        LOG.info("----------------------------- Initial WOVP Sync --------------------------------------------------");
        synchronizeWovp();
        LOG.info("----------------------------- /Initial WOVP Sync -------------------------------------------------");
        LOG.info("Initial sync finished, took {}ms", (System.currentTimeMillis() - start));
      }).start();
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.WOVP_SETTINGS.equals(propertyName)) {
      synchronizeWovp();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

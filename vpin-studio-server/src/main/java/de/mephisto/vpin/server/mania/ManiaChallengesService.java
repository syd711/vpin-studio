package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.mania.ManiaChallengeCompetitionSynchronizer;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

/**
 * Keeps the VPin Mania Discord challenges in sync with the competitions of type MANIA_CHALLENGE.
 */
@Service
public class ManiaChallengesService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaChallengesService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private ManiaChallengeCompetitionSynchronizer maniaChallengeCompetitionSynchronizer;

  public boolean synchronize(boolean forceReload) {
    return maniaChallengeCompetitionSynchronizer.synchronizeChallenges(forceReload);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.MANIA_SETTINGS.equals(propertyName)) {
      new Thread(() -> {
        Thread.currentThread().setName("VPin Mania Challenges Synchronizer");
        synchronize(false);
      }).start();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.MANIA_ENABLED) {
      preferencesService.addChangeListener(this);
    }
    LOG.info("Initialized {}", this.getClass().getSimpleName());
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    if (Features.MANIA_ENABLED) {
      new Thread(() -> {
        Thread.currentThread().setName("VPin Mania Challenges Synchronizer");
        synchronize(false);
      }).start();
    }
  }
}

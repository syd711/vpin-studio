package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tournaments.TournamentConfig;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TournamentsService implements InitializingBean, PreferenceChangedListener, TableStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsService.class);

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private TournamentSynchronizer tournamentSynchronizer;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private TournamentsHighscoreChangeListener tournamentsHighscoreChangeListener;

  private VPinManiaClient maniaClient;

  public TournamentConfig getConfig() {
    TournamentConfig config = new TournamentConfig();
    config.setSystemId(SystemUtil.getBoardSerialNumber());
    config.setUrl(maniaHost);
    return config;
  }

  public TournamentSettings saveSettings(TournamentSettings settings) {
    try {
      preferencesService.savePreference(PreferenceNames.TOURNAMENTS_SETTINGS, settings);
      return getSettings();
    } catch (Exception e) {
      LOG.error("Saving tournament settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving tournament settings failed: " + e.getMessage());
    }
  }

  public TournamentSettings getSettings() {
    return preferencesService.getJsonPreference(PreferenceNames.TOURNAMENTS_SETTINGS, TournamentSettings.class);
  }

  public boolean synchronize(TournamentMetaData metaData) {
    return tournamentSynchronizer.synchronize(metaData);
  }

  public boolean synchronize() {
    return tournamentSynchronizer.synchronize();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.TOURNAMENTS_SETTINGS.equals(propertyName)) {
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
      this.tournamentsHighscoreChangeListener.setCabinet(cabinet);
      LOG.info("Registered Tournaments HighscoreChangeListener");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.TOURNAMENTS_ENABLED) {
      try {
        TournamentConfig config = getConfig();
        maniaClient = new VPinManiaClient(config.getUrl(), config.getSystemId());
        Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
        if(cabinet != null) {
          LOG.info("Cabinet is registered on VPin-Mania");
        }
        else {
          LOG.info("Cabinet is not registered on VPin-Mania");
        }

        tournamentsHighscoreChangeListener.setVPinManiaClient(maniaClient);
        highscoreService.addHighscoreChangeListener(tournamentsHighscoreChangeListener);

        preferencesService.addChangeListener(this);
        preferenceChanged(PreferenceNames.TOURNAMENTS_SETTINGS, null, null);

        tournamentSynchronizer.setClient(maniaClient);
        tournamentSynchronizer.synchronize();

        frontendStatusService.addTableStatusChangeListener(this);
      } catch (Exception e) {
        Features.TOURNAMENTS_ENABLED = false;
        LOG.info("Error initializing tournament service: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    new Thread(() -> {
      Game game = event.getGame();
      Thread.currentThread().setName("Tournament Synchronizer for " + game.getGameDisplayName());
      tournamentSynchronizer.synchronize(game);
    }).start();
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {

  }

  public VPinManiaClient getManiaClient() {
    return maniaClient;
  }
}

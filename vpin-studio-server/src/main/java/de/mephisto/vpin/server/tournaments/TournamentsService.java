package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tournaments.TournamentConfig;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.iscored.IScoredService;
import de.mephisto.vpin.server.players.PlayerService;
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
public class TournamentsService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsService.class);

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private IScoredService iScoredService;

  private VPinManiaClient maniaClient;
  private TournamentsHighscoreChangeListener tournamentsHighscoreChangeListener;

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

  public boolean synchronize() {
    LOG.info("Running Tournament Synchronization");
    return false;
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
      TournamentConfig config = getConfig();
      maniaClient = new VPinManiaClient(config.getUrl(), config.getSystemId());

      tournamentsHighscoreChangeListener = new TournamentsHighscoreChangeListener(maniaClient, playerService, iScoredService);
      highscoreService.addHighscoreChangeListener(tournamentsHighscoreChangeListener);

      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.TOURNAMENTS_SETTINGS, null, null);

      synchronize();
    }
  }
}

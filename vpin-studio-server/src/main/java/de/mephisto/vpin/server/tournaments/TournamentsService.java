package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TournamentsService implements InitializingBean, TableStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsService.class);

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

  @Autowired
  private ManiaService maniaService;

  public TournamentSettings saveSettings(TournamentSettings settings) {
    try {
      preferencesService.savePreference(PreferenceNames.TOURNAMENTS_SETTINGS, settings);
      return getSettings();
    }
    catch (Exception e) {
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
    tournamentSynchronizer.synchronizeTournaments();
    return true;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {

  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    if (Features.MANIA_ENABLED) {
      tournamentSynchronizer.synchronizeTournaments();
    }
  }

  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.MANIA_ENABLED) {
      try {
        Cabinet cabinet = maniaService.getClient().getCabinetClient().getCabinet();
        if (cabinet != null) {
          TournamentSettings settings = getSettings();
          settings.setEnabled(true);
          preferencesService.savePreference(PreferenceNames.TOURNAMENTS_SETTINGS, settings);
        }

        highscoreService.addHighscoreChangeListener(tournamentsHighscoreChangeListener);
        tournamentSynchronizer.synchronizeTournaments();

        frontendStatusService.addTableStatusChangeListener(this);
      }
      catch (Exception e) {
        Features.MANIA_ENABLED = false;
        LOG.info("Error initializing tournament service: " + e.getMessage(), e);
      }
    }
  }
}

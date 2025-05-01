package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetOnlineStatus;
import de.mephisto.vpin.connectors.mania.model.CabinetStatus;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
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

  public boolean synchronize(TournamentMetaData metaData) {
    return tournamentSynchronizer.synchronize(metaData);
  }

  public boolean synchronize() {
    tournamentSynchronizer.synchronizeTournaments();
    return true;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    if (Features.MANIA_ENABLED) {
      try {
        Cabinet cabinet = maniaService.getClient().getCabinetClient().getCabinet();
        if (cabinet != null) {
          ManiaSettings settings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
          if (settings.isShowOnlineStatus() && settings.isShowActiveGameStatus()) {
            CabinetStatus status = cabinet.getStatus();
            Game game = event.getGame();

            status.setStatus(CabinetOnlineStatus.online);
            status.setActiveGame(game.getGameDisplayName());
          }
          maniaService.getClient().getCabinetClient().update(cabinet);
        }
      }
      catch (Exception e) {
        LOG.error("Error updating mania online status: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    if (Features.MANIA_ENABLED) {
      tournamentSynchronizer.synchronizeTournaments();
      try {
        Cabinet cabinet = maniaService.getClient().getCabinetClient().getCabinet();
        if (cabinet != null) {
          ManiaSettings settings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
          if (settings.isShowOnlineStatus() && settings.isShowActiveGameStatus()) {
            cabinet.getStatus().setStatus(CabinetOnlineStatus.online);
            cabinet.getStatus().setActiveGame(null);
          }
          maniaService.getClient().getCabinetClient().update(cabinet);
        }
      }
      catch (Exception e) {
        LOG.error("Error updating mania online status: {}", e.getMessage(), e);
      }
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
        Cabinet cabinet = maniaService.getClient().getCabinetClient().getCabinetCached();
        if (cabinet != null) {
          if (cabinet.getStatus() == null) {
            cabinet.setStatus(new CabinetStatus());
          }
          cabinet.getStatus().setStatus(CabinetOnlineStatus.offline);
          cabinet.getStatus().setActiveGame(null);

          ManiaSettings settings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
          settings.setEnabled(true);
          preferencesService.savePreference(PreferenceNames.MANIA_SETTINGS, settings);

          if (settings.isShowOnlineStatus()) {
            cabinet.getStatus().setStatus(CabinetOnlineStatus.online);
          }

          maniaService.getClient().getCabinetClient().update(cabinet);
          LOG.info("Switched cabinet to modus: {}", cabinet.getStatus().getStatus());
        }

        highscoreService.addHighscoreChangeListener(tournamentsHighscoreChangeListener);
        frontendStatusService.addTableStatusChangeListener(this);
      }
      catch (Exception e) {
        Features.MANIA_ENABLED = false;
        LOG.info("Error initializing tournament service: " + e.getMessage(), e);
      }
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.iscored.IScoredService;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentsHighscoreChangeListener implements HighscoreChangeListener, PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsHighscoreChangeListener.class);

  private TournamentSettings tournamentSettings;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private IScoredService iScoredService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private TournamentSynchronizer tournamentSynchronizer;

  @Autowired
  private ManiaService maniaService;

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    if(event.getEventOrigin().equals(EventOrigin.TABLE_SCAN)) {
      LOG.info("Ignored highscore change, because of table scans are skipped.");
      return;
    }
    VPinManiaClient maniaClient = maniaService.getClient();
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();

    if (cabinet != null) {
      new Thread(() -> {
        Thread.currentThread().setName("VPin Mania Highscore ChangeListener Thread");

        Game game = event.getGame();
        Score newScore = event.getNewScore();

        try {
          TableScore newTableScore = createTableScore(game, newScore);
          Player player = newScore.getPlayer();
          if (player.getTournamentUserUuid() == null) {
            Player adminPlayer = playerService.getAdminPlayer();
            if (adminPlayer != null) {
              player = adminPlayer;
            }
          }

          Account accountByUuid = null;
          if (player.getTournamentUserUuid() != null) {
            accountByUuid = maniaClient.getAccountClient().getAccountByUuid(player.getTournamentUserUuid());
            if (accountByUuid != null) {
              newTableScore.setAccountId(accountByUuid.getId());
            }
            else {
              LOG.warn("The new highscore has not been submitted to VPin Mania, no registered player could be determined.");
              return;
            }
          }

          // the user might have selected not to submit all scores, but only tournament scores
          TableScore createdTableScore = null;
          if (tournamentSettings.isSubmitAllScores()) {
            createdTableScore = maniaClient.getHighscoreClient().submitOrUpdate(newTableScore);
            LOG.info("Submitted VPinMania score " + createdTableScore);
          }

          //sync info before submitting to possible resetted tables
          tournamentSynchronizer.synchronize();

          List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
          for (Tournament tournament : tournaments) {
            try {
              TournamentTable tournamentTable = findTournamentTable(tournament, game.getExtTableId(), game.getExtTableVersionId());
              if (tournamentTable != null) {
                if (tournament.isActive() && tournamentTable.isActive()) {
                  TableScore tournamentScore = maniaClient.getTournamentClient().submitTournamentScore(tournament, createdTableScore);
                  LOG.info("Linked " + createdTableScore + " to " + tournament);

                  if (accountByUuid != null && tournament.getDashboardUrl() != null && iScoredService.isIscoredGameRoomUrl(tournament.getDashboardUrl())) {
                    iScoredService.submitTournamentScore(tournament, tournamentTable, tournamentScore, accountByUuid);
                  }
                }
                else {
                  LOG.info("Found " + tournamentTable + ", but it is not active or the tournament " + tournament + " is already finished.");
                }
              }
            }
            catch (Exception e) {
              LOG.error("Failed to submit tournament score for " + tournament + ": " + e.getMessage(), e);
            }
          }
        }
        catch (Exception e) {
          LOG.error("Failed to submit VPin Mania highscore: " + e.getMessage(), e);
        }
      }).start();
    }
  }

  private TournamentTable findTournamentTable(Tournament tournament, String vpsTableId, String vpsTableVersionId) {
    List<TournamentTable> tournamentTables = maniaService.getClient().getTournamentClient().getTournamentTables(tournament.getId());
    return getApplicableTable(tournament, tournamentTables, vpsTableId, vpsTableVersionId);
  }

  private TournamentTable getApplicableTable(Tournament tournament, List<TournamentTable> tables, String vpsTableId, String vpsTableVersionId) {
    if (tournament.isActive()) {
      for (TournamentTable table : tables) {
        if (!String.valueOf(table.getVpsTableId()).equals(vpsTableId)) {
          continue;
        }
        if (!String.valueOf(table.getVpsVersionId()).equals(vpsTableVersionId)) {
          continue;
        }
        return table;
      }
    }
    return null;
  }

  @Nullable
  private TableScore createTableScore(Game game, Score newScore) {
    TableScore tableScore = new TableScore();
    tableScore.setVpsTableId(game.getExtTableId());
    tableScore.setVpsVersionId(game.getExtTableVersionId());
    tableScore.setScoreType(game.getHighscoreType() != null ? HighscoreType.valueOf(game.getHighscoreType().name()) : null);
    tableScore.setScore(Double.valueOf(newScore.getNumericScore()).longValue());
    tableScore.setScoreText(newScore.getScore());
    tableScore.setScoreSource(game.getRom() != null ? game.getRom() : game.getTableName());
    tableScore.setCreationDate(newScore.getCreatedAt());
    tableScore.setTableName(game.getGameDisplayName());

    if (tableScore.getScoreSource() == null && game.getHsFileName() != null) {
      tableScore.setScoreSource(game.getHsFileName());
    }
    return tableScore;
  }

  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.TOURNAMENTS_SETTINGS)) {
      this.tournamentSettings = preferencesService.getJsonPreference(PreferenceNames.TOURNAMENTS_SETTINGS, TournamentSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.TOURNAMENTS_SETTINGS, null, null);
  }
}

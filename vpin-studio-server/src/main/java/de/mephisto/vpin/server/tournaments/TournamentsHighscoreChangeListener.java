package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentsHighscoreChangeListener implements HighscoreChangeListener, PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsHighscoreChangeListener.class);

  private ManiaSettings maniaSettings;

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
  public void highscoreChanged(@NonNull HighscoreChangeEvent event) {
    if (event.getEventOrigin().equals(EventOrigin.TABLE_SCAN)) {
      LOG.info("Ignored tournament highscore change, because of table scans are skipped.");
      return;
    }

    if (event.getNewScore().getPlayer() == null) {
      LOG.info("Ignored tournament highscore change, because no player set for this score.");
      SLOG.info("Ignored tournament highscore change, because no player set for this score.");
      return;
    }

    VPinManiaClient maniaClient = maniaService.getClient();
    Cabinet cabinet = maniaService.getCabinet();

    if (cabinet != null) {
      try {
        Score newScore = event.getNewScore();
        Game game = event.getGame();

        if (maniaService.isOnDenyList(game, newScore)) {
          return;
        }

        String tournamentPlayerUuid = getTournamentUserId(newScore);
        TableScore newTableScore = createTableScore(game, newScore);
        Account accountByUuid = null;
        if (tournamentPlayerUuid != null) {
          accountByUuid = maniaClient.getAccountClient().getAccountByUuid(tournamentPlayerUuid);
          if (accountByUuid != null) {
            newTableScore.setAccountId(accountByUuid.getId());
          }
          else {
            LOG.warn("The new highscore has not been submitted to VPin Mania, no registered player could be determined.");
            SLOG.warn("The new highscore has not been submitted to VPin Mania, no registered player could be determined.");
            return;
          }
        }

        // the user might have selected not to submit all scores, but only tournament scores
        TableScore createdTableScore = null;
        if (maniaSettings.isSubmitAllScores()) {
          try {
            createdTableScore = maniaClient.getHighscoreClient().submitOrUpdate(newTableScore);
            LOG.info("Submitted VPinMania score " + createdTableScore);
            SLOG.info("Submitted VPinMania score " + createdTableScore);
          }
          catch (Exception e) {
            LOG.warn("Mania score submission failed: " + e.getMessage());
            SLOG.warn("Mania score submission failed: " + e.getMessage());
          }
        }

        if (createdTableScore != null && maniaSettings.isTournamentsEnabled()) {
          List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
          for (Tournament tournament : tournaments) {
            try {
              TournamentTable tournamentTable = findTournamentTable(tournament, game.getExtTableId(), game.getExtTableVersionId());
              if (tournamentTable != null) {
                if (tournament.isActive() && tournamentTable.isActive()) {
                  List<TableScoreDetails> tournamentScores = maniaClient.getHighscoreClient().getTournamentScores(tournament.getId());
                  if (tournamentScores.contains(createdTableScore)) {
                    LOG.warn("Skipped reporting duplicated tournament score \"" + createdTableScore + "\"");
                  }
                  else {
                    maniaClient.getTournamentClient().submitTournamentScore(tournament, createdTableScore);
                    LOG.info("Linked " + createdTableScore + " to " + tournament);
                  }

                  if (accountByUuid != null) {
                    iScoredService.submitTournamentScore(tournament, tournamentTable, createdTableScore, accountByUuid);
                  }
                }
                else {
                  LOG.info("Found " + tournamentTable + ", but it is not active or the tournament " + tournament + " is already finished.");
                }
              }
            }
            catch (Exception e) {
              LOG.error("Failed to submit tournament score for " + tournament + ": " + e.getMessage(), e);
              SLOG.error("Failed to submit tournament score for " + tournament + ": " + e.getMessage());
            }
          }
        }
      }
      catch (Exception e) {
        LOG.error("Failed to submit VPin Mania highscore: " + e.getMessage(), e);
        SLOG.error("Failed to submit VPin Mania highscore: " + e.getMessage());
      }
    }
  }

  private String getTournamentUserId(Score newScore) {
    Player player = newScore.getPlayer();
    if (player == null || player.getTournamentUserUuid() == null) {
      LOG.info("");
      Player adminPlayer = playerService.getAdminPlayer();
      if (adminPlayer != null) {
        player = adminPlayer;
      }
    }
    if (player != null) {
      return player.getTournamentUserUuid();
    }
    return null;
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
    tableScore.setScore(newScore.getScore());
    tableScore.setScoreText(newScore.getFormattedScore());
    tableScore.setScoreSource(game.getRom() != null ? game.getRom() : game.getTableName());
    tableScore.setCreationDate(newScore.getCreatedAt());
    tableScore.setTableName(game.getGameDisplayName());

    if (tableScore.getScoreSource() == null && game.getHsFileName() != null) {
      tableScore.setScoreSource(game.getHsFileName());
    }
    return tableScore;
  }

  @Override
  public void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore) {

  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.MANIA_SETTINGS)) {
      this.maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.MANIA_SETTINGS, null, null);
  }
}

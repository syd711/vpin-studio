package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.commons.fx.Features;
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

  private VPinManiaClient maniaClient;
  private Cabinet cabinet;

  private TournamentSettings tournamentSettings;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private IScoredService iScoredService;

  @Autowired
  private PreferencesService preferencesService;

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    if (cabinet != null) {
      new Thread(() -> {
        Thread.currentThread().setName("VPin Mania Highscore ChangeListener Thread");

        Game game = event.getGame();
        Score newScore = event.getNewScore();

        try {
          TableScore newTableScore = createTableScore(game, newScore);
          if (newTableScore != null) {
            // General score submission!
            TableScore createdTableScore = maniaClient.getHighscoreClient().submit(newTableScore);
            LOG.info("Submitted VPinMania score " + createdTableScore);

            List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
            for (Tournament tournament : tournaments) {
              TournamentTable tournamentTable = findTournamentTable(tournament, createdTableScore);
              if (tournamentTable != null) {
                if (tournament.isActive()) {
                  maniaClient.getTournamentClient().addScore(tournament, createdTableScore);
                  LOG.info("Linked " + createdTableScore + " to " + tournament);

                  if (Features.ISCORED_ENABLED && tournament.getDashboardUrl() != null && iScoredService.isIscoredGameRoomUrl(tournament.getDashboardUrl())) {
                    iScoredService.submitTableScore(tournament, tournamentTable, createdTableScore);
                  }
                }
                else {
                  LOG.info("Found " + tournamentTable + ", but it is not active.");
                }
              }
            }
          }
          else {
            LOG.info("Cancelled VPin Mania score submission.");
          }
        } catch (Exception e) {
          LOG.error("Failed to submit VPin Mania highscore: " + e.getMessage(), e);
        }
      }).start();
    }
  }

  private TournamentTable findTournamentTable(Tournament tournament, TableScore tableScore) {
    List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());
    return getApplicableTable(tournament, tournamentTables, tableScore);
  }

  private TournamentTable getApplicableTable(Tournament tournament, List<TournamentTable> tables, TableScore score) {
    if (tournament.isActive()) {
      for (TournamentTable table : tables) {
        if (!String.valueOf(table.getVpsTableId()).equals(score.getVpsTableId())) {
          continue;
        }
        if (!String.valueOf(table.getVpsVersionId()).equals(score.getVpsVersionId())) {
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
    tableScore.setScoreType(HighscoreType.valueOf(game.getHighscoreType().name()));
    tableScore.setScore(Double.valueOf(newScore.getNumericScore()).longValue());
    tableScore.setScoreText(newScore.getScore());
    tableScore.setScoreSource(game.getRom() != null ? game.getRom() : game.getTableName());
    tableScore.setCreationDate(newScore.getCreatedAt());
    tableScore.setCabinetId(cabinet.getId());
    tableScore.setTableName(game.getGameDisplayName());

    if (tableScore.getScoreSource() == null && game.getHsFileName() != null) {
      tableScore.setScoreSource(game.getHsFileName());
    }

    Player player = newScore.getPlayer();
    if (player.getTournamentUserUuid() == null) {
      Player adminPlayer = playerService.getAdminPlayer();
      if (adminPlayer != null) {
        player = adminPlayer;
      }
    }

    if (player.getTournamentUserUuid() != null) {
      Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(player.getTournamentUserUuid());
      if (accountByUuid != null) {
        tableScore.setPlayerName(accountByUuid.getDisplayName());
        tableScore.setPlayerInitials(accountByUuid.getInitials());
        tableScore.setAccountId(accountByUuid.getId());
        return tableScore;
      }
    }

    LOG.warn("The new highscore has not been submitted to VPin Mania, no registered player could be determined.");
    return null;
  }

  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  public void setCabinet(Cabinet cabinet) {
    this.cabinet = cabinet;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.TOURNAMENTS_SETTINGS)) {
      this.tournamentSettings = preferencesService.getJsonPreference(PreferenceNames.TOURNAMENTS_SETTINGS, TournamentSettings.class);
    }
  }

  public void setVPinManiaClient(VPinManiaClient maniaClient) {
    this.maniaClient = maniaClient;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.maniaClient = null;
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.TOURNAMENTS_SETTINGS, null, null);
  }
}

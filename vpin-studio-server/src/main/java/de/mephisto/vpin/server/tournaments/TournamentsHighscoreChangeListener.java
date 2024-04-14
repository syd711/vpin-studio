package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.iscored.IScoredService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TournamentsHighscoreChangeListener implements HighscoreChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsHighscoreChangeListener.class);

  private final VPinManiaClient maniaClient;
  private final PlayerService playerService;
  private final IScoredService iScoredService;
  private Cabinet cabinet;

  public TournamentsHighscoreChangeListener(VPinManiaClient maniaClient, PlayerService playerService, IScoredService iScoredService) {
    this.maniaClient = maniaClient;
    this.playerService = playerService;
    this.iScoredService = iScoredService;
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    if (cabinet != null) {
      new Thread(() -> {
        Thread.currentThread().setName("VPin Mania Highscore ChangeListener Thread");

        Game game = event.getGame();
        Score newScore = event.getNewScore();

        try {
          TableScore tableScore = createTableScore(game, newScore);
          if (tableScore != null) {
            TableScore submit = maniaClient.getHighscoreClient().submit(tableScore);
            LOG.info("Submitted VPinMania score " + submit);

            List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
            for (Tournament tournament : tournaments) {
              List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());
              TournamentTable applicableTable = getApplicableTable(tournament, tournamentTables, tableScore);
              if (applicableTable != null) {
                maniaClient.getTournamentClient().addScore(tournament, submit);
                LOG.info("Linked " + submit + " to " + tournament);

                if (tournament.getDashboardUrl() != null) {
                  iScoredService.submitTableScore(tournament, applicableTable, tableScore);
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
}

package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.HighscoreType;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TournamentsHighscoreChangeListener implements HighscoreChangeListener {

  private final VPinManiaClient maniaClient;
  private Cabinet cabinet;

  public TournamentsHighscoreChangeListener(VPinManiaClient maniaClient) {
    this.maniaClient = maniaClient;
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    if (cabinet != null) {
      new Thread(() -> {
        Thread.currentThread().setName("VPin Mania Highscore ChangeListener Thread");

        Game game = event.getGame();

        TableScore tableScore = new TableScore();
        tableScore.setVpsTableId(game.getExtTableId());
        tableScore.setVpsVersionId(game.getExtTableVersionId());
        tableScore.setScoreType(HighscoreType.valueOf(game.getHighscoreType().name()));

        List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
        for (Tournament tournament : tournaments) {

        }


      }).start();
    }
  }

  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  public void setCabinet(Cabinet cabinet) {
    this.cabinet = cabinet;
  }
}

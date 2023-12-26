package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import org.apache.commons.lang3.StringUtils;

public class TournamentTreeModel {
  private final ManiaTournamentRepresentation tournament;
  private final VpsTableVersion vpsTableVersion;
  private final VpsTable vpsTable;
  private final GameRepresentation game;

  private boolean highscoreAvailable;

  public TournamentTreeModel(ManiaTournamentRepresentation tournament, GameRepresentation game, VpsTable vpsTable, VpsTableVersion vpsTableVersion) {
    this.tournament = tournament;
    this.vpsTable = vpsTable;
    this.vpsTableVersion = vpsTableVersion;
    this.game = game;

    if (game != null) {
      ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
      highscoreAvailable = !StringUtils.isEmpty(summary.getRaw());
    }
  }

  public GameRepresentation getGame() {
    return game;
  }

  public boolean isValid() {
    return game != null && vpsTable != null && vpsTableVersion != null && isHighscoreAvailable();
  }

  public boolean isHighscoreAvailable() {
    return highscoreAvailable;
  }

  public VpsTable getVpsTable() {
    return vpsTable;
  }

  public ManiaTournamentRepresentation getTournament() {
    return tournament;
  }

  public VpsTableVersion getVpsTableVersion() {
    return vpsTableVersion;
  }
}


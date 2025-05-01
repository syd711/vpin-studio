package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentTreeModel {
  private Tournament tournament;
  private TournamentTable tournamentTable;
  private VpsTableVersion vpsTableVersion;
  private VpsTable vpsTable;
  private GameRepresentation game;

  private boolean highscoreAvailable;

  public static void expandTreeView(TreeItem<?> item) {
    if (item != null && !item.isLeaf()) {
      item.setExpanded(true);
      for (TreeItem<?> child : item.getChildren()) {
        expandTreeView(child);
      }
    }
  }

  public static TreeItem<TournamentTreeModel> create(Tournament tournament, List<TournamentTable> tournamentTables) {
    TreeItem<TournamentTreeModel> tournamentNode = new TreeItem<>(new TournamentTreeModel(tournament, null, null, null, null));
    for (TournamentTable tournamentTable : tournamentTables) {
      VpsTable table = client.getVpsService().getTableById(tournamentTable.getVpsTableId());
      VpsTableVersion version = null;
      GameRepresentation game = null;
      if (!StringUtils.isEmpty(tournamentTable.getVpsVersionId())) {
        version = table.getTableVersionById(tournamentTable.getVpsVersionId());
        game = client.getGameService().getGameByVpsTable(table, version);
      }

      TournamentTreeModel model = new TournamentTreeModel(tournament, game, tournamentTable, table, version);
      tournamentNode.getChildren().add(new TreeItem<>(model));
    }
    return tournamentNode;
  }

  public TournamentTreeModel(@Nullable Tournament tournament, @Nullable GameRepresentation game, @Nullable TournamentTable tournamentTable, VpsTable vpsTable, VpsTableVersion vpsTableVersion) {
    this.tournament = tournament;
    this.tournamentTable = tournamentTable;
    this.vpsTable = vpsTable;
    this.vpsTableVersion = vpsTableVersion;
    this.game = game;

    if (game != null) {
      ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
      highscoreAvailable = !StringUtils.isEmpty(summary.getRaw());
    }
  }

  public void setTournament(Tournament tournament) {
    this.tournament = tournament;
  }

  public void setTournamentTable(TournamentTable tournamentTable) {
    this.tournamentTable = tournamentTable;
  }

  public void setVpsTableVersion(VpsTableVersion vpsTableVersion) {
    this.vpsTableVersion = vpsTableVersion;
  }

  public void setVpsTable(VpsTable vpsTable) {
    this.vpsTable = vpsTable;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    if (game != null) {
      ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
      highscoreAvailable = !StringUtils.isEmpty(summary.getRaw());
    }
  }

  public TournamentTable getTournamentTable() {
    return tournamentTable;
  }

  public boolean isTournamentNode() {
    return this.tournamentTable == null;
  }

  public GameRepresentation getGame() {
    return game;
  }

  public boolean isValid() {
    return game != null && vpsTable != null && isHighscoreAvailable();
  }

  public boolean isHighscoreAvailable() {
    return highscoreAvailable;
  }

  public VpsTable getVpsTable() {
    return vpsTable;
  }

  public Tournament getTournament() {
    return tournament;
  }

  public VpsTableVersion getVpsTableVersion() {
    return vpsTableVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    TournamentTreeModel that = (TournamentTreeModel) o;
    return Objects.equals(vpsTableVersion, that.vpsTableVersion) && Objects.equals(vpsTable, that.vpsTable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vpsTableVersion, vpsTable);
  }
}


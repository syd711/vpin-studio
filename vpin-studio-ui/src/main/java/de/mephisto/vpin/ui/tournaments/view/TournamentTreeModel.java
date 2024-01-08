package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentTreeModel {
  private final ManiaTournamentRepresentation tournament;
  private final VpsTableVersion vpsTableVersion;
  private final VpsTable vpsTable;
  private final GameRepresentation game;

  private boolean highscoreAvailable;

  public static void expandTreeView(TreeItem<?> item){
    if(item != null && !item.isLeaf()){
      item.setExpanded(true);
      for(TreeItem<?> child:item.getChildren()){
        expandTreeView(child);
      }
    }
  }

  public static TreeItem<TournamentTreeModel> create(ManiaTournamentRepresentation tournament) {
    TreeItem<TournamentTreeModel> tournamentNode = new TreeItem<>(new TournamentTreeModel(tournament, null, null, null));
    List<String> tableIdList = tournament.getTableIdList();
    for (String s : tableIdList) {
      VpsTableVersion version = null;
      VpsTable table = null;
      GameRepresentation game = null;
      String[] split = s.split("#");
      if (split.length > 0) {
        String tableId = split[0];
        table = VPS.getInstance().getTableById(tableId);
        if (table != null && split.length == 2) {
          String versionId = split[1];
          if(!StringUtils.isEmpty(versionId)) {
            version = table.getVersion(versionId);
          }
          game = client.getGameService().getGameByVpsTable(table, version);
        }
      }

      TournamentTreeModel model = new TournamentTreeModel(tournament, game, table, version);
      tournamentNode.getChildren().add(new TreeItem<>(model));
    }
    return tournamentNode;
  }

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
    return game != null && vpsTable != null && isHighscoreAvailable();
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


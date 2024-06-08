package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import javafx.scene.control.TreeItem;

public class TournamentCreationModel {
  private TreeItem<TournamentTreeModel> newTournamentModel;
  private String badge;
  private boolean resetHighscore;

  public TreeItem<TournamentTreeModel> getNewTournamentModel() {
    return newTournamentModel;
  }

  public void setNewTournamentModel(TreeItem<TournamentTreeModel> newTournamentModel) {
    this.newTournamentModel = newTournamentModel;
  }

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
  }

  public boolean isResetHighscore() {
    return resetHighscore;
  }

  public void setResetHighscore(boolean resetHighscore) {
    this.resetHighscore = resetHighscore;
  }
}

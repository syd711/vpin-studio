package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentUpdateProgressModel extends ProgressModel<TreeItem<TournamentTreeModel>> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final List<TreeItem<TournamentTreeModel>> tournaments;
  private Tournament updatedTournament;

  private final Iterator<TreeItem<TournamentTreeModel>> iterator;

  public TournamentUpdateProgressModel(TreeItem<TournamentTreeModel> tournamentModel) {
    super("Updating Tournament \"" + tournamentModel.getValue().getTournament().getDisplayName() + "\"");
    this.tournaments = new ArrayList<>(Arrays.asList(tournamentModel));
    this.tournaments.addAll(tournamentModel.getChildren());
    this.iterator = this.tournaments.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public int getMax() {
    return tournaments.size();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public TreeItem<TournamentTreeModel> getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(TreeItem<TournamentTreeModel> item) {
    if (item.getValue().isTournamentNode()) {
      return "Saving Tournament \"" + item.getValue().getTournament().getDisplayName() + "\"";
    }
    return "Updating \"" + item.getValue().getVpsTable().getDisplayName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TreeItem<TournamentTreeModel> next) {
    try {
      if (next.getValue().isTournamentNode()) {
        updatedTournament = maniaClient.getTournamentClient().update(next.getValue().getTournament());
        List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(updatedTournament.getId());
        for (TournamentTable tournamentTable : tournamentTables) {
          maniaClient.getTournamentClient().removeTable(tournamentTable);
          LOG.info("Removed " + tournamentTable);
        }
        progressResultModel.getResults().add(updatedTournament);
      }
      else {
        TournamentTable tournamentTable = next.getValue().getTournamentTable();
        tournamentTable.setTournamentId(updatedTournament.getId());
        maniaClient.getTournamentClient().addTable(tournamentTable);
        LOG.info("(Re)added " + tournamentTable);
      }
    } catch (Exception e) {
      LOG.error("Error creating tournament: " + e.getMessage(), e);
      progressResultModel.getResults().add(e.getMessage());
    }
  }
}

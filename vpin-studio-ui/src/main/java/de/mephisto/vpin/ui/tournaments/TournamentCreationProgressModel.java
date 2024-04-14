package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentCreationProgressModel extends ProgressModel<TreeItem<TournamentTreeModel>> {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentCreationProgressModel.class);
  private final List<TreeItem<TournamentTreeModel>> tournaments;
  private final Account account;
  private final BufferedImage badge;
  private Tournament newTournament;

  private final Iterator<TreeItem<TournamentTreeModel>> iterator;

  public TournamentCreationProgressModel(TreeItem<TournamentTreeModel> tournamentModel, Account account, BufferedImage badge) {
    super("Creating Tournament \"" + tournamentModel.getValue().getTournament().getDisplayName() + "\"");
    this.tournaments = new ArrayList<>(Arrays.asList(tournamentModel));
    this.tournaments.addAll(tournamentModel.getChildren());
    this.account = account;
    this.badge = badge;
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
    if(item.getValue().isTournamentNode()) {
      return "Saving Tournament \"" + item.getValue().getTournament().getDisplayName() + "\"";
    }
    return "Adding \"" + item.getValue().getVpsTable().getDisplayName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TreeItem<TournamentTreeModel> next) {
    try {
      if(next.getValue().isTournamentNode()) {
        newTournament = maniaClient.getTournamentClient().create(next.getValue().getTournament(), account, badge);
        progressResultModel.getResults().add(newTournament);
      }
      else {
        TournamentTable tournamentTable = next.getValue().getTournamentTable();
        tournamentTable.setTournamentId(newTournament.getId());
        maniaClient.getTournamentClient().addTable(tournamentTable);
      }
    } catch (Exception e) {
      LOG.error("Error creating tournament: " + e.getMessage(), e);
      progressResultModel.getResults().add(e.getMessage());
    }
  }
}

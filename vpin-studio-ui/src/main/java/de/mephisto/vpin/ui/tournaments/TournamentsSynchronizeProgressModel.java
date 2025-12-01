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

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentsSynchronizeProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final List<String> values;

  private final Iterator<String> iterator;

  public TournamentsSynchronizeProgressModel() {
    super("Synchronizing Tournaments");
    this.values = new ArrayList<>(Arrays.asList(""));
    this.iterator = this.values.iterator();
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
    return this.values.size();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(String item) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String next) {
    try {
      TreeItem<TournamentTreeModel> tournamentTreeModelTreeItem = loadTreeModel();
      client.getTournamentsService().synchronize();
      progressResultModel.getResults().add(tournamentTreeModelTreeItem);
    }
    catch (Exception e) {
      LOG.error("Error synchronizing tournaments: " + e.getMessage(), e);
    }
  }



  private TreeItem<TournamentTreeModel> loadTreeModel() {
    List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
    LOG.info("Loaded " + tournaments.size() + " tournaments.");
    TreeItem<TournamentTreeModel> root = new TreeItem<>(new TournamentTreeModel(null, null, null, null, null));
    for (Tournament tournament : tournaments) {
      List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());
      TreeItem<TournamentTreeModel> treeModel = TournamentTreeModel.create(tournament, tournamentTables);

      root.getChildren().add(treeModel);
    }
    root.setExpanded(true);
    return root;
  }
}

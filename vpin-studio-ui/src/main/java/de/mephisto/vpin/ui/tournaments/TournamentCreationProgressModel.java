package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.*;

public class TournamentCreationProgressModel extends ProgressModel<TreeItem<TournamentTreeModel>> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final List<TreeItem<TournamentTreeModel>> tournaments;
  private final TournamentCreationModel tournamentModel;
  private final Account account;
  private final BufferedImage badge;
  private Tournament newTournament;

  private final Iterator<TreeItem<TournamentTreeModel>> iterator;

  public TournamentCreationProgressModel(TournamentCreationModel tournamentModel, Account account, BufferedImage badge) {
    super("Creating Tournament \"" + tournamentModel.getNewTournamentModel().getValue().getTournament().getDisplayName() + "\"");
    this.tournaments = new ArrayList<>(Arrays.asList(tournamentModel.getNewTournamentModel()));
    this.tournamentModel = tournamentModel;
    this.tournaments.addAll(tournamentModel.getNewTournamentModel().getChildren());
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
    if (item.getValue().isTournamentNode()) {
      return "Saving Tournament \"" + item.getValue().getTournament().getDisplayName() + "\"";
    }
    return "Adding \"" + item.getValue().getVpsTable().getDisplayName() + "\"";
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    try {
      if (newTournament != null) {
        TournamentMetaData metaData = new TournamentMetaData();
        metaData.setResetHighscores(tournamentModel.isResetHighscore());
        metaData.setBadge(tournamentModel.getBadge());
        metaData.setTournamentId(newTournament.getId());
        client.getTournamentsService().synchronize(metaData);
      }
    } catch (Exception e) {
      LOG.error("Failed to synchronize new tournament: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(stage, "Error", "Failed to synchronize new tournament: " + e.getMessage());
      });
    }
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TreeItem<TournamentTreeModel> next) {
    try {
      if (next.getValue().isTournamentNode()) {
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

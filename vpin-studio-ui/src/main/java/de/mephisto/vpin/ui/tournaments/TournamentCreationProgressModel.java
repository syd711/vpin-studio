package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentCreationProgressModel extends ProgressModel<ManiaTournamentRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentCreationProgressModel.class);
  private List<ManiaTournamentRepresentation> tournaments;
  private final File badgeFile;

  private Iterator<ManiaTournamentRepresentation> iterator;

  public TournamentCreationProgressModel(ManiaTournamentRepresentation tournamentRepresentation, File badgeFile) {
    super("Creating Tournament \"" + tournamentRepresentation.getDisplayName() + "\"");
    this.tournaments = Arrays.asList(tournamentRepresentation);
    this.badgeFile = badgeFile;
    this.iterator = this.tournaments.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
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
  public ManiaTournamentRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(ManiaTournamentRepresentation game) {
    return "Saving Tournament";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ManiaTournamentRepresentation next) {
    try {
      ManiaTournamentRepresentation maniaTournamentRepresentation = maniaClient.getTournamentClient().create(next, badgeFile);
      progressResultModel.getResults().add(maniaTournamentRepresentation);
    } catch (Exception e) {
      LOG.error("Error creating tournament: " + e.getMessage(), e);
    }
  }
}

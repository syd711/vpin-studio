package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentCreationProgressModel extends ProgressModel<ManiaTournamentRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentCreationProgressModel.class);
  private List<ManiaTournamentRepresentation> tournaments;
  private final ManiaAccountRepresentation account;
  private final byte[] badge;
  private File badgeFile;

  private Iterator<ManiaTournamentRepresentation> iterator;

  public TournamentCreationProgressModel(ManiaTournamentRepresentation tournamentRepresentation, ManiaAccountRepresentation account, byte[] badge) {
    super("Creating Tournament \"" + tournamentRepresentation.getDisplayName() + "\"");
    this.tournaments = Arrays.asList(tournamentRepresentation);
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
  public ManiaTournamentRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    this.badgeFile.delete();
  }

  @Override
  public String nextToString(ManiaTournamentRepresentation game) {
    return "Saving Tournament";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ManiaTournamentRepresentation next) {
    try {
      badgeFile = File.createTempFile("avatar", ".png");
      badgeFile.deleteOnExit();

      FileOutputStream out = new FileOutputStream(badgeFile);
      IOUtils.write(badge, out);
      out.close();

      ManiaTournamentRepresentation maniaTournamentRepresentation = maniaClient.getTournamentClient().create(next, account, badgeFile);
      progressResultModel.getResults().add(maniaTournamentRepresentation);
    } catch (Exception e) {
      LOG.error("Error creating tournament: " + e.getMessage(), e);
    }
  }
}

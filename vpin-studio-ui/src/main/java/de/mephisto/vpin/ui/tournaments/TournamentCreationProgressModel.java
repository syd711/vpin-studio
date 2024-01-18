package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Tournament;
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

public class TournamentCreationProgressModel extends ProgressModel<Tournament> {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentCreationProgressModel.class);
  private List<Tournament> tournaments;
  private final Account account;
  private final byte[] badge;
  private File badgeFile;

  private Iterator<Tournament> iterator;

  public TournamentCreationProgressModel(Tournament tournament, Account account, byte[] badge) {
    super("Creating Tournament \"" + tournament.getDisplayName() + "\"");
    this.tournaments = Arrays.asList(tournament);
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
  public Tournament getNext() {
    return iterator.next();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    this.badgeFile.delete();
  }

  @Override
  public String nextToString(Tournament game) {
    return "Saving Tournament";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Tournament next) {
    try {
      badgeFile = File.createTempFile("avatar", ".png");
      badgeFile.deleteOnExit();

      FileOutputStream out = new FileOutputStream(badgeFile);
      IOUtils.write(badge, out);
      out.close();

      Tournament tournament = maniaClient.getTournamentClient().create(next, account, badgeFile);
      progressResultModel.getResults().add(tournament);
    } catch (Exception e) {
      LOG.error("Error creating tournament: " + e.getMessage(), e);
      progressResultModel.getResults().add(e.getMessage());
    }
  }
}

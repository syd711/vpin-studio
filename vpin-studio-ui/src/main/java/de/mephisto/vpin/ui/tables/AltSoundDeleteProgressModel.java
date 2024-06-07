package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AltSoundDeleteProgressModel extends ProgressModel<Integer> {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundDeleteProgressModel.class);
  private final List<Integer> games;

  private final Iterator<Integer> gameIterator;
  private final GameRepresentation gameRepresentation;

  public AltSoundDeleteProgressModel(GameRepresentation gameRepresentation) {
    super("ALTSound Deletion");
    this.gameRepresentation = gameRepresentation;
    this.games = Arrays.asList(gameRepresentation.getId());
    this.gameIterator = games.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean hasNext() {
    return this.gameIterator.hasNext();
  }

  @Override
  public Integer getNext() {
    return gameIterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return this.games.size() == 1;
  }

  @Override
  public String nextToString(Integer game) {
    return "Deleting ALTSound Package";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Integer gameId) {
    try {
      Studio.client.getAltSoundService().delete(gameId);
      EventManager.getInstance().notifyTableChange(gameId, gameRepresentation.getRom());
    }
    catch (Exception e) {
      LOG.error("Error during dismissal: " + e.getMessage(), e);
    }
  }
}

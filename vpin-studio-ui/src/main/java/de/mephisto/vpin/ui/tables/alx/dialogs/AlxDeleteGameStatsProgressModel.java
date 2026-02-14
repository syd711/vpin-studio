package de.mephisto.vpin.ui.tables.alx.dialogs;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxDeleteGameStatsProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<GameRepresentation> iterator;
  private final GameRepresentation gameRepresentation;
  private final boolean deleteTime;
  private final boolean deletePlays;
  private final boolean deleteScores;
  private List<GameRepresentation> deletions = new ArrayList<>();

  public AlxDeleteGameStatsProgressModel(String title, GameRepresentation gameRepresentation, boolean deleteTime, boolean deletePlays, boolean deleteScores) {
    super(title);
    this.gameRepresentation = gameRepresentation;
    this.deleteTime = deleteTime;
    this.deletePlays = deletePlays;
    this.deleteScores = deleteScores;
    this.deletions.add(gameRepresentation);
    this.iterator = this.deletions.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public GameRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      if (deleteTime) {
        client.getAlxService().deleteNumberPlaysForGame(game.getId());
      }
      if (deletePlays) {
        client.getAlxService().deleteNumberPlaysForGame(game.getId());
      }
//      if (deleteScores) {
//        DeleteDescriptor descriptor = new DeleteDescriptor();
//        descriptor.setDeleteHighscores(true);
//        descriptor.setGameIds(Arrays.asList(game.getId()));
//        client.getGameService().deleteGame(descriptor, game);
//      }
      EventManager.getInstance().notifyAlxUpdate(gameRepresentation);
    }
    catch (Exception e) {
      LOG.error("ALX deletion failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

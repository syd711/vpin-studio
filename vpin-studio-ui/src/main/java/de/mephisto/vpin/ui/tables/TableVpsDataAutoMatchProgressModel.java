package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.MAX_REFRESH_COUNT;

public class TableVpsDataAutoMatchProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<GameRepresentation> games;

  private final boolean overwrite;
  private final boolean simulate;
  private final Iterator<GameRepresentation> gameIterator;

  public TableVpsDataAutoMatchProgressModel(List<GameRepresentation> games, boolean overwrite, boolean simulate) {
    super("Auto-Matching");
    this.overwrite = overwrite;
    this.simulate = simulate;
    this.games = games;
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
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);

    for (GameRepresentation game : games) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      Studio.client.getFrontendService().autoMatch(game.getId(), overwrite, simulate);
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("Error auto-matching table data: " + e.getMessage(), e);
    }
  }
}

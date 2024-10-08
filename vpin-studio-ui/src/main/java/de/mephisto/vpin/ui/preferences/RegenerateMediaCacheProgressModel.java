package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class RegenerateMediaCacheProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(RegenerateMediaCacheProgressModel.class);
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;

  public RegenerateMediaCacheProgressModel(List<GameRepresentation> games) {
    super("Regenerating Media Cache");
    this.games = games;
    this.gameIterator = games.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    EventManager.getInstance().notifyTablesChanged();
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
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      Studio.client.getAssetService().deleteGameAssets(game.getId());
      Studio.client.getBackglassServiceClient().getDefaultPicture(game);
    }
    catch (Exception e) {
      LOG.error("Error re-renerating game asset: " + e.getMessage(), e);
    }
  }
}

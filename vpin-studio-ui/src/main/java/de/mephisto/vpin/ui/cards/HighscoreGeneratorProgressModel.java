package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class HighscoreGeneratorProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreGeneratorProgressModel.class);
  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> gameInfos;

  private final VPinStudioClient client;

  public HighscoreGeneratorProgressModel(VPinStudioClient client, String title) {
    super(title);
    this.client = client;
    this.gameInfos = client.getGames();
    iterator = gameInfos.iterator();
  }

  @Override
  public int getMax() {
    return gameInfos.size();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  public String processNext(ProgressResultModel progressResultModel) {
    try {
      GameRepresentation game = iterator.next();
      boolean result = client.generateHighscoreCard(game);
      if (result) {
        progressResultModel.addProcessed();
      }
      else {
        progressResultModel.addSkipped();
      }
      return game.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}

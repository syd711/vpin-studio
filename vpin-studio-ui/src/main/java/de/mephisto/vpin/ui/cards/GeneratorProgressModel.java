package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class GeneratorProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(GeneratorProgressModel.class);
  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> gameInfos;

  private final VPinStudioClient client;

  public GeneratorProgressModel(VPinStudioClient client, String screen, String title) {
    super(title);
    this.client = client;
    gameInfos = new VPinStudioClient().getGames();
    iterator = gameInfos.iterator();
  }

  @Override
  public int getMax() {
    return gameInfos.size();
  }

  @Override
  public Iterator getIterator() {
    return iterator;
  }

  public String processNext(ProgressResultModel progressResultModel) {
    try {
      GameRepresentation game = iterator.next();
      client.getHighscoreCard(game);
      progressResultModel.addProcessed();
      return game.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
    return null;
  }
}

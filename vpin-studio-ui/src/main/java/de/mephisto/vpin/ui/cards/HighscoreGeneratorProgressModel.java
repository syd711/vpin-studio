package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class HighscoreGeneratorProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreGeneratorProgressModel.class);
  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> gameInfos;

  private final VPinStudioClient client;

  public HighscoreGeneratorProgressModel(VPinStudioClient client, String title) {
    super(title);
    this.client = client;
    this.gameInfos = client.getGameService().getGamesCached().stream().filter(g -> g.getHighscoreType() != null ).collect(Collectors.toList());
    iterator = gameInfos.iterator();
  }

  @Override
  public int getMax() {
    return gameInfos.size();
  }

  @Override
  public GameRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      boolean result = client.getHighscoreCardsService().generateHighscoreCard(game);
      if (result) {
        progressResultModel.addProcessed();
      }
      else {
        progressResultModel.addSkipped();
      }
    } catch (Exception e) {
      LOG.error("Generate card error: " + e.getMessage(), e);
    }
  }
}

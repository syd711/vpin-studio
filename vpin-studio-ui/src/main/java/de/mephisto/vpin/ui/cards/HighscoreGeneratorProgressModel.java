package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HighscoreGeneratorProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreGeneratorProgressModel.class);
  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> gameInfos;
  private final CardTemplateType templateType;

  private final VPinStudioClient client;

  public HighscoreGeneratorProgressModel(VPinStudioClient client, String title, CardTemplateType templateType) {
    super(title);
    this.client = client;
    this.gameInfos = client.getGameService().getVpxGamesCached();
    iterator = gameInfos.iterator();
    this.templateType = templateType;
  }

  public HighscoreGeneratorProgressModel(VPinStudioClient client, String title, GameRepresentation game, CardTemplateType templateType) {
    super(title);
    this.client = client;
    this.gameInfos = Arrays.asList(game);
    iterator = gameInfos.iterator();
    this.templateType = templateType;
  }

  @Override
  public int getMax() {
    return gameInfos.size();
  }

  @Override
  public boolean isIndeterminate() {
    return this.gameInfos.size() == 1;
  }

  @Override
  public GameRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isShowSummary() {
    return false;
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
      boolean result = client.getHighscoreCardsService().generateHighscoreCard(game, templateType);
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

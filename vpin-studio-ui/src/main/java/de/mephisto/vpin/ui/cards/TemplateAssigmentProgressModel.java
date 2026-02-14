package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TemplateAssigmentProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TemplateAssigmentProgressModel.class);
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final CardTemplate cardTemplate;
  private final boolean switchToCustom;
  private final CardTemplateType templateType;

  /**
   * BaseTemplate is used when CardTemplate is null, the template use 
   */
  public TemplateAssigmentProgressModel(List<GameRepresentation> games, @Nullable CardTemplate cardTemplate, boolean switchToCustom, CardTemplateType templateType) {
    super("Applying Template");
    this.games = games;
    this.gameIterator = games.iterator();
    this.cardTemplate = cardTemplate;
    this.switchToCustom = switchToCustom;
    this.templateType = templateType;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return games.size() == 1;
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
      client.getHighscoreCardTemplatesClient().assignTemplate(game, cardTemplate.getId(), switchToCustom, templateType);
      client.getHighscoreCardTemplatesClient().getTemplates();
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
    catch (Exception e) {
      LOG.error("Failed to save template mapping: {}", e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save template mapping: " + e.getMessage());
      });
    }
  }
}

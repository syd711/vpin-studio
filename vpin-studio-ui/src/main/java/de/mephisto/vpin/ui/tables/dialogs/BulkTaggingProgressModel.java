package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.MAX_REFRESH_COUNT;
import static de.mephisto.vpin.ui.Studio.client;

public class BulkTaggingProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(BulkTaggingProgressModel.class);
  private List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final List<String> tags;

  public BulkTaggingProgressModel(List<GameRepresentation> games, List<String> tags) {
    super("Tagging Tables");
    this.games = games;
    this.gameIterator = games.iterator();
    this.tags = tags;
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
    return "Tagging \"" + game.getGameDisplayName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      TableDetails tableDetails = client.getFrontendService().getTableDetails(game.getId());
      List<String> tableTags = TaggingUtil.getTags(tableDetails.getTags());

      for (String tag : tags) {
        if (!tableTags.contains(tag)) {
          tableTags.add(tag);
        }
      }
      tableDetails.setTags(String.join(",", tableTags));
      client.getFrontendService().saveTableDetails(tableDetails, game.getId());
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
    catch (Exception e) {
      LOG.error("Failed to tag table: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to tag table: " + e.getMessage());
      });
    }
  }
}

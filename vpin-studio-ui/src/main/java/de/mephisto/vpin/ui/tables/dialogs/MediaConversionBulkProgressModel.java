package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.converter.MediaOperationResult;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaConversionBulkProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> games;
  private final VPinScreen screen;
  private final MediaConversionCommand command;

  public MediaConversionBulkProgressModel(String title, List<GameRepresentation> games, VPinScreen screen, MediaConversionCommand command) {
    super(title);
    this.games = games;
    this.screen = screen;
    this.command = command;
    this.iterator = games.iterator();
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
  public String nextToString(GameRepresentation item) {
    return "Converting assets for \"" + item.getGameDisplayName() + "\"";
  }

  @Override
  public boolean isIndeterminate() {
    return this.games.size() == 1;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);

    StringBuilder builder = new StringBuilder();
    try {
      List<Object> results = progressResultModel.getResults();
      for (Object result : results) {
        MediaOperationResult r = (MediaOperationResult) result;
        if(!StringUtils.isEmpty(r.getResult())) {
          GameRepresentation game = client.getGameService().getGame(r.getMediaOperation().getGameId());
          if(game != null) {
            builder.append("Result for \"" + game.getGameDisplayName() + "\":\n");
            builder.append(r.getResult());
            builder.append("\n");
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to collect conversion results: {}", e.getMessage(), e);
    }

    Platform.runLater(() -> {
      WidgetFactory.showOutputDialog(Studio.stage, "Results", "The logs of the media conversion are shown below.", null, builder.toString());
    });
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      MediaOperationResult convert = client.getMediaConversionService().convert(game.getId(), screen, null, command);
      progressResultModel.getResults().add(convert);
      EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
    }
    catch (Exception e) {
      LOG.error("Media conversion failed: " + e.getMessage(), e);
      progressResultModel.getResults().add("Media conversion failed: " + e.getMessage());
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

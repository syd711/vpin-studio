package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class GamePatcherUploadPostProcessingProgressModel extends ProgressModel<UploadDescriptor> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<UploadDescriptor> iterator;
  private final GameRepresentation game;
  private final UploadDescriptor uploadDescriptor;

  public GamePatcherUploadPostProcessingProgressModel(String title, UploadDescriptor uploadDescriptor, GameRepresentation game) {
    super(title);
    this.uploadDescriptor = uploadDescriptor;
    iterator = Arrays.asList(uploadDescriptor).iterator();
    this.game = game;
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
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public UploadDescriptor getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(UploadDescriptor descriptor) {
    return "Applying \"" + uploadDescriptor.getOriginalUploadFileName() + "\", please wait...";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, UploadDescriptor descriptor) {
    try {
      UploadDescriptor result = client.getPatcherService().proccessTablePatch(uploadDescriptor);
      progressResultModel.getResults().add(result);

      client.getGameService().scanGame(game.getId());
      EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Post processing failed: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreSynchronizeProgressModel extends ProgressModel<VpsTable> {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreSynchronizeProgressModel.class);

  private final Iterator<VpsTable> iterator;
  private final List<VpsTable> vpsTableList;

  public HighscoreSynchronizeProgressModel(String title, List<VpsTable> vpsTableList) {
    super(title);
    this.iterator = vpsTableList.iterator();
    this.vpsTableList = vpsTableList;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public VpsTable getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(VpsTable t) {
    return "Synchronizing \"" + t.getDisplayName() + "\"";
  }

  @Override
  public int getMax() {
    return vpsTableList.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VpsTable next) {
    try {
      client.getManiaService().synchronizeHighscore(next.getId());
    } catch (Exception e) {
      LOG.error("Failed to synchronize the highscore for \"" + next.getDisplayName() + "\": " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Highscore Sync Failed", "Failed to synchronize the highscore for \"" + next.getDisplayName() + "\": " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

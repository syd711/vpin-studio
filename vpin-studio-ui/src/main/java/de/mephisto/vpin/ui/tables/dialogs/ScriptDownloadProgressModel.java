package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ScriptDownloadProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(ScriptDownloadProgressModel.class);

  private final Iterator<GameRepresentation> iterator;
  private final VPinStudioClient client;

  public ScriptDownloadProgressModel(VPinStudioClient client, String title, GameRepresentation game) {
    super(title);
    this.client = client;
    iterator = Collections.singletonList(game).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      GameRepresentation next = iterator.next();
      File tableScript = Studio.client.getTableScript(next.getId());
      progressResultModel.addProcessed(tableScript);
      return next.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

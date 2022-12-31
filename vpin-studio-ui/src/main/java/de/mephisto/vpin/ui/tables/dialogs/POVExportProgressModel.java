package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;

public class POVExportProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(POVExportProgressModel.class);

  private final Iterator<GameRepresentation> iterator;
  private final VPinStudioClient client;

  public POVExportProgressModel(VPinStudioClient client, String title, GameRepresentation game) {
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
      POVRepresentation pov = client.createPOV(next.getId());
      if(pov != null) {
        progressResultModel.addProcessed(pov);
      }
      else {
        progressResultModel.addProcessed();
      }
      return next.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("POV extraction failed: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

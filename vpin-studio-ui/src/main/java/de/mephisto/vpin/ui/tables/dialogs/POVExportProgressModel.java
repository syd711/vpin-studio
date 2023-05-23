package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;

public class POVExportProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(POVExportProgressModel.class);

  private final Iterator<GameRepresentation> iterator;

  public POVExportProgressModel(String title, GameRepresentation game) {
    super(title);
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
  public GameRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation next) {
    try {
      POVRepresentation pov = Studio.client.getVpx().createPOV(next.getId());
      if(pov != null) {
        progressResultModel.addProcessed(pov);
      }
      else {
        progressResultModel.addProcessed();
      }
    } catch (Exception e) {
      LOG.error("POV extraction failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

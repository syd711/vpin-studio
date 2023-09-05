package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionCreationProgressModel extends ProgressModel<CompetitionRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionCreationProgressModel.class);

  private final Iterator<CompetitionRepresentation> iterator;

  public CompetitionCreationProgressModel(String title, CompetitionRepresentation competition) {
    super(title);
    this.iterator = Arrays.asList(competition).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public CompetitionRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(CompetitionRepresentation c) {
    return "Creating \"" + c.getName() + "\"";
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, CompetitionRepresentation next) {
    try {
      CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(next);
      progressResultModel.addProcessed(newCmp);
      Thread.sleep(3000);
    } catch (Exception e) {
      LOG.error("VPS database download failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Download Failed", "VPS database download failed: " + e.getMessage());
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

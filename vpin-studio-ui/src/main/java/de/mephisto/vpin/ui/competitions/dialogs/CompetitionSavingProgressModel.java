package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionSavingProgressModel extends ProgressModel<CompetitionRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionSavingProgressModel.class);

  private final Iterator<CompetitionRepresentation> iterator;

  public CompetitionSavingProgressModel(String title, CompetitionRepresentation competition) {
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
    return "Saving \"" + c.getName() + "\"";
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
      Thread.sleep(6000);
    } catch (Exception e) {
      LOG.error("Failed to save competitions data: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Competition Update Failed", "Failed to save competitions data: " + e.getMessage());
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

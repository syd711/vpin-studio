package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
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

public class IScoredSynchronizationProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSynchronizationProgressModel.class);

  private final Iterator<String> iterator;
  private final List<String> competitions;

  public IScoredSynchronizationProgressModel() {
    super("iScored Synchronization");
    this.competitions = Arrays.asList("Synchronizing Game Rooms");
    this.iterator = competitions.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String c) {
    return "";
  }

  @Override
  public int getMax() {
    return competitions.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String next) {
    try {
      List<CompetitionRepresentation> competitionRepresentations = client.getCompetitionService().synchronizeIScored();
      progressResultModel.getResults().add(competitionRepresentations);
    }
    catch (Exception e) {
      LOG.error("Failed to sync competitions data: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "iScored Synchronization Failed", "Result: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

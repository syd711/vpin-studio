package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionSavingProgressModel extends ProgressModel<CompetitionRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionSavingProgressModel.class);

  private final Iterator<CompetitionRepresentation> iterator;
  private final List<CompetitionRepresentation> competitions;

  public CompetitionSavingProgressModel(String title, List<CompetitionRepresentation> competitions) {
    super(title);
    this.iterator = competitions.iterator();
    this.competitions = competitions;
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
    return competitions.size() == 1;
  }

  @Override
  public String nextToString(CompetitionRepresentation c) {
    return "Saving \"" + c.getName() + "\"";
  }

  @Override
  public int getMax() {
    return competitions.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, CompetitionRepresentation next) {
    try {
      CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(next);
      progressResultModel.addProcessed(newCmp);
      GameRepresentation game = client.getGameService().getGame(newCmp.getGameId());
      if (game != null) {
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
    } catch (Exception e) {
      LOG.error("Failed to save competitions data: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Competition Update Failed", "Failed to save competitions data: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

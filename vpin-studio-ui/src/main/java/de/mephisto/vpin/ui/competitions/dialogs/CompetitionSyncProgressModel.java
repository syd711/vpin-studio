package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionSyncProgressModel extends ProgressModel<CompetitionRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionSyncProgressModel.class);

  private final Iterator<CompetitionRepresentation> iterator;
  private final List<CompetitionRepresentation> competitions;

  public CompetitionSyncProgressModel(String title, List<CompetitionRepresentation> competitions) {
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
    return false;
  }

  @Override
  public String nextToString(CompetitionRepresentation c) {
    return "Synchronizing \"" + c.toString() + "\"";
  }

  @Override
  public int getMax() {
    return competitions.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, CompetitionRepresentation next) {
    try {
      client.getDiscordService().checkCompetition(next);
    } catch (Exception e) {
      LOG.error("Failed sync competitions data: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Competition Synchronize Failed", "Failed to sync competitions data: " + e.getMessage());
    }
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    client.getDiscordService().clearCache();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

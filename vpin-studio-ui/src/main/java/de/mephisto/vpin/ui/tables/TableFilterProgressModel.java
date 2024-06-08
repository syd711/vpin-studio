package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableFilterProgressModel extends ProgressModel<FilterSettings> {
  private final static Logger LOG = LoggerFactory.getLogger(TableFilterProgressModel.class);

  private List<FilterSettings> settingsList;
  private Iterator<FilterSettings> settingsIterator;

  public TableFilterProgressModel(FilterSettings filterSettings) {
    super("Filtering Tables");
    this.settingsList = Arrays.asList(filterSettings);
    this.settingsIterator = settingsList.iterator();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return settingsList.size();
  }

  @Override
  public boolean hasNext() {
    return this.settingsIterator.hasNext();
  }

  @Override
  public FilterSettings getNext() {
    return settingsIterator.next();
  }

  @Override
  public String nextToString(FilterSettings id) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, FilterSettings next) {
    try {
      List<Integer> integers = client.getGameService().filterGames(next);
      progressResultModel.getResults().add(integers);
    } catch (Exception e) {
      LOG.error("Error during filtering: " + e.getMessage(), e);
    }
  }
}

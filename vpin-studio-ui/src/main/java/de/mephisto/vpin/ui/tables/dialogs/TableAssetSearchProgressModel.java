package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableAssetSearchProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetSearchProgressModel.class);

  private final Iterator<String> iterator;
  private final int gameId;
  private final VPinScreen screen;
  private List<String> terms = new ArrayList<>();

  public TableAssetSearchProgressModel(String title, int gameId, VPinScreen screen, String term) {
    super(title);
    this.gameId = gameId;
    this.screen = screen;
    this.terms.add(term);
    this.iterator = this.terms.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return terms.size();
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
  public String nextToString(String term) {
    return "Search Popper for '" + term + "'";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String term) {
    try {
      TableAssetSearch result = client.getGameMediaService().searchTableAsset(gameId, screen, term);
      progressResultModel.getResults().add(result);
    } catch (Exception e) {
      LOG.error("Popper search failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

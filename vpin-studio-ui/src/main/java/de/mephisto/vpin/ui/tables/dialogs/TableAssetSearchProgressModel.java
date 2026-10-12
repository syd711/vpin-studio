package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Searches the given term in each of the given asset sources, a null source searches all asset sources.
 */
public class TableAssetSearchProgressModel extends ProgressModel<TableAssetSource> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<TableAssetSource> iterator;
  private final int gameId;
  private final List<TableAssetSource> sources;
  private final VPinScreen screen;
  private final String term;

  public TableAssetSearchProgressModel(String title, int gameId, List<TableAssetSource> sources, VPinScreen screen, String term) {
    super(title);
    this.gameId = gameId;
    this.sources = new ArrayList<>(sources);
    this.screen = screen;
    this.term = term;
    this.iterator = this.sources.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return sources.size();
  }

  @Override
  public TableAssetSource getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return sources.size() <= 1;
  }

  @Override
  public String nextToString(TableAssetSource source) {
    return Messages.get("dialog.search_assets_for", term);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TableAssetSource source) {
    try {
      TableAssetSearch result = client.getGameMediaService().searchTableAsset(source, gameId, screen, term);
      progressResultModel.getResults().add(result);
    } catch (Exception e) {
      LOG.error("Assets search failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

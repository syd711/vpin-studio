package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class TableAssetDownloadProgressModel extends ProgressModel<TableAsset> {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetDownloadProgressModel.class);

  private final VPinScreen VPinScreen;
  private final GameRepresentation game;
  private final Iterator<TableAsset> iterator;
  private final boolean append;

  public TableAssetDownloadProgressModel(VPinScreen VPinScreen, GameRepresentation game, TableAsset tableAsset, boolean append) {
    super("Downloading " + tableAsset.getName());
    this.VPinScreen = VPinScreen;
    this.game = game;
    this.iterator = Arrays.asList(tableAsset).iterator();
    this.append = append;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public TableAsset getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(TableAsset asset) {
    return "Downloading " + asset;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TableAsset tableAsset) {
    try {
      client.getPinUPPopperService().downloadTableAsset(tableAsset, this.VPinScreen, this.game, append);
    } catch (Exception e) {
      LOG.error("Asset download failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Download Failed", "Popper table asset download failed: " + e.getMessage());
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

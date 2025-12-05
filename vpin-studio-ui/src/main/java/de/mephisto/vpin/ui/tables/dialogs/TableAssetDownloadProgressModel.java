package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class TableAssetDownloadProgressModel extends ProgressModel<TableAsset> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Stage stage;
  private final VPinScreen screen;
  private PlaylistRepresentation playlist;
  private GameRepresentation game;
  private final Iterator<TableAsset> iterator;
  private final boolean append;

  public TableAssetDownloadProgressModel(Stage stage, VPinScreen VPinScreen, GameRepresentation game, TableAsset tableAsset, boolean append) {
    super("Downloading " + tableAsset.getName());
    this.stage = stage;
    this.screen = VPinScreen;
    this.game = game;
    this.iterator = Arrays.asList(tableAsset).iterator();
    this.append = append;
  }

  public TableAssetDownloadProgressModel(Stage stage, VPinScreen VPinScreen, PlaylistRepresentation playlist, TableAsset tableAsset, boolean append) {
    super("Downloading " + tableAsset.getName());
    this.stage = stage;
    this.screen = VPinScreen;
    this.playlist = playlist;
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
      if (game != null) {
        client.getGameMediaService().downloadTableAsset(tableAsset, this.screen, this.game, append);
      }
      else {
        client.getPlaylistMediaService().downloadPlaylistAsset(tableAsset, this.screen, this.playlist, append);
      }
    }
    catch (Exception e) {
      LOG.error("Asset download failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(stage, "Download Failed", "Table asset download failed: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

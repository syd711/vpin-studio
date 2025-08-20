package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.*;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.util.HttpUtils;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * And asset search service based on the local filesystem
 */
public class VideoTutorialsTableAssetAdapter extends DefaultTableAssetAdapter implements TableAssetsAdapter<Game> {
  private final static Logger LOG = LoggerFactory.getLogger(VideoTutorialsTableAssetAdapter.class);

  public final static String SOURCE_ID = TableAssetSourceType.TutorialVideos + "-Kongedam";

  @NonNull
  private final VpsService vpsService;

  public VideoTutorialsTableAssetAdapter(@NonNull VpsService vpsService, @NonNull TableAssetSource source) {
    super(source);
    this.vpsService = vpsService;
  }

  @Override
  public TableAssetSource getAssetSource() {
    return source;
  }

  @Override
  public List<TableAsset> search(String emulatorName, String screenSegment, @Nullable Game game, String term) throws Exception {
    if (!source.isEnabled()) {
      return Collections.emptyList();
    }

    if (source.getLookupStrategy().equals(AssetLookupStrategy.screens) && !source.supportsScreen(screenSegment)) {
      return Collections.emptyList();
    }

    VpsTable vpsTable = getVpsTable(game);
    if (vpsTable != null) {
      return List.of(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, vpsTable));
    }
    return Collections.emptyList();
  }

  @Nullable
  private VpsTable getVpsTable(@Nullable Game game) {
    if (game != null) {
      VpsTable vpsTable = vpsService.getTableById(game.getExtTableId());
      List<VpsTutorialUrls> tutorialFiles = vpsTable.getTutorialFiles();
      for (VpsTutorialUrls tutorialFile : tutorialFiles) {
        if (tutorialFile.getAuthors().contains("Kongedam")) {
          String videoUrl = "https://assets.vpin-mania.net/tutorials/kongedam/" + vpsTable.getId() + ".mp4";
          boolean check = HttpUtils.check(videoUrl);
          if (check) {
            return vpsTable;
          }
        }
      }
    }
    return null;
  }

  @Override
  public Optional<TableAsset> get(String emulatorName, String screenSegment, @Nullable Game game, String
      folder, String name) throws Exception {
    VpsTable vpsTable = getVpsTable(game);
    if (vpsTable != null) {
      return Optional.of(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, vpsTable));
    }
    return Optional.empty();
  }

  @Override
  public void writeAsset(@NonNull OutputStream outputStream, @NonNull TableAsset tableAsset) throws Exception {
    writeUrlAsset(outputStream, tableAsset);
  }

  @Override
  public boolean testConnection() {
    return true;
  }

  @NotNull
  private static TableAsset toTableAsset(@NotNull TableAssetSource tableAssetSource,
                                         @NotNull EmulatorType emulator,
                                         @NotNull String screenSegment,
                                         @NonNull VpsTable vpsTable) {
    TableAsset asset = new TableAsset();
    asset.setEmulator(null);
    asset.setScreen(screenSegment);
    asset.setMimeType("video/mp4");
    asset.setUrl("https://assets.vpin-mania.net/tutorials/kongedam/" + vpsTable.getId() + ".mp4");
    asset.setSourceId(SOURCE_ID);
    asset.setName(vpsTable.getName() + ".mp4");
    asset.setAuthor(tableAssetSource.getName());
    asset.setLength(-1);

    return asset;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    VideoTutorialsTableAssetAdapter that = (VideoTutorialsTableAssetAdapter) o;
    return Objects.equals(source, that.source);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source);
  }

  @Override
  public String toString() {
    return "VideoTutorialsTableAssetAdapter{" +
        "mediaSource=" + source +
        '}';
  }
}

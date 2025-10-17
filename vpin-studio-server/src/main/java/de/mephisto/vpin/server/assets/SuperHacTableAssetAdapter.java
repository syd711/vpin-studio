package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.*;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.HttpUtils;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;

/**
 * And asset search service based on the local filesystem
 */
public class SuperHacTableAssetAdapter extends DefaultTableAssetAdapter  {
  private final static Logger LOG = LoggerFactory.getLogger(SuperHacTableAssetAdapter.class);

  public final static String SOURCE_ID = TableAssetSourceType.SuperHacRepo.name();


  public SuperHacTableAssetAdapter(@NonNull TableAssetSource source) {
    super(source);
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

    if (game == null || StringUtils.isEmpty(game.getExtTableId())) {
      return Collections.emptyList();
    }


    if (source.getLookupStrategy().equals(AssetLookupStrategy.screens) && !source.supportsScreen(screenSegment)) {
      return Collections.emptyList();
    }

    List<TableAsset> result = new ArrayList<>();
    VPinScreen screen = VPinScreen.valueOfSegment(screenSegment);
    switch (screen) {
      case BackGlass: {
        String url = createUrl(game.getExtTableId(), "bg");
        boolean check = HttpUtils.check(url);
        if (check) {
          result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, "bg"));
        }
        break;
      }
      case DMD: {
        String url = createUrl(game.getExtTableId(), "dmd");
        boolean check = HttpUtils.check(url);
        if (check) {
          result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, "bg"));
        }
        break;
      }
      case GameInfo:
      case GameHelp: {
        String url = createUrl(game.getExtTableId(), "fss");
        boolean check = HttpUtils.check(url);
        if (check) {
          result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, "bg"));
        }
        url = createBaseUrl(game.getExtTableId(), "cab");
        check = HttpUtils.check(url);
        if (check) {
          result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, "cab"));
        }
        break;
      }
      case PlayField: {
        String url = createUrl(game.getExtTableId(), "table");
        boolean check = HttpUtils.check(url);
        if (check) {
          result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, "bg"));
        }
        break;
      }
      case Wheel: {
        String url = createBaseUrl(game.getExtTableId(), "wheel");
        boolean check = HttpUtils.check(url);
        if (check) {
          result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, "wheel"));
        }
        break;
      }
    }

    return result;
  }

  @Override
  public Optional<TableAsset> get(String emulatorName,
                                  String screenSegment,
                                  @Nullable Game game,
                                  String folder,
                                  String name) throws Exception {
    if (game != null) {
      String baseName = FilenameUtils.getBaseName(name);
      String url = createUrl(game.getExtTableId(), baseName);
      if (name.equalsIgnoreCase("cab")) {
        url = createBaseUrl(game.getExtTableId(), baseName);
      }
      return Optional.of(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, name));
    }
    return Optional.empty();
  }

  @Override
  public void writeAsset(@NonNull OutputStream outputStream, @NonNull TableAsset tableAsset, long start, long length) throws Exception {
    writeUrlAsset(outputStream, tableAsset, start, length);
  }

  @Override
  public boolean testConnection() {
    return true;
  }

  @NotNull
  private static TableAsset toTableAsset(@NotNull TableAssetSource tableAssetSource,
                                         @NotNull EmulatorType emulator,
                                         @NotNull String screenSegment,
                                         @NonNull String url,
                                         @NonNull String name) {
    TableAsset asset = new TableAsset();
    asset.setEmulator(null);
    asset.setScreen(screenSegment);
    asset.setMimeType("image/png");
    asset.setUrl(url);
    asset.setSourceId(SOURCE_ID);
    asset.setName(name + ".png");
    asset.setAuthor(tableAssetSource.getName());
    asset.setLength(-1);

    return asset;
  }

  private static String createUrl(String vpsId, String name) {
    return "https://github.com/superhac/vpinmediadb/raw/refs/heads/main/" + vpsId + "/1k/" + name + ".png";
  }

  private static String createBaseUrl(String vpsId, String name) {
    return "https://github.com/superhac/vpinmediadb/raw/refs/heads/main/" + vpsId + "/" + name + ".png";
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SuperHacTableAssetAdapter that = (SuperHacTableAssetAdapter) o;
    return Objects.equals(source, that.source);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source);
  }

  @Override
  public String toString() {
    return "SuperHacTableAssetAdapter{" +
        "mediaSource=" + source +
        '}';
  }
}

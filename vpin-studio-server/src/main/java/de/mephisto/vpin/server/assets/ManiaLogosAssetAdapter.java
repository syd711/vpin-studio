package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.AssetLookupStrategy;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.HttpUtils;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

/**
 * And asset search for manufacturer logos
 */
public class ManiaLogosAssetAdapter extends DefaultTableAssetAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaLogosAssetAdapter.class);

  public final static String SOURCE_ID = TableAssetSourceType.ManiaLogos.name();

  public ManiaLogosAssetAdapter(@NonNull TableAssetSource source) {
    super(source);
  }

  private final List<String> logos = new ArrayList<>();

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

    if (logos.isEmpty()) {
      List<String> logoList = FileUtils.readLines(new File(RESOURCES, "logos.txt"), StandardCharsets.UTF_8);
      logos.addAll(logoList);
    }

    List<TableAsset> result = new ArrayList<>();
    VPinScreen screen = VPinScreen.valueOfSegment(screenSegment);
    if (screen == null) {
      return Collections.emptyList();
    }

    for (String logo : logos) {
      if (logo.toLowerCase().contains(term.toLowerCase())) {
        String url = createUrl(logo);
        result.add(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, url, logo));
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
      String url = createUrl(name);
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
    asset.setName(name);
    asset.setAuthor(tableAssetSource.getName());
    asset.setLength(-1);

    return asset;
  }

  private static String createUrl(String name) {
    return "https://assets.vpin-mania.net/logos/" + name.replaceAll(" ", "%20");
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ManiaLogosAssetAdapter that = (ManiaLogosAssetAdapter) o;
    return Objects.equals(source, that.source);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source);
  }

  @Override
  public String toString() {
    return "ManiaLogosAssetAdapter{" +
        "mediaSource=" + source +
        '}';
  }
}

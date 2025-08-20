package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.AssetLookupStrategy;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * And asset search service based on the local filesystem
 */
public class FileSystemTableAssetAdapter extends DefaultTableAssetAdapter implements TableAssetsAdapter<Game> {
  private final static Logger LOG = LoggerFactory.getLogger(FileSystemTableAssetAdapter.class);


  public FileSystemTableAssetAdapter(@NonNull TableAssetSource source) {
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

    if (source.getLookupStrategy().equals(AssetLookupStrategy.screens) && !source.supportsScreen(screenSegment)) {
      return Collections.emptyList();
    }

    List<File> result = new ArrayList<>();
    File folder = new File(source.getLocation());
    if (folder.exists() && folder.isDirectory()) {
      de.mephisto.vpin.restclient.util.FileUtils.findFileRecursive(folder, Arrays.asList("png", "apng", "mov", "mp4", "mp3", "ogg", "mkv"), term, result);
    }

    if (source.getLookupStrategy().equals(AssetLookupStrategy.autoDetect)) {
      result = result.stream().filter(f -> matches(screenSegment, f.getAbsolutePath())).collect(Collectors.toList());
    }

    return result.stream().map(f -> {
      return toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, f);
    }).collect(Collectors.toList());
  }

  @Override
  public Optional<TableAsset> get(String emulatorName, String screenSegment, @Nullable Game game, String folder, String name) throws Exception {
    File f = new File(folder, name);
    return Optional.of(toTableAsset(source, EmulatorType.valueOf(emulatorName), screenSegment, f));
  }

  @Override
  public void writeAsset(@NonNull OutputStream outputStream, @NonNull TableAsset tableAsset) throws Exception {
    String decoded = URLDecoder.decode(tableAsset.getUrl(), StandardCharsets.UTF_8);
    String fileName = decoded.substring("/".length());
    fileName = fileName.replaceAll("@2x", "");
    File source = new File(fileName);
    if (source.exists()) {
      try {
        FileUtils.copyFile(source, outputStream);
        LOG.info("Copied {} from {}", source.getAbsolutePath(), this.source);
      }
      catch (Exception e) {
        //do not log URL
        LOG.error("Failed to execute media item copy: " + e.getClass().getSimpleName(), e);
      }
    }
    else {
      LOG.error("Failed to resolve media source file {} from source {}", source.getAbsolutePath(), getAssetSource());
    }
  }

  @Override
  public boolean testConnection() {
    return true;
  }

  @NotNull
  private static TableAsset toTableAsset(@NotNull TableAssetSource tableAssetSource, @NotNull EmulatorType emulator, @NotNull String screenSegment, @NonNull File f) {
    String filename = f.getName();

    TableAsset asset = new TableAsset();
    asset.setEmulator(null);
    asset.setScreen(screenSegment);

    asset.setMimeType(MimeTypeUtil.determineMimeType(FilenameUtils.getExtension(filename).toLowerCase()));

    String url = "/" + URLEncoder.encode(f.getAbsolutePath().replaceAll("\\\\", "/"), StandardCharsets.UTF_8);
    asset.setUrl(url);
    asset.setSourceId(tableAssetSource.getId());
    asset.setName(filename);
    asset.setAuthor(tableAssetSource.getName());
    asset.setLength(filename.length());

    return asset;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    FileSystemTableAssetAdapter that = (FileSystemTableAssetAdapter) o;
    return Objects.equals(source, that.source);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source);
  }

  @Override
  public String toString() {
    return "FileSystemTableAssetAdapter{" +
        "mediaSource=" + source +
        '}';
  }
}

package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
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

/**
 * And asset search service based on the local filesystem
 */
public class FileSystemTableAssetAdapter implements TableAssetsAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(FileSystemTableAssetAdapter.class);

  @NonNull
  private final TableAssetSource tableAssetSource;

  public FileSystemTableAssetAdapter(@NonNull TableAssetSource tableAssetSource) {
    this.tableAssetSource = tableAssetSource;
  }

  @Override
  public TableAssetSource getAssetSource() {
    return null;
  }

  @Override
  public List<TableAsset> search(String emulatorName, String screenSegment, String term) throws Exception {
    if (tableAssetSource.isEnabled()) {
      File folder = new File(tableAssetSource.getLocation());
      if (folder.exists() && folder.isDirectory()) {
        List<File> result = new ArrayList<>();
        de.mephisto.vpin.restclient.util.FileUtils.findFileRecursive(folder, Arrays.asList("png", "apng", "mov", "mp4", "mp3", "ogg", "mkv"), term, result);
//        return result.stream().map(f -> {
//          return toTableAsset(tableAssetSource, emulator, screen, f);
//        }).collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  @Override
  public Optional<TableAsset> get(String emulatorName, String screenSegment, String folder, String name) throws Exception {
    String folderName = folder.substring("file:///".length());
    File f = new File(folderName, name);
    return null;//Optional.of(toTableAsset(tableAssetSource, emulatorName, screenSegment, f));
  }

  @Override
  public void writeAsset(@NonNull OutputStream outputStream, @NonNull TableAsset tableAsset) throws Exception {
    String decoded = URLDecoder.decode(tableAsset.getUrl(), StandardCharsets.UTF_8);
    String fileName = decoded.substring("file:///".length());
    File source = new File(fileName);
    if (source.exists()) {
      try {
        FileUtils.copyFile(source, outputStream);
        LOG.info("Copied {} from {}", source.getAbsolutePath(), tableAssetSource);
      }
      catch (Exception e) {
        //do not log URL
        LOG.error("Failed to execute media item copy: " + e.getClass().getSimpleName(), e);
      }
    }
  }

  @Override
  public boolean testConnection() {
    return false;
  }

  @NotNull
  private static TableAsset toTableAsset(@NotNull TableAssetSource tableAssetSource, @NotNull EmulatorType emulator, @NotNull VPinScreen screen, @NonNull File f) {
    String filename = f.getName();

    TableAsset asset = new TableAsset();
    asset.setEmulator(null);
    asset.setScreen(screen.getSegment());

    asset.setMimeType(MimeTypeUtil.determineMimeType(FilenameUtils.getExtension(filename).toLowerCase()));

    String url = URLEncoder.encode("file:///" + f.getAbsolutePath().replaceAll("\\\\", "/"), StandardCharsets.UTF_8);
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
    return Objects.equals(tableAssetSource, that.tableAssetSource);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(tableAssetSource);
  }

  @Override
  public String toString() {
    return "FileSystemTableAssetAdapter{" +
        "mediaSource=" + tableAssetSource +
        '}';
  }
}

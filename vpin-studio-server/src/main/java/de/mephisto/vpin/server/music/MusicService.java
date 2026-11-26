package de.mephisto.vpin.server.music;


import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import de.mephisto.vpin.server.vpx.MusicInstallationUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MusicService {
  private final static Logger LOG = LoggerFactory.getLogger(MusicService.class);

  @Autowired
  private FolderLookupService folderLookupService;

  @Nullable
  public File getGameMusicFolder(Game game) {
    return folderLookupService.getGameMusicFolder(game);
  }

  public void installMusic(@NonNull File out, @Nullable Game game, @Nullable GameEmulator gameEmulator, @NonNull UploaderAnalysis analysis) throws IOException {
    // the table decides where the music goes: next to it in the per-table layout, the shared folder otherwise
    File musicFolder = null;
    if (game != null) {
      musicFolder = folderLookupService.getMusicFolder(game);
      if (musicFolder != null && folderLookupService.isPerTableFileStructure(game)) {
        // VPX does not create the folder next to the table
        musicFolder.mkdirs();
      }
    }
    if (musicFolder == null && gameEmulator != null) {
      musicFolder = folderLookupService.getMusicFolder(gameEmulator);
    }

    if (musicFolder == null || !musicFolder.exists()) {
      LOG.warn("Skipped installation of music bundle, no music folder {} found.", musicFolder);
      return;
    }

    String suffix = FilenameUtils.getExtension(out.getName());
    if (suffix.equalsIgnoreCase(AssetType.VPA.name())) {
      PackageUtil.unpackTargetFolder(out, musicFolder, analysis.getMusicFolder(), Collections.emptyList(), null);
    }
    else {
      String relativeMusicPath = analysis.getRelativeMusicPath();
      MusicInstallationUtil.unpack(out, musicFolder, analysis, game, relativeMusicPath);
    }
  }

  public List<File> getMp3Files(Game game) {
    File musicFolder = folderLookupService.getGameMusicFolder(game);
    if (musicFolder == null || !musicFolder.exists()) {
      return Collections.emptyList();
    }
    // the game music folder is derived from the table script, it must be the music folder or below
    File musicRoot = folderLookupService.getMusicFolder(game);
    if (musicRoot != null && !musicFolder.getAbsoluteFile().equals(musicRoot.getAbsoluteFile()) && !FileUtils.isStrictlyBelow(musicRoot, musicFolder)) {
      LOG.warn("Ignoring music folder \"{}\" of {}, it is not located in \"{}\"", musicFolder.getAbsolutePath(), game, musicRoot);
      return Collections.emptyList();
    }
    String assetsStr = game.getAssets();
    if (StringUtils.isBlank(assetsStr)) {
      return Collections.emptyList();
    }
    List<File> result = new ArrayList<>();
    Path root = musicFolder.toPath();
    String currentFolderName = musicFolder.getName(); // e.g., "AMH"
    for (String asset : assetsStr.split("\\|")) {
      if (StringUtils.isBlank(asset) || asset.contains("/.mp3") || asset.contains("/*.mp3")) {
        continue;
      }

        // Normalize slashes for safe prefix checking
        String normalizedAsset = asset.replace('\\', '/');
        String finalAssetPath;

        // Strip the folder prefix if it overlaps with the resolved music directory
        if (normalizedAsset.startsWith(currentFolderName + "/")) {
            finalAssetPath = normalizedAsset.substring(currentFolderName.length() + 1);
        } else {
            finalAssetPath = normalizedAsset;
        }

        // asset paths come from the table script and must never point outside the music folder
        if (!FileUtils.isSafeRelativePath(finalAssetPath)) {
          continue;
        }
        // a pure wildcard like "*.mp3" (e.g. from 'track & ".mp3"') would match every file of the (shared) music folder
        if (finalAssetPath.contains("*") && StringUtils.isEmpty(StringUtils.remove(FilenameUtils.getBaseName(finalAssetPath), '*')) && !finalAssetPath.contains("/")) {
          continue;
        }

        // Now evaluate using the cleaned finalAssetPath
        if (!finalAssetPath.contains("*")) {
        File f = new File(musicFolder, finalAssetPath);
        if (f.exists() && !result.contains(f)) {
          result.add(f);
        }
      } else {
        // "*" must not match across folders, so the depth of the pattern and the file must be the same
        int depth = StringUtils.countMatches(finalAssetPath, '/');
        try (Stream<Path> walk = Files.walk(root, depth + 1)) {
          walk.filter(p -> !Files.isDirectory(p))
              .filter(p -> root.relativize(p).getNameCount() == depth + 1)
              .filter(p -> FilenameUtils.wildcardMatch(root.relativize(p).toString().replace('\\', '/'), finalAssetPath))
              .map(Path::toFile)
              .filter(f -> !result.contains(f))
              .forEach(result::add);
        } catch (IOException e) {
          LOG.warn("Failed to resolve asset {} in {}: {}", asset, musicFolder.getAbsolutePath(), e.getMessage());
        }
      }
    }
    return result;
  }

  public List<String> getMissingMp3Files(Game game) {
    File musicFolder = folderLookupService.getGameMusicFolder(game);
    if (musicFolder == null || !musicFolder.exists()) {
      return Collections.emptyList();
    }
    String assetsStr = game.getAssets();
    if (StringUtils.isEmpty(assetsStr)) {
      return Collections.emptyList();
    }
    List<String> missing = new ArrayList<>();
    for (String asset : assetsStr.split("\\|")) {
        if (StringUtils.isEmpty(asset) || asset.contains("/.mp3") || asset.contains("/*.mp3")) {
            continue;
        }

        // Extract just the file name (e.g., "File1.mp3" from "AMH/File1.mp3")
        String fileNameOnly = org.apache.commons.io.FilenameUtils.getName(asset);

        // Check BOTH variants to cover any erratic table paths safely
        boolean existsAsRawPath = assetExists(musicFolder, asset);
        boolean existsAsFileName = assetExists(musicFolder, fileNameOnly);

        if (!existsAsRawPath && !existsAsFileName) {
            missing.add(asset); // Log original asset string as missing
        }
    }
    return missing;
  }

  private boolean assetExists(File musicFolder, String asset) {
    if (!asset.contains("*")) {
      return new File(musicFolder, asset).exists();
    }
    // Wildcard pattern: walk the music folder and match relative paths
    Path root = musicFolder.toPath();
    try (Stream<Path> walk = Files.walk(root)) {
      return walk
          .filter(p -> !Files.isDirectory(p))
          .map(p -> root.relativize(p).toString().replace('\\', '/'))
          .anyMatch(rel -> FilenameUtils.wildcardMatch(rel, asset));
    }
    catch (IOException e) {
      LOG.warn("Failed to check asset {} in {}: {}", asset, musicFolder.getAbsolutePath(), e.getMessage());
      return false;
    }
  }

  public boolean delete(Game game) {
    List<File> mp3Files = getMp3Files(game);
    boolean result = true;
    for (File mp3File : mp3Files) {
      if (!SystemUtil.deleteFileOrFolder(mp3File)) {
        result = false;
        LOG.warn("Deleted failed for {}", mp3File.getAbsolutePath());
      }
      else {
        LOG.info("Deleted {}", mp3File.getAbsolutePath());
      }
    }
    return result;
  }
}

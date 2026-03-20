package de.mephisto.vpin.server.music;


import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import de.mephisto.vpin.server.vpx.MusicInstallationUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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

  public void installMusic(@NonNull File out, @NonNull Game game, @NonNull UploaderAnalysis analysis, boolean acceptAllAudio) throws IOException {
    File musicFolder = folderLookupService.getGameMusicFolder(game);
    if (musicFolder == null || !musicFolder.exists()) {
      LOG.warn("Skipped installation of music bundle, no music folder {} found.", musicFolder);
      return;
    }
    MusicInstallationUtil.unpack(out, musicFolder, analysis, game, analysis.getRelativeMusicPath(acceptAllAudio));
  }

  public List<File> getMp3Files(Game game) {
    File musicFolder = folderLookupService.getMusicFolder(game);
    if (musicFolder == null || !musicFolder.exists()) {
      return Collections.emptyList();
    }
    String assetsStr = game.getAssets();
    if (StringUtils.isEmpty(assetsStr)) {
      return Collections.emptyList();
    }
    List<File> result = new ArrayList<>();
    Path root = musicFolder.toPath();
    for (String asset : assetsStr.split("\\|")) {
      if (StringUtils.isEmpty(asset) || asset.contains("/.mp3") || asset.contains("/*.mp3")) {
        continue;
      }
      if (!asset.contains("*")) {
        File f = new File(musicFolder, asset);
        if (f.exists() && !result.contains(f)) {
          result.add(f);
        }
      }
      else {
        try (Stream<Path> walk = Files.walk(root)) {
          walk.filter(p -> !Files.isDirectory(p))
              .filter(p -> FilenameUtils.wildcardMatch(root.relativize(p).toString().replace('\\', '/'), asset))
              .map(Path::toFile)
              .filter(f -> !result.contains(f))
              .forEach(result::add);
        }
        catch (IOException e) {
          LOG.warn("Failed to resolve asset {} in {}: {}", asset, musicFolder.getAbsolutePath(), e.getMessage());
        }
      }
    }
    return result;
  }

  public List<String> getMissingMp3Files(Game game) {
    File musicFolder = folderLookupService.getMusicFolder(game);
    if (musicFolder == null || !musicFolder.exists()) {
      return Collections.emptyList();
    }
    String assetsStr = game.getAssets();
    if (StringUtils.isEmpty(assetsStr)) {
      return Collections.emptyList();
    }
    List<String> missing = new ArrayList<>();
    for (String asset : assetsStr.split("\\|")) {
      if (StringUtils.isEmpty(asset)) {
        continue;
      }
      if (asset.contains("/.mp3") || asset.contains("/*.mp3")) {
        continue;
      }

      if (!assetExists(musicFolder, asset)) {
        missing.add(asset);
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
    File musicFolder = getGameMusicFolder(game);
    return FileUtils.deleteFolder(musicFolder);
  }
}

package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MusicInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MusicInstallationUtil.class);

  private static final List<String> MUSIC_SUFFIXES = Arrays.asList("mp3", "ogg", "wav");

  public static boolean unpack(@NonNull File archiveFile, @NonNull File musicFolder, @NonNull UploaderAnalysis analysis, @NonNull Game game, @Nullable String relativePath) throws IOException {
    if (!musicFolder.exists()) {
      LOG.error("Music upload failed, no music folder found for default emulator.");
    }

    LOG.info("Extracting music pack into \"{}\" with relative path: {}", musicFolder.getAbsolutePath(), relativePath);

    String suffix = FilenameUtils.getExtension(archiveFile.getName());

    if (suffix.equalsIgnoreCase(AssetType.VPA.name())) {
      PackageUtil.unpackTargetFolder(archiveFile, musicFolder, analysis.getMusicFolder(), Collections.emptyList(), null);
    }
    else {
      // ZIP, RAR, 7z: determine the archive folder prefix to extract from.
      // relativePath == null  → scan for a "music/" folder in the archive
      // relativePath == "/"   → audio files are at the archive root, no prefix to strip
      // relativePath == "X/"  → audio files are under folder X; strip that prefix on extraction
      String archiveFolderName;
      if (StringUtils.isEmpty(relativePath)) {
        archiveFolderName = "music/";
      }
      else if (relativePath.equals("/")) {
        archiveFolderName = null;
      }
      else {
        archiveFolderName = relativePath;
      }
      PackageUtil.unpackTargetFolder(archiveFile, musicFolder, archiveFolderName, MUSIC_SUFFIXES, null);
    }

    return true;
  }
}

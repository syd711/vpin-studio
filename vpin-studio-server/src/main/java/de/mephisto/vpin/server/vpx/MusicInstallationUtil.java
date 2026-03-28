package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MusicInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MusicInstallationUtil.class);

  private static final List<String> MUSIC_SUFFIXES = Arrays.asList("mp3", "ogg", "wav");

  public static boolean unpack(@NonNull File archiveFile, @NonNull File musicFolder, @NonNull UploaderAnalysis analysis, @Nullable Game game, @Nullable String relativePath) throws IOException {
    if (!musicFolder.exists()) {
      LOG.error("Music upload failed, no music folder found for default emulator.");
    }

    // Determine which archive-root folder to strip.
    // If the archive wraps files under a generic "Music/" folder (e.g. "Music/cyberrace/file.mp3"),
    // strip only that top-level component so the game-specific subfolder is preserved in the target.
    // If the first component is already game-specific (e.g. "cyberrace/file.mp3") or there is no
    // subfolder at all, pass null so all entries are extracted with their full relative paths intact.
    String archiveFolderName = null;
    if (relativePath != null) {
      int firstSlash = relativePath.indexOf('/');
      if (firstSlash >= 0) {
        String firstFolder = relativePath.substring(0, firstSlash);
        if (firstFolder.equalsIgnoreCase("music")) {
          archiveFolderName = relativePath.substring(0, firstSlash + 1); // e.g. "Music/"
        }
      }
    }

    LOG.info("Extracting music pack into \"{}\" using archive folder prefix: {}", musicFolder.getAbsolutePath(), archiveFolderName);
    PackageUtil.unpackTargetFolder(archiveFile, musicFolder, archiveFolderName, MUSIC_SUFFIXES, null);
    return true;
  }
}

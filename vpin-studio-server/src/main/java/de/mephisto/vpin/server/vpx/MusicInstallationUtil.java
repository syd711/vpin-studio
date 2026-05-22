package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    // Find the "Music/" segment anywhere in the path (as a proper segment, not a substring of another
    // folder name) and strip everything up to and including it, so the game-specific subfolder is
    // preserved in the target.  This handles both "Music/primus/file.mp3" and wrapper-folder layouts
    // like "Primus(Stern 2018) 1.1/Music/primus/file.mp3".
    // If no "Music/" segment is found, pass null so entries are extracted with their relative paths intact.
    String archiveFolderName = null;
    if (relativePath != null) {
      String lower = relativePath.toLowerCase();
      int musicIdx = lower.indexOf("music/");
      if (musicIdx >= 0 && (musicIdx == 0 || lower.charAt(musicIdx - 1) == '/')) {
        archiveFolderName = relativePath.substring(0, musicIdx + "music/".length());
      }
    }

    LOG.info("Extracting music pack into \"{}\" using archive folder prefix: {}", musicFolder.getAbsolutePath(), archiveFolderName);
    PackageUtil.unpackTargetFolder(archiveFile, musicFolder, archiveFolderName, MUSIC_SUFFIXES, null);
    return true;
  }
}

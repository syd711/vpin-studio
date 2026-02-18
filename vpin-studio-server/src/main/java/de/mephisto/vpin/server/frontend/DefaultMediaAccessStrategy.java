package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract public class DefaultMediaAccessStrategy implements MediaAccessStrategy {

  private final Map<String, MediaMonitor> monitors = new ConcurrentHashMap<>();

  protected List<File> getMediaFiles(File screenMediaFolder) {
    String key = screenMediaFolder.getAbsolutePath();
    // if the folder does not exist, do not register the monitor
    if (screenMediaFolder.exists()) {
      if (!monitors.containsKey(key)) {
        MediaMonitor gameMediaMonitor = new MediaMonitor(screenMediaFolder);
        monitors.put(key, gameMediaMonitor);
      }
      return monitors.get(key).getFiles();
    }
    else {
      return Collections.emptyList();
    }
  }

  @Override
  public void stopMonitoring(File screenMediaFolder) {
    String key = screenMediaFolder.getAbsolutePath();
    MediaMonitor gameMediaMonitor = monitors.remove(key);
    if (gameMediaMonitor != null) {
      gameMediaMonitor.stopMonitoring();
    }
  }

  /**
   * The logic here is the same as for all but can be adapted for frontends.
   *
   * @param game
   * @param screen
   * @return
   */
  @Override
  public List<File> getScreenMediaFiles(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String mediaSearchTerm) {
    File screenMediaFolder = getGameMediaFolder(game, screen, null, false);
    List<File> allFiles = getMediaFiles(screenMediaFolder);

    if (!StringUtils.isEmpty(mediaSearchTerm) && mediaSearchTerm.contains("*")) {
      String term = mediaSearchTerm.replaceAll("\\*", "").toLowerCase();
      return allFiles.stream().filter(f -> f.getName().toLowerCase().startsWith(term)).collect(Collectors.toList());
    }

    String baseFilename = game.getGameName();
    List<File> mediaFiles = allFiles.stream().filter(f -> f.getName().toLowerCase().startsWith(baseFilename.toLowerCase())).collect(Collectors.toList());
    Pattern plainMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\.[a-zA-Z0-9]*");
    Pattern screenMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\(.*\\)\\.[a-zA-Z0-9]*");
    return mediaFiles.stream().filter(f -> plainMatcher.matcher(f.getName()).matches() || screenMatcher.matcher(f.getName()).matches()).collect(Collectors.toList());
  }

  /**
   * Ensure media folders exists
   */
  protected File ensureDirExist(File file, boolean create) {
    if (file != null && !file.exists() && create) {
      file.mkdirs();
    }
    return file;
  }

  @Override
  public File createMedia(@NonNull Game game, @NonNull VPinScreen screen, String suffix, boolean append) {
    File gameMediaFolder = getGameMediaFolder(game, screen, suffix, true);
    return createMediaFile(gameMediaFolder, game.getGameName(), suffix, append);
  }

  @Override
  public File createMedia(@NonNull Playlist playlist, @NonNull VPinScreen screen, String suffix, boolean append) {
    File mediaFolder = getPlaylistMediaFolder(playlist, screen, true);
    String mediaName = !StringUtils.isEmpty(playlist.getMediaName()) ? playlist.getMediaName() : playlist.getName();
    return createMediaFile(mediaFolder, mediaName, suffix, append);
  }

  /**
   * Utility method to create a unique media file in the target folder.
   */
  private File createMediaFile(File mediaFolder, String mediaName, String suffix, boolean append) {
    File out = new File(mediaFolder, mediaName + "." + suffix);
    if (append) {
      int index = 1;
      while (out.exists()) {
        String nameIndex = index <= 9 ? "0" + index : String.valueOf(index);
        out = new File(out.getParentFile(), mediaName + nameIndex + "." + suffix);
        index++;
      }
    }
    return out;
  }
}

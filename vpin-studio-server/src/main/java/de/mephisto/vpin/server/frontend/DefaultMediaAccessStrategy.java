package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract public class DefaultMediaAccessStrategy implements MediaAccessStrategy {

  private final Map<String, MediaMonitor> monitors = new ConcurrentHashMap<>();

  protected List<File> getMediaFiles(File screenMediaFolder) {
    String key = screenMediaFolder.getAbsolutePath();
    if (!monitors.containsKey(key)) {
      MediaMonitor gameMediaMonitor = new MediaMonitor(screenMediaFolder);
      monitors.put(key, gameMediaMonitor);
    }
    return monitors.get(key).getFiles();
  }

  @Override
  public void stopMonitoring() {
    Collection<MediaMonitor> values = monitors.values();
    for (MediaMonitor value : values) {
      value.stopMonitoring();
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
}

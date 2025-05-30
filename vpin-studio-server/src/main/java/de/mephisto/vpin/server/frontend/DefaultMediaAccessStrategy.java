package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
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

  /**
   * The logic here is the same as for all but can be adapted for frontends.
   * @param game
   * @param screen
   * @return
   */
  @Override
  public List<File> getScreenMediaFiles(@NonNull Game game, @NonNull VPinScreen screen) {
    File screenMediaFolder = getGameMediaFolder(game, screen, null);
    List<File> allFiles = getMediaFiles(screenMediaFolder);

    String baseFilename = game.getGameName();
    List<File> mediaFiles = allFiles.stream().filter(f -> f.getName().toLowerCase().startsWith(baseFilename.toLowerCase())).collect(Collectors.toList());
    Pattern plainMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\.[a-zA-Z0-9]*");
    Pattern screenMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\(.*\\)\\.[a-zA-Z0-9]*");
    return mediaFiles.stream().filter(f -> plainMatcher.matcher(f.getName()).matches() || screenMatcher.matcher(f.getName()).matches()).collect(Collectors.toList());
  }
}

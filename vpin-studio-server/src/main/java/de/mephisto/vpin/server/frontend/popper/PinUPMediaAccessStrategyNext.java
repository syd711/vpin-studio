package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.playlists.Playlist;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PinUPMediaAccessStrategyNext extends PinUPMediaAccessStrategy {

  public PinUPMediaAccessStrategyNext(@NonNull PinUPConnector pinUPConnector) {
    super(pinUPConnector);
  }

  @Override
  public File getGameMediaFolder(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String extension, boolean create) {
    if (isUseLegacyMode(game)) {
      return super.getGameMediaFolder(game, screen, extension, create);
    }

    return new File(game.getGameFile().getParentFile(), "media/");
  }

  @Override
  public File getPlaylistMediaFolder(@NonNull Playlist playlist, @NonNull VPinScreen screen, boolean create) {
    //AFAIK this logic won't change
    return super.getPlaylistMediaFolder(playlist, screen, create);
  }

  @Override
  public List<File> getScreenMediaFiles(@NonNull Game game, @NonNull VPinScreen screen, @Nullable String mediaSearchTerm) {
    if (isUseLegacyMode(game)) {
      return super.getScreenMediaFiles(game, screen, mediaSearchTerm);
    }

    File screenMediaFolder = getGameMediaFolder(game, screen, null, false);
    List<File> allFiles = getMediaFiles(screenMediaFolder);
    List<File> screenFiles = filterForScreen(allFiles, screen);

    if (StringUtils.isEmpty(mediaSearchTerm)) {
      return screenFiles;
    }

    if (mediaSearchTerm.contains("*")) {
      String term = mediaSearchTerm.replaceAll("\\*", "").toLowerCase();
      return screenFiles.stream().filter(f -> f.getName().toLowerCase().startsWith(term)).collect(Collectors.toList());
    }

    String baseFilename = game.getGameName();
    List<File> mediaFiles = screenFiles.stream().filter(f -> f.getName().toLowerCase().startsWith(baseFilename.toLowerCase())).collect(Collectors.toList());
    Pattern plainMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\.[a-zA-Z0-9]*");
    Pattern screenMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\(.*\\)\\.[a-zA-Z0-9]*");
    return mediaFiles.stream().filter(f -> plainMatcher.matcher(f.getName()).matches() || screenMatcher.matcher(f.getName()).matches()).collect(Collectors.toList());
  }

  @Override
  public boolean deleteMedia(@NonNull Game game, @NonNull VPinScreen screen) {
    if (isUseLegacyMode(game)) {
      return super.deleteMedia(game, screen);
    }

    List<File> screenMediaFiles = getScreenMediaFiles(game, screen, null);
    screenMediaFiles.forEach(FileUtils::delete);
    return true;
  }

  @Override
  public boolean deleteMedia(@NonNull Playlist playlist, @NonNull VPinScreen screen) {
    //AFAIK this logic won't change
    return super.deleteMedia(playlist, screen);
  }

  private List<File> filterForScreen(List<File> allFiles, VPinScreen screen) {
    return allFiles.stream()
        .filter(f -> f.getName().startsWith(screen.getSegment()) || f.getName().startsWith(screen.name()))
        .collect(Collectors.toList());
  }

  private boolean isUseLegacyMode(@NonNull Game game) {
    return true;//TODO 10.8.1
  }

  private boolean isUseLegacyMode(Playlist playlist) {
    return true;//TODO 10.8.1
  }
}

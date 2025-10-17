package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.PlaylistFrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.frontend.WheelIconDelete;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PlaylistMediaService {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistMediaService.class);

  @Autowired
  private PlaylistService playlistService;

  @Autowired
  private FrontendService frontendService;

  // OLE moved form PinupConnector and shared accross Connectors
  public FrontendMedia getPlaylistMedia(int playlistId) {
    Playlist playlist = frontendService.getPlayList(playlistId);
    if (playlist == null) {
      return null;
    }

    FrontendMedia frontendMedia = new FrontendMedia();
    List<VPinScreen> screens = frontendService.getFrontend().getSupportedScreens();
    for (VPinScreen screen : screens) {
      List<FrontendMediaItem> itemList = new ArrayList<>();
      List<File> mediaFiles = getPlaylistMediaFiles(playlist, screen);
      for (File file : mediaFiles) {
        FrontendMediaItem item = new PlaylistFrontendMediaItem(playlist.getId(), screen, file);
        itemList.add(item);
      }
      frontendMedia.getMedia().put(screen.name(), itemList);
    }
    return frontendMedia;
  }

  @NonNull
  public List<File> getPlaylistMediaFiles(@NonNull Playlist playlist, @NonNull VPinScreen screen) {
    try {
      if (playlist.getMediaName() != null) {
        String baseFilename = !StringUtils.isEmpty(playlist.getMediaName()) ? playlist.getMediaName().toLowerCase() : playlist.getName().toLowerCase();
        File mediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen, false);
        if (mediaFolder != null && mediaFolder.exists()) {
          File[] mediaFiles = mediaFolder.listFiles((dir, name) -> name.toLowerCase().startsWith(baseFilename));
          if (mediaFiles != null && mediaFiles.length > 0) {
            Pattern plainMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}");
            return Arrays.stream(mediaFiles).filter(f -> plainMatcher.matcher(FilenameUtils.getBaseName(f.getName().toLowerCase())).matches()).collect(Collectors.toList());
          }
        }
        else {
          LOG.error("Failed to resolve playlist media folder: " + (mediaFolder != null ? mediaFolder.getAbsolutePath() : null));
        }
      }
      else {
        LOG.warn("No media name set for playlist {}", playlist.getName());
      }
    }
    catch (Exception e) {
      LOG.error("Error resolving media files for playlist{}: {}", playlist.getName(), e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public boolean deleteMedia(int playlistId, VPinScreen screen, String filename) {
    Playlist playlist = playlistService.getPlaylist(playlistId);
    File mediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen, false);
    File media = new File(mediaFolder, filename);

    if (media.exists() && media.delete()) {
      LOG.info("Deleted {} of screen {}", media.getAbsolutePath(), screen.name());
      if (screen.equals(VPinScreen.Wheel)) {
        new WheelAugmenter(media).deAugment();
        new WheelIconDelete(media).delete();
      }
      return true;
    }
    return false;
  }

  public File buildMediaAsset(Playlist playlist, VPinScreen screen, String suffix, boolean append) {
    File playlistMediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen, true);
    String mediaName = !StringUtils.isEmpty(playlist.getMediaName()) ? playlist.getMediaName() : playlist.getName();

    File out = new File(playlistMediaFolder, mediaName + "." + suffix);
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

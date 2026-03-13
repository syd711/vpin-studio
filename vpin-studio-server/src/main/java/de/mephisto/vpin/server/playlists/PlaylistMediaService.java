package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.frontend.MediaService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.frontend.WheelIconDelete;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class PlaylistMediaService extends MediaService {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistMediaService.class);

  @Autowired
  private PlaylistService playlistService;

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
        FrontendMediaItem item = FrontendMediaItem.forPlaylist(playlist.getId(), screen, file);
        itemList.add(item);
      }
      frontendMedia.getMedia().put(screen.name(), itemList);
    }
    return frontendMedia;
  }

  @NonNull
  public List<File> getPlaylistMediaFiles(@NonNull Playlist playlist, @NonNull VPinScreen screen) {
    File mediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen, false);
    if (mediaFolder != null && mediaFolder.exists()) {
      String baseFilename = !StringUtils.isEmpty(playlist.getMediaName()) ? playlist.getMediaName().toLowerCase() : playlist.getName().toLowerCase();
      File[] mediaFiles = mediaFolder.listFiles((dir, name) -> FileUtils.isAssetOf(name, baseFilename));
      return Arrays.asList(mediaFiles);
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

  //-------------------------

  @NotNull
  @Override
  public List<File> getMediaFiles(int playlistId, VPinScreen screen) {
    Playlist playlist = playlistService.getPlaylist(playlistId);
    if (playlist != null) {
      return getPlaylistMediaFiles(playlist, screen);
    }
    return Collections.emptyList();
  }

  @Override
  protected File uniqueMediaAsset(int playlistId, VPinScreen screen, String suffix, boolean append) {
    Playlist playlist = playlistService.getPlaylist(playlistId);
    if (playlist != null) {
      return frontendService.getFrontendConnector().getMediaAccessStrategy().createMedia(playlist, screen, suffix, append);
    }
    return null;
  }

  @Override
  protected void notifyGameScreenAssetsChanged(int playlistId, VPinScreen screen, File asset) {
    //do nothing
  }
}

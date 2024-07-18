package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PlaylistMediaService {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistMediaService.class);

  @Autowired
  private PlaylistService playlistService;

  @Autowired
  private FrontendService frontendService;

  public boolean deleteMedia(int playlistId, VPinScreen screen, String filename) {
    Playlist playlist = playlistService.getPlaylist(playlistId);
    File mediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen);
    File media = new File(mediaFolder, filename);
    return media.exists() && media.delete();
  }

  public File buildMediaAsset(Playlist playlist, VPinScreen screen, String suffix, boolean append) {
    File playlistMediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen);
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

package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.popper.Playlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  public List<Playlist> getPlaylists(boolean excludeSqlLists) {
    return pinUPConnector.getPlayLists(excludeSqlLists);
  }

  public Playlist getPlaylist(int playlistId) {
    return pinUPConnector.getPlayList(playlistId);
  }

  public Playlist setPlaylistColor(int playlistId, long color) {
    pinUPConnector.setPlaylistColor(playlistId, color);
    return pinUPConnector.getPlayList(playlistId);
  }

  public Playlist removeFromPlaylist(int playlistId, int gameId) {
    pinUPConnector.deleteFromPlaylist(playlistId, gameId);
    return pinUPConnector.getPlayList(playlistId);
  }

  public void removeFromPlaylists(int gameId) {
    pinUPConnector.deleteFromPlaylists(gameId);
  }

  public Playlist addToPlaylist(int playlistId, int gameId) {
    pinUPConnector.addToPlaylist(playlistId, gameId);
    return pinUPConnector.getPlayList(playlistId);
  }
}

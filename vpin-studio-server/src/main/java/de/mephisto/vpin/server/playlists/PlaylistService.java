package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.PlaylistOrder;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistService.class);

  @Autowired
  private FrontendService frontendService;

  public List<Playlist> getPlaylists() {
    return frontendService.getPlaylists();
  }

  public Playlist getPlaylistTree() {
    return frontendService.getPlaylistTree();
  }

  public Playlist getPlaylist(int playlistId) {
    return frontendService.getPlayList(playlistId);
  }
  public Playlist clearPlaylist(int playlistId) {
    return frontendService.clearPlayList(playlistId);
  }

  public Playlist removeFromPlaylist(int playlistId, int gameId) {
    frontendService.deleteFromPlaylist(playlistId, gameId);
    return frontendService.getPlayList(playlistId);
  }

  public void removeFromPlaylists(int gameId) {
    frontendService.deleteFromPlaylists(gameId);
  }

  public Playlist addToPlaylist(int playlistId, int gameId, int favMode) {
    frontendService.addToPlaylist(playlistId, gameId, favMode);
    return frontendService.getPlayList(playlistId);
  }

  public Playlist updatePlaylistGame(int playlistId, int gameId, int favMode) {
    frontendService.updatePlaylistGame(playlistId, gameId, favMode);
    return frontendService.getPlayList(playlistId);
  }

  public boolean delete(int playlistId) {
    return frontendService.deletePlaylist(playlistId);
  }

  public Playlist save(Playlist playlist) {
    return frontendService.savePlaylist(playlist);
  }

  public PlaylistOrder saveOrder(PlaylistOrder playlistOrder) {
    frontendService.savePlaylistOrder(playlistOrder);
    return playlistOrder;
  }

  public boolean clearCache() {
    FrontendConnector connector = frontendService.getFrontendConnector();
    if (connector != null) {
      connector.reloadCache();
    }
    return true;
  }
}

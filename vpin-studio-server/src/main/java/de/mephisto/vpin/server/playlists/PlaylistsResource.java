package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "playlists")
public class PlaylistsResource {

  @Autowired
  private PlaylistService playlistService;

  @Autowired
  private FrontendService frontendService;

  @DeleteMapping("/media/{playlistId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("playlistId") int playlistId, @PathVariable("screen") VPinScreen screen, @PathVariable("file") String filename) {
    Playlist playlist = playlistService.getPlaylist(playlistId);
    File mediaFolder = frontendService.getPlaylistMediaFolder(playlist, screen);
    File media = new File(mediaFolder, filename);
    return media.exists() && media.delete();
  }

  @GetMapping
  public List<Playlist> getPlaylists() {
    return playlistService.getPlaylists();
  }

  @GetMapping("/{playlistId}")
  public Playlist getPlaylist(@PathVariable("playlistId") int playlistId) {
    return playlistService.getPlaylist(playlistId);
  }

  @DeleteMapping("/{playlistId}/{gameId}")
  public Playlist removeFromPlaylist(@PathVariable("playlistId") int playlistId, @PathVariable("gameId") int gameId) {
    return playlistService.removeFromPlaylist(playlistId, gameId);
  }

  @PutMapping("/{playlistId}/{gameId}/{favMode}")
  public Playlist addToPlaylist(@PathVariable("playlistId") int playlistId, @PathVariable("gameId") int gameId, @PathVariable("favMode") int favMode) {
    return playlistService.addToPlaylist(playlistId, gameId, favMode);
  }


  @PutMapping("/favs/{playlistId}/{gameId}/{favMode}")
  public Playlist updatePlaylistGame(@PathVariable("playlistId") int playlistId, @PathVariable("gameId") int gameId, @PathVariable("favMode") int favMode) {
    return playlistService.updatePlaylistGame(playlistId, gameId, favMode);
  }

  @PutMapping("/{playlistId}/color/{color}")
  public Playlist setPlaylistColor(@PathVariable("playlistId") int playlistId, @PathVariable("color") long color) {
    return playlistService.setPlaylistColor(playlistId, color);
  }
}

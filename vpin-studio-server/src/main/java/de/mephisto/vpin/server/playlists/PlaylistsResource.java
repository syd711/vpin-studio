package de.mephisto.vpin.server.playlists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "playlists")
public class PlaylistsResource {

  @Autowired
  private PlaylistService playlistService;

  @GetMapping
  public List<Playlist> getPlaylists() {
    return playlistService.getPlaylists();
  }

  @GetMapping("/tree")
  public List<Playlist> getPlaylistTree() {
    return playlistService.getPlaylistTree();
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

  @PostMapping("/save")
  public Playlist saveOrUpdate(@RequestBody Playlist playlist) {
    return playlistService.save(playlist);
  }

  @DeleteMapping("{playlistId}")
  public boolean delete(@PathVariable("playlistId") int playlistId) {
    return playlistService.delete(playlistId);
  }
}

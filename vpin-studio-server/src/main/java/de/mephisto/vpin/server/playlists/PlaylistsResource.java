package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.popper.PlaylistRepresentation;
import de.mephisto.vpin.server.popper.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "playlists")
public class PlaylistsResource {

  @Autowired
  private PlaylistService playlistService;

  @GetMapping
  public List<PlaylistRepresentation> getPlaylists() {
    return toRepresentation(playlistService.getPlaylists(false));
  }

  @GetMapping("/static")
  public List<PlaylistRepresentation> getStaticPlaylists() {
    return toRepresentation(playlistService.getPlaylists(true));
  }

  @GetMapping("/{playlistId}")
  public PlaylistRepresentation getPlaylist(@PathVariable("playlistId") int playlistId) {
    return toRepresentation(playlistService.getPlaylist(playlistId));
  }

  @DeleteMapping("/{playlistId}/{gameId}")
  public PlaylistRepresentation removeFromPlaylist(@PathVariable("playlistId") int playlistId, @PathVariable("gameId") int gameId) {
    return toRepresentation(playlistService.removeFromPlaylist(playlistId, gameId));
  }

  @PutMapping("/{playlistId}/{gameId}")
  public PlaylistRepresentation addToPlaylist(@PathVariable("playlistId") int playlistId, @PathVariable("gameId") int gameId) {
    return toRepresentation(playlistService.addToPlaylist(playlistId, gameId));
  }

  @PutMapping("/{playlistId}/color/{color}")
  public PlaylistRepresentation setPlaylistColor(@PathVariable("playlistId") int playlistId, @PathVariable("color") long color) {
    return toRepresentation(playlistService.setPlaylistColor(playlistId, color));
  }

  private List<PlaylistRepresentation> toRepresentation(List<Playlist> playlists) {
    List<PlaylistRepresentation> result = new ArrayList<>();
    for (Playlist playlist : playlists) {
      result.add(toRepresentation(playlist));
    }
    return result;
  }

  private PlaylistRepresentation toRepresentation(Playlist playlist) {
    PlaylistRepresentation rep = new PlaylistRepresentation();
    rep.setMenuColor(playlist.getMenuColor());
    rep.setId(playlist.getId());
    rep.setName(playlist.getName());
    rep.setGameIds(playlist.getGameIds());
    rep.setSqlPlayList(playlist.isSqlPlayList());
    return rep;
  }
}

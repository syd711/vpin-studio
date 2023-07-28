package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.server.popper.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  private List<PlaylistRepresentation> toRepresentation(List<Playlist> playlists) {
    List<PlaylistRepresentation> result = new ArrayList<>();
    for (Playlist playlist : playlists) {
      PlaylistRepresentation rep = new PlaylistRepresentation();
      rep.setMenuColor(playlist.getMenuColor());
      rep.setId(playlist.getId());
      rep.setName(playlist.getName());
      rep.setGameIds(playlist.getGameIds());
      rep.setSqlPlayList(playlist.isSqlPlayList());
      result.add(rep);
    }
    return result;
  }
}

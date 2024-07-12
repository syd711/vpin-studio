package de.mephisto.vpin.restclient.playlists;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*********************************************************************************************************************
 * Playlists
 ********************************************************************************************************************/
public class PlaylistsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public PlaylistsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<PlaylistRepresentation> getPlaylists() {
    return Arrays.asList(getRestClient().get(API + "playlists", PlaylistRepresentation[].class));
  }

  public PlaylistRepresentation getPlaylist(int playlistId) {
    return getRestClient().get(API + "playlists/" + playlistId, PlaylistRepresentation.class);
  }


  public PlaylistRepresentation removeFromPlaylist(PlaylistRepresentation playlist, GameRepresentation game) {
    getRestClient().delete(API + "playlists/" + playlist.getId() + "/" + game.getId(), new HashMap<>());
    return getPlaylist(playlist.getId());
  }

  public PlaylistRepresentation updatePlaylistGame(PlaylistRepresentation playlist, GameRepresentation game, boolean fav, boolean globalFav) throws Exception {
    int favMode = 0;
    if (fav) {
      favMode = 1;
    }
    if (globalFav) {
      favMode = 2;
    }
    return getRestClient().put(API + "playlists/favs/" + playlist.getId() + "/" + game.getId() + "/" + favMode, new HashMap<>(), PlaylistRepresentation.class);
  }

  public PlaylistRepresentation addToPlaylist(PlaylistRepresentation playlist, GameRepresentation game, boolean fav, boolean globalFav) throws Exception {
    int favMode = 0;
    if (fav) {
      favMode = 1;
    }
    if (globalFav) {
      favMode = 2;
    }
    return getRestClient().put(API + "playlists/" + playlist.getId() + "/" + game.getId() + "/" + favMode, new HashMap<>(), PlaylistRepresentation.class);
  }

  public PlaylistRepresentation setPlaylistColor(PlaylistRepresentation playlist, String colorhex) throws Exception {
    if (colorhex.startsWith("#")) {
      colorhex = colorhex.substring(1);
    }
    long color = Long.parseLong(colorhex, 16);
    return getRestClient().put(API + "playlists/" + playlist.getId() + "/color/" + color, new HashMap<>(), PlaylistRepresentation.class);
  }
}

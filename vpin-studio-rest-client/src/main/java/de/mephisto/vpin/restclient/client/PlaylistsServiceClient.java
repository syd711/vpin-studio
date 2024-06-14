package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.frontend.Playlist;
import de.mephisto.vpin.restclient.games.GameRepresentation;
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

  public List<Playlist> getStaticPlaylists() {
    return Arrays.asList(getRestClient().get(API + "playlists/static", Playlist[].class));
  }

  public List<Playlist> getPlaylists() {
    return Arrays.asList(getRestClient().get(API + "playlists", Playlist[].class));
  }

  public Playlist getPlaylist(int playlistId) {
    return getRestClient().get(API + "playlists/" + playlistId, Playlist.class);
  }


  public Playlist removeFromPlaylist(Playlist playlist, GameRepresentation game) {
    getRestClient().delete(API + "playlists/" + playlist.getId() + "/" + game.getId(), new HashMap<>());
    return getPlaylist(playlist.getId());
  }

  public Playlist updatePlaylistGame(Playlist playlist, GameRepresentation game, boolean fav, boolean globalFav) throws Exception {
    int favMode = 0;
    if (fav) {
      favMode = 1;
    }
    if (globalFav) {
      favMode = 2;
    }
    return getRestClient().put(API + "playlists/favs/" + playlist.getId() + "/" + game.getId() + "/" + favMode, new HashMap<>(), Playlist.class);
  }

  public Playlist addToPlaylist(Playlist playlist, GameRepresentation game, boolean fav, boolean globalFav) throws Exception {
    int favMode = 0;
    if (fav) {
      favMode = 1;
    }
    if (globalFav) {
      favMode = 2;
    }
    return getRestClient().put(API + "playlists/" + playlist.getId() + "/" + game.getId() + "/" + favMode, new HashMap<>(), Playlist.class);
  }

  public Playlist setPlaylistColor(Playlist playlist, String colorhex) throws Exception {
    if (colorhex.startsWith("#")) {
      colorhex = colorhex.substring(1);
    }
    long color = Long.parseLong(colorhex, 16);
    return getRestClient().put(API + "playlists/" + playlist.getId() + "/color/" + color, new HashMap<>(), Playlist.class);
  }
}

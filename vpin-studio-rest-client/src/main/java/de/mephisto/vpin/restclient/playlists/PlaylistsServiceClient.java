package de.mephisto.vpin.restclient.playlists;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
    PlaylistRepresentation[] playlists = getRestClient().get(API + "playlists", PlaylistRepresentation[].class);
    ArrayList<PlaylistRepresentation> list = new ArrayList<>(Arrays.asList(playlists));
    return list;
  }

  public PlaylistRepresentation getPlaylistTree() {
    return getRestClient().get(API + "playlists/tree", PlaylistRepresentation.class);
  }

  public PlaylistRepresentation getPlaylist(int playlistId) {
    return getRestClient().get(API + "playlists/" + playlistId, PlaylistRepresentation.class);
  }

  public PlaylistRepresentation clearPlaylist(int playlistId) {
    return getRestClient().get(API + "playlists/clear/" + playlistId, PlaylistRepresentation.class);
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


  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "playlists/clearcache", Boolean.class);
  }

  public PlaylistRepresentation savePlaylist(PlaylistRepresentation playlist) throws Exception {
    try {
      return getRestClient().post(API + "playlists/save", playlist, PlaylistRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save playlist: " + e.getMessage(), e);
      throw e;
    }
  }

  public Boolean delete(int id) {
    return getRestClient().delete(API + "playlists/" + id);
  }
}

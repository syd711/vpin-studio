package de.mephisto.vpin.restclient.playlists;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.PlaylistOrder;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletionException;

/*********************************************************************************************************************
 * Playlists
 ********************************************************************************************************************/
public class PlaylistsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

  public PlaylistRepresentation savePlaylist(PlaylistRepresentation playlist) {
    try {
      return getRestClient().post(API + "playlists/save", playlist, PlaylistRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save playlist: " + e.getMessage(), e);
      throw new CompletionException(e);
    }
  }

  public PlaylistOrder savePlaylistOrder(PlaylistOrder order) {
    try {
      return getRestClient().post(API + "playlists/saveOrder", order, PlaylistOrder.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save playlist order: " + e.getMessage(), e);
      throw new CompletionException(e);
    }
  }

  public Boolean delete(int id) {
    return getRestClient().delete(API + "playlists/" + id);
  }
}

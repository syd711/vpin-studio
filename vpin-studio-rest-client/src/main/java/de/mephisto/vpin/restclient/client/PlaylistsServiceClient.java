package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Playlists
 ********************************************************************************************************************/
public class PlaylistsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  PlaylistsServiceClient(VPinStudioClient client) {
    super(client);
  }


  public List<PlaylistRepresentation> getStaticPlaylists() {
    return Arrays.asList(getRestClient().get(API + "playlists/static", PlaylistRepresentation[].class));
  }

  public List<PlaylistRepresentation> getPlaylists() {
    return Arrays.asList(getRestClient().get(API + "playlists", PlaylistRepresentation[].class));
  }
}

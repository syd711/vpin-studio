package de.mephisto.vpin.restclient.wovp;

import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.connectors.wovp.models.WovpPlayer;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*********************************************************************************************************************
 * Wovp
 ********************************************************************************************************************/
public class WOVPServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPServiceClient.class);

  public WOVPServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ApiKeyValidationResponse test(@NonNull String key) {
    return getRestClient().get(API + "wovp/test/" + key, ApiKeyValidationResponse.class);
  }

  public List<WovpPlayer> getPlayers() {
    return Arrays.asList(getRestClient().get(API + "wovp/players", WovpPlayer[].class));
  }

  public boolean clearCache() {
    return getRestClient().get(API + "wovp/clearCache", Boolean.class);
  }

  /**
   * @return true if the table script contains the WoVP telemetry
   */
  public boolean isPatched(int gameId) {
    return getRestClient().get(API + "wovp/patch/" + gameId, Boolean.class);
  }

  /**
   * Adds the WoVP telemetry to the table. This extracts and re-assembles the table and can take a while.
   *
   * @param backup creates a .vpx.bak copy of the original table first
   */
  public WovpPatchResult patch(int gameId, boolean backup) {
    return getRestClient().post(API + "wovp/patch/" + gameId + "?backup=" + backup, new HashMap<>(), WovpPatchResult.class);
  }

  /**
   * Removes the WoVP telemetry from the table.
   */
  public WovpPatchResult unpatch(int gameId, boolean backup) {
    return getRestClient().post(API + "wovp/unpatch/" + gameId + "?backup=" + backup, new HashMap<>(), WovpPatchResult.class);
  }
}

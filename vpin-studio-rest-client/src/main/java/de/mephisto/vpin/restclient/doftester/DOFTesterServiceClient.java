package de.mephisto.vpin.restclient.doftester;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*********************************************************************************************************************
 * DOF Tester
 ********************************************************************************************************************/
public class DOFTesterServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public DOFTesterServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ToySummary getToys(int gameId) {
    try {
      return getRestClient().get(API + "doftester/toys/" + gameId, ToySummary.class);
    }
    catch (Exception e) {
      LOG.error("Failed to get DOF toys for gameId {}: {}", gameId, e.getMessage(), e);
      ToySummary result = new ToySummary();
      result.setError("Failed to get DOF toys: " + e.getMessage());
      return result;
    }
  }

  public boolean testToy(int gameId, String toyName, int durationMs) {
    try {
      Boolean result = getRestClient().post(API + "doftester/test/" + gameId + "/" + toyName + "?durationMs=" + durationMs, null, Boolean.class);
      return Boolean.TRUE.equals(result);
    }
    catch (Exception e) {
      LOG.error("Failed to test DOF toy '{}' for gameId {}: {}", toyName, gameId, e.getMessage(), e);
      return false;
    }
  }

  public boolean testToy(int gameId, String toyName) {
    return testToy(gameId, toyName, 200);
  }

  public boolean reloadConfig() {
    try {
      Boolean result = getRestClient().post(API + "doftester/reload", null, Boolean.class);
      return Boolean.TRUE.equals(result);
    }
    catch (Exception e) {
      LOG.error("Failed to reload DOF tester config: {}", e.getMessage(), e);
      return false;
    }
  }

  public boolean clearCache() {
    return getRestClient().get(API + "doftester/clearcache", Boolean.class);
  }
}

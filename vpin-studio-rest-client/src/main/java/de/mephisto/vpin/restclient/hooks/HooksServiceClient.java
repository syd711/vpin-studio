package de.mephisto.vpin.restclient.hooks;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * Hooks
 ********************************************************************************************************************/
public class HooksServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private HookList hookList;

  public HooksServiceClient(VPinStudioClient client) {
    super(client);
  }


  public HookList getHookList() {
    if (hookList == null) {
      hookList = getRestClient().get(API + "hooks", HookList.class);
    }
    return hookList;
  }

  public HookCommand executeHook(HookCommand cmd) {
    try {
      return getRestClient().post(API + "hooks", cmd, HookCommand.class);
    }
    catch (Exception e) {
      LOG.error("Failed to execute hooks: " + e.getMessage(), e);
      throw e;
    }
  }

  public void clearCache() {
    this.hookList = null;
  }
}

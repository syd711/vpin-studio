package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.util.ini.IniSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

/*********************************************************************************************************************
 * PinVol
 ********************************************************************************************************************/
public class PINemHiServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PINemHiServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean isAutoStartEnabled() {
    return getRestClient().get(API + "pinemhi/autostart", Boolean.class);
  }

  public IniSettings getSettings() {
    return new IniSettings(getRestClient().get(API + "pinemhi/settings", HashMap.class));
  }

  public boolean toggleAutoStart() {
    return getRestClient().get(API + "pinemhi/autostart/toggle", Boolean.class);
  }

  public boolean kill() {
    return getRestClient().get(API + "pinemhi/kill", Boolean.class);
  }

  public boolean isRunning() {
    return getRestClient().get(API + "pinemhi/running", Boolean.class);
  }

  public boolean restart() {
    return getRestClient().get(API + "pinemhi/restart", Boolean.class);
  }

  public void save(IniSettings settings) throws Exception {
    try {
      getRestClient().post(API + "pinemhi/save", settings.getSettings(), HashMap.class);
    } catch (Exception e) {
      throw e;
    }
  }
}

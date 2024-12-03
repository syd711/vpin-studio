package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.pinvol.PinVolTablePreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * PinVol
 ********************************************************************************************************************/
public class PinVolServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);


  private PinVolTablePreferences pinVolTablePreferences;

  public PinVolServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean isAutoStartEnabled() {
    return getRestClient().get(API + "pinvol/autostart", Boolean.class);
  }

  public boolean toggleAutoStart() {
    return getRestClient().get(API + "pinvol/autostart/toggle", Boolean.class);
  }

  public PinVolTablePreferences getPinVolTablePreferences() {
    if (pinVolTablePreferences == null) {
      pinVolTablePreferences = getRestClient().get(API + "pinvol/preferences", PinVolTablePreferences.class);
    }
    return pinVolTablePreferences;
  }

  public void clearCache() {
    this.pinVolTablePreferences = null;
  }

  public boolean kill() {
    return getRestClient().get(API + "pinvol/kill", Boolean.class);
  }

  public boolean setVolume() {
    return getRestClient().get(API + "pinvol/setvolume", Boolean.class);
  }

  public boolean isRunning() {
    return getRestClient().get(API + "pinvol/running", Boolean.class);
  }

  public boolean restart() {
    return getRestClient().get(API + "pinvol/restart", Boolean.class);
  }
}

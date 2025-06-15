package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.pinvol.PinVolUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * PinVol
 ********************************************************************************************************************/
public class PinVolServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);


  private PinVolPreferences pinVolTablePreferences;

  public PinVolServiceClient(VPinStudioClient client) {
    super(client);
  }

  public PinVolPreferences getPinVolTablePreferences() {
    if (pinVolTablePreferences == null) {
      pinVolTablePreferences = getRestClient().get(API + "pinvol/preferences", PinVolPreferences.class);
    }
    return pinVolTablePreferences;
  }

  public PinVolPreferences save(PinVolUpdate update) throws Exception {
    try {
      pinVolTablePreferences = getRestClient().post(API + "pinvol/save", update, PinVolPreferences.class);
      return getPinVolTablePreferences();
    }
    catch (Exception e) {
      LOG.error("Failed to save pinvol settings: " + e.getMessage(), e);
      throw e;
    }
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

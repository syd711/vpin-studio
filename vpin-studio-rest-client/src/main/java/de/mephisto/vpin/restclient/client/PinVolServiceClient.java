package de.mephisto.vpin.restclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * PinVol
 ********************************************************************************************************************/
public class PinVolServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  PinVolServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean isAutoStartEnabled() {
    return getRestClient().get(API + "pinvol/autostart", Boolean.class);
  }

  public boolean toggleAutoStart() {
    return getRestClient().get(API + "pinvol/autostart/toggle", Boolean.class);
  }

  public boolean kill() {
    return getRestClient().get(API + "pinvol/kill", Boolean.class);
  }
}

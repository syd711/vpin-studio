package de.mephisto.vpin.restclient.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Alx
 ********************************************************************************************************************/
public class AlxServiceClient extends VPinStudioClientService {
  public AlxServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AlxSummary getAlxSummary() {
    return getRestClient().get(API + "alx", AlxSummary.class);
  }
}

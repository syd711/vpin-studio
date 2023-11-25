package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Alx
 ********************************************************************************************************************/
public class AlxServiceClient extends VPinStudioClientService {
  AlxServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AlxSummary getAlxSummary() {
    return getRestClient().get(API + "alx", AlxSummary.class);
  }
}

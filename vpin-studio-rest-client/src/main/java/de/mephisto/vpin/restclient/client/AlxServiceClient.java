package de.mephisto.vpin.restclient.client;

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

  public List<TableAlxEntry> getAlxEntries() {
    return Arrays.asList(getRestClient().get(API + "alx", TableAlxEntry[].class));
  }
}

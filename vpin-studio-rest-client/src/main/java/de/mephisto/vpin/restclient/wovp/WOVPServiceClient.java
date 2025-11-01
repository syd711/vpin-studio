package de.mephisto.vpin.restclient.wovp;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Wovp
 ********************************************************************************************************************/
public class WOVPServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPServiceClient.class);

  public WOVPServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean test() {
    return getRestClient().get(API + "wovp/test", Boolean.class);
  }
}

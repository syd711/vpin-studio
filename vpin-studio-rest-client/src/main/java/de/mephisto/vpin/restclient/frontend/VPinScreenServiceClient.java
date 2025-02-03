package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*********************************************************************************************************************
 * Frontend
 ********************************************************************************************************************/
public class VPinScreenServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinScreenServiceClient.class);
  private static final String API_SEGMENT_SCREENS = "screens";

  public VPinScreenServiceClient(VPinStudioClient client) {
    super(client);
  }

  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    return getRestClient().get(API + API_SEGMENT_SCREENS + "/screen/" + screen.name(), FrontendPlayerDisplay.class);
  }

}

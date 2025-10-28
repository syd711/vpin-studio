package de.mephisto.vpin.restclient.notifications;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*********************************************************************************************************************
 * Notifications
 ********************************************************************************************************************/
public class NotificationsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public NotificationsServiceClient(VPinStudioClient client) {
    super(client);
  }


  public boolean test() {
    try {
      return getRestClient().get(API + "notifications/test", Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to emit test notification: {}", e.getMessage(), e);
    }
    return false;
  }
}

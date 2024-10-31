package de.mephisto.vpin.restclient.dof;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.highscores.logging.EventLogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * DOF
 ********************************************************************************************************************/
public class DOFServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(DOFServiceClient.class);

  public DOFServiceClient(VPinStudioClient client) {
    super(client);
  }

  public DOFSettings getSettings() {
    return getRestClient().get(API + "dof", DOFSettings.class);
  }

  public DOFSettings saveSettings(DOFSettings s) throws Exception {
    try {
      return getRestClient().post(API + "dof", s, DOFSettings.class);
    } catch (Exception e) {
      LOG.error("Failed to save dof settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public JobDescriptor sync(boolean wait) {
    return getRestClient().get(API + "dof/sync/" + wait, JobDescriptor.class);
  }
}

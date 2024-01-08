package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * DOF
 ********************************************************************************************************************/
public class DOFServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(DOFServiceClient.class);

  DOFServiceClient(VPinStudioClient client) {
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

  public JobExecutionResult sync() {
    return getRestClient().get(API + "dof/sync", JobExecutionResult.class);
  }
}

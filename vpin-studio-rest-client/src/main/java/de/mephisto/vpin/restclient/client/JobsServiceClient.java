package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.descriptors.JobDescriptor;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Jobs
 ********************************************************************************************************************/
public class JobsServiceClient extends VPinStudioClientService {
  JobsServiceClient(VPinStudioClient client) {
    super(client);
  }
  public List<JobDescriptor> getJobs() {
    return Arrays.asList(getRestClient().get(API + "jobs", JobDescriptor[].class));
  }

}

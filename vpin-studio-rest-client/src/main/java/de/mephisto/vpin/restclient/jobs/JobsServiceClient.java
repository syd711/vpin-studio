package de.mephisto.vpin.restclient.jobs;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Jobs
 ********************************************************************************************************************/
public class JobsServiceClient extends VPinStudioClientService {
  public JobsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<JobDescriptor> getJobs() {
    return Arrays.asList(getRestClient().get(API + "jobs", JobDescriptor[].class));
  }

  public List<JobExecutionResult> getResults() {
    return Arrays.asList(getRestClient().get(API + "jobs/results", JobExecutionResult[].class));
  }

  public boolean dismissAll() {
    return getRestClient().get(API + "jobs/dismiss", Boolean.class);
  }

  public boolean dismiss(@PathVariable("uuid") String uuid) {
    return getRestClient().get(API + "jobs/dismiss/" + uuid, Boolean.class);
  }
}
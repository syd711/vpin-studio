package de.mephisto.vpin.restclient.jobs;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*********************************************************************************************************************
 * Jobs
 ********************************************************************************************************************/
public class JobsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(JobsServiceClient.class);

  public JobsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<JobDescriptor> getJobs() {
    try {
      return Arrays.asList(getRestClient().get(API + "jobs", JobDescriptor[].class));
    }
    catch (Exception e) {
      LOG.error("Failed to read jobs: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public JobDescriptor getJob(String uuid) {
    return getRestClient().get(API + "jobs/job/" + uuid, JobDescriptor.class);
  }

  public boolean dismissAll() {
    return getRestClient().get(API + "jobs/dismiss", Boolean.class);
  }

  public boolean dismiss(@PathVariable("uuid") String uuid) {
    return getRestClient().get(API + "jobs/dismiss/" + uuid, Boolean.class);
  }

  public boolean cancel(@PathVariable("uuid") String uuid) {
    return getRestClient().get(API + "jobs/cancel/" + uuid, Boolean.class);
  }
}

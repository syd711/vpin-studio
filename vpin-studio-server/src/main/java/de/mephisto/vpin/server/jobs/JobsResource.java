package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.tables.descriptors.JobDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "jobs")
public class JobsResource {

  @Autowired
  private JobService jobService;

  @Autowired
  private JobQueue jobQueue;

  @GetMapping
  public List<JobDescriptor> getStatus() {
    return jobQueue.status();
  }

  @GetMapping("/results")
  public List<JobExecutionResult> results() {
    return jobService.getJobResults();
  }

  @GetMapping("/dismiss")
  public boolean dismissAll() {
    jobService.dismissAll();
    return true;
  }

  @GetMapping("/dismiss/{uuid}")
  public boolean dismiss(@PathVariable("uuid") String uuid) {
    jobService.dismiss(uuid);
    return true;
  }
}

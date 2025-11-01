package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
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

  @GetMapping
  public List<JobDescriptor> getJobs() {
    return jobService.getJobs();
  }

  @GetMapping("/dismiss")
  public boolean dismissAll() {
    jobService.dismissAll();
    return true;
  }

  @GetMapping("/job/{uuid}")
  public JobDescriptor getJob(@PathVariable("uuid") String uuid) {
    return jobService.getJob(uuid);
  }

  @GetMapping("/dismiss/{uuid}")
  public boolean dismiss(@PathVariable("uuid") String uuid) {
    jobService.dismiss(uuid);
    return true;
  }

  @GetMapping("/cancel/{uuid}")
  public boolean cancel(@PathVariable("uuid") String uuid) {
    jobService.cancel(uuid);
    return true;
  }

  @GetMapping("/cancelall")
  public boolean cancelAll() {
    jobService.cancelAll();
    return true;
  }
}

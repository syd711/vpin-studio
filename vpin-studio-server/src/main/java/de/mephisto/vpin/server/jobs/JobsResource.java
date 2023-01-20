package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping
  public List<JobDescriptor> status() {
    return JobQueue.getInstance().getElements();
  }
}

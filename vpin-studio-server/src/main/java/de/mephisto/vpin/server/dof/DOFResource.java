package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "dof")
public class DOFResource {
  private final static Logger LOG = LoggerFactory.getLogger(DOFResource.class);

  @Autowired
  private DOFService dofService;

  @GetMapping("sync")
  public JobExecutionResult syncDofConfig() {
    return dofService.sync();
  }

  @GetMapping
  public DOFSettings getSettings() {
    return dofService.getSettings();
  }

  @PostMapping
  public DOFSettings save(@RequestBody DOFSettings settings) {
    return dofService.saveSettings(settings);
  }
}

package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.JobExecutionResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class JobService {

  private List<JobExecutionResult> jobResults = Collections.synchronizedList(new ArrayList<>());

  public List<JobExecutionResult> getJobResults() {
    return new ArrayList<>(jobResults);
  }

  public void addResult(JobExecutionResult result) {
    this.jobResults.add(result);
  }

  public void dismissAll() {
    this.jobResults.clear();
  }

  public void dismiss(String uuid) {
    jobResults.removeIf(jobExecutionResult -> jobExecutionResult.getUuid().equals(uuid));
  }
}

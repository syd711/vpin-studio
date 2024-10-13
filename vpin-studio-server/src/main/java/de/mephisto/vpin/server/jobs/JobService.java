package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {
  private final static Logger LOG = LoggerFactory.getLogger(JobService.class);

  @Autowired
  private JobQueue jobQueue;

  private final List<JobDescriptor> jobList = new ArrayList<>();

  public void dismissAll() {
    List<JobDescriptor> collect = jobList.stream().filter(j -> j.isFinished() || j.isCancelled()).collect(Collectors.toList());
    for (JobDescriptor jobDescriptor : collect) {
      jobList.remove(jobDescriptor);
    }
  }

  public void dismiss(String uuid) {
    Optional<JobDescriptor> job = jobList.stream().filter(j -> j.getUuid().equals(uuid)).findFirst();
    if (job.isPresent()) {
      jobList.remove(job.get());
      LOG.info("Dismissed job \"" + job.get() + "\"");
    }
  }

  public void offer(JobDescriptor descriptor) {
    jobQueue.submit(descriptor);
    jobList.add(descriptor);
    LOG.info("Job list size: " + jobList.size());
  }

  public void cancel(@NonNull String uuid) {
    Optional<JobDescriptor> job = jobList.stream().filter(j -> j.getUuid().equals(uuid)).findFirst();
    if (job.isPresent()) {
      jobQueue.cancel(job.get());
    }
  }

  public void cancel(@NonNull JobType jobType) {
    List<JobDescriptor> jobs = jobList.stream().filter(j -> j.getJobType().equals(jobType)).collect(Collectors.toList());
    for (JobDescriptor job : jobs) {
      if (!job.isCancelled() && !job.isFinished()) {
        jobQueue.cancel(job);
      }
    }
  }

  public List<JobDescriptor> getJobs() {
    for (JobDescriptor jobDescriptor : jobList) {
      jobDescriptor.setCancelable(jobDescriptor.getJob().isCancelable());
    }
    return new ArrayList<>(jobList);
  }
}

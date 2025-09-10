package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

  @Autowired
  private JobQueue jobQueue;

  public void dismissAll() {
    List<JobDescriptor> jobList = getJobs();
    List<JobDescriptor> collect = jobList.stream().filter(j -> j.isFinished() || j.isCancelled()).collect(Collectors.toList());
    for (JobDescriptor jobDescriptor : collect) {
      jobList.remove(jobDescriptor);
    }
  }

  public void dismiss(String uuid) {
    List<JobDescriptor> jobList = getJobs();
    Optional<JobDescriptor> job = jobList.stream().filter(j -> j.getUuid().equals(uuid)).findFirst();
    if (job.isPresent()) {
      jobQueue.cancel(job.get());
    }
  }

  public void offer(JobDescriptor descriptor) {
    jobQueue.submit(descriptor);
  }

  public void cancel(@NonNull String uuid) {
    List<JobDescriptor> jobList = getJobs();
    Optional<JobDescriptor> job = jobList.stream().filter(j -> j.getUuid().equals(uuid)).findFirst();
    if (job.isPresent()) {
      jobQueue.cancel(job.get());
    }
  }

  public void cancel(@NonNull JobType jobType) {
    List<JobDescriptor> jobList = getJobs();
    List<JobDescriptor> jobs = jobList.stream().filter(j -> j.getJobType().equals(jobType)).collect(Collectors.toList());
    for (JobDescriptor job : jobs) {
      if (!job.isCancelled() && !job.isFinished()) {
        jobQueue.cancel(job);
      }
    }
  }

  public List<JobDescriptor> getJobs() {
    for (JobDescriptor jobDescriptor : jobQueue.getJobs()) {
      jobDescriptor.setCancelable(jobDescriptor.getJob().isCancelable());
    }
    return jobQueue.getJobs();
  }

  public JobDescriptor getJob(String uuid) {
    List<JobDescriptor> jobList = getJobs();
    Optional<JobDescriptor> job = jobList.stream().filter(j -> j.getUuid().equals(uuid)).findFirst();
    if (job.isPresent()) {
      return job.get();
    }
    return null;
  }
}

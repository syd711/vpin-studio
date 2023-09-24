package de.mephisto.vpin.restclient.tables.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobType;

public class JobDescriptor {
  private String uuid;
  private String imageUrl;
  private String title;
  private String description;
  private JobType jobType;
  private double progress;
  private String status;
  private int gameId;

  @JsonIgnore
  private Job job;

  public JobDescriptor() {

  }

  public JobDescriptor(JobType jobType, String uuid) {
    this.jobType = jobType;
    this.uuid = uuid;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public double getProgress() {
    return progress;
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setJobType(JobType jobType) {
    this.jobType = jobType;
  }

  public JobType getJobType() {
    return jobType;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "Job '" + this.getTitle() + "'";
  }

  public void execute(VPinStudioClient client) {
    //only for client jobs
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JobDescriptor that = (JobDescriptor) o;

    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }
}

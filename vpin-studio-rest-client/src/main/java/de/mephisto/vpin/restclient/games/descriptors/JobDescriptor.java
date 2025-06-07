package de.mephisto.vpin.restclient.games.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobType;

import java.util.UUID;

public class JobDescriptor {
  private String uuid;
  private String title;
  private String status;
  private JobType jobType;
  private double progress;
  private String error;
  private String errorHint;
  private int gameId;
  private boolean cancelled;
  private boolean cancelable;

  private int tasksExecuted;
  private Object userData;

  public Object getUserData() {
    return userData;
  }

  public void setUserData(Object userData) {
    this.userData = userData;
  }

  @JsonIgnore
  private Job job;

  public String getErrorHint() {
    return errorHint;
  }

  public void setErrorHint(String errorHint) {
    this.errorHint = errorHint;
  }


  public int getTasksExecuted() {
    return tasksExecuted;
  }

  public void setTasksExecuted(int tasksExecuted) {
    this.tasksExecuted = tasksExecuted;
  }

  public JobDescriptor() {

  }

  public boolean isCancelable() {
    return cancelable;
  }

  public void setCancelable(boolean cancelable) {
    this.cancelable = cancelable;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public JobDescriptor(JobType jobType) {
    this.jobType = jobType;
    this.uuid = UUID.randomUUID().toString();
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return "Job '" + this.getTitle() + "'";
  }

  public void execute(VPinStudioClient client) {
  }

  public boolean isErrorneous() {
    return error != null;
  }

  public boolean isFinished() {
    return error != null || progress >= 1;
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

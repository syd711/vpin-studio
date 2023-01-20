package de.mephisto.vpin.restclient;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JobDescriptor {
  private String imageUrl;
  private String title;
  private String description;

  @JsonIgnore
  private Job job;

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
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
}

package de.mephisto.vpin.connectors.wovp.models;

import java.util.ArrayList;
import java.util.List;

public class UploadResponseData {
  private String photoTempId;
  private List<UploadResponsePhoto> photos;
  private boolean hasSucceeded;
  private List<String> errors = new ArrayList<>();


  public boolean isHasSucceeded() {
    return hasSucceeded;
  }

  public void setHasSucceeded(boolean hasSucceeded) {
    this.hasSucceeded = hasSucceeded;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public String getPhotoTempId() {
    return photoTempId;
  }

  public void setPhotoTempId(String photoTempId) {
    this.photoTempId = photoTempId;
  }

  public List<UploadResponsePhoto> getPhotos() {
    return photos;
  }

  public void setPhotos(List<UploadResponsePhoto> photos) {
    this.photos = photos;
  }
}

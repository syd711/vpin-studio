package de.mephisto.vpin.restclient.converter;

public class MediaOperationResult {
  private MediaOperation mediaOperation;
  private String result;

  public MediaOperation getMediaOperation() {
    return mediaOperation;
  }

  public void setMediaOperation(MediaOperation mediaOperation) {
    this.mediaOperation = mediaOperation;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}

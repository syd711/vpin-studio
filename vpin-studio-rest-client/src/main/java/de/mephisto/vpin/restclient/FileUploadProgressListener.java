package de.mephisto.vpin.restclient;

public interface FileUploadProgressListener {
  void process(double percent);
}
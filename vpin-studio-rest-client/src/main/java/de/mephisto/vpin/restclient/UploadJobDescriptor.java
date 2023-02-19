package de.mephisto.vpin.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;

public class UploadJobDescriptor extends JobDescriptor {
  private final static Logger LOG = LoggerFactory.getLogger(UploadJobDescriptor.class);

  @NonNull
  private final String url;
  @NonNull
  private final File target;

  public UploadJobDescriptor(@NonNull String url, @NonNull File target) {
    this.url = url;
    this.target = target;
  }

  @Override
  public void execute(VPinStudioClient client) {
    try {
      client.download(url, target);
      LOG.info("Download finished, written " + target.getAbsolutePath());
    } catch (Exception e) {
      LOG.error("Download failed: " + e.getMessage(), e);
    }
  }
}

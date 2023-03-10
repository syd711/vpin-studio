package de.mephisto.vpin.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;

import static de.mephisto.vpin.restclient.JobType.VPA_DOWNLOAD_TO_FILESYSTEM;

public class DownloadJobDescriptor extends JobDescriptor {
  private final static Logger LOG = LoggerFactory.getLogger(DownloadJobDescriptor.class);

  @NonNull
  private final String url;
  @NonNull
  private final File target;

  public DownloadJobDescriptor(@NonNull String url, @NonNull File target, @NonNull String uuid) {
    super(VPA_DOWNLOAD_TO_FILESYSTEM, uuid);
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

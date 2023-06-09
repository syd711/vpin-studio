package de.mephisto.vpin.restclient.descriptors;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.util.UUID;

import static de.mephisto.vpin.restclient.jobs.JobType.ARCHIVE_DOWNLOAD_TO_FILESYSTEM;

public class DownloadJobDescriptor extends JobDescriptor {
  private final static Logger LOG = LoggerFactory.getLogger(DownloadJobDescriptor.class);

  @NonNull
  private final String url;
  @NonNull
  private final File target;

  public DownloadJobDescriptor(@NonNull String url, @NonNull File target) {
    super(ARCHIVE_DOWNLOAD_TO_FILESYSTEM, UUID.randomUUID().toString());
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

package de.mephisto.vpin.restclient.games.descriptors;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import static de.mephisto.vpin.restclient.jobs.JobType.ARCHIVE_DOWNLOAD_TO_FILESYSTEM;

public class DownloadJobDescriptor extends JobDescriptor {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @NonNull
  private final String url;
  @NonNull
  private final File target;

  public DownloadJobDescriptor(@NonNull String url, @NonNull File target) {
    super(ARCHIVE_DOWNLOAD_TO_FILESYSTEM);
    this.url = url;
    this.target = target;
  }

  @Override
  public void execute(VPinStudioClient client) {
    try {
      client.download(url, target);
      LOG.info("Download finished, written " + target.getAbsolutePath());
    }
    catch (Exception e) {
      setError(e.getMessage());
      LOG.error("Download failed: " + e.getMessage(), e);
    }

    setProgress(1);
    setGameId(getGameId());
  }
}

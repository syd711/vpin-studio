package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.util.RarUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class DOFSynchronizationJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(DOFSynchronizationJob.class);

  @NonNull
  private final DOFSettings settings;
  @NonNull
  private String workingDir;

  public DOFSynchronizationJob(@NonNull DOFSettings dofSettings, @NonNull String workingDir) {
    this.settings = dofSettings;
    this.workingDir = workingDir;
  }

  @Override
  public void execute(JobDescriptor result) {
    try {
      String downloadUrl = "https://configtool.vpuniverse.com/api.php?query=getconfig&apikey=" + settings.getApiKey();
      LOG.info("Downloading " + "https://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");
      result.setStatus("Downloading " + "https://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");

      File zipFile = new File(workingDir, "directoutputconfig.zip");
      if (zipFile.exists()) {
        zipFile.delete();
      }

      String output = downloadViaWebClient(downloadUrl, zipFile);

      LOG.info("DOF download finished: {}", output);

      if (zipFile.exists()) {
        LOG.info("Downloaded file " + zipFile.getAbsolutePath());
        if (!StringUtils.isEmpty(settings.getInstallationPath())) {
          File targetFolder = new File(settings.getInstallationPath(), "Config");
          if (!targetFolder.exists()) {
            result.setError("Invalid target folder for synchronization: " + targetFolder.getAbsolutePath());
            return;
          }
          LOG.info("Extracting DOF config folder " + settings.getInstallationPath());
          result.setStatus("Extracting DOF config folder " + settings.getInstallationPath());
          RarUtil.unrar(zipFile, targetFolder);
        }
      }
      else {
        LOG.info("Failed to download DOF configuration, please check your API key !");
        result.setError("Failed to download DOF configuration");
      }

      if (result.isCancelled()) {
        return;
      }

      result.setProgress(1);
    }
    catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
      result.setError("Failed to execute download: " + e.getMessage());
    }
    finally {
      LOG.info("DOF sync job finished.");
    }
  }

  protected String downloadViaWebClient(String downloadUrl, File zipFile) throws Exception {
    try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX)) {
      UnexpectedPage downloadPage = webClient.getPage(downloadUrl);
      try (InputStream in = downloadPage.getInputStream()) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile)) {
          StreamUtils.copy(in, fileOutputStream);
        }
      }
    }
    return "file downloaded";
  }

  protected String downloadViaDownloaderVbs(String downloadUrl, File zipFile) throws Exception {
    SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("cscript", "downloader.vbs", "\"" + downloadUrl + "\"", "\"" + zipFile.getAbsolutePath() + "\""), false);
    executor.setDir(new File(workingDir));
    executor.executeCommand();

    Thread.sleep(500);

    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    return standardOutputFromCommand.toString();
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    Job.super.cancel(jobDescriptor);
    LOG.info("Cancelled " + this);
  }
}

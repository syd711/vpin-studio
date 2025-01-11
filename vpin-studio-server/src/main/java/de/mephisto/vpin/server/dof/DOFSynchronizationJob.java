package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.util.RarUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Arrays;

public class DOFSynchronizationJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(DOFSynchronizationJob.class);

  @NonNull
  private final DOFSettings settings;
  private HttpsURLConnection connection;

  public DOFSynchronizationJob(@NonNull DOFSettings dofSettings) {
    this.settings = dofSettings;
  }

  @Override
  public void execute(JobDescriptor result) {
    try {
      String downloadUrl = "https://configtool.vpuniverse.com/api.php?query=getconfig&apikey=" + settings.getApiKey();
      LOG.info("Downloading " + "https://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");
      result.setStatus("Downloading " + "https://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");

      File zipFile = new File(SystemService.RESOURCES, "directoutputconfig.zip");
      if (zipFile.exists()) {
        zipFile.delete();
      }

      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("cscript", "downloader.vbs", "\"" + downloadUrl + "\"", "\"" + zipFile.getAbsolutePath() + "\""), false);
      executor.setDir(new File(SystemService.RESOURCES));
      executor.executeCommand();

      Thread.sleep(500);

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      LOG.info("DOF download finished: {}", standardOutputFromCommand);

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

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    Job.super.cancel(jobDescriptor);

    try {
      if (connection != null) {
        connection.disconnect();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to cancel DOF job: " + e.getMessage());
    }
    finally {
      jobDescriptor.setError("The job has been cancelled");
    }
    LOG.info("Cancelled " + this);
  }
}

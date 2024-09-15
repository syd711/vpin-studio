package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.commons.utils.ZipUtil;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DOFSynchronizationJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(DOFSynchronizationJob.class);

  @NonNull
  private final DOFSettings settings;
  private HttpURLConnection connection;

  public DOFSynchronizationJob(@NonNull DOFSettings dofSettings) {
    this.settings = dofSettings;
  }

  @Override
  public void execute(JobDescriptor result) {
    try {
      String downloadUrl = "http://configtool.vpuniverse.com/api.php?query=getconfig&apikey=" + settings.getApiKey();
      LOG.info("Downloading " + "http://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");
      result.setStatus("Downloading " + "http://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");
      URL url = new URL(downloadUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File zipFile = new File(SystemService.RESOURCES, "directoutputconfig.zip");
      if (zipFile.exists()) {
        zipFile.delete();
      }
      FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();
      connection = null;

      if (new String(dataBuffer).contains("API")) {
        zipFile.delete();
        result.setError(new String(dataBuffer));
        return;
      }

      if (result.isCancelled()) {
        return;
      }

      LOG.info("Downloaded file " + zipFile.getAbsolutePath());
      if (!StringUtils.isEmpty(settings.getInstallationPath())) {
        File targetFolder = new File(settings.getInstallationPath(), "Config");
        if (!targetFolder.exists()) {
          result.setError("Invalid target folder for synchronization: " + targetFolder.getAbsolutePath());
          return;
        }
        LOG.info("Extracting DOF config for 64-bit folder " + settings.getInstallationPath());
        result.setStatus("Extracting DOF config for 64-bit folder " + settings.getInstallationPath());
        ZipUtil.unzip(zipFile, targetFolder);
      }

      if (result.isCancelled()) {
        return;
      }

      if (!StringUtils.isEmpty(settings.getInstallationPath32())) {
        File targetFolder = new File(settings.getInstallationPath32(), "Config");
        if (!targetFolder.exists()) {
          result.setError("Invalid target folder for synchronization: " + targetFolder.getAbsolutePath());
          return;
        }
        LOG.info("Extracting DOF config for 32-bit folder " + settings.getInstallationPath32());
        result.setStatus("Extracting DOF config for 32-bit folder " + settings.getInstallationPath32());
        ZipUtil.unzip(zipFile, targetFolder);
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

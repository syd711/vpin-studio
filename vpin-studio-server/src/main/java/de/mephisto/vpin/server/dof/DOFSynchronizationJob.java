package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.commons.utils.ZipUtil;
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
  private final DOFService dofService;
  @NonNull
  private final DOFSettings settings;

  public DOFSynchronizationJob(@NonNull DOFService dofService, @NonNull DOFSettings dofSettings) {
    this.dofService = dofService;
    this.settings = dofSettings;
  }

  @Override
  public JobExecutionResult execute() {
    try {
      String downloadUrl = "http://configtool.vpuniverse.com/api.php?query=getconfig&apikey=" + settings.getApiKey();
      LOG.info("Downloading " + "http://configtool.vpuniverse.com/api.php?query=getconfig&apikey=XXXXXXXXXXX");
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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

      if (new String(dataBuffer).contains("API")) {
        zipFile.delete();
        return JobExecutionResultFactory.error(new String(dataBuffer));
      }

      LOG.info("Downloaded file " + zipFile.getAbsolutePath());

      if (!StringUtils.isEmpty(settings.getInstallationPath())) {
        File targetFolder = new File(settings.getInstallationPath(), "Config");
        if (!targetFolder.exists()) {
          return JobExecutionResultFactory.error("Invalid target folder for synchronization: " + targetFolder.getAbsolutePath());
        }
        LOG.info("Extracting DOF config for 64-bit folder " + settings.getInstallationPath());
        ZipUtil.unzip(zipFile, targetFolder);
      }

      if (!StringUtils.isEmpty(settings.getInstallationPath32())) {
        File targetFolder = new File(settings.getInstallationPath32(), "Config");
        if (!targetFolder.exists()) {
          return JobExecutionResultFactory.error("Invalid target folder for synchronization: " + targetFolder.getAbsolutePath());
        }
        LOG.info("Extracting DOF config for 32-bit folder " + settings.getInstallationPath32());
        ZipUtil.unzip(zipFile, targetFolder);
      }
    } catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("Failed to execute download: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();

  }

  @Override
  public double getProgress() {
    return 0;
  }

  @Override
  public String getStatus() {
    return "Synchronizing DOF Settings";
  }
}

package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.WindowsShortcut;
import de.mephisto.vpin.server.util.ZipUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class DOFService {
  private final static Logger LOG = LoggerFactory.getLogger(DOFService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public DOFSettings saveSettings(DOFSettings settings) {
    try {
      preferencesService.savePreference(PreferenceNames.DOF_SETTINGS, settings);
      return getSettings();
    } catch (Exception e) {
      LOG.error("Saving dof settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving dof settings failed: " + e.getMessage());
    }
  }

  public DOFSettings getSettings() {
    try {
      DOFSettings settings = preferencesService.getJsonPreference(PreferenceNames.DOF_SETTINGS, DOFSettings.class);
      settings.setValidDOFFolder(settings.getInstallationPath() != null && new File(settings.getInstallationPath(), "DirectOutput.dll").exists());
      if (StringUtils.isEmpty(settings.getInstallationPath())) {
        File autoResolvedFolder = getDOFInstallationFolder();
        if (autoResolvedFolder != null && autoResolvedFolder.exists() && new File(autoResolvedFolder, "DirectOutput.dll").exists()) {
          settings.setValidDOFFolder(true);
          settings.setInstallationPath(autoResolvedFolder.getAbsolutePath());
        }
      }
      return settings;
    } catch (Exception e) {
      LOG.error("Getting dof settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Get of settings failed: " + e.getMessage());
    }
  }

  public JobExecutionResult sync() {
    try {
      String downloadUrl = "http://configtool.vpuniverse.com/api.php?query=getconfig&apikey=" + getSettings().getApiKey();
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

      File targetFolder = new File(getDOFInstallationFolder(), "Config");
      if (!targetFolder.exists()) {
        return JobExecutionResultFactory.error("Invalid target folder for synchronization: " + targetFolder.getAbsolutePath());
      }

      ZipUtil.unzip(zipFile, targetFolder);
    } catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("Failed to execute download: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }

  private File getDOFInstallationFolder() {
    try {
      GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
      File backglassServerDirectory = defaultGameEmulator.getBackglassServerDirectory();
      File dofConfigFolder = new File(backglassServerDirectory, "Plugins64/DirectOutputx64.lnk");
      LOG.info("Checking DOF config folder " + dofConfigFolder.getAbsolutePath());

      boolean potentialValidLink = WindowsShortcut.isPotentialValidLink(dofConfigFolder);
      if (!potentialValidLink) {
        LOG.info("Checking DOF config folder " + dofConfigFolder.getAbsolutePath());
        dofConfigFolder = new File(backglassServerDirectory, "Plugins64/DirectOutput.lnk");
        potentialValidLink = WindowsShortcut.isPotentialValidLink(dofConfigFolder);
      }
      if (!potentialValidLink) {
        LOG.error("Failed to determine DOF zipFile folder.");
        return null;
      }

      WindowsShortcut windowsShortcut = new WindowsShortcut(dofConfigFolder);
      return new File(windowsShortcut.getRealFilename());
    } catch (Exception e) {
      LOG.error("Failed to resolve DOF path: " + e.getMessage());
    }
    return null;
  }
}
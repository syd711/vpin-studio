package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class DOFService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DOFService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private JobService jobService;

  public DOFSettings saveSettings(DOFSettings settings) {
    try {
      preferencesService.savePreference(settings);
      return getSettings();
    }
    catch (Exception e) {
      LOG.error("Saving dof settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving dof settings failed: " + e.getMessage());
    }
  }

  public DOFSettings getSettings() {
    try {
      DOFSettings settings = preferencesService.getJsonPreference(PreferenceNames.DOF_SETTINGS, DOFSettings.class);
      settings.setValidDOFFolder(StringUtils.isEmpty(settings.getInstallationPath()) || new File(settings.getInstallationPath(), "config").exists());
      return settings;
    }
    catch (Exception e) {
      LOG.error("Getting dof settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Get of settings failed: " + e.getMessage());
    }
  }

  public JobDescriptor asyncSync() {
    DOFSynchronizationJob job = new DOFSynchronizationJob(getSettings(), SystemService.RESOURCES);
    JobDescriptor jobDescriptor = new JobDescriptor(JobType.DOF_SYNC);
    jobDescriptor.setTitle("Synchronizing DOF Settings");
    jobDescriptor.setJob(job);

    jobService.offer(jobDescriptor);
    LOG.info("Offered DOF Sync job.");
    return jobDescriptor;
  }

  public JobDescriptor sync(boolean wait) {
    if (wait) {
      DOFSynchronizationJob job = new DOFSynchronizationJob(getSettings(), SystemService.RESOURCES);
      JobDescriptor result = new JobDescriptor();
      result.setTitle("Synchronizing DOF Settings");
      result.setJob(job);
      job.execute(result);
      return result;
    }

    return asyncSync();
  }

  private boolean isValidInstallation(DOFSettings settings) {
    if (StringUtils.isEmpty(settings.getInstallationPath())) {
      return false;
    }
    return new File(settings.getInstallationPath(), "config").exists();
  }

  private boolean doSync(String installationPath, int interval) {
    File configFolder = new File(installationPath, "Config");
    File mappingsFile = new File(configFolder, "tablemappings.xml");

    if (mappingsFile.exists()) {
      LocalTime time = LocalDateTime.ofInstant(new Date(mappingsFile.lastModified()).toInstant(), ZoneId.systemDefault()).toLocalTime();
      LocalTime plus = time.plusHours(interval * 24L);
      if (plus.isBefore(LocalTime.now())) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public File getInstallationFolder() {
    DOFSettings settings = preferencesService.getJsonPreference(PreferenceNames.DOF_SETTINGS, DOFSettings.class);
    if (StringUtils.isEmpty(settings.getInstallationPath())) {
      return null;
    }

    return new File(settings.getInstallationPath());
  }

  public boolean isValid() {
    return getInstallationFolder() != null && getInstallationFolder().exists();
  }

  public ComponentSummary getComponentSummary() {
    ComponentSummary summary = new ComponentSummary();
    summary.setType(ComponentType.doflinx);

    if (isValid()) {
      File tableMappingsfile = new File(getInstallationFolder(), "Config/tablemappings.xml");
      if (tableMappingsfile.exists()) {
        summary.addEntry("tablemappings.xml", tableMappingsfile.getAbsolutePath());
        summary.addEntry("Last Modified", DateUtil.formatDateTime(new Date(tableMappingsfile.lastModified())));
      }
    }
    else {
      summary.addEntry("tablemappings.xml", "-");
      summary.addEntry("Last Modified", "-");
    }

    return summary;
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      try {
        long start = System.currentTimeMillis();
        Thread.currentThread().setName("DOF Synchronizer");
        DOFSettings settings = getSettings();
        if (!settings.getSyncEnabled()) {
          return;
        }

        int interval = settings.getInterval();
        String key = settings.getApiKey();
        if (interval > 0 && !StringUtils.isEmpty(key)) {
          boolean doSync = false;
          if (isValidInstallation(settings)) {
            doSync = doSync(settings.getInstallationPath(), interval);
          }
          else {
            LOG.info("Skipped DOF sync, because the config is not valid or set.");
          }

          if (doSync) {
            sync(false);
            LOG.info("DOF Synchronizer finished, took " + (System.currentTimeMillis() - start) + "ms.");
          }
        }
      }
      catch (Exception e) {
        LOG.error("Failed to synchronize DOF settings: " + e.getMessage(), e);
      }
    }).start();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

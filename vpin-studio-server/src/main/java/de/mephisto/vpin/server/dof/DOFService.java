package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.preferences.PreferencesService;
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
import java.util.UUID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class DOFService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DOFService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private JobQueue jobQueue;

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
      settings.setValidDOFFolder(StringUtils.isEmpty(settings.getInstallationPath()) || new File(settings.getInstallationPath(), "DirectOutput.dll").exists());
      settings.setValidDOFFolder32(StringUtils.isEmpty(settings.getInstallationPath32()) || new File(settings.getInstallationPath32(), "DirectOutput.dll").exists());
      return settings;
    } catch (Exception e) {
      LOG.error("Getting dof settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Get of settings failed: " + e.getMessage());
    }
  }

  public JobExecutionResult asyncSync() {
    DOFSynchronizationJob job = new DOFSynchronizationJob(this, getSettings());
    JobDescriptor jobDescriptor = new JobDescriptor(JobType.DOF_SYNC, UUID.randomUUID().toString());

    jobDescriptor.setTitle("Synchronizing DOF Settings");
    jobDescriptor.setDescription("Synchronizing with http://configtool.vpuniverse.com");
    jobDescriptor.setJob(job);
    jobDescriptor.setStatus(job.getStatus());

    jobQueue.offer(jobDescriptor);
    LOG.info("Offered DOF Sync job.");

    return JobExecutionResultFactory.empty();
  }

  public JobExecutionResult sync(boolean wait) {
    if (wait) {
      DOFSynchronizationJob job = new DOFSynchronizationJob(this, getSettings());
      return job.execute();
    }

    return asyncSync();
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
          if (!StringUtils.isEmpty(settings.getInstallationPath())) {
            doSync = doSync(settings.getInstallationPath(), interval);
          }
          if (!doSync && !StringUtils.isEmpty(settings.getInstallationPath32())) {
            doSync = doSync(settings.getInstallationPath32(), interval);
          }

          if (doSync) {
            sync(false);
            LOG.info("DOF Synchronizer finished, took " + (System.currentTimeMillis() - start) + "ms.");
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to synchronize DOF settings: " + e.getMessage(), e);
      }
    }).start();
  }
}

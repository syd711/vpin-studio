package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.commons.OrbitalPins;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.JCodec;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PupPacksService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PupPacksService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private JobQueue jobQueue;

  private final Map<String, PupPack> pupPackFolders = new ConcurrentHashMap<>();

  public PupPack getMenuPupPack() {
    File pupPackFolder = new File(systemService.getPinUPSystemFolder(), "PUPVideos");
    File menuPupPackFolder = new File(pupPackFolder, "PinUpMenu");
    return loadPupPack(menuPupPackFolder);
  }

  public boolean delete(@NonNull Game game) {
    PupPack pupPack = getPupPack(game);
    if (pupPack != null) {
      if(pupPack.delete()) {
        LOG.info("Deleting " + pupPack.getPupPackFolder().getAbsolutePath());
        clearCache();
      }
    }
    return false;
  }

  @Nullable
  public PupPack getPupPack(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getPupPackName()) && pupPackFolders.containsKey(game.getPupPackName().toLowerCase())) {
      return pupPackFolders.get(game.getPupPackName().toLowerCase());
    }
    if (!StringUtils.isEmpty(game.getRom()) && pupPackFolders.containsKey(game.getRom().toLowerCase())) {
      return pupPackFolders.get(game.getRom().toLowerCase());
    }
    if (!StringUtils.isEmpty(game.getRomAlias()) && pupPackFolders.containsKey(game.getRomAlias().toLowerCase())) {
      return pupPackFolders.get(game.getRomAlias().toLowerCase());
    }
    if (!StringUtils.isEmpty(game.getTableName()) && pupPackFolders.containsKey(game.getTableName().toLowerCase())) {
      return pupPackFolders.get(game.getTableName().toLowerCase());
    }
    return null;
  }

  private void refresh() {
    this.pupPackFolders.clear();
    long start = System.currentTimeMillis();
    File pupPackFolder = new File(systemService.getPinUPSystemFolder(), "PUPVideos");
    if (pupPackFolder.exists()) {
      File[] pupPacks = pupPackFolder.listFiles((dir, name) -> new File(dir, name).isDirectory());
      if (pupPacks != null) {
        for (File packFolder : pupPacks) {
          loadPupPack(packFolder);
        }
      }
    }
    else {
      LOG.error("PUP pack folder " + pupPackFolder.getAbsolutePath() + " does not exist.");
    }
    long end = System.currentTimeMillis();
    LOG.info("Finished PUP pack scan, found " + pupPackFolders.size() + " packs (" + (end - start) + "ms)");
  }

  public PupPack loadPupPack(File packFolder) {
    PupPack pupPack = new PupPack(packFolder);
    if (new File(packFolder, "scriptonly.txt").exists()) {
      pupPack.setScriptOnly(true);
    }

    pupPack.load();

    if ((OrbitalPins.isOrbitalPin(packFolder.getName()) || !FileUtils.listFiles(packFolder, new String[]{"mp4", "png"}, true).isEmpty())) {
//      LOG.info("Loaded PUP Pack " + packFolder.getName());
      pupPackFolders.put(packFolder.getName().toLowerCase(), pupPack);
    }
    return pupPack;
  }

  public JobExecutionResult option(Game game, String option) {
    PupPack pupPack = getPupPack(game);
    return pupPack.executeOption(option);
  }

  public boolean setPupPackEnabled(Game game, boolean enable) {
    if (enable) {
      pinUPConnector.updateGamesField(game, "LaunchCustomVar", "");
    }
    else {
      pinUPConnector.updateRom(game, game.getRom());
      pinUPConnector.updateGamesField(game, "LaunchCustomVar", "HIDEPUP");
    }
    return true;
  }

  public boolean isPupPackDisabled(@Nullable Game game) {
    if (game == null) {
      return false;
    }

    String effectiveRom = game.getRom();
    if (StringUtils.isEmpty(effectiveRom)) {
      return false;
    }

    String rom = pinUPConnector.getGamesStringValue(game, "ROM");
    String custom = pinUPConnector.getGamesStringValue(game, "LaunchCustomVar");
    return rom != null && !StringUtils.isEmpty(custom) && custom.equals("HIDEPUP");
  }

  public void writePUPHideNext(Game game) {
    try {
      PupPack pupPack = getPupPack(game);
      if (pupPack != null) {
        File pupPackFolder = pupPack.getPupPackFolder();
        if (pupPackFolder.exists()) {
          File hideNextFile = new File(pupPackFolder, "PUPHideNext.txt");
          if (!hideNextFile.exists()) {
            FileUtils.touch(hideNextFile);
            LOG.info("Written PUPHideNext.txt for " + game.getRom());
          }
          else {
            LOG.info("PUPHideNext.txt already exists for " + game.getRom());
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to create PUPHideNext.txt for " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }
  }

  public JobExecutionResult installPupPack(Game game, File pupTmpArchive) {
    File pupVideosFolder = new File(systemService.getPinUPSystemFolder(), "PUPVideos");
    if (!pupVideosFolder.exists()) {
      return JobExecutionResultFactory.error("Invalid target folder: " + pupVideosFolder.getAbsolutePath());
    }

    LOG.info("Extracting PUP pack to " + pupVideosFolder.getAbsolutePath());
    if (!pupVideosFolder.exists()) {
      if (!pupVideosFolder.mkdirs()) {
        return JobExecutionResultFactory.error("Failed to create PUP pack directory " + pupVideosFolder.getAbsolutePath());
      }
    }

    PupPackInstallerJob job = new PupPackInstallerJob(this, pupTmpArchive, pupVideosFolder, game);
    JobDescriptor jobDescriptor = new JobDescriptor(JobType.PUP_INSTALL, UUID.randomUUID().toString());

    jobDescriptor.setTitle("Installing PUP pack for \"" + game.getGameDisplayName() + "\"");
    jobDescriptor.setDescription("Unzipping " + pupTmpArchive.getName());
    jobDescriptor.setJob(job);
    jobDescriptor.setStatus(job.getStatus());

    jobQueue.offer(jobDescriptor);

    return JobExecutionResultFactory.empty();
  }

  public void exportDefaultPicture(@NonNull PupPack pupPack, @NonNull File target) {
    File defaultPicture = new File(target.getParentFile(), SystemService.DEFAULT_BACKGROUND);
    if (defaultPicture.exists() && defaultPicture.length() > 0) {
      return;
    }

    if (defaultPicture.exists() && defaultPicture.length() == 0) {
      return;
    }

    if (!target.getParentFile().exists()) {
      target.getParentFile().mkdirs();
    }

    PupDefaultVideoResolver resolver = new PupDefaultVideoResolver(pupPack);
    File defaultVideo = resolver.findDefaultVideo();
    if (defaultVideo != null && defaultVideo.exists()) {
      if (defaultVideo.getName().endsWith(".png") || defaultVideo.getName().endsWith(".jpg") || defaultVideo.getName().endsWith(".jpeg")) {
        try {
          FileUtils.copyFile(defaultVideo, defaultPicture);
        } catch (IOException e) {
          LOG.error("failed to copy: " + e.getMessage());
        }
      }
      else {
        if (JCodec.export(defaultVideo, defaultPicture)) {
          LOG.info("Successfully extracted default background image " + defaultPicture.getAbsolutePath());
        }
      }
    }
  }

  public boolean clearCache() {
    refresh();
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      try {
        Thread.currentThread().setName("PUP Pack Scanner");
        refresh();
      } catch (Exception e) {
        LOG.error("Error in PUP Pack Scanner thread: " + e.getMessage(), e);
      }
    }).start();
  }
}

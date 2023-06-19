package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.commons.OrbitalPins;
import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.jobs.JobQueue;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PupPacksService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PupPacksService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private JobQueue jobQueue;

  private final Map<String, PupPack> pupPackFolders = new ConcurrentHashMap<>();

  @Nullable
  public PupPack getPupPack(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom()) && pupPackFolders.containsKey(game.getRom().toLowerCase())) {
      return pupPackFolders.get(game.getRom().toLowerCase());
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

  public void loadPupPack(File packFolder) {
    if (new File(packFolder, "scriptonly.txt").exists()) {
      return;
    }

    PupPack pupPack = new PupPack(packFolder);
    pupPack.load();

    if (!pupPack.getScreensPup().exists() && !pupPack.getOptions().isEmpty()) {
      LOG.info("No config files found for pup pack \"" + packFolder.getName() + "\", executing first option.");
      String s = pupPack.getOptions().get(0);
      pupPack.executeOption(s);
    }

    if ((pupPack.getScreensPup().exists() && pupPack.getTriggersPup().exists()) ||
        (OrbitalPins.isOrbitalPin(packFolder.getName()) && !FileUtils.listFiles(packFolder, new String[]{"mp4"}, true).isEmpty())) {
      pupPackFolders.put(packFolder.getName().toLowerCase(), pupPack);
    }
  }

  public JobExecutionResult option(Game game, String option) {
    PupPack pupPack = getPupPack(game);
    return pupPack.executeOption(option);
  }

  public boolean setPupPackEnabled(Game game, boolean enable) {
    return false;
  }

  public boolean isPupPackEnabled(Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {

    }
    return false;
  }

  public JobExecutionResult installPupPack(Game game, File out) {
    File pupPackFolder = game.getPupPackFolder();
    if (pupPackFolder == null) {
      return JobExecutionResultFactory.error("Missing ROM name for game.");
    }

    LOG.info("Extracting archive to " + pupPackFolder.getAbsolutePath());
    if (!pupPackFolder.exists()) {
      if (!pupPackFolder.mkdirs()) {
        return JobExecutionResultFactory.error("Failed to create PUP pack directory " + pupPackFolder.getAbsolutePath());
      }
    }

    PupPackInstallerJob job = new PupPackInstallerJob(this, out, pupPackFolder, game);
    JobDescriptor jobDescriptor = new JobDescriptor(JobType.PUP_INSTALL, UUID.randomUUID().toString());

    jobDescriptor.setTitle("Installing PUP pack for \"" + game.getGameDisplayName() + "\"");
    jobDescriptor.setDescription("Unzipping " + out.getName());
    jobDescriptor.setJob(job);
    jobDescriptor.setStatus(job.getStatus());

    jobQueue.offer(jobDescriptor);

    return JobExecutionResultFactory.empty();
  }


  @Nullable
  public File exportDefaultPicture(@NonNull PupPack pupPack, @NonNull File target) {
    File defaultPicture = new File(target, SystemService.DEFAULT_BACKGROUND);
    if (defaultPicture.exists() && defaultPicture.length() > 0) {
      return defaultPicture;
    }

    if (defaultPicture.exists() && defaultPicture.length() == 0) {
      return null;
    }

    if (!target.exists()) {
      target.mkdirs();
    }

    PupDefaultVideoResolver resolver = new PupDefaultVideoResolver(pupPack);
    File defaultVideo = resolver.findDefaultVideo();
    if (defaultVideo != null && defaultVideo.exists()) {
      boolean success = JCodec.export(defaultVideo, defaultPicture);
      if (success) {
        LOG.info("Successfully extracted default background image " + defaultPicture.getAbsolutePath());
        return defaultPicture;
      }
    }
    return null;
  }

  public boolean clearCache() {
    refresh();
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      Thread.currentThread().setName("PUP Pack Scanner");
      refresh();
    }).start();
  }
}

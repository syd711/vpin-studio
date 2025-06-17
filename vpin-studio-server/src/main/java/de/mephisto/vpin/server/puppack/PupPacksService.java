package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.commons.OrbitalPins;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.jobs.JobService;
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

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PupPacksService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PupPacksService.class);

  private final static String PUP_PACK_TWEAKER_EXE = "PupPackScreenTweaker.exe";

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private JobService jobService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private final Map<String, PupPack> pupPackCache = new ConcurrentHashMap<>();

  /**
   * Return where pinup player is installed, read it today from installation directory,
   * independently of the frontend, could be usefull to support standalone installation with
   * pinup player only
   *
   * @return The PupVideos folder of a Pinup Plyer installation
   */
  private File getPupPackFolder() {
    return new File(systemService.getPinupInstallationFolder(), "PUPVideos");
  }

  public PupPack getMenuPupPack() {
    File pupPackFolder = getPupPackFolder();
    File menuPupPackFolder = new File(pupPackFolder, "PinUpMenu");
    return loadPupPack(menuPupPackFolder);
  }

  public boolean delete(@NonNull Game game) {
    PupPack pupPack = getPupPack(game);
    if (pupPack != null) {
      if (pupPack.delete()) {
        LOG.info("Deleting " + pupPack.getPupPackFolder().getAbsolutePath());
        clearCache();
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PUP_PACK, pupPack.getName());
      }
    }
    return false;
  }

  @Nullable
  public PupPack getPupPackCached(@NonNull Game game) {
    return getCachedPupPack(game);
  }

  @Nullable
  public PupPack getPupPack(@NonNull Game game) {
    PupPack cachedPupPack = getCachedPupPack(game);
    if (cachedPupPack != null) {
      invalidate(cachedPupPack);
    }
    return getCachedPupPack(game);
  }

  @Nullable
  private PupPack getCachedPupPack(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getPupPackName()) && pupPackCache.containsKey(game.getPupPackName().toLowerCase())) {
      return pupPackCache.get(game.getPupPackName().toLowerCase());
    }
    if (!StringUtils.isEmpty(game.getRomAlias()) && pupPackCache.containsKey(game.getRomAlias().toLowerCase())) {
      return pupPackCache.get(game.getRomAlias().toLowerCase());
    }
    if (!StringUtils.isEmpty(game.getRom()) && pupPackCache.containsKey(game.getRom().toLowerCase())) {
      return pupPackCache.get(game.getRom().toLowerCase());
    }
    if (!StringUtils.isEmpty(game.getTableName()) && pupPackCache.containsKey(game.getTableName().toLowerCase())) {
      return pupPackCache.get(game.getTableName().toLowerCase());
    }
    return null;
  }

  private void invalidate(PupPack cachedPupPack) {
    File pupPackFolder = cachedPupPack.getPupPackFolder();
    loadPupPack(pupPackFolder);
    LOG.info("Invalidated PUP Pack \"{}\"", pupPackFolder.getAbsolutePath());
  }

  private void refresh() {
    if (!Features.PUPPACKS_ENABLED) {
      return;
    }

    this.pupPackCache.clear();
    long start = System.currentTimeMillis();
    File pupPackFolder = getPupPackFolder();
    LOG.info("Refreshing PUP pack info from \"" + pupPackFolder.getAbsolutePath() + "\"");
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
    LOG.info("Finished PUP pack scan, found " + pupPackCache.size() + " packs (" + (end - start) + "ms)");
  }

  public PupPack loadPupPack(File packFolder) {
    PupPack pupPack = new PupPack(packFolder);
    if (new File(packFolder, "scriptonly.txt").exists()) {
      pupPack.setScriptOnly(true);
    }

    boolean orbitalPin = OrbitalPins.isOrbitalPin(packFolder.getName());
    boolean containsMedia = pupPack.containsFileWithSuffixes("mp4", "mkv", "png");
    if ((orbitalPin || containsMedia)) {
//      LOG.info("Loaded PUP Pack " + packFolder.getName() + " (orbitalPin: " + orbitalPin + ")");
      pupPackCache.put(packFolder.getName().toLowerCase(), pupPack);
    }
    else {
//      LOG.info("Skipped PUP pack folder \"" + packFolder.getName() + "\", no media found.");
    }
    return pupPack;
  }

  public JobDescriptor option(Game game, String option) {
    PupPack pupPack = getPupPack(game);
    JobDescriptor jobDescriptor = pupPack.executeOption(option);
    gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PUP_PACK, pupPack.getName());
    return jobDescriptor;
  }

  public boolean setPupPackEnabled(Game game, boolean enable) {
    boolean b = frontendService.setPupPackEnabled(game, enable);
    gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PUP_PACK, game.getRom());
    return b;
  }

  public boolean hasPupPack(Game game) {
    return getCachedPupPack(game) != null;
  }

  public boolean isPupPackDisabled(@Nullable Game game) {
    return game.isPupPackDisabled();
  }


  public List<String> getMissingResources(Game game) {
    PupPack puppack = getCachedPupPack(game);
    if (puppack != null) {
      return puppack.getMissingResources();
    }
    return null;
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
            gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.PUP_PACK, pupPack.getName());
          }
          else {
            LOG.info("PUPHideNext.txt already exists for " + game.getRom());
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create PUPHideNext.txt for " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }
  }

  public void installPupPack(@NonNull UploadDescriptor uploadDescriptor, @NonNull UploaderAnalysis analysis, boolean async) throws IOException {
    if (!Features.PUPPACKS_ENABLED) {
      return;
    }

    File tempFile = new File(uploadDescriptor.getTempFilename());
    File pupVideosFolder = getPupPackFolder();
    if (!pupVideosFolder.exists()) {
      uploadDescriptor.setError("Invalid target folder: " + pupVideosFolder.getAbsolutePath());
      return;
    }

    LOG.info("Extracting PUP pack to " + pupVideosFolder.getAbsolutePath());
    if (!pupVideosFolder.exists()) {
      if (!pupVideosFolder.mkdirs()) {
        uploadDescriptor.setError("Failed to create PUP pack directory " + pupVideosFolder.getAbsolutePath());
        return;
      }
    }

    String rom = analysis.getRomFromPupPack();
    if (StringUtils.isEmpty(rom)) {
      LOG.info("PUP pack extraction has been cancelled, no ROM could be resolved");
      return;
    }

    File pupPackFolder = new File(pupVideosFolder, rom);
    if (pupPackFolder.exists() && pupPackFolder.isDirectory()) {
      LOG.info("Existing PUP pack folder \"{}\" found, deleting it first.", rom);
      de.mephisto.vpin.restclient.util.FileUtils.deleteFolder(pupPackFolder);
      pupPackFolder.mkdirs();
    }

    LOG.info("Starting PUP pack extraction for ROM '" + rom + "'");
    PupPackInstallerJob job = new PupPackInstallerJob(this, tempFile, pupVideosFolder, analysis.getPupPackRootDirectory(), rom, async);
    if (!async) {
      JobDescriptor jobDescriptor = new JobDescriptor(JobType.PUP_INSTALL);
      job.execute(jobDescriptor);
    }
    else {
      JobDescriptor jobDescriptor = new JobDescriptor(JobType.PUP_INSTALL);
      jobDescriptor.setTitle("Installing PUP pack \"" + uploadDescriptor.getOriginalUploadFileName() + "\"");
      jobDescriptor.setJob(job);

      jobService.offer(jobDescriptor);
    }
  }

  public void exportDefaultPicture(@NonNull Game game, @NonNull File target) {
    PupPack pupPack = getPupPack(game);
    if (pupPack == null) {
      return;
    }

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
        }
        catch (IOException e) {
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

  public void loadPupPack(String rom) {
    if (!StringUtils.isEmpty(rom)) {
      File pupVideosFolder = getPupPackFolder();
      if (pupVideosFolder.exists()) {
        File pupPackFolder = new File(pupVideosFolder, rom);
        if (pupPackFolder.exists()) {
          loadPupPack(pupPackFolder);
        }
      }
      gameLifecycleService.notifyGameAssetsChanged(AssetType.PUP_PACK, rom);
    }
  }

  public boolean clearCache() {
    refresh();
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    if (!Features.PUPPACKS_ENABLED) {
      return;
    }

    try {
      File pupPackScreenTweakerExe = new File(systemService.getPinupInstallationFolder(), PUP_PACK_TWEAKER_EXE);
      if (!pupPackScreenTweakerExe.exists()) {
        File source = new File(SystemService.RESOURCES, PUP_PACK_TWEAKER_EXE);
        FileUtils.copyFile(source, pupPackScreenTweakerExe);
        LOG.info("Copied {}", pupPackScreenTweakerExe.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy {}: {}", PUP_PACK_TWEAKER_EXE, e.getMessage(), e);
    }

    new Thread(() -> {
      try {
        Thread.currentThread().setName("PUP Pack Scanner");
        refresh();
      }
      catch (Exception e) {
        LOG.error("Error in PUP Pack Scanner thread: " + e.getMessage(), e);
      }
    }).start();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

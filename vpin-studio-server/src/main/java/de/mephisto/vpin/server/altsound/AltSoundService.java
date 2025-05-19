package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.mame.MameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
@Service
public class AltSoundService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundService.class);

  @Autowired
  private AltSoundBackupService altSoundBackupService;

  @Autowired
  private MameService mameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private final Map<String, AltSound> altSoundFolder2AltSound = new ConcurrentHashMap<>();

  public boolean isAltSoundAvailable(@NonNull Game game) {
    return game.getAltSoundFolder() != null && altSoundFolder2AltSound.containsKey(game.getAltSoundFolder().getAbsolutePath().toLowerCase());
  }

  public boolean delete(@NonNull Game game) {
    GameEmulator emulator = emulatorService.getGameEmulator(game.getEmulatorId());
    if (!StringUtils.isEmpty(game.getRom())) {
      File folder = new File(emulator.getAltSoundFolder(), game.getRom());
      if (folder.exists()) {
        altSoundFolder2AltSound.remove(game.getAltSoundFolder().getAbsolutePath().toLowerCase());
        LOG.info("Deleting ALTSound folder " + folder.getAbsolutePath());
        if (FileUtils.deleteFolder(folder)) {
          return clearCache();
        }
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
      }
    }
    return false;
  }

  @NonNull
  public AltSound getAltSound(@NonNull Game game) {
    if (isAltSoundAvailable(game)) {
      String folder = game.getAltSoundFolder().getAbsolutePath();
      AltSound altSound = altSoundFolder2AltSound.get(folder.toLowerCase());
      altSound.setFolder(folder);
      altSound = AltSoundLoaderFactory.load(altSound);
      altSoundFolder2AltSound.put(folder.toLowerCase(), altSound);
      return altSound;
    }
    return new AltSound();
  }

  public AltSound save(@NonNull Game game, @NonNull AltSound altSound) {
    altSoundBackupService.synchronizeBackup(game);
    if (game.isAltSoundAvailable()) {
      if (altSound.getFormat().equals(AltSoundFormats.gsound)) {
        new AltSound2Writer(game.getAltSoundFolder()).write(altSound);
      }
      else {
        new AltSoundWriter(game.getAltSoundFolder()).write(altSound);
      }
      createAltSound(new File(altSound.getFolder()), altSound.getEmulatorId());
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
    }
    return altSound;
  }

  public boolean setAltSoundEnabled(@NonNull String rom, boolean b) {
    if (!StringUtils.isEmpty(rom)) {
      MameOptions options = mameService.getOptions(rom);
      options.setSoundMode(b ? 1 : 0);
      mameService.saveOptions(options);
      return true;
    }
    return false;
  }

  public int getAltSoundMode(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {
      MameOptions options = mameService.getOptions(game.getRom());
      int mode = options.getSoundMode();
      return mode;
    }
    return -1;
  }

  public JobDescriptor installAltSound(int emulatorId, @NonNull String rom, @NonNull File archive) {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    File altSoundFolder = new File(gameEmulator.getAltSoundFolder(), rom);
    if (!altSoundFolder.exists() && !altSoundFolder.mkdirs()) {
      return JobDescriptorFactory.error("Failed to create ALT sound directory \"" + altSoundFolder.getAbsolutePath() + "\"");
    }

    LOG.info("Extracting ALT sound to " + altSoundFolder.getAbsolutePath());
    AltSoundUtil.unpack(archive, altSoundFolder);
    setAltSoundEnabled(rom, true);
    clearCache();
    return JobDescriptorFactory.empty();
  }

  public AltSound restore(Game game) {
    if (game != null) {
      this.altSoundBackupService.restore(game);
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
      return getAltSound(game);
    }
    return new AltSound();
  }

  public boolean clearCache() {
    long start = System.currentTimeMillis();
    this.altSoundFolder2AltSound.clear();
    List<GameEmulator> vpxGameEmulators = emulatorService.getVpxGameEmulators();
    for (GameEmulator vpxGameEmulator : vpxGameEmulators) {
      File altSoundFolder = vpxGameEmulator.getAltSoundFolder();
      if (altSoundFolder.exists()) {
        File[] altSoundBundles = altSoundFolder.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory();
          }
        });
        if (altSoundBundles != null) {
          for (File altSoundDir : altSoundBundles) {
            createAltSound(altSoundDir, vpxGameEmulator.getId());
          }
        }
      }
    }
    LOG.info("Loading of " + altSoundFolder2AltSound.size() + " ALTSounds finished, took " + (System.currentTimeMillis() - start) + "ms.");
    return true;
  }

  private void createAltSound(@Nullable File altSoundDir, int emualtorId) {
    if (altSoundDir != null) {
      AltSound altSound = AltSoundLoaderFactory.create(altSoundDir, emualtorId);
      if (altSound != null) {
        altSoundFolder2AltSound.put(altSoundDir.getAbsolutePath().toLowerCase(), altSound);
      }
      else {
        LOG.warn("Skipped caching ALT sound '{}'", altSoundDir.getName());
      }
    }
  }

  /**
   * Only used for streaming
   *
   * @param emuId
   * @param name
   * @return
   */
  public AltSound getAltSound(int emuId, String name) {
    GameEmulator emulator = emulatorService.getGameEmulator(emuId);
    File folder = new File(emulator.getAltSoundFolder(), name);
    return AltSoundLoaderFactory.load(folder, emuId);
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      Thread.currentThread().setName("ALTSound Loader");
      clearCache();
    }).start();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

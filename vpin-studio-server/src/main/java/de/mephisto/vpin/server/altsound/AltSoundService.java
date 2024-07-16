package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
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
  private PinUPConnector pinUPConnector;

  private final Map<String, AltSound> altSoundFolder2AltSound = new ConcurrentHashMap<>();

  public boolean isAltSoundAvailable(@NonNull Game game) {
    return game.getAltSoundFolder() != null && altSoundFolder2AltSound.containsKey(game.getAltSoundFolder().getAbsolutePath());
  }

  public boolean delete(@NonNull Game game) {
    GameEmulator emulator = pinUPConnector.getGameEmulator(game.getEmulatorId());
    if (!StringUtils.isEmpty(game.getRom())) {
      File folder = new File(emulator.getAltSoundFolder(), game.getRom());
      if (folder.exists()) {
        altSoundFolder2AltSound.remove(game.getAltSoundFolder().getAbsolutePath());
        LOG.info("Deleting ALTSound folder " + folder.getAbsolutePath());
        return FileUtils.deleteFolder(folder);
      }
    }
    return false;
  }

  @NonNull
  public AltSound getAltSound(@NonNull Game game) {
    if (isAltSoundAvailable(game)) {
      return altSoundFolder2AltSound.get(game.getAltSoundFolder().getAbsolutePath());
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
      loadAltSound(game.getAltSoundFolder());
    }
    return altSound;
  }

  public boolean setAltSoundEnabled(@NonNull String rom, boolean b) {
    if (!StringUtils.isEmpty(rom)) {
      MameOptions options = mameService.getOptions(rom);
      options.setSoundMode(b ? 1 : 0);
      mameService.saveOptions(options);
    }
    return b;
  }

  public int getAltSoundMode(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {
      MameOptions options = mameService.getOptions(game.getRom());
      int mode = options.getSoundMode();
      return mode;
    }
    return -1;
  }

  public JobExecutionResult installAltSound(int emulatorId, @NonNull String rom, @NonNull File archive) {
    GameEmulator gameEmulator = pinUPConnector.getGameEmulator(emulatorId);
    File altSoundFolder = new File(gameEmulator.getAltSoundFolder(), rom);
    if (!altSoundFolder.exists() && !altSoundFolder.mkdirs()) {
      return JobExecutionResultFactory.error("Failed to create ALT sound directory \"" + altSoundFolder.getAbsolutePath() + "\"");
    }

    LOG.info("Extracting ALT sound to " + altSoundFolder.getAbsolutePath());
    AltSoundUtil.unpack(archive, altSoundFolder);
    setAltSoundEnabled(rom, true);
    clearCache();
    return JobExecutionResultFactory.empty();
  }

  public AltSound restore(Game game) {
    if (game != null) {
      this.altSoundBackupService.restore(game);
      loadAltSound(game.getAltSoundFolder());
      return getAltSound(game);
    }
    return new AltSound();
  }

  public boolean clearCache() {
    long start = System.currentTimeMillis();
    this.altSoundFolder2AltSound.clear();
    List<GameEmulator> vpxGameEmulators = pinUPConnector.getVpxGameEmulators();
    for (GameEmulator vpxGameEmulator : vpxGameEmulators) {
      File altSoundFolder = vpxGameEmulator.getAltSoundFolder();
      if (altSoundFolder.exists()) {
        File[] files = altSoundFolder.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory();
          }
        });
        if (files != null) {
          for (File altSoundDir : files) {
            loadAltSound(altSoundDir);
          }
        }
      }
    }
    LOG.info("Loading of " + altSoundFolder2AltSound.size() + " ALTSounds finished, took " + (System.currentTimeMillis() - start) + "ms.");
    return true;
  }

  private void loadAltSound(@Nullable File altSoundDir) {
    if (altSoundDir != null) {
      AltSound altSound = new AltSoundLoaderFactory(altSoundDir).load();
      altSoundFolder2AltSound.put(altSoundDir.getAbsolutePath(), altSound);
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
    GameEmulator emulator = pinUPConnector.getGameEmulator(emuId);
    File folder = new File(emulator.getAltSoundFolder(), name);
    return new AltSoundLoaderFactory(folder).load();
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      Thread.currentThread().setName("ALTSound Loader");
      clearCache();
    }).start();
  }
}

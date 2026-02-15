package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
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
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
@Service
public class AltSoundService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private AltSoundBackupService altSoundBackupService;

  @Autowired
  private MameService mameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private FolderLookupService folderLookupService;

  private final Map<String, AltSound> altSoundFolder2AltSound = new ConcurrentHashMap<>();

  public boolean isAltSoundAvailable(@NonNull Game game) {
    File altSoundFolder = getAltSoundFolder(game);
    if (altSoundFolder != null) {
      String altSoundKey = altSoundFolder.getAbsolutePath().toLowerCase();

      // auto discovery of non previously detected altSound
      if (!altSoundFolder2AltSound.containsKey(altSoundKey)) {
        AltSound altSound = AltSoundLoaderFactory.create(altSoundFolder, game.getEmulatorId());
        if (altSound != null) {
          altSoundFolder2AltSound.put(altSoundKey, altSound);
        }
      }
      return altSoundFolder2AltSound.get(altSoundKey) != null;
    }
    return false;
  }

  @Nullable
  public File getAltSoundFolder(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRomAlias()) && game.getEmulator() != null) {
      return folderLookupService.getAltSoundFolder(game, game.getRomAlias());
    }
    if (!StringUtils.isEmpty(game.getRom()) && game.getEmulator() != null) {
      return folderLookupService.getAltSoundFolder(game, game.getRom());
    }
    return null;
  }

  public boolean delete(@NonNull Game game) {
    File folder = getAltSoundFolder(game);
    if (folder != null && folder.exists()) {
      altSoundFolder2AltSound.remove(folder.getAbsolutePath().toLowerCase());
      LOG.info("Deleting ALTSound folder " + folder.getAbsolutePath());
      if (FileUtils.deleteFolder(folder)) {
        return clearCache();
      }
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
    }
    return true;
  }

  @NonNull
  public AltSound getAltSound(@NonNull Game game) {
    if (isAltSoundAvailable(game)) {
      File altSoundFolder = getAltSoundFolder(game);
      String altSoundKey = altSoundFolder.getAbsolutePath().toLowerCase();
      AltSound altSound = altSoundFolder2AltSound.get(altSoundKey);
      altSound.setFolder(altSoundFolder.getAbsolutePath());
      altSound = AltSoundLoaderFactory.load(altSound);
      return altSound;
    }
    return null;
  }

  public boolean save(@NonNull Game game, @NonNull AltSound altSound) {
    File altSoundFolder = getAltSoundFolder(game);
    if (altSoundFolder != null) {
      altSoundBackupService.synchronizeBackup(altSoundFolder);
      if (game.isAltSoundAvailable()) {
        if (altSound.getFormat().equals(AltSoundFormats.gsound)) {
          new AltSound2Writer(altSoundFolder).write(altSound);
        }
        else {
          new AltSoundWriter(altSoundFolder).write(altSound);
        }
        createAltSound(new File(altSound.getFolder()), altSound.getEmulatorId());
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
        return true;
      }
    }
    return false;
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

  public JobDescriptor installAltSound(int emulatorId, @NonNull String rom, @NonNull File archive, @Nullable String archivePath) {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    File altSoundFolder = new File(folderLookupService.getAltSoundFolder(), rom);
    if (!altSoundFolder.exists() && !altSoundFolder.mkdirs()) {
      return JobDescriptorFactory.error("Failed to create ALT sound directory \"" + altSoundFolder.getAbsolutePath() + "\"");
    }

    LOG.info("Extracting ALT sound to " + altSoundFolder.getAbsolutePath());

    PackageUtil.unpackTargetFolder(archive, altSoundFolder, archivePath, Arrays.asList(".ogg", ".mp3", ".wav", ".csv", ".ini"), null);
    setAltSoundEnabled(rom, true);
    clearCache();
    return JobDescriptorFactory.empty();
  }

  public boolean restore(Game game) {
    if (game != null) {
      File altSoundFolder = getAltSoundFolder(game);
      if (altSoundFolder != null) {
        this.altSoundBackupService.restore(altSoundFolder);
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.ALT_SOUND, game.getRom());
        return true;
      }
    }
    return false;
  }

  public boolean clearCache() {
    long start = System.currentTimeMillis();
    this.altSoundFolder2AltSound.clear();

    List<GameEmulator> vpxGameEmulators = emulatorService.getVpxGameEmulators();
    for (GameEmulator emulator : vpxGameEmulators) {
      File altSoundFolder = emulator.getAltSoundFolder();
      if (altSoundFolder.exists()) {
        File[] altSoundBundles = altSoundFolder.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory();
          }
        });
        if (altSoundBundles != null) {
          for (File altSoundDir : altSoundBundles) {
            createAltSound(altSoundDir, emulator.getId());
          }
        }
      }
    }
    LOG.info("Clearing cache of " + altSoundFolder2AltSound.size() + " ALTSounds finished, took " + (System.currentTimeMillis() - start) + "ms.");
    return true;
  }

  private void createAltSound(@Nullable File altSoundDir, int emulatorId) {
    if (altSoundDir != null) {
      AltSound altSound = AltSoundLoaderFactory.create(altSoundDir, emulatorId);
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

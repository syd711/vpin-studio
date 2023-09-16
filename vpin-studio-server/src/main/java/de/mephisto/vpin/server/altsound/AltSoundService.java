package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
@Service
public class AltSoundService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private AltSoundBackupService altSoundBackupService;

  @Autowired
  private MameService mameService;

  public boolean isAltSoundAvailable(@NonNull Game game) {
    File gSoundCsv = new File(game.getAltSoundFolder(), "g-sound.csv");
    File altSoundCsv = new File(game.getAltSoundFolder(), "altsound.csv");
    return gSoundCsv.exists() || altSoundCsv.exists();
  }

  public boolean delete(@NonNull Game game) {
    AltSound altSound = getAltSound(game);
    if (altSound.getCsvFile() != null) {
      return FileUtils.deleteFolder(altSound.getCsvFile().getParentFile());
    }
    return false;
  }

  @NonNull
  public AltSound getAltSound(@NonNull Game game) {
    altSoundBackupService.getOrCreateBackup(game);
    if(game.isAltSoundAvailable()) {
      return new AltSoundLoaderFactory(game.getAltSoundFolder()).load();
    }
    return new AltSound();
  }

  public AltSound getAltSound(String name) {
    File folder = new File(systemService.getAltSoundFolder(), name);
    return new AltSoundLoaderFactory(folder).load();
  }

  public AltSound save(@NonNull Game game, @NonNull AltSound altSound) {
    if (game.isAltSoundAvailable()) {
      return new AltSoundWriter(game.getAltSoundFolder()).write(altSound);
    }
    return new AltSound();
  }

  public boolean setAltSoundEnabled(@NonNull Game game, boolean b) {
    String rom = game.getRom();
    if (!StringUtils.isEmpty(rom)) {
      MameOptions options = mameService.getOptions(rom);
      options.setSoundMode(b);
      mameService.saveOptions(options);
    }
    return b;
  }

  public boolean isAltSoundEnabled(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {
      MameOptions options = mameService.getOptions(game.getRom());
      return options.isSoundMode();
    }
    return false;
  }

  public JobExecutionResult installAltSound(Game game, File out) {
    File altSoundFolder = game.getAltSoundFolder();
    if (altSoundFolder != null) {
      LOG.info("Extracting archive to " + altSoundFolder.getAbsolutePath());
      if (!altSoundFolder.exists()) {
        if (!altSoundFolder.mkdirs()) {
          return JobExecutionResultFactory.error("Failed to create ALT sound directory " + altSoundFolder.getAbsolutePath());
        }
      }

      AltSoundUtil.unzip(out, altSoundFolder);
      if (!out.delete()) {
        return JobExecutionResultFactory.error("Failed to delete temporary file.");
      }
      setAltSoundEnabled(game, true);
    }
    return JobExecutionResultFactory.empty();
  }

  public AltSound restore(Game game) {
    if (game != null) {
      this.altSoundBackupService.restore(game);
      return getAltSound(game);
    }
    return new AltSound();
  }

  @Override
  public void afterPropertiesSet() {
    File altSoundFolder = systemService.getAltSoundFolder();
    if (!altSoundFolder.exists() && altSoundFolder.getParentFile().exists()) {
      if (!altSoundFolder.mkdirs()) {
        LOG.error("Failed to create altsound folder " + altSoundFolder.getName());
      }
    }
  }
}

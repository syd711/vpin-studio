package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.AltSoundEntry;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
@Service
public class AltSoundService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundService.class);
  public static final String SOUND_MODE = "sound_mode";

  @Autowired
  private SystemService systemService;

  private final Map<String, File> altSounds = new ConcurrentHashMap<>();

  public boolean isAltSoundAvailable(@NonNull Game game) {
    return getAltSoundCsvFile(game) != null;
  }

  public boolean delete(@NonNull Game game) {
    File altSoundCsvFile = getAltSoundCsvFile(game);
    if (altSoundCsvFile != null && altSoundCsvFile.exists()) {
      return FileUtils.deleteFolder(altSoundCsvFile.getParentFile());
    }
    return true;
  }

  @NonNull
  public AltSound getAltSound(@NonNull Game game) {
    AltSound altSound = new AltSound();
    File csvFile = this.getAltSoundCsvFile(game);
    if (csvFile == null) {
      return altSound;
    }
    altSound.setModificationDate(new Date(csvFile.lastModified()));

    //make sure a backup is there
    this.getOrCreateBackup(csvFile);

    long size = csvFile.length();
    FileReader in = null;
    Map<String, String> audioFiles = new HashMap<>();
    try {
      in = new FileReader(csvFile);
      Iterable<CSVRecord> records = CSVFormat.RFC4180
          .withIgnoreEmptyLines(true)
          .withQuoteMode(QuoteMode.NON_NUMERIC)
          .withQuote('"')
          .withTrim().parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      CSVRecord header = iterator.next();
      altSound.setHeaders(header.toList());

      while (iterator.hasNext()) {
        CSVRecord record = iterator.next();
        File audioFile = new File(csvFile.getParentFile(), record.get(7).replaceAll("\"", ""));

        AltSoundEntry entry = new AltSoundEntry();
        entry.setId(record.get(0));
        entry.setChannel(record.isSet(1) ? record.get(1) : "");
        entry.setDuck(record.isSet(2) ? getInt(record.get(2)) : 0);
        entry.setGain(record.isSet(3) ? getInt(record.get(3)) : 0);
        entry.setLoop(record.isSet(4) ? getInt(record.get(4)) : 0);
        entry.setStop(record.isSet(5) ? getInt(record.get(5)) : 0);
        entry.setName(record.isSet(6) ? record.get(6).replaceAll("\"", "") : "");
        entry.setFilename(record.isSet(7) ? record.get(7).replaceAll("\"", "") : "");
        entry.setExists(record.isSet(7) && audioFile.exists());
        entry.setGroup(record.isSet(8) ? getInt(record.get(8)) : 0);
        entry.setShaker(record.isSet(9) ? record.get(9) : "");
        entry.setSerial(record.isSet(10) ? record.get(10) : "");
        entry.setPreload(record.isSet(11) ? getInt(record.get(11)) : 0);
        entry.setStopCmd(record.isSet(12) ? record.get(12) : "");

        if (audioFile.exists()) {
          entry.setSize(audioFile.length());
        }


        File soundFile = new File(csvFile.getParentFile(), entry.getFilename());
        if (soundFile.exists()) {
          audioFiles.put(entry.getFilename(), entry.getFilename());
          size += soundFile.length();
        }
        else {
          altSound.setMissingAudioFiles(true);
        }

        altSound.getEntries().add(entry);
      }

      in.close();

      altSound.setFilesize(size);
      altSound.setFiles(audioFiles.size());
    } catch (Exception e) {
      LOG.error("Failed to read altsound CSV " + csvFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return altSound;
  }

  private int getInt(String value) {
    if (StringUtils.isEmpty(value)) {
      return 0;
    }

    return Integer.parseInt(value.trim());
  }

  public AltSound save(@NonNull Game game, @NonNull AltSound altSound) {
    if (game.isAltSoundAvailable()) {
      File altSoundCsv = this.getAltSoundCsvFile(game);
      try {
        org.apache.commons.io.FileUtils.writeStringToFile(altSoundCsv, altSound.toCSV(), StandardCharsets.UTF_8);
        LOG.info("Written ALTSound for " + game.getGameDisplayName());
        return null;
      } catch (Exception e) {
        LOG.error("Error writing CSV " + altSoundCsv.getAbsolutePath() + ": " + e.getMessage(), e);
      }
    }
    return this.getAltSound(game);
  }

  public File getOrCreateBackup(@NonNull File csvFile) {
    File backup = new File(csvFile.getParentFile(), csvFile.getName() + ".bak");
    if (!backup.exists()) {
      try {
        org.apache.commons.io.FileUtils.copyFile(csvFile, backup);
      } catch (IOException e) {
        LOG.error("Error creating CSV backup: " + e.getMessage(), e);
      }
    }
    return backup;
  }

  public AltSound restore(@NonNull Game game) {
    try {
      File altSoundCsv = this.getAltSoundCsvFile(game);
      if (altSoundCsv != null && altSoundCsv.exists()) {
        File backup = getOrCreateBackup(altSoundCsv);
        if (backup.exists()) {
          org.apache.commons.io.FileUtils.copyFile(backup, altSoundCsv);
        }
        else {
          LOG.error("Failed to restore ALT sound backup, the backup file " + backup.getAbsolutePath() + " does not exists.");
        }
      }
    } catch (IOException e) {
      LOG.error("Error restoring CSV backup: " + e.getMessage(), e);
    }
    return null;
  }

  private File getAltSoundCsvFile(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom()) && this.altSounds.containsKey(game.getRom())) {
      return this.altSounds.get(game.getRom());
    }
    if (!StringUtils.isEmpty(game.getTableName()) && this.altSounds.containsKey(game.getTableName())) {
      return this.altSounds.get(game.getTableName());
    }
    return null;
  }

  public boolean setAltSoundEnabled(@NonNull Game game, boolean b) {
    String rom = game.getRom();
    if (!StringUtils.isEmpty(rom)) {
      if (b) {
        systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, SOUND_MODE, 1);
      }
      else {
        systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, SOUND_MODE, 0);
      }
    }
    return b;
  }

  public boolean isAltSoundEnabled(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {
      String sound_mode = systemService.getMameRegistryValue(game.getRom(), SOUND_MODE);
      return String.valueOf(sound_mode).equals("0x1") || String.valueOf(sound_mode).equals("1");
    }
    return false;
  }

  public boolean clearCache() {
    long start = System.currentTimeMillis();
    File altSoundsFolder = systemService.getAltSoundFolder();
    if (altSoundsFolder.exists()) {
      File[] altSoundFolder = altSoundsFolder.listFiles((dir, name) -> new File(dir, name).isDirectory());
      if (altSoundFolder != null) {
        for (File altSound : altSoundFolder) {
          File csv = new File(altSound, "altsound.csv");
          if (csv.exists()) {
            this.altSounds.put(altSound.getName(), csv);
          }
        }
      }
    }
    else {
      LOG.error("altsound folder " + altSoundsFolder.getAbsolutePath() + " does not exist.");
    }
    long end = System.currentTimeMillis();
    LOG.info("Finished altsound pack scan, found " + altSounds.size() + " alt sound packs (" + (end - start) + "ms)");
    return true;
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
      clearCache();
      setAltSoundEnabled(game, true);
    }
    return JobExecutionResultFactory.empty();
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      clearCache();
    }).start();
  }
}

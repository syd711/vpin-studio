package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.AltSoundEntry;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME,GROUP,SHAKER,SERIAL,PRELOAD,STOPCMD
 * 0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
 */
@Service
public class AltSoundService {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundService.class);

  @Autowired
  private GameService gameService;

  @NonNull
  public AltSound getAltSound(int id) {
    Game game = gameService.getGame(id);
    if (game != null && game.isAltSoundAvailable()) {
      return getAltSound(game);
    }
    return new AltSound();
  }

  @NonNull
  public AltSound getAltSound(@NonNull Game game) {
    AltSound altSound = new AltSound();
    File csvFile = game.getAltSoundCsv();
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
        AltSoundEntry entry = new AltSoundEntry();
        entry.setId(record.get(0));
        entry.setChannel(record.isSet(1) ? getInt(record.get(1)) : 0);
        entry.setDuck(record.isSet(2) ? getInt(record.get(2)) : 0);
        entry.setGain(record.isSet(3) ? getInt(record.get(3)) : 0);
        entry.setLoop(record.isSet(4) ? getInt(record.get(4)) : 0);
        entry.setStop(record.isSet(5) ? getInt(record.get(5)) : 0);
        entry.setName(record.isSet(6) ? record.get(6).replaceAll("\"", "") : "");
        entry.setFilename(record.isSet(7) ? record.get(7).replaceAll("\"", "") : "");
        entry.setExists(record.isSet(7) && new File(game.getAltSoundFolder(), record.get(7).replaceAll("\"", "")).exists());
        entry.setGroup(record.isSet(8) ? getInt(record.get(8)) : 0);
        entry.setShaker(record.isSet(9) ? record.get(9) : "");
        entry.setSerial(record.isSet(10) ? record.get(10) : "");
        entry.setPreload(record.isSet(11) ? getInt(record.get(11)) : 0);
        entry.setStopCmd(record.isSet(12) ? record.get(12) : "");


        File soundFile = new File(game.getAltSoundFolder(), entry.getFilename());
        if (soundFile.exists()) {
          audioFiles.put(entry.getFilename(), entry.getFilename());
          size += soundFile.length();
        }

        if(!altSound.getChannels().contains(entry.getChannel())) {
          altSound.getChannels().add(entry.getChannel());
        }

        altSound.getEntries().add(entry);
      }

      altSound.setFilesize(size);
      altSound.setFiles(audioFiles.size());
    } catch (Exception e) {
      LOG.error("Failed to read altsound CSV " + csvFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return altSound;
  }

  private int getInt(String value) {
    if(StringUtils.isEmpty(value)) {
      return 0;
    }

    return Integer.parseInt(value.trim());
  }

  public AltSound save(int id, AltSound altSound) {
    Game game = gameService.getGame(id);
    if (game != null && game.isAltSoundAvailable()) {
      File altSoundCsv = game.getAltSoundCsv();
      try {
//        FileUtils.writeStringToFile(altSoundCsv, altSound.toCSV(), StandardCharsets.UTF_8);
        System.out.println(altSound.toCSV());
        return null;
      } catch (Exception e) {
        LOG.error("Error writing CSV " + altSoundCsv.getAbsolutePath() + ": " + e.getMessage(), e);
      }
    }
    return this.getAltSound(id);
  }

  public File getOrCreateBackup(@NonNull File csvFile) {
    File backup = new File(csvFile.getParentFile(), csvFile.getName() + ".bak");
    if (!backup.exists()) {
      try {
        FileUtils.copyFile(csvFile, backup);
      } catch (IOException e) {
        LOG.error("Error creating CSV backup: " + e.getMessage(), e);
      }
    }
    return backup;
  }

  public AltSound restore(int id) {
    Game game = gameService.getGame(id);
    if (game != null && game.isAltSoundAvailable()) {
      File altSoundCsv = game.getAltSoundCsv();
      File backup = getOrCreateBackup(altSoundCsv);
      try {
        FileUtils.copyFile(backup, altSoundCsv);
      } catch (IOException e) {
        LOG.error("Error restoring CSV backup: " + e.getMessage(), e);
      }
    }
    return this.getAltSound(id);
  }
}

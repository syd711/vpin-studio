package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AltSoundLoader {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final File csvFile;

  AltSoundLoader(@NonNull File csvFile) {
    this.csvFile = csvFile;
  }

  @NonNull
  public AltSound load() {
    AltSound altSound = new AltSound();
    altSound.setFormat(AltSoundFormats.altsound);
    altSound.setCsvFile(csvFile);
    altSound.setFolder(csvFile.getParentFile().getAbsolutePath());
    altSound.setName(csvFile.getParentFile().getName());
    altSound.setModificationDate(new Date(csvFile.lastModified()));

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
      LOG.error("Failed to read altsound CSV " + csvFile.getAbsolutePath() + ": " + e.getMessage());
    }
    return altSound;
  }

  private int getInt(String value) {
    try {
      if (StringUtils.isEmpty(value)) {
        return 0;
      }

      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOG.error("AltSoundLoader failed to format value '" + value + "' (" + e.getMessage() + ")");
    }
    return 0;
  }
}

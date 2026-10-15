package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AltSoundLoader {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String BOM = "﻿";

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
    altSound.setModificationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(csvFile.lastModified()), ZoneId.systemDefault()));

    long size = csvFile.length();
    Map<String, String> audioFiles = new HashMap<>();
    try (FileReader in = new FileReader(csvFile)) {
      CSVFormat format = CSVFormat.RFC4180.builder()
          .setIgnoreEmptyLines(true)
          .setQuoteMode(QuoteMode.NON_NUMERIC)
          .setQuote('"')
          .setTrim(true)
          .get();

      Iterable<CSVRecord> records = format.parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      if (iterator.hasNext()) {
        CSVRecord header = iterator.next();
        List<String> headers = new ArrayList<>();
        for (String h : header.toList()) {
          headers.add(h.replace(BOM, "").replaceAll("\"", "").trim());
        }
        altSound.setHeaders(headers);
      }

      Map<String, Integer> columns = getColumns(altSound.getHeaders());
      while (iterator.hasNext()) {
        CSVRecord record = iterator.next();

        AltSoundEntry entry = new AltSoundEntry();
        entry.setId(getValue(record, columns, AltSoundEntry.ID));
        entry.setChannel(getValue(record, columns, AltSoundEntry.CHANNEL));
        entry.setDuck(getInt(getValue(record, columns, AltSoundEntry.DUCK)));
        entry.setGain(getInt(getValue(record, columns, AltSoundEntry.GAIN)));
        entry.setLoop(getInt(getValue(record, columns, AltSoundEntry.LOOP)));
        entry.setStop(getInt(getValue(record, columns, AltSoundEntry.STOP)));
        entry.setName(getValue(record, columns, AltSoundEntry.NAME).replaceAll("\"", ""));
        entry.setFilename(getValue(record, columns, AltSoundEntry.FNAME).replaceAll("\"", ""));
        entry.setGroup(getInt(getValue(record, columns, AltSoundEntry.GROUP)));
        entry.setShaker(getValue(record, columns, AltSoundEntry.SHAKER));
        entry.setSerial(getValue(record, columns, AltSoundEntry.SERIAL));
        entry.setPreload(getInt(getValue(record, columns, AltSoundEntry.PRELOAD)));
        entry.setStopCmd(getValue(record, columns, AltSoundEntry.STOPCMD));

        File audioFile = new File(csvFile.getParentFile(), entry.getFilename());
        if (!StringUtils.isEmpty(entry.getFilename()) && audioFile.isFile()) {
          entry.setExists(true);
          entry.setSize(audioFile.length());
          audioFiles.put(entry.getFilename(), entry.getFilename());
          size += audioFile.length();
        }
        else {
          altSound.setMissingAudioFiles(true);
        }

        altSound.getEntries().add(entry);
      }

      altSound.setFilesize(size);
      altSound.setFiles(audioFiles.size());
    } catch (Exception e) {
      LOG.error("Failed to read altsound CSV {}: {}", csvFile.getAbsolutePath(), e.getMessage());
    }
    return altSound;
  }

  /**
   * Maps the column names to their index. Files without a known header are read in the default column order.
   */
  private Map<String, Integer> getColumns(List<String> headers) {
    Map<String, Integer> columns = new HashMap<>();
    for (int i = 0; i < headers.size(); i++) {
      columns.putIfAbsent(headers.get(i).toUpperCase(), i);
    }

    if (!columns.containsKey(AltSoundEntry.ID) || !columns.containsKey(AltSoundEntry.FNAME)) {
      LOG.warn("Unknown altsound CSV header {} in {}, using default column order.", headers, csvFile.getAbsolutePath());
      columns.clear();
      for (int i = 0; i < AltSoundEntry.DEFAULT_HEADERS.size(); i++) {
        columns.put(AltSoundEntry.DEFAULT_HEADERS.get(i), i);
      }
      headers.clear();
      headers.addAll(AltSoundEntry.DEFAULT_HEADERS);
    }
    return columns;
  }

  @NonNull
  private static String getValue(CSVRecord record, Map<String, Integer> columns, String column) {
    Integer index = columns.get(column);
    if (index == null || !record.isSet(index)) {
      return "";
    }
    return StringUtils.defaultString(record.get(index));
  }

  private int getInt(String value) {
    try {
      if (StringUtils.isEmpty(value)) {
        return 0;
      }

      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOG.error("AltSoundLoader failed to format value '{}' ({})", value, e.getMessage());
    }
    return 0;
  }
}

package de.mephisto.vpin.server.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TriggersPup {
  private final static Logger LOG = LoggerFactory.getLogger(TriggersPup.class);

  private final List<TriggerEntry> entries = new ArrayList<>();
  private final File triggersPupFile;

  public TriggersPup(@NonNull File triggersPupFile) {
    this.triggersPupFile = triggersPupFile;
    Reader in = null;
    try {
      if (triggersPupFile.exists()) {
        in = new FileReader(triggersPupFile);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
        Iterator<CSVRecord> iterator = records.iterator();
        iterator.next();

        while (iterator.hasNext()) {
          CSVRecord record = iterator.next();
          TriggerEntry entry = new TriggerEntry(record);
          this.entries.add(entry);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to load for " + triggersPupFile.getAbsolutePath() + ": " + e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }

  public long length() {
    return triggersPupFile.length();
  }

  public boolean exists() {
    return triggersPupFile.exists();
  }

  public List<TriggerEntry> getEntries() {
    return entries;
  }
}

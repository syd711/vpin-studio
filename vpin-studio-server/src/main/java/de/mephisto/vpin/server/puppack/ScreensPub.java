package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.ScreenMode;
import org.jspecify.annotations.NonNull;
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

public class ScreensPub {
  private final static Logger LOG = LoggerFactory.getLogger(ScreensPub.class);

  private final List<ScreenEntry> entries = new ArrayList<>();

  private final File screensPupFile;

  public ScreensPub(@NonNull File screensPupFile) {
    this.screensPupFile = screensPupFile;
    try (Reader in = new FileReader(screensPupFile)) {
      if (screensPupFile.exists()) {
        CSVFormat format = CSVFormat.RFC4180.builder().build(); // Use builder for modern API
        Iterable<CSVRecord> records = format.parse(in);
        Iterator<CSVRecord> iterator = records.iterator();
        if (iterator.hasNext()) { // Skip header
          iterator.next();
        }

        while (iterator.hasNext()) {
          CSVRecord record = iterator.next();
          ScreenEntry entry = new ScreenEntry(record);
          this.entries.add(entry);
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to load {}: {}", screensPupFile.getAbsolutePath(), e.getMessage());
    }
  }

  public long length() {
    return screensPupFile.length();
  }

  public boolean exists() {
    return screensPupFile.exists();
  }

  @NonNull
  public ScreenMode getScreenMode(@NonNull VPinScreen screen) {
    int id = screen.getCode();
    for (ScreenEntry entry : this.entries) {
      if (entry.getScreenNum() == id) {
        ScreenMode screenMode = entry.getScreenMode();
        if(screenMode != null) {
          return screenMode;
        }
      }
    }
    return ScreenMode.off;
  }

  @NonNull
  public boolean isTransparent(@NonNull VPinScreen screen) {
    int id = screen.getCode();
    for (ScreenEntry entry : this.entries) {
      if (entry.getScreenNum() == id) {
        return entry.isTransparent();
      }
    }
    return false;
  }

  @NonNull
  public List<ScreenEntry> getEntries() {
    return entries;
  }
}

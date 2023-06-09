package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.ScreenMode;
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

public class ScreensPub {
  private final static Logger LOG = LoggerFactory.getLogger(ScreensPub.class);

  private final List<ScreenEntry> entries = new ArrayList<>();

  private final File screensPupFile;

  public ScreensPub(@NonNull File screensPupFile) {
    this.screensPupFile = screensPupFile;
    Reader in = null;
    try {
      if (screensPupFile.exists()) {
        in = new FileReader(screensPupFile);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
        Iterator<CSVRecord> iterator = records.iterator();
        iterator.next();

        while (iterator.hasNext()) {
          CSVRecord record = iterator.next();
          ScreenEntry entry = new ScreenEntry(record);
          this.entries.add(entry);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to load " + screensPupFile.getAbsolutePath() + ": " + e.getMessage(), e);
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

  public boolean exists() {
    return screensPupFile.exists();
  }

  @NonNull
  public ScreenMode getScreenMode(@NonNull PopperScreen screen) {
    int id = PopperScreen.toId(screen);
    for (ScreenEntry entry : this.entries) {
      if (entry.getScreenNum() == id) {
        return entry.getScreenMode();
      }
    }
    return ScreenMode.off;
  }

  @NonNull
  public List<ScreenEntry> getEntries() {
    return entries;
  }
}

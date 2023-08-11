package de.mephisto.vpin.server.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class TriggerEntry {

  private final CSVRecord record;

  public TriggerEntry(@NonNull CSVRecord record) {
    this.record = record;
  }

  public int getScreenNum() {
    String num = record.get(4);
    if(StringUtils.isEmpty(num)) {
      return -1;
    }
    try {
      return Integer.parseInt(num);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public boolean isActive() {
    String s = record.get(1);
    return !StringUtils.isEmpty(s) && s.equals("1");
  }

  public String getPlayList() {
    return record.get(5);
  }

  public String getPlayFile() {
    return record.get(6);
  }
}

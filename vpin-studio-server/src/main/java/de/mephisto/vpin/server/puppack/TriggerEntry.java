package de.mephisto.vpin.server.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

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
    return Integer.parseInt(num);
  }

  public String getPlayList() {
    return record.get(5);
  }

  public String getPlayFile() {
    return record.get(6);
  }
}

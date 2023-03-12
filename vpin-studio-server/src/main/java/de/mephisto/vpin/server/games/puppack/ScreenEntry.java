package de.mephisto.vpin.server.games.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class ScreenEntry {

  private CSVRecord record;

  public ScreenEntry(@NonNull CSVRecord record) {
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
    return record.get(2);
  }

  public String getPlayFile() {
    return record.get(3);
  }
}

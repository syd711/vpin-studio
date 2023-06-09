package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.popper.ScreenMode;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class ScreenEntry {

  private CSVRecord record;

  public ScreenEntry(@NonNull CSVRecord record) {
    this.record = record;
  }

  public String getPlayList() {
    return record.get(2);
  }

  public String getScreenDes() {
    return record.get(1);
  }

  public int getScreenNum() {
    try {
      return Integer.parseInt(record.get(0));
    } catch (Exception e) {
      return -1;
    }
  }

  public ScreenMode getScreenMode() {
    String screenMode = record.get(5);
    if (StringUtils.isEmpty(screenMode)) {
      return ScreenMode.off;
    }
    return ScreenMode.valueOf(screenMode);
  }

  public String getPlayFile() {
    return record.get(3);
  }
}

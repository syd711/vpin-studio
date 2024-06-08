package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.popper.ScreenMode;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenEntry {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenEntry.class);

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

  public boolean isTransparent() {
    try {
      String transparency = record.get(4);
      if (StringUtils.isEmpty(transparency) || transparency.equals("0")) {
        return false;
      }
      return transparency.trim().equals("1");
    } catch (Exception e) {
      LOG.warn("Invalid transparency value: " + e.getMessage());
      return false;
    }
  }

  @Nullable
  public ScreenMode getScreenMode() {
    try {
      String screenMode = record.get(5);
      if (StringUtils.isEmpty(screenMode) || screenMode.equals("0")) {
        return ScreenMode.off;
      }
      return ScreenMode.valueOf(screenMode);
    } catch (Exception e) {
      LOG.warn("Invalid screen mode: " + e.getMessage());
      return null;
    }
  }

  public String getPlayFile() {
    return record.get(3);
  }
}

package de.mephisto.vpin.restclient.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String getUniqueSystemId() {
    String id = getCpuSerialNumber();
    if(StringUtils.isEmpty(id)) {
      id = getBoardSerialNumber();
    }
    return id;
  }

  private static String getBoardSerialNumber() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "baseboard", "get", "serialnumber"), false);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        String serial = split[split.length - 1];
        if (StringUtils.isEmpty(serial) || serial.contains("filled by") || serial.contains("Serial")) {
          return null;
        }

        return serial;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to resolve cabinet id: " + e.getMessage());
    }
    return null;
  }

  private static String getCpuSerialNumber() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "cpu", "get", "ProcessorId"), false);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        String serial = split[split.length - 1];
        if (StringUtils.isEmpty(serial) || serial.contains("filled by") || serial.contains("Serial")) {
          return null;
        }
        return serial;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to resolve cpu id: " + e.getMessage());
    }
    return null;

  }
}

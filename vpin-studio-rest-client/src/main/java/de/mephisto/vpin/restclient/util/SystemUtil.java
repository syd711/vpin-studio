package de.mephisto.vpin.restclient.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  private final static List<String> INVALID_NAMES = Arrays.asList("Default", "filled by", "Serial");

  public static String getUniqueSystemId() {
    String id = getBoardSerialNumber();
    if (StringUtils.isEmpty(id)) {
      id = getCpuSerialNumber();
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
        if (!isNotValid(serial)) {
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
        if (!isNotValid(serial)) {
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

  private static boolean isNotValid(String serial) {
    if(StringUtils.isEmpty(serial)) {
      return false;
    }
    for (String invalidName : INVALID_NAMES) {
      if(serial.contains(invalidName)) {
        return false;
      }
    }
    return true;
  }
}

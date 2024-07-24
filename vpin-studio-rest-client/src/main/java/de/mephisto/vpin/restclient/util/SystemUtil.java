package de.mephisto.vpin.restclient.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String getBoardSerialNumber() throws Exception {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "baseboard", "get", "serialnumber"), false);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        String serial = split[split.length - 1];
        if (StringUtils.isEmpty(serial) || serial.contains("filled by")) {
          return getCpuSerialNumber();
        }

        return serial;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to resolve cabinet id: " + e.getMessage());
      return getCpuSerialNumber();
    }
    return null;
  }

  private static String getCpuSerialNumber() throws Exception {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "cpu", "get", "ProcessorId"), false);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        String serial = split[split.length - 1];
        if (StringUtils.isEmpty(serial)) {
          throw new UnsupportedOperationException("Failed to calculate unique system id.");
        }
        return serial;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to resolve cpu id: " + e.getMessage());
      throw e;
    }
    return null;

  }
}

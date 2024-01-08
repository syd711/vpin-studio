package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String getBoardSerialNumber() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "baseboard", "get", "serialnumber"), false);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        return split[split.length-1];
      }
    } catch (Exception e) {
      LOG.error("Failed to resolve cabinet id: " + e.getMessage());
    }
    return null;
  }
}

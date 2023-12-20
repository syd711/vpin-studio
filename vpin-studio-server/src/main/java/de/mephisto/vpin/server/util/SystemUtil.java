package de.mephisto.vpin.server.util;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.VPinStudioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = VPinStudioServer.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    } catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }

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

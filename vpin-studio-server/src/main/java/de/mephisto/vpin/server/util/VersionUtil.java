package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.VPinStudioServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VersionUtil.class);

  private static String version;

  public static String getVersion() {
    if (version != null) {
      return version;
    }

    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = VPinStudioServer.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      version = properties.getProperty("vpin.studio.version");
    }
    catch (IOException e) {
      LOG.error("Failed to read version number: {}", e.getMessage(), e);
    }
    return version;
  }

  public static boolean isMinorVersion() {
    return getPatchVersion() != null && getPatchVersion().equalsIgnoreCase("0");
  }

  private static String getPatchVersion() {
    String version = getVersion();
    if (!StringUtils.isEmpty(version)) {
      version = version.substring(version.lastIndexOf(".") + 1);
    }

    return version;
  }
}

package de.mephisto.vpin.server.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.preferences.PreferencesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

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
}

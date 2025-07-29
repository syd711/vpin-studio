package de.mephisto.vpin.server.frontend.pinbally;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PinballYSettingsParser {
  private final static Logger LOG = LoggerFactory.getLogger(PinballYSettingsParser.class);

  public Properties loadSettings(@NonNull File pinballYSettings) {
    Properties p = new Properties();
    try (BufferedReader file = new BufferedReader(new FileReader(pinballYSettings))) {
      load(p, file);
    }
    catch (Exception e) {
      LOG.error("cannot parse settings file " + pinballYSettings, e);
    }
    return p;
  }

  private void load(Properties p, BufferedReader lr) throws IOException {
    String line;

    while ((line = lr.readLine()) != null) {
      if (!line.startsWith("#")) {
        int pos = line.indexOf('=');
        if (pos > 0) {
          String key = line.substring(0, pos).trim();
          String value = line.substring(pos + 1).trim();
          p.setProperty(key, value);
        }
      }
    }
  }
}

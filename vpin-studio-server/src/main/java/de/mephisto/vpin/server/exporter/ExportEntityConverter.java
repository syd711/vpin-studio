package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.games.GameEmulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

public class ExportEntityConverter {
  private final static Logger LOG = LoggerFactory.getLogger(ExportEntityConverter.class);

  public static String convert(String name, Object property) {
    if (property instanceof String) {

    }
    else if (property instanceof Boolean) {

    }
    else if (property instanceof Integer) {

    }
    else if (property instanceof Long) {

    }
    else if (property instanceof File) {
      File f = (File) property;
      property = f.exists();
    }
    else if (property instanceof ValidationState) {
      ValidationState s = (ValidationState) property;
      property = s.getCode();
    }
    else if (property instanceof Date) {
      Date s = (Date) property;
      property = DateUtil.formatDateTime(s);
    }
    else if (property instanceof HighscoreType) {
      HighscoreType s = (HighscoreType) property;
      property = s.name();
    }
    else if (property instanceof AltColorTypes) {
      AltColorTypes s = (AltColorTypes) property;
      property = s.name();
    }
    else if (property instanceof GameEmulator) {
      GameEmulator s = (GameEmulator) property;
      property = s.getName();
    }
    else {
      LOG.warn("Unmapped field type: " + property.getClass().getSimpleName() + ", field name:" + name);
    }
    return String.valueOf(property)
        .replaceAll("\n", " ")
        .replaceAll("\r", "")
        .replaceAll("\t", "");
  }
}

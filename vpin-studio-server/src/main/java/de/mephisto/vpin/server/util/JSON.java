package de.mephisto.vpin.server.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mephisto.vpin.server.system.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

public class JSON {
  private final static Logger LOG = LoggerFactory.getLogger(JSON.class);

  private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static void write(Object object, String filename) {
    try {
      if (!filename.endsWith(".json")) {
        filename += ".json";
      }
      File file = new File(SystemInfo.RESOURCES, filename);
      FileWriter fileWriter = new FileWriter(file);
      gson.toJson(object, fileWriter);
      fileWriter.flush();
      LOG.info("Written " + file.getAbsolutePath());
    } catch (IOException e) {
      LOG.error("Error writing " + filename + ": " + e.getMessage(), e);
    }
  }

  public static <T> T read(Class<T> clazz, String filename) {
    try {
      if (!filename.endsWith(".json")) {
        filename += ".json";
      }
      File file = new File(SystemInfo.RESOURCES, filename);
      if (file.exists()) {
        Reader reader = Files.newBufferedReader(file.toPath());
        LOG.info("Reading " + file.getAbsolutePath());
        return gson.fromJson(reader, clazz);
      }
    } catch (IOException e) {
      LOG.error("Error reading " + filename + ": " + e.getMessage(), e);
    }
    return null;
  }
}

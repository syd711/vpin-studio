package de.mephisto.vpin.tools;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.mania.TarcisioWheels;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TarcisioWheelDBWriter {

  public static void main(String[] args) throws IOException {
    File origFolder = new File("C:\\workspace\\tarcisio-wheel-icons");
    File[] files = origFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".png");
      }
    });

    TarcisioWheels wheels = new TarcisioWheels();
    for (File file : files) {
      wheels.getData().put(FilenameUtils.getBaseName(file.getName()), file.getName());
    }

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    String s = objectMapper.writeValueAsString(wheels);
    File file = new File("C:\\workspace\\tarcisio-wheel-icons/wheels.json");
    if(file.exists()) {
      file.delete();
    }
    FileUtils.write(file, s, StandardCharsets.UTF_8);
  }

}

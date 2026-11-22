package de.mephisto.vpin.restclient.system;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FileInfoTest {

  @Test
  public void testJsonRoundTrip() {
    JsonMapper mapper = JsonMapper.builder().build();

    FileInfo file = FileInfo.file(new File("C:/vPinball/Tables/Twister/user/VPReg.stg"), new File("C:/vPinball/Tables/Twister/user"));
    String json = mapper.writeValueAsString(file);
    FileInfo read = mapper.readValue(json, FileInfo.class);
    assertTrue(read.isFile(), json);
    assertEquals(file.getFile(), read.getFile());
    assertEquals(file.getFallback(), read.getFallback());

    FileInfo folder = FileInfo.folder(new File("C:/vPinball/Tables/Twister/altsound"), null);
    read = mapper.readValue(mapper.writeValueAsString(folder), FileInfo.class);
    assertFalse(read.isFile());
    assertEquals(folder.getFile(), read.getFile());
  }
}

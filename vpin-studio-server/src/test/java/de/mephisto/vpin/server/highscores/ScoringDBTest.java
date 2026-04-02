package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import de.mephisto.vpin.server.system.SystemService;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScoringDBTest {

  static {
    SystemService.RESOURCES = "../resources/";
  }

  @Test
  public void testScoringDB() throws IOException, InterruptedException {
    ScoringDB db = ScoringDB.load();
    assertFalse(db.getSupportedNvRams().isEmpty());

    String[] split = PINemHiService.getPinemhiSupportedNVRams();

    System.out.println("Missing roms in DB:");
    List<String> result = new ArrayList<>();
    for (String s : split) {
      if(!db.getSupportedNvRams().contains(s.trim())) {
        System.out.println("\"" + s + "\",");
        result.add(s);
      }
    }

    assertTrue(result.isEmpty());
  }
}

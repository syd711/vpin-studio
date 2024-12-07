package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScoringDBTest {

  @Test
  public void testScoringDB() throws IOException, InterruptedException {
    ScoringDB db = ScoringDB.load();
    assertFalse(db.getSupportedNvRams().isEmpty());

    List<String> commands = Arrays.asList("pinemhi.exe", "-lr");
//      LOG.info("PinemHI: " + String.join(" ", commands));
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("../resources/pinemhi/"));
    executor.executeCommand();
    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    String stdOut = standardOutputFromCommand.toString();
    String[] split = stdOut.split("\n");
    System.out.println("Missing roms in DB:");
    List<String> result = new ArrayList<>();
    for (String s : split) {
      if(!db.getSupportedNvRams().contains(s)) {
        System.out.println("\"" + s + "\",");
        result.add(s);
      }
    }

    assertTrue(result.isEmpty());
  }
}

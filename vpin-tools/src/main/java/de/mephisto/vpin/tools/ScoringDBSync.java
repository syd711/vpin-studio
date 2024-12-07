package de.mephisto.vpin.tools;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ScoringDBSync {

  public static void main(String[] args) throws IOException, InterruptedException {
    ScoringDB db = ScoringDB.load();


    List<String> commands = Arrays.asList("pinemhi.exe", "-lr");
//      LOG.info("PinemHI: " + String.join(" ", commands));
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./resources/pinemhi/"));
    executor.executeCommand();
    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    String stdOut = standardOutputFromCommand.toString();
    String[] split = stdOut.split("\n");
    System.out.println("Missing roms in DB:");
    for (String s : split) {
      if(!db.getSupportedNvRams().contains(s)) {
        System.out.println("\"" + s + "\",");
      }
    }

  }
}

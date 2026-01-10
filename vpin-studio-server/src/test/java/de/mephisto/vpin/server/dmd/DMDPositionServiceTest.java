package de.mephisto.vpin.server.dmd;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;

import org.apache.commons.configuration2.INIConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DMDPositionServiceTest {

  @Test
  public void testGameNameCleansing() throws IOException {
    DMDPositionService svc = new DMDPositionService();
    doTest(svc, "The Leprechaun King (Original 2019) ScottyWic 0.35", "The Leprechaun King (Original 2019) ScottyWic");
    doTest(svc, "Stranger Things - Stranger Edition (Original 2018) LoadedWeapon 3.01 VR", "Stranger Things - Stranger Edition (Original 2018) LoadedWeapon 3.01 VR");
    doTest(svc, "Bird Fly (Original 2022) Loloallo 0.9.0", "Bird Fly (Original 2022) Loloallo");
    doTest(svc, "Bird Fly (Original 2022) Loloallo 0.9.0-DOF", "Bird Fly (Original 2022) Loloallo");
  }

  private void doTest(DMDPositionService svc, String gameFileName, String expectedStoreName) {
    Game game = new Game();
    game.setDMDType(DMDPackageTypes.UltraDMD);
    game.setGameFileName(gameFileName + ".vpx");

    String storename = svc.getStoreName(game);
    Assertions.assertEquals(expectedStoreName, storename);
  }

  @Test
  public void saveIni() throws Exception {
      INIConfiguration authSvn = new INIConfiguration();     
      //var groups = authSvn.getSection("sec.sec");
      authSvn.setProperty("sec..sec.p1", "val1");
      authSvn.setProperty("sec..sec.p2", "val2");     

      authSvn.setSeparatorUsedInOutput("=");
      StringWriter w = new StringWriter();
      authSvn.write(w);
      String output = w.toString();
      Assertions.assertEquals("[sec.sec]\r\n" + //
                "p1=val1\r\n" + //
                "p2=val2\r\n" + //
                "\r\n", output);
  }

}

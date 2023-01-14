package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.server.VPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class VpaServiceTest extends VPinServerTest {


  @Autowired
  private VpaService vpaService;

  @Autowired
  private GameService gameService;

  @Test
  public void testPack() throws IOException {
//    test(VPinServerTest.TEST_GAME_FILENAME);
    test("Hayburners (WIlliams 1951).vpx");
    test("Cirqus Voltaire.vpx");
//    test("The Addams Family.vpx");
    test("Jaws.vpx");
//    test("Stranger Things.vpx");
  }

  private void test(String name) {
    Game game = gameService.getGameByFilename(name);
    File zipFile = new File("E:\\temp\\" + game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
    vpaService.export(game, exportDescriptor, zipFile);
    System.out.println("Written " + zipFile.getAbsolutePath());
    assertTrue(zipFile.exists());
  }
}

package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class VpaServiceTest extends AbstractVPinServerTest {

  @Autowired
  private VpaService vpaService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Test
  public void testExport() throws IOException {
//    test(VPinServerTest.TEST_GAME_FILENAME);
//    test("Hayburners (WIlliams 1951).vpx");
//    exportTest("Attack from Mars 2.0.1.vpx");
//    test("The Addams Family.vpx");
    exportTest("Jaws in English.vpx");
//    test("Stranger Things.vpx");
  }

  private void exportTest(String name) {
    Game game = gameService.getGameByFilename(name);

    ExportDescriptor descriptor = new ExportDescriptor();
    descriptor.setExportHighscores(true);
    descriptor.setExportPupPack(false);
    descriptor.setExportPopperMedia(false);

    VpaManifest manifest = new VpaManifest();
    descriptor.setManifest(manifest);

    descriptor.getGameIds().add(game.getId());
    File target = new File("E:\\downloads\\" + game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
    List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
    Optional<Highscore> highscore = highscoreService.getHighscore(game.getId());
    VpaExporterJob exporter = new VpaExporterJob(systemService.getVPRegFile(), systemService.getVPXMusicFolder(), game, descriptor, manifest, highscore.get(), versions, target);
    exporter.execute();
    assertTrue(target.exists());
  }
}

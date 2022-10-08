package de.mephisto.vpin.server.generators;

import de.mephisto.vpin.server.directb2s.DirectB2SService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "generator")
public class GeneratorResource {
  private final static Logger LOG = LoggerFactory.getLogger(GeneratorResource.class);

  public final static File GENERATED_OVERLAY_FILE = new File(SystemService.RESOURCES, "overlay.jpg");

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DirectB2SService directB2SService;

  @GetMapping("/overlay")
  public ResponseEntity<byte[]> generateOverlay() throws Exception {
    onOverlayGeneration();
    return RequestUtil.serializeImage(GENERATED_OVERLAY_FILE);
  }

  @GetMapping("/card/{gameId}")
  public ResponseEntity<byte[]> generateCard(@PathVariable("gameId") int gameId) throws Exception {
    if(onCardGeneration(gameId, true)) {
      return RequestUtil.serializeImage(getCardSampleFile());
    }

    return RequestUtil.serializeImage(new File(SystemService.RESOURCES, "empty-preview.png"));
  }

  @GetMapping("/cards/{gameId}")
  public boolean generateCards(@PathVariable("gameId") int gameId) throws Exception {
    return onCardGeneration(gameId, false);
  }

  @GetMapping("/backgrounds")
  public List<String> getBackgrounds() {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> name.endsWith("jpg") || name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  @GetMapping("/background/{name}")
  public ResponseEntity<byte[]> getBackground(@PathVariable("name") String imageName) throws Exception {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> URLEncoder.encode(FilenameUtils.getBaseName(name)).equals(imageName));
    return RequestUtil.serializeImage(files[0]);
  }

  private BufferedImage onOverlayGeneration() throws Exception {
    try {
      BufferedImage bufferedImage = new OverlayGraphics(gameService, highscoreService).draw();
      ImageUtil.write(bufferedImage, GENERATED_OVERLAY_FILE);
      return bufferedImage;
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
  }

  private boolean onCardGeneration(int gameId, boolean sampleCard) throws Exception {
    try {
      Game game = gameService.getGame(gameId);
      BufferedImage bufferedImage = new CardGraphics(highscoreService, directB2SService, game).draw();
      if(bufferedImage != null) {
        if(sampleCard) {
          ImageUtil.write(bufferedImage, getCardSampleFile());
          return true;
        }
        else {
          File sampleFile = getCardFile(gameId);
          ImageUtil.write(bufferedImage, sampleFile);
          return true;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
    return false;
  }

  private File getCardSampleFile() {
    return new File(SystemService.RESOURCES, "highscore-card-sample.png");
  }

  private File getCardFile(int gameId) {
    Game game = gameService.getGame(gameId);
    PopperScreen screen = PopperScreen.valueOf(Config.getCardGeneratorConfig().getString("popper.screen", PopperScreen.Other2.name()));
    return game.getPopperScreenMedia(screen);
  }
}

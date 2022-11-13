package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.server.directb2s.DirectB2SService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.RequestUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "cards")
public class CardsResource {
  private final static Logger LOG = LoggerFactory.getLogger(CardsResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DirectB2SService directB2SService;

  @Autowired
  private SystemService systemService;

  @GetMapping("/preview/{gameId}")
  public ResponseEntity<byte[]> generateCard(@PathVariable("gameId") int gameId) throws Exception {
    File cardSampleFile = getCardSampleFile();
    if(!cardSampleFile.exists()) {
      onCardGeneration(gameId, true);
    }
    return RequestUtil.serializeImage(getCardSampleFile());
  }

  @GetMapping("/generate/{gameId}")
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
    if (files != null) {
      return RequestUtil.serializeImage(files[0]);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/directb2supload")
  public Boolean directb2supload(@RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam(value = "uploadType", required = false) String uploadType,
                                 @RequestParam("gameId") Integer gameId) {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    Game game = gameService.getGame(gameId);
    String directb2sFilename = FilenameUtils.getBaseName(game.getGameFileName()) + ".directb2s";
    File out = new File(systemService.getVPXTablesFolder(), directb2sFilename);
    if (uploadType != null && uploadType.equals("generator")) {
      out = new File(systemService.getDirectB2SFolder(), directb2sFilename);
    }

    out.mkdirs();
    LOG.info("Uploading " + out.getAbsolutePath());
    return UploadUtil.upload(file, out);
  }

  @PostMapping(value = "/backgroundupload")
  public Boolean upload(@RequestPart(value = "file", required = false) MultipartFile file, HttpServletRequest request) throws IOException {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    String name = file.getOriginalFilename().replaceAll("/", "").replaceAll("\\\\", "");
    File backgroundsFolder = new File(SystemService.RESOURCES, "backgrounds");
    File out = new File(backgroundsFolder, name);
    return UploadUtil.upload(file, out);
  }

  private boolean onCardGeneration(int gameId, boolean generateSampleCard) throws Exception {
    try {
      Game game = gameService.getGame(gameId);
      Highscore highscore = highscoreService.getHighscore(game);
      if (highscore != null && highscore.getRaw() != null) {
        Config.getCardGeneratorConfig().reload();

        BufferedImage bufferedImage = new CardGraphics(directB2SService, game, highscore).draw();
        if (bufferedImage != null) {
          if (generateSampleCard) {
            ImageUtil.write(bufferedImage, getCardSampleFile());
            return true;
          }
          else {
            File highscoreCard = getCardFile(game);
            ImageUtil.write(bufferedImage, highscoreCard);
            return true;
          }
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

  @NonNull
  private File getCardFile(@NonNull Game game) {
    String screenName = Config.getCardGeneratorConfig().getString("popper.screen", PopperScreen.Other2.name());
    PopperScreen screen = PopperScreen.valueOf(screenName);
    File mediaFolder = game.getEmulator().getPinUPMediaFolder(screen);
    return new File(mediaFolder, FilenameUtils.getBaseName(game.getGameFileName()) + ".png");
  }
}

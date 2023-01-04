package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "cards")
public class CardsResource {
  private final static Logger LOG = LoggerFactory.getLogger(CardsResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private CardService cardService;

  @GetMapping("/preview/{gameId}")
  public ResponseEntity<byte[]> generateCard(@PathVariable("gameId") int gameId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return RequestUtil.serializeImage(cardService.generateSampleCard(game));
    }
    throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + gameId);
  }

  @GetMapping("/generate/{gameId}")
  public boolean generateCards(@PathVariable("gameId") int gameId) throws Exception {
    Game game = gameService.getGame(gameId);
    return cardService.generateCard(game, false);
  }

  @GetMapping("/generatesample/{gameId}")
  public boolean generateSampleCard(@PathVariable("gameId") int gameId) throws Exception {
    Game game = gameService.getGame(gameId);
    return cardService.generateCard(game, true);
  }

  @GetMapping("/backgrounds")
  public List<String> getBackgrounds() {
    return cardService.getBackgrounds();
  }

  @GetMapping("/background/{name}")
  public ResponseEntity<byte[]> getBackground(@PathVariable("name") String imageName) throws Exception {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> URLEncoder.encode(FilenameUtils.getBaseName(name), StandardCharsets.UTF_8).equals(imageName));
    if (files != null) {
      return RequestUtil.serializeImage(files[0]);
    }
    return ResponseEntity.notFound().build();
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
}

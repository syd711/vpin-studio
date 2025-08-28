package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
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
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "cards")
public class CardsResource {
  private final static Logger LOG = LoggerFactory.getLogger(CardsResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private CardService cardService;

  @GetMapping("/cardtemplate/{gameId}")
  public CardTemplate getCardTemplate(@PathVariable("gameId") int gameId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return cardService.getCardTemplate(game.getTemplateId());
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/gamedata/{gameId}/{templateId}")
  public CardData getCardData(@PathVariable("gameId") int gameId, @PathVariable("templateId") long templateId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return cardService.getCardData(game, templateId, false);
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/preview/{gameId}")
  public ResponseEntity<byte[]> generateCardPreview(@PathVariable("gameId") int gameId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return RequestUtil.serializeImage(cardService.generateTableCardFile(game), "highscore-card-sample.png");
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/preview/{gameId}/{templateId}")
  public ResponseEntity<byte[]> generateCardPreview(@PathVariable("gameId") int gameId, @PathVariable("templateId") int templateId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return RequestUtil.serializeImage(cardService.generateTemplateTableCardFile(game, templateId), "highscore-card-sample.png");
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/generate/{gameId}")
  public boolean generateCard(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return cardService.generateCard(game);
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  // OLE Not used but kept in API
  @GetMapping("/generate/{gameId}/{templateId}")
  public boolean generateCardWithTemplate(@PathVariable("gameId") int gameId, @PathVariable("templateId") int templateId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return cardService.generateCard(game, templateId);
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/image/{name}/{gameId}/{templateId}")
  public ResponseEntity<byte[]> getImage(@PathVariable("gameId") int gameId, @PathVariable("templateId") long templateId, @PathVariable("name") String imageName) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      CardTemplate template = cardService.getCardTemplate(templateId);
      CardData cardData = cardService.getCardData(game, template, false);
      if (cardData != null) {
        byte[] bytes = cardService.getImage(game, cardData, template, imageName);
        if (bytes != null && bytes.length > 0) {
          return RequestUtil.serializeImage(bytes, imageName + ".png");
        }
        return ResponseEntity.notFound().build();
      }
      throw new ResponseStatusException(NOT_FOUND, "No template found for id " + templateId);
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/backgrounds")
  public List<String> getBackgrounds() {
    return cardService.getBackgrounds();
  }

  @GetMapping("/background/{name}")
  public ResponseEntity<byte[]> getBackground(@PathVariable("name") String imageName) throws Exception {
    String image = imageName.replaceAll("\\+", " ");
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> FilenameUtils.getBaseName(name).equals(image));
    if (files != null && files.length > 0) {
      return RequestUtil.serializeImage(files[0]);
    }
    return ResponseEntity.notFound().build();
  }


  @PostMapping(value = "/backgroundupload")
  public Boolean upload(@RequestPart(value = "file", required = false) MultipartFile file, HttpServletRequest request) throws IOException {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return false;
      }

      String name = file.getOriginalFilename().replaceAll("/", "").replaceAll("\\\\", "");
      File backgroundsFolder = new File(SystemService.RESOURCES, "backgrounds");
      File out = new File(backgroundsFolder, name);
      return UploadUtil.upload(file, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Background upload failed: " + e.getMessage());
    }
  }
}

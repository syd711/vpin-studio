package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import de.mephisto.vpin.server.util.UploadUtil;
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


  @GetMapping("/resolution/{templateType}")
  public CardResolution getCardResolution(@PathVariable("templateType") CardTemplateType templateType) {
    return cardService.getCardResolution(templateType);
  }

  @GetMapping("/cardtemplate/{gameId}/{templateType}")
  public CardTemplate getCardTemplate(@PathVariable("gameId") int gameId, @PathVariable("templateType") CardTemplateType templateType) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      if (game.getTemplateId(templateType) != null) {
        return cardService.getCardTemplate(game.getTemplateId(templateType));
      }
    }
    throw new ResponseStatusException(NOT_FOUND, "No game or template found for id " + gameId);
  }

  @GetMapping("/gamedata/{gameId}/{templateId}")
  public CardData getCardData(@PathVariable("gameId") int gameId, @PathVariable("templateId") long templateId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return cardService.getCardData(game, templateId, false);
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/preview/{gameId}/{templateType}")
  public ResponseEntity<byte[]> generateCardPreview(@PathVariable("gameId") int gameId, @PathVariable("templateType") CardTemplateType templateType) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return RequestUtil.serializeImage(cardService.generateTableCardFile(game, templateType), "card-sample.png");
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/preview/{gameId}/{templateId}")
  public ResponseEntity<byte[]> generateCardPreview(@PathVariable("gameId") int gameId, @PathVariable("templateId") int templateId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return RequestUtil.serializeImage(cardService.generateTemplateTableCardFile(game, templateId), "card-sample.png");
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  @GetMapping("/generate/{gameId}/{templateType}")
  public boolean generateCard(@PathVariable("gameId") int gameId, @PathVariable("templateType") CardTemplateType templateType) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return cardService.generateCard(game, templateType);
    }
    throw new ResponseStatusException(NOT_FOUND, "No game found for id " + gameId);
  }

  // OLE Not used but kept in API
  @GetMapping("/generate/{gameId}/{templateId}")
  public boolean generateCardWithTemplate(@PathVariable("gameId") int gameId, @PathVariable("templateId") long templateId) {
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

  //-------------------------------------------

  @GetMapping("/backgrounds")
  public List<String> getBackgrounds() {
    return cardService.getImages("backgrounds");
  }
  @GetMapping("/frames")
  public List<String> getFrames() {
    return cardService.getImages("frames");
  }

  @GetMapping("/background/{name}")
  public ResponseEntity<byte[]> getBackground(@PathVariable("name") String imageName) throws Exception {
    File img = cardService.getImage("backgrounds", imageName);
    return img != null ? RequestUtil.serializeImage(img) : ResponseEntity.notFound().build();
  }

  @GetMapping("/frame/{name}")
  public ResponseEntity<byte[]> getFrame(@PathVariable("name") String imageName) throws Exception {
    File img = cardService.getImage("frames", imageName);
    return img != null ? RequestUtil.serializeImage(img) : ResponseEntity.notFound().build();
  }

  @PostMapping(value = "/backgroundUpload")
  public Boolean backgroundUpload(@RequestPart(value = "file", required = false) MultipartFile file, HttpServletRequest request) throws IOException {
    return doUpload(file, "backgrounds");
  }

  @PostMapping(value = "/frameUpload")
  public Boolean frameUpload(@RequestPart(value = "file", required = false) MultipartFile file, HttpServletRequest request) throws IOException {
    return doUpload(file, "frames");
  }

  private Boolean doUpload(MultipartFile file, String subfolder) throws IOException {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return false;
      }

      String name = file.getOriginalFilename().replaceAll("/", "").replaceAll("\\\\", "");
      File backgroundsFolder = new File(SystemService.RESOURCES, subfolder);
      File out = new File(backgroundsFolder, name);
      return UploadUtil.upload(file, out);
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Background upload failed: " + e.getMessage());
    }
  }
}

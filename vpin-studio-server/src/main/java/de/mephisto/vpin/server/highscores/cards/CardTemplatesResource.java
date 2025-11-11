package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.JsonArg;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "cardtemplates")
public class CardTemplatesResource {

  @Autowired
  private CardTemplatesService cardTemplatesService;

  @GetMapping
  public List<CardTemplate> getTemplates() throws Exception {
    return cardTemplatesService.getTemplates();
  }

  @GetMapping("/{id}")
  public CardTemplate getTemplate(@PathVariable("id") long id) throws Exception {
    return cardTemplatesService.getTemplate(id);
  }

  @PostMapping(value = "/save")
  public CardTemplate saveTemplate(@RequestBody CardTemplate cardTemplate) throws Exception {
    return cardTemplatesService.save(cardTemplate);
  }

  @DeleteMapping(value = "/delete/{id}")
  public boolean deleteTemplate(@PathVariable("id") int id) throws Exception {
    return cardTemplatesService.delete(id);
  }

  @PostMapping(value = "/assign")
  public Boolean assignTemplate(@JsonArg("gameId") int gameId, 
                                @JsonArg("templateId") long templateId, 
                                @JsonArg("switchToCardMode") boolean switchToCardMode, 
                                @JsonArg("templateType") String templateType) throws Exception {
    return cardTemplatesService.assignTemplate(gameId, templateId, switchToCardMode, CardTemplateType.valueOf(templateType));
  }

  @PostMapping("/upload")
  public CardTemplate uploadTemplate(@RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam(value = "templateName", required = true) String templateName) throws Exception {
    if (file == null) {
      return null;
    }
    String json = new String(file.getBytes());
    return cardTemplatesService.createFromJson(templateName, json);
  }
}

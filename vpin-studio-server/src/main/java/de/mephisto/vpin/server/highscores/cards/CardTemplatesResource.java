package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.JsonArg;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}

package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
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

  @PostMapping(value = "/savetemplate")
  public CardTemplate saveTemplate(@RequestBody CardTemplate cardTemplate) throws Exception {
    return cardTemplatesService.save(cardTemplate);
  }

  @DeleteMapping(value = "/deletetemplate/{id}")
  public boolean deleteTemplate(@PathVariable("id") int id) throws Exception {
    return cardTemplatesService.delete(id);
  }
}

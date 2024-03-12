package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CardTemplatesService {

  @Autowired
  private TemplateMappingRepository templateMappingRepository;

  public CardTemplate save(CardTemplate cardTemplate) throws Exception {
    TemplateMapping mapping = new TemplateMapping();
    mapping.setCardTemplate(cardTemplate);
    TemplateMapping updatedMapping = templateMappingRepository.saveAndFlush(mapping);
    CardTemplate updatedTemplate = CardTemplate.fromJson(CardTemplate.class, updatedMapping.getTemplateJson());
    updatedTemplate.setId(updatedTemplate.getId());
    return updatedTemplate;
  }

  public boolean delete(int id) {
    templateMappingRepository.deleteById((long) id);
    return true;
  }

  public List<CardTemplate> getTemplates() throws Exception {
    List<CardTemplate> result = new ArrayList<>();
    List<TemplateMapping> all = templateMappingRepository.findAll();
    for (TemplateMapping mapping : all) {
      CardTemplate template = CardTemplate.fromJson(CardTemplate.class, mapping.getTemplateJson());
      template.setId(mapping.getId());
    }
    return result;
  }

  public CardTemplate getTemplatesForGame(int id) {
    return new CardTemplate();
  }
}

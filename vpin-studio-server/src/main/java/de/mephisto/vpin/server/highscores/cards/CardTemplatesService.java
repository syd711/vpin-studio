package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CardTemplatesService {

  @Autowired
  private TemplateMappingRepository templateMappingRepository;

  public CardTemplate save(CardTemplate cardTemplate) throws Exception {
    if (cardTemplate.getId() != null) {
      Optional<TemplateMapping> mapping = templateMappingRepository.findById(cardTemplate.getId());
      if (mapping.isPresent()) {
        TemplateMapping m = mapping.get();
        m.setCardTemplate(cardTemplate);
        templateMappingRepository.saveAndFlush(m);
        return cardTemplate;
      }
    }

    TemplateMapping m = new TemplateMapping();
    m.setCardTemplate(cardTemplate);
    TemplateMapping updatedMapping = templateMappingRepository.saveAndFlush(m);
    cardTemplate.setId(updatedMapping.getId());
    return cardTemplate;
  }

  public boolean delete(int id) {
    templateMappingRepository.deleteById((long) id);
    return true;
  }

  public List<CardTemplate> getTemplates() throws Exception {
    List<CardTemplate> result = new ArrayList<>();
    List<TemplateMapping> all = templateMappingRepository.findAll();
    if (all.isEmpty()) {
      CardTemplate template = new CardTemplate();
      template.setName(CardTemplate.DEFAULT);
      save(template);
    }

    all = templateMappingRepository.findAll();
    for (TemplateMapping mapping : all) {
      CardTemplate template = CardTemplate.fromJson(CardTemplate.class, mapping.getTemplateJson());
      template.setId(mapping.getId());
      result.add(template);
    }
    return result;
  }

  public CardTemplate getTemplatesForGame(int id) {
    return new CardTemplate();
  }
}

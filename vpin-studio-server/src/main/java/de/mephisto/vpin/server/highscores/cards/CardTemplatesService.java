package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CardTemplatesService {

  /** 
   * The current version of CardTemplate. Increment by 1 when incompatible changes 
   * in CardTemplate are introduced and update the checkversion() method
   */
  public static final Integer CURRENT_VERSION = 2;

  @Autowired
  private TemplateMappingRepository templateMappingRepository;

  @Autowired
  private PreferencesService preferencesService;

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
      all = templateMappingRepository.findAll();
    }

    for (TemplateMapping mapping : all) {
      CardTemplate template = CardTemplate.fromJson(CardTemplate.class, mapping.getTemplateJson());
      template = checkVersion(template);
      template.setId(mapping.getId());
      result.add(template);
    }
    return result;
  }

  public CardTemplate getTemplateForGame(Game game) throws Exception {
    if (game.getTemplateId() != null) {
      Optional<TemplateMapping> byId = templateMappingRepository.findById(game.getTemplateId());
      if (byId.isPresent()) {
        TemplateMapping mapping = byId.get();
        CardTemplate template = CardTemplate.fromJson(CardTemplate.class, mapping.getTemplateJson());
        template = checkVersion(template);
        template.setId(mapping.getId());
        return template;
      }
    }

    return getCardTemplate(CardTemplate.DEFAULT);
  }

  private CardTemplate getCardTemplate(String name) throws Exception {
    Optional<CardTemplate> first = getTemplates().stream().filter(c -> c.getName().equals(name)).findFirst();
    if (first.isEmpty()) {
      first = getTemplates().stream().filter(c -> c.getName().equals(CardTemplate.DEFAULT)).findFirst();
    }

    return first.get();
  }

  public CardTemplate getTemplate(long templateId) throws Exception {
    Optional<TemplateMapping> mapping = templateMappingRepository.findById(templateId);
    if (mapping.isPresent()) {
      TemplateMapping m = mapping.get();
      CardTemplate template = CardTemplate.fromJson(CardTemplate.class, m.getTemplateJson());
      template = checkVersion(template);
      template.setId(m.getId());
      return template;
    }

    return getCardTemplate(CardTemplate.DEFAULT);
  }

  //-------------------------------------------------- Template version management

  private CardTemplate checkVersion(CardTemplate template) throws Exception {
    Integer version = template.getVersion();
    if (version == null || version == 1) {
      template = upgradeFromVersion1(template);
      template.setVersion(CURRENT_VERSION);
      template = save(template);
    }
  
    return template;
  }

  private CardTemplate upgradeFromVersion1(CardTemplate template) {

    CardSettings cardSettings = preferencesService.getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS);
    CardResolution res = cardSettings.getCardResolution();
    if (res != null) {
      double width = res.toWidth();
      double height = res.toHeight();

      template.setCanvasX(template.getCanvasX() / width);
      template.setCanvasY(template.getCanvasY() / height);
      template.setCanvasWidth(template.getCanvasWidth() / width);
      template.setCanvasHeight(template.getCanvasHeight() / height);

      double currentY = template.getMarginTop();
      template.setTitleY(currentY / height);
      template.setTitleHeight(template.getTitleFontSize() / height);

      currentY += template.getTitleFontSize() + template.getPadding();
      template.setTableY(currentY / height);
      template.setTableHeight(template.getTableFontSize() / height);

      currentY += template.getTableFontSize() + template.getPadding();
      template.setWheelY(currentY / height);
      template.setScoresY(currentY / height);
      template.setScoresHeight((height - currentY - template.getMarginBottom()) / height);


      double currentX = template.getMarginLeft();
      template.setWheelX(currentX / width);
      if (template.isRenderWheelIcon()) {
        currentX += template.getWheelSize() + template.getPadding();
      }
      template.setWheelSize(template.getWheelSize() / width);

      template.setScoresX(currentX / width);
      template.setScoresWidth((width - currentX - template.getMarginRight()) / width);
    }
    return template;
  }
}


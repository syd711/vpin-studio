package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  @Autowired
  private TemplateMerger templateMerger;

  public CardTemplate save(CardTemplate cardTemplate) {
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
    return getTemplateOrDefault(updatedMapping.getId());
  }

  public synchronized boolean delete(int id) {
    Optional<TemplateMapping> byId = templateMappingRepository.findById((long) id);
    if (byId.isPresent()) {
      TemplateMapping templateMapping = byId.get();
      if (!templateMapping.getTemplate().getName().equals(CardTemplate.DEFAULT)) {
        templateMappingRepository.deleteById((long) id);
        return true;
      }
    }
    return false;
  }

  public List<CardTemplate> getTemplates() {
    
    List<TemplateMapping> all = templateMappingRepository.findAll();
    if (all.isEmpty()) {
      CardTemplate template = new CardTemplate();
      template.setName(CardTemplate.DEFAULT);
      save(template);
      all = templateMappingRepository.findAll();
    }

    List<CardTemplate> results = all.stream()
      .map(m -> mappingToTemplate(m))
      .collect(Collectors.toList());

    return results;
  }

  public CardTemplate getTemplateForGame(Game game) {
    return getTemplateOrDefault(game.getTemplateId());
  }

  public CardTemplate getTemplateOrDefault(Long templateId) {
    if (templateId != null) {
      Optional<TemplateMapping> mapping = templateMappingRepository.findById(templateId);
      if (mapping.isPresent()) {
        return mappingToTemplate(mapping.get());
      }
    }
    // oll other cases
    return getDefaultTemplate();
  }

  private CardTemplate getDefaultTemplate() {
    List<TemplateMapping> all = templateMappingRepository.findAll();
    return all.stream()
      .filter(m -> m.getTemplate().isTemplate() && CardTemplate.DEFAULT.equals(m.getTemplate().getName()))
      .map(m -> mappingToTemplate(m))
      .findFirst()
      .orElse(null);
  }

  //-------------------------------------------------- Merge of templates

  private CardTemplate mappingToTemplate(TemplateMapping m) {
    CardTemplate template = checkVersion(m.getTemplate());
    template.setId(m.getId());
    if (!template.isTemplate()) {
      mergeWithParent(template);
    }
    return template;
  }

  private CardTemplate mergeWithParent(CardTemplate template) {
    CardTemplate parent = null;
    if (template.getParentId() != null) {
      parent = getTemplateOrDefault(template.getParentId());
    }
    else {
      // no parent, merger with DEFAULT
      parent = getDefaultTemplate();
    }

    return templateMerger._merge(template, parent);
  }

  //-------------------------------------------------- Template version management

  private CardTemplate checkVersion(CardTemplate template) {
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

      currentY += template.getTitleFontSize();
      template.setTableY(currentY / height);
      template.setTableHeight(template.getTableFontSize() / height);

      currentY += template.getTableFontSize();
      template.setWheelY(currentY / height);
      template.setScoresY(currentY / height);
      template.setScoresHeight((height - currentY - template.getMarginBottom()) / height);


      double currentX = template.getMarginLeft();
      template.setWheelX(currentX / width);
      if (template.isRenderWheelIcon()) {
        currentX += template.getWheelSize();
      }
      template.setWheelSize(template.getWheelSize() / width);

      template.setScoresX(currentX / width);
      template.setScoresWidth((width - currentX - template.getMarginRight()) / width);
    }
    return template;
  }
}


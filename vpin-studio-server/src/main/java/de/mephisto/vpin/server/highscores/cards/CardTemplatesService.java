package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CardTemplatesService {
  private final static Logger LOG = LoggerFactory.getLogger(CardTemplatesService.class);

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
  private GameService gameService;

  @Autowired
  private TemplateMerger templateMerger;

  public synchronized CardTemplate save(CardTemplate cardTemplate) {
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

  public synchronized boolean delete(long id) {
    Optional<TemplateMapping> byId = templateMappingRepository.findById(id);
    if (byId.isPresent()) {
      //OLE : prevent delete DEFAULT via UI but not via service ? + delete allow sthe reset feature
      //TemplateMapping templateMapping = byId.get();
      //if (!templateMapping.getTemplate().getName().equals(CardTemplate.DEFAULT)) {
        templateMappingRepository.deleteById(id);
        return true;
      //}
    }
    return false;
  }

  public List<CardTemplate> getTemplates() {
    List<TemplateMapping> all = templateMappingRepository.findAll();

    List<CardTemplate> results = all.stream().map(m -> mappingToTemplate(m)).collect(Collectors.toList());

    createDefaultIfAbsent(results, CardTemplateType.HIGSCORE_CARD);
    createDefaultIfAbsent(results, CardTemplateType.INSTRUCTIONS_CARD);
    createDefaultIfAbsent(results, CardTemplateType.WHEEL);

    return results;
  }


  private void createDefaultIfAbsent(List<CardTemplate> results, CardTemplateType templateType) {
    if (ListUtils.indexOf(results, template -> template.isDefault() && templateType.equals(template.getTemplateType())) < 0) {
      CardTemplate template = new CardTemplate();
      template.setName(CardTemplate.DEFAULT);
      template.setVersion(CURRENT_VERSION);
      template.setTemplateType(templateType);
      switch (templateType) {
        case HIGSCORE_CARD:
          template.resetDefaultHighscoreCard();
          break;
        case INSTRUCTIONS_CARD:
          template.resetDefaultInstructionsCard();
          break;
        case WHEEL:
          template.resetDefaultWheel();
          break;
      }

      // save the template
      template = save(template);
      results.add(template);
    }
  }

  public CardTemplate getTemplateForGame(Game game, CardTemplateType templateType) {
    return getTemplateOrDefault(game.getTemplateId(templateType));
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

  //TODO need to be reviewed as a defaultTemplate with a type no more need anything
  private CardTemplate getDefaultTemplate() {
    List<TemplateMapping> all = templateMappingRepository.findAll();
    return all.stream()
      .filter(m -> m.getTemplate().isTemplate() && m.getTemplate().isDefault())
      .map(m -> mappingToTemplate(m))
      .findFirst()
      .orElse(null);
  }

  public boolean assignTemplate(int gameId, long templateId, boolean switchToCardMode, CardTemplateType templateType) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      LOG.error("Cannot assign template to game {} as the game does not exist", gameId);
      return false;
    }
    
    Optional<TemplateMapping> baseTemplateMapping = templateMappingRepository.findById(templateId);
    if (baseTemplateMapping.isEmpty()) {
      LOG.error("Cannot assign template {} as it does not exist", templateId);
      return false;
    }
    CardTemplate baseTemplate = baseTemplateMapping.get().getTemplate();

    Long oldTemplateId = game.getTemplateId(templateType);

      //create new card template
    if (switchToCardMode) {
      //delete possible existing one
      TemplateMapping mapping = oldTemplateId != null ? templateMappingRepository.findById(oldTemplateId).orElse(null) : null;
      if (mapping != null) {
        CardTemplate template = mapping.getTemplate();
        if (!template.isTemplate()) {
          templateMappingRepository.deleteById(oldTemplateId);
        }
      }

      baseTemplate.setId(null);
      baseTemplate.setParentId(templateId);
      baseTemplate.setName(CardTemplate.CARD_TEMPLATE_PREFIX + game.getId());
      baseTemplate = save(baseTemplate);

      game.setTemplateId(templateType, baseTemplate.getId());
      gameService.save(game);
      return true;
    }
    else if (oldTemplateId != null && oldTemplateId != templateId || oldTemplateId == null && !baseTemplate.isDefault()) {
      game.setTemplateId(templateType, templateId);
      gameService.save(game);
      return true;
    }
    return false;
  }

  //-------------------------------------------------- Merge of templates

  private CardTemplate mappingToTemplate(TemplateMapping m) {
    CardTemplate template = checkVersion(m.getTemplate());
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


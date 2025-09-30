package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/*********************************************************************************************************************
 * Highscore Card Templates
 ********************************************************************************************************************/
public class HighscoreCardTemplatesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private List<CardTemplate> cachedTemplates = new ArrayList<>();

  public HighscoreCardTemplatesServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<CardTemplate> getTemplates() {
    if (cachedTemplates.isEmpty()) {
      CardTemplate[] templates = getRestClient().get(API + "cardtemplates", CardTemplate[].class);
      if (templates != null) {
        cachedTemplates = new ArrayList<>(Arrays.asList(templates));
      }
    }
    return cachedTemplates;
  }

  public List<CardTemplate> getTemplates(CardTemplateType templateType) {
    return getTemplates().stream().filter(template -> templateType.equals(template.getTemplateType())).collect(Collectors.toList());
  }

  public void deleteTemplate(Long id) {
    try {
      getRestClient().delete(API + "cardtemplates/delete/" + id);
      cachedTemplates.clear();
    }
    catch (Exception e) {
      LOG.error("Failed to delete template: " + e.getMessage(), e);
    }
  }

  public CardTemplate save(CardTemplate template) {
    try {
      return getRestClient().post(API + "cardtemplates/save", template, CardTemplate.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save template: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
    finally {
      cachedTemplates.clear();
    }
  }

  public CardTemplate getTemplateById(Long id) {
    List<CardTemplate> templates = getTemplates();
    Optional<CardTemplate> first = templates.stream().filter(t -> t.getId().equals(id)).findFirst();
    return first.orElse(null);
  }

  public CardTemplate getDefaultTemplate(CardTemplateType templateType) {
    List<CardTemplate> templates = getTemplates();
    Optional<CardTemplate> first = templates.stream().filter(t -> t.isDefault() && templateType.equals(t.getTemplateType())).findFirst();
    return first.orElse(null);
  }

  public CardTemplate getBaseCardTemplateForGame(GameRepresentation game, CardTemplateType templateType) {
    CardTemplate templateById = null;
    if (game.getTemplateId(templateType) != null) {
      templateById = getTemplateById(game.getTemplateId(templateType));
      if (templateById != null && !templateById.isTemplate()) {
        templateById = getTemplateById(templateById.getParentId());
      }
    }
    return templateById != null ? templateById : getDefaultTemplate(templateType);
  }

  public CardTemplate getCardTemplateForGame(GameRepresentation game, CardTemplateType templateType) {
    CardTemplate template = null;
    if (game.getTemplateId(templateType) != null) {
      template = getTemplateById(game.getTemplateId(templateType));
    }
    return template != null ? template : getDefaultTemplate(templateType);
  }

  public boolean assignTemplate(GameRepresentation game, Long templateId, boolean switchToCardMode, CardTemplateType templateType) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("gameId", game.getId());
      params.put("templateId", templateId);
      params.put("templateType", templateType);
      params.put("switchToCardMode", switchToCardMode);
      return getRestClient().post(API + "cardtemplates/assign", params, Boolean.class);
    }
    finally {
      cachedTemplates.clear();
    }
  }
}
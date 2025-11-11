package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
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

  public String getCardTemplateUrl(CardTemplate cardTemplate) {
    return getRestClient().getBaseUrl() + API + "cardtemplates/" + cardTemplate.getId();
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

  public @Nullable CardTemplate uploadTemplate(String templateName, File templateFile) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "cardtemplates/upload";
      
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("templateName", templateName);
      HttpEntity<MultiValueMap<String, Object>> upload = createUpload(map, templateFile, -1, UploadType.uploadAndImport, AssetType.CARD_ASSET, null);

      //new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      ResponseEntity<CardTemplate> ret = createUploadTemplate().exchange(url, HttpMethod.POST, upload, CardTemplate.class);
      finalizeUpload(upload);
      return ret.getBody();
    }
    catch (Exception e) {
      LOG.error("Upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

}
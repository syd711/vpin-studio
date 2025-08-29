package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


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
      cachedTemplates = new ArrayList<>(Arrays.asList(getRestClient().get(API + "cardtemplates", CardTemplate[].class)));
    }
    return cachedTemplates;
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

  public CardTemplate getTemplateById(Long parentId) {
    List<CardTemplate> templates = getTemplates();
    Optional<CardTemplate> first = templates.stream().filter(t -> t.getId().equals(parentId)).findFirst();
    return first.orElse(getDefaultTemplate());
  }

  public CardTemplate getTemplateByName(String name) {
    List<CardTemplate> templates = getTemplates();
    Optional<CardTemplate> first = templates.stream().filter(t -> t.getName().equals(name)).findFirst();
    return first.orElse(null);
  }

  public CardTemplate getDefaultTemplate() {
    return getTemplateByName(CardTemplate.DEFAULT);
  }

  public CardTemplate getCardTemplateForGame(GameRepresentation game) {
    if (game.getTemplateId() != null) {
      CardTemplate templateById = getTemplateById(game.getTemplateId());
      if (templateById != null) {
        return templateById;
      }
    }
    return getDefaultTemplate();
  }
}

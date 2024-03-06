package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.JsonSettings;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CardTemplates extends JsonSettings {
  private List<CardTemplate> templates = new ArrayList<>();

  public List<CardTemplate> getTemplates() {
    return templates;
  }

  public boolean contains(CardTemplate t) {
    return templates.contains(t);
  }

  public void setTemplates(List<CardTemplate> templates) {
    this.templates = templates;
  }

  @NonNull
  public CardTemplate getDefaultTemplate() {
    for (CardTemplate template : templates) {
      if (template.getName().equals(CardTemplate.DEFAULT)) {
        return template;
      }
    }
    return new CardTemplate();
  }

  public void remove(CardTemplate cardTemplate) {
    templates.remove(cardTemplate);
  }
}

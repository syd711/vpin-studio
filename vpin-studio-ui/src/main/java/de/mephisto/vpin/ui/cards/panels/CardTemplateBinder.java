package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.ui.util.binding.BeanBinder;

public class CardTemplateBinder extends BeanBinder<CardTemplate> {

  private CardResolution resolution;

  public CardTemplateBinder() {
    super();
  }

  public CardResolution getResolution() {
    return resolution;
  }

  public void setResolution(CardResolution res) {
    this.resolution = res;
  }

  public CardTemplateType getTemplateType() {
    return bean != null ? bean.getTemplateType() : null;
  }
}

package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.ui.util.binding.BeanBinder;

public class CardTemplateBinder extends BeanBinder<CardTemplate> {

  private int width;
  private int height;

  public CardTemplateBinder() {
    super();
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public CardTemplateType getTemplateType() {
    return bean != null ? bean.getTemplateType() : null;
  }
}

package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;

public class CardTemplateBinder extends BeanBinder<CardTemplate> {

  private CardResolution resolution;

  public CardTemplateBinder(BindingChangedListener listener) {
    super(listener);
  }

  public CardResolution getResolution() {
    return resolution;
  }

  public void setResolution(CardResolution res) {
    this.resolution = res;
  }
}

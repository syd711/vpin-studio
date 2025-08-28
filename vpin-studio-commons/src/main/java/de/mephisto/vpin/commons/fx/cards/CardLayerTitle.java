package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;

public class CardLayerTitle extends CardLayerBaseText {

  @Override
  protected CardTextData getTextData(@Nonnull CardTemplate template, @Nullable CardData data) {
    CardTextData textData = new CardTextData();
    textData.text = template.getTitle();
    textData.fontName = template.getTitleFontName();
    textData.fontStyle = template.getTitleFontStyle();
    textData.fontSIZE = template.getTitleFontSize();
    textData.useDefaultColor = template.isTitleUseDefaultColor();
    textData.fontColor = template.getTitleColor();
    return textData;
  }

}

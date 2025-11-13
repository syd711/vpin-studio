package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;

public class CardLayerOtherMedia extends CardLayerBaseImage {

  @Override
  protected boolean keepAspectRatio(@Nonnull CardTemplate template) {
    return template.isOtherMediaKeepAspectRatio();
  }

  @Override
  protected byte[] getImage(@Nonnull CardTemplate template, @Nullable CardData data) {
    return data != null ? data.getOtherMedia() : null;
  }

  protected double getImageWidth(@Nonnull CardTemplate template) {
    return template.getOtherMediaWidth();
  }

  protected double getImageHeight(@Nonnull CardTemplate template) {
    return template.getOtherMediaHeight();
  }
 }

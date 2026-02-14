package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;

public class CardLayerOtherMedia extends CardLayerBaseImage {

  @Override
  public boolean keepAspectRatio(@Nonnull CardTemplate template) {
    return template.isOtherMediaKeepAspectRatio();
  }

  @Override
  protected int getAlignment(@Nonnull CardTemplate template) {
    return template.getOtherMediaAlignment();
  }

  @Override
  protected byte[] getImage(@Nonnull CardTemplate template, @Nullable CardData data) {
    return data != null ? data.getOtherMedia() : null;
  }
 }

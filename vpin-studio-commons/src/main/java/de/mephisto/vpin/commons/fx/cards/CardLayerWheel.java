package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;

public class CardLayerWheel extends CardLayerBaseImage {

  @Override
  protected boolean keepAspectRatio(@Nonnull CardTemplate template) {
    return true;
  }

  @Override
  protected byte[] getImage(@Nonnull CardTemplate template, @Nullable CardData data) {
    return data != null ? data.getWheel() : null;
  }
}

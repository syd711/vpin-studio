package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;

public class CardLayerManufacturer extends CardLayerBaseImage {

  @Override
  protected boolean keepAspectRatio(@Nonnull CardTemplate template) {
    return template.isManufacturerLogoKeepAspectRatio();
  }

  @Override
  protected byte[] getImage(@Nonnull CardTemplate template, @Nullable CardData data) {
    return data != null ? data.getManufacturerLogo() : null;
  }

}

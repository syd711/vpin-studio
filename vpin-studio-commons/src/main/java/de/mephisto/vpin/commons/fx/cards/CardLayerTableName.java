package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;

public class CardLayerTableName extends CardLayerText {

  @Override
  protected CardTextData getTextData(@Nonnull CardTemplate template, @Nullable CardData data) {
    CardTextData textData = new CardTextData();

    textData.text = "<Game Name>";
    if (data != null) {
      textData.text = data.getGameDisplayName();
      // if game has an associated VPS ID and use VPS name
      if (template.isTableUseVpsName() && data.getVpsTableId() != null) {
        StringBuilder suffix = new StringBuilder();
        if (template.isTableRenderManufacturer() && StringUtils.isNotBlank(data.getManufacturer())) {
          suffix.append(data.getManufacturer());
        }
        if (template.isTableRenderYear() && data.getYear() != null && data.getYear() > 0) {
          if (suffix.length() > 0) {
            suffix.append(" ");
          }
          suffix.append(data.getYear());
        }
        if (suffix.length() > 0) {
          suffix.insert(0, " (").append(")");
        }

        // finally add the VPS game name
        suffix.insert(0, data.getVpsName());
        textData.text = suffix.toString();
      }
    }

    textData.fontName = template.getTableFontName();
    textData.fontStyle = template.getTableFontStyle();
    textData.fontSIZE = template.getTableFontSize();
    textData.useDefaultColor = template.isTableUseDefaultColor();
    textData.fontColor = template.getTableColor();
    return textData;
  }

}

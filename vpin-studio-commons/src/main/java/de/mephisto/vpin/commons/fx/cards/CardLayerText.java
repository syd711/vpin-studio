package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.geometry.VPos;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class CardLayerText extends Text implements CardLayer {

  public static enum CardLayerTextType {
    Title, TableName
  }
  
  private CardLayerTextType type;

  public CardLayerText(CardLayerTextType type) {
    super();
    this.type = type;
  }
  
  public CardLayerTextType getType() {
    return type;
  }

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {

    String text = null;
    boolean useDefaultColor = true;
    String fontName = null, fontStyle = null, fontColor = null;
    int fontSIZE = -1;
    switch (type) {
      case Title: {
        text = template.getTitle();
        fontName = template.getTitleFontName();
        fontStyle = template.getTitleFontStyle();
        fontSIZE = template.getTitleFontSize();
        useDefaultColor = template.isTitleUseDefaultColor();
        fontColor = template.getTitleColor();
        break;
      }
      case TableName: {
        text = "<Game Name>";
        if (data != null) {
          text = data.getGameDisplayName();
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
            text = suffix.toString();
          }
        }

        fontName = template.getTableFontName();
        fontStyle = template.getTableFontStyle();
        fontSIZE = template.getTableFontSize();
        useDefaultColor = template.isTableUseDefaultColor();
        fontColor = template.getTableColor();
        break;
      }
    }
    setText(text);

    Paint paint = Paint.valueOf(useDefaultColor ? template.getFontColor() : fontColor);
    setFill(paint);

    Font font = createFont(fontName, fontStyle, fontSIZE * zoomY);
    setFont(font);

    setTextAlignment(TextAlignment.CENTER);
    setTextOrigin(VPos.TOP);
  }

  //------------------------------------

  /** Store the y of the bounding box as real Text.y is compensated */
  private double locY;
  /** Store the height of the bounding box as it does not exist */
  private double height;

  @Override
  public double getLocY() {
    return locY;
  }
  @Override
  public void relocate(double x, double y) {
    locY = y;
    super.relocate(x, y + getHeight() * 0);
  }

  @Override
  public double getWidth() {
    return getWrappingWidth();
  }

  @Override
  public void setWidth(double w) {
    setWrappingWidth(w);
  }

  @Override
  public double getHeight() {
    return height;
  }

  @Override
  public void setHeight(double h) {
    this.height = h;
  }

}

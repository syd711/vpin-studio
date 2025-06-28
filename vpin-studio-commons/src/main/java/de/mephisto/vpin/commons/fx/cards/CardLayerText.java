package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.geometry.VPos;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

public class CardLayerText extends Text implements CardLayer {

  public static enum CardLayerTextType {
    Title, TableName
  }
  
  private CardLayerTextType type;

  public CardLayerText(CardLayerTextType type) {
    super();
    this.type = type;
  }

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {

    String text = null;
    String fontName = null, fontStyle = null;
    int fontSIZE = -1;
    switch (type) {
      case Title: {
        text = template.getTitle();
        fontName = template.getTitleFontName();
        fontStyle = template.getTitleFontStyle();
        fontSIZE = template.getTitleFontSize();
        break;
      }
      case TableName: {
        text = data != null ? data.getGameDisplayName() : "<Game Name>";
        fontName = template.getTableFontName();
        fontStyle = template.getTableFontStyle();
        fontSIZE = template.getTableFontSize();
        break;
      }
    }
    setText(text);

    Paint paint = Paint.valueOf(template.getFontColor());
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
    super.relocate(x, y - getHeight() * 0.10);
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

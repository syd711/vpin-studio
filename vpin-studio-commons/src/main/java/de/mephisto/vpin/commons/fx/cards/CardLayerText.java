package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class CardLayerText extends CardLayer {

  public static enum CardLayerTextType {
    Title, TableName
  }
  
  private CardLayerTextType type;

  public CardLayerText(CardLayerTextType type) {
    super();
    this.type = type;
  }

  @Override
  protected void draw(GraphicsContext g, @Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception {

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

    Paint paint = Paint.valueOf(template.getFontColor());
    g.setFill(paint);

    Font font = createFont(fontName, fontStyle, fontSIZE * zoomY);
    g.setFont(font);

    g.setTextAlign(TextAlignment.CENTER);
    g.setTextBaseline(VPos.CENTER);
    g.fillText(text, getWidth() * 0.5, getHeight() * 0.45);
  }

}

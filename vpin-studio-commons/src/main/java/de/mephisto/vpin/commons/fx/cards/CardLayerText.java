package de.mephisto.vpin.commons.fx.cards;

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
  protected void draw(GraphicsContext g, CardTemplate template, CardData data) throws Exception {

    String text = null;
    String fontName = null, fontStyle = null;
    int fontSize = 12;
    switch (type) {
      case Title: {
        text = template.getTitle();
        fontName = template.getTitleFontName();
        fontStyle = template.getTitleFontStyle();
        fontSize = template.getTitleFontSize();
        break;
      }
      case TableName: {
        text = data.getGameDisplayName();
        fontName = template.getTableFontName();
        fontStyle = template.getTableFontStyle();
        fontSize = template.getTableFontSize();
        break;
      }
    }

    Paint paint = Paint.valueOf(template.getFontColor());
    g.setFill(paint);

    Font font = createFont(fontName, fontStyle, fontSize);
    g.setFont(font);

    g.setTextAlign(TextAlignment.CENTER);
    g.setTextBaseline(VPos.CENTER);
    g.fillText(text, getWidth() * 0.5, getHeight() * 0.45);
  }

}

package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public abstract class CardLayerText extends Canvas implements CardLayer {

  protected abstract CardTextData getTextData(@Nonnull CardTemplate template, @Nullable CardData data);

  @Override
  public void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) {
    CardTextData textData = getTextData(template, data);

    double width = getWidth();
    double height = getHeight();
    GraphicsContext g = getGraphicsContext2D();
    g.clearRect(0, 0, width, height);

    Paint paint = Paint.valueOf(textData.useDefaultColor || StringUtils.isEmpty(textData.fontColor) ? template.getFontColor() : textData.fontColor);
    g.setFill(paint);


    Font font = createFont(textData.fontName, textData.fontStyle, textData.fontSIZE * zoomY);
    g.setFont(font);

    g.setTextAlign(TextAlignment.CENTER);
    g.setTextBaseline(VPos.CENTER);

    g.fillText(textData.text, width / 2, height / 2, width);
  }

  protected class CardTextData {
    String text = null;
    boolean useDefaultColor = true;
    String fontName = null;
    String fontStyle = null;
    String fontColor = null;
    int fontSIZE = -1;
  }

}

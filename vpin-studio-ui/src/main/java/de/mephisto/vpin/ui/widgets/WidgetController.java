package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.ui.Studio;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

public class WidgetController {

  private static Font scoreFont;
  private static Font scoreFontBold;
  private static Font scoreFontSmall;

  static {
    Font.loadFont(Studio.class.getResourceAsStream("digital-7.ttf"), 22);
    Font.loadFont(Studio.class.getResourceAsStream("impact.ttf"), 22);
    String SCORE_FONT_NAME = "Digital-7"; //widgetProperties.getProperty("overlay.score.font.name", "Arial");
    scoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 34);
    scoreFontBold = Font.font(SCORE_FONT_NAME, FontPosture.findByName("bold"), 34);
    scoreFontSmall = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 28);
  }

  public WidgetController() {
  }

  public static Font getScoreFontSmall() {
    return scoreFont;
  }

  public static Font getScoreFontBold() {
    return scoreFontBold;
  }

  protected Font getScoreFont() {
    return scoreFont;
  }
}

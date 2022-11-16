package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.ui.Studio;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

public class WidgetController {

  private static Font scoreFont;

  static {
    Font.loadFont(Studio.class.getResourceAsStream("digital-7.ttf"), 22);
    String SCORE_FONT_NAME = "Digital-7"; //widgetProperties.getProperty("overlay.score.font.name", "Arial");
    scoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 34);
  }

  public WidgetController() {
  }

  protected Font getScoreFont() {
    return scoreFont;
  }
}

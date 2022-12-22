package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

public class WidgetController {

  private static Font scoreFont;
  private static Font competitionScoreFont;
  private static Font scoreFontText;

  static {
    Font.loadFont(OverlayWindowFX.class.getResourceAsStream("MonospaceBold.ttf"), 22);
    Font.loadFont(OverlayWindowFX.class.getResourceAsStream("digital_counter_7.ttf"), 22);
    Font.loadFont(OverlayWindowFX.class.getResourceAsStream("impact.ttf"), 22);

    String SCORE_FONT_NAME = "Digital Counter 7";
    String SCORE_TEXT_FONT_NAME = "Monospace";

    scoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 36);
    competitionScoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 28);

    scoreFontText = Font.font(SCORE_TEXT_FONT_NAME, FontPosture.findByName("regular"), 16);
  }

  public WidgetController() {
  }

  public static Font getCompetitionScoreFont() {
    return competitionScoreFont;
  }

  public Font getScoreFont() {
    return scoreFont;
  }


  public static Font getScoreFontText() {
    return scoreFontText;
  }
}

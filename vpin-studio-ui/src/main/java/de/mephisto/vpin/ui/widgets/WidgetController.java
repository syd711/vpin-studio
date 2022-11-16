package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.ui.Studio;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

public class WidgetController {

  private static ObservedProperties widgetProperties;

  private static Font tableFont;
  private static Font titleFont;
  private static Font scoreFont;

  static {
    widgetProperties = Studio.client.getProperties("overlay-generator");

    String TABLE_FONT_NAME = widgetProperties.getProperty("overlay.table.font.name", "Arial");
    String TABLE_FONT_STYLE = widgetProperties.getProperty("overlay.table.font.style", "regular");
    int TABLE_FONT_SIZE = widgetProperties.getProperty("overlay.table.font.size", 60);

    String SCORE_FONT_NAME = widgetProperties.getProperty("overlay.score.font.name", "Arial");
    String SCORE_FONT_STYLE = widgetProperties.getProperty("overlay.score.font.style", "regular");
    int SCORE_FONT_SIZE = widgetProperties.getProperty("overlay.score.font.size", 60);

    String TITLE_FONT_NAME = widgetProperties.getProperty("overlay.title.font.name", "Arial");
    String TITLE_FONT_STYLE = widgetProperties.getProperty("overlay.title.font.style", "regular");
    int TITLE_FONT_SIZE = widgetProperties.getProperty("overlay.title.font.size", 60);


    tableFont = Font.font(TABLE_FONT_NAME, FontPosture.findByName(TABLE_FONT_STYLE), TABLE_FONT_SIZE);
    titleFont = Font.font(TITLE_FONT_NAME, FontPosture.findByName(TITLE_FONT_STYLE), TITLE_FONT_SIZE);
    scoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName(SCORE_FONT_STYLE), SCORE_FONT_SIZE);
  }


  public WidgetController() {
  }

  protected Font getTitleFont() {
    return tableFont;
  }

  protected Font getTableFont() {
    return tableFont;
  }

  protected Font getScoreFont() {
    return scoreFont;
  }
}

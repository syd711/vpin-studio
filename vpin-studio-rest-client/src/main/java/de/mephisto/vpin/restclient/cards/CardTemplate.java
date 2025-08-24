package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.JsonSettings;

import java.util.Objects;

public class CardTemplate extends JsonSettings {
  public final static String DEFAULT = "Default";

  private Long id;
  private String name = DEFAULT;

  private Integer version = null;

  private boolean renderBackground = true;
  private boolean renderFrame = true;
  private boolean renderTableName = true;
  private boolean renderTitle = true;
  private boolean renderWheelIcon = true;
  private boolean renderCanvas = false;
  private boolean renderScores = true;

  // Background images
  private boolean useDefaultBackground = true;
  private String background = "Old Bumbers";
  private boolean useColoredBackground = true;
  private String backgroundColor = "#000000";

  // BACKGROUND SETTINGS
  private double backgroundX = 0;
  private double backgroundY = 0;
  private double zoom = 1.0;
  private boolean useDmdPositions = false;
  private boolean fullScreen = true;

  private int transparentPercentage = 0;
  private int alphaBlack = 33;
  private int alphaWhite = 1;
  private int blur = 6;
  private boolean grayScale = false;

  // FRAME SETTINGS
  private int borderWidth = 1;
  public int borderRadius = 0;
  private String borderColor = "#FFFFFF";

  private int marginTop = 10;
  private int marginRight = 10;
  private int marginBottom = 10;
  private int marginLeft = 10;

  /**@deprecated no more used */
  private int padding = 10;

  // WHEEL SETTINGS
  private double wheelX = 0.0;
  private double wheelY = 0.5;
  private double wheelSize = 0.3;
  /**@deprecated no more used */
  private int wheelPadding = 32;

  // SCORES SETTINGS
  private double scoresX = 0.3;
  private double scoresY = 0.4;
  private double scoresWidth = 0.7;
  private double scoresHeight = 0.6;

  private int rowMargin = 5;
  private boolean rawScore = true;
  private int maxScores = 0;

  private String fontColor = "#FFFFFF";
  private String friendsFontColor = "#CCCCCC";

  private String scoreFontName = "Monospaced";
  private int scoreFontSize = 90;
  private String scoreFontStyle = "Regular";

  private boolean renderFriends = true;
  private boolean renderPositions = true;
  private boolean renderScoreDates = true;

  // TABLENAME SETTINGS
  private boolean tableUseVpsName = false;
  private boolean tableRenderManufacturer = true;
  private boolean tableRenderYear = true;
  private String tableFontName = "Impact";
  private int tableFontSize = 72;
  private String tableFontStyle = "Regular";
  private boolean tableUseDefaultColor = true;
  private String tableColor = "#FFFFFF";
  private double tableX = 0.0;
  private double tableY = 0.2;
  private double tableWidth = 1.0;
  private double tableHeight = 0.2;

  // TITLE SETTINGS
  private String title = "Highscores";
  private String titleFontName = "Cambria";
  private int titleFontSize = 120;
  private String titleFontStyle = "Regular";
  private boolean titleUseDefaultColor = true;
  private String titleColor = "#FFFFFF";
  private double titleX = 0;
  private double titleY = 0;
  private double titleWidth = 1.0;
  private double titleHeight = 0.2;

  // CANVAS SETTINGS
  private double canvasX = 0.1;
  private double canvasY = 0.1;
  private double canvasWidth = 0.8;
  private double canvasHeight = 0.8;
  private String canvasBackground;
  private int canvasAlphaPercentage = 0;
  private int canvasBorderRadius = 0;

  // OVERLAY SETTINGS
  private boolean overlayMode = false;
  private String overlayScreen = null;


  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  //----------------------------------------

  public double getBackgroundX() {
    return backgroundX;
  }

  public void setBackgroundX(double backgroundX) {
    this.backgroundX = backgroundX;
  }

  public double getBackgroundY() {
    return backgroundY;
  }

  public void setBackgroundY(double backgroundY) {
    this.backgroundY = backgroundY;
  }

  public double getZoom() {
    return zoom;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public boolean isUseDmdPositions() {
    return useDmdPositions;
  }

  public void setUseDmdPositions(boolean useDmdPositions) {
    this.useDmdPositions = useDmdPositions;
  }

  public boolean isFullScreen() {
    return fullScreen;
  }

  public void setFullScreen(boolean fullScreen) {
    this.fullScreen = fullScreen;
  }

  public String getFriendsFontColor() {
    return friendsFontColor;
  }

  public void setFriendsFontColor(String friendsFontColor) {
    this.friendsFontColor = friendsFontColor;
  }

  public boolean isRenderFriends() {
    return renderFriends;
  }

  public void setRenderFriends(boolean renderFriends) {
    this.renderFriends = renderFriends;
  }

  public boolean isOverlayMode() {
    return overlayMode;
  }

  public void setOverlayMode(boolean overlayMode) {
    this.overlayMode = overlayMode;
  }

  public String getOverlayScreen() {
    return overlayScreen;
  }

  public void setOverlayScreen(String overlayScreen) {
    this.overlayScreen = overlayScreen;
  }

  public int getMaxScores() {
    return maxScores;
  }

  public void setMaxScores(int maxScores) {
    this.maxScores = maxScores;
  }

  public int getCanvasBorderRadius() {
    return canvasBorderRadius;
  }

  public void setCanvasBorderRadius(int canvasBorderRadius) {
    this.canvasBorderRadius = canvasBorderRadius;
  }

  public boolean isRenderCanvas() {
    return renderCanvas;
  }

  public void setRenderCanvas(boolean renderCanvas) {
    this.renderCanvas = renderCanvas;
  }

  public double getCanvasX() {
    return canvasX;
  }

  public void setCanvasX(double canvasX) {
    this.canvasX = canvasX;
  }

  public double getCanvasY() {
    return canvasY;
  }

  public void setCanvasY(double canvasY) {
    this.canvasY = canvasY;
  }

  public double getCanvasWidth() {
    return canvasWidth;
  }

  public void setCanvasWidth(double canvasWidth) {
    this.canvasWidth = canvasWidth;
  }

  public double getCanvasHeight() {
    return canvasHeight;
  }

  public void setCanvasHeight(double canvasHeight) {
    this.canvasHeight = canvasHeight;
  }

  public String getCanvasBackground() {
    return canvasBackground;
  }

  public void setCanvasBackground(String canvasBackground) {
    this.canvasBackground = canvasBackground;
  }

  public int getCanvasAlphaPercentage() {
    return canvasAlphaPercentage;
  }

  public void setCanvasAlphaPercentage(int canvasAlphaPercentage) {
    this.canvasAlphaPercentage = canvasAlphaPercentage;
  }

  public boolean isRenderPositions() {
    return renderPositions;
  }

  public void setRenderPositions(boolean renderPositions) {
    this.renderPositions = renderPositions;
  }

  public boolean isRenderScoreDates() {
    return renderScoreDates;
  }

  public void setRenderScoreDates(boolean renderscoreDate) {
    this.renderScoreDates = renderscoreDate;
  }

  public double getWheelX() {
    return wheelX;
  }

  public void setWheelX(double wheelX) {
    this.wheelX = wheelX;
  }

  public double getWheelY() {
    return wheelY;
  }

  public void setWheelY(double wheelY) {
    this.wheelY = wheelY;
  }

  public double getWheelSize() {
    return wheelSize;
  }

  public void setWheelSize(double wheelSize) {
    this.wheelSize = wheelSize;
  }
  
  public double getScoresX() {
    return scoresX;
  }

  public void setScoresX(double scoreX) {
    this.scoresX = scoreX;
  }

  public double getScoresY() {
    return scoresY;
  }

  public void setScoresY(double scoreY) {
    this.scoresY = scoreY;
  }

  public double getScoresWidth() {
    return scoresWidth;
  }

  public void setScoresWidth(double scoreWidth) {
    this.scoresWidth = scoreWidth;
  }

  public double getScoresHeight() {
    return scoresHeight;
  }

  public void setScoresHeight(double scoreHeight) {
    this.scoresHeight = scoreHeight;
  }

  public int getMarginTop() {
    return marginTop;
  }

  public void setMarginTop(int marginTop) {
    this.marginTop = marginTop;
  }

  public int getMarginRight() {
    return marginRight;
  }

  public void setMarginRight(int marginRight) {
    this.marginRight = marginRight;
  }

  public int getMarginBottom() {
    return marginBottom;
  }

  public void setMarginBottom(int marginBottom) {
    this.marginBottom = marginBottom;
  }

  public int getMarginLeft() {
    return marginLeft;
  }

  public void setMarginLeft(int marginLeft) {
    this.marginLeft = marginLeft;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isRenderBackground() {
    return renderBackground;
  }

  public void setRenderBackground(boolean renderBackground) {
    this.renderBackground = renderBackground;
  }
  
  public boolean isRenderFrame() {
    return renderFrame;
  }

  public void setRenderFrame(boolean renderFrame) {
    this.renderFrame = renderFrame;
  }

  public boolean isRenderWheelIcon() {
    return renderWheelIcon;
  }

  public void setRenderWheelIcon(boolean renderWheelIcon) {
    this.renderWheelIcon = renderWheelIcon;
  }

  public boolean isRenderTitle() {
    return renderTitle;
  }

  public void setRenderTitle(boolean renderTitle) {
    this.renderTitle = renderTitle;
  }

  public boolean isRenderTableName() {
    return renderTableName;
  }

  public void setRenderTableName(boolean renderTableName) {
    this.renderTableName = renderTableName;
  }

  public boolean isRenderScores() {
    return renderScores;
  }

  public void setRenderScores(boolean renderScore) {
    this.renderScores = renderScore;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTransparentPercentage() {
    return transparentPercentage;
  }

  public void setTransparentPercentage(int transparentPercentage) {
    this.transparentPercentage = transparentPercentage;
  }

  public int getAlphaBlack() {
    return alphaBlack;
  }

  public void setAlphaBlack(int alphaBlack) {
    this.alphaBlack = alphaBlack;
  }

  public int getAlphaWhite() {
    return alphaWhite;
  }

  public void setAlphaWhite(int alphaWhite) {
    this.alphaWhite = alphaWhite;
  }

  public String getBackground() {
    return background;
  }

  public void setBackground(String background) {
    this.background = background;
  }
 
  public String getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public int getBorderWidth() {
    return borderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public int getBorderRadius() {
    return borderRadius;
  }

  public void setBorderRadius(int borderRadius) {
    this.borderRadius = borderRadius;
  }

  public int getRowMargin() {
    return rowMargin;
  }

  public void setRowMargin(int rowMargin) {
    this.rowMargin = rowMargin;
  }

  public int getBlur() {
    return blur;
  }

  public void setBlur(int blur) {
    this.blur = blur;
  }

  public String getFontColor() {
    return fontColor;
  }

  public void setFontColor(String fontColor) {
    this.fontColor = fontColor;
  }

  public boolean isGrayScale() {
    return grayScale;
  }

  public void setGrayScale(boolean grayScale) {
    this.grayScale = grayScale;
  }

  public boolean isRawScore() {
    return rawScore;
  }

  public void setRawScore(boolean rawScore) {
    this.rawScore = rawScore;
  }

  public String getScoreFontName() {
    return scoreFontName;
  }

  public void setScoreFontName(String scoreFontName) {
    this.scoreFontName = scoreFontName;
  }

  public int getScoreFontSize() {
    return scoreFontSize;
  }

  public void setScoreFontSize(int scoreFontSize) {
    this.scoreFontSize = scoreFontSize;
  }

  public String getScoreFontStyle() {
    return scoreFontStyle;
  }

  public void setScoreFontStyle(String scoreFontStyle) {
    this.scoreFontStyle = scoreFontStyle;
  }

  public String getTableFontName() {
    return tableFontName;
  }

  public void setTableFontName(String tableFontName) {
    this.tableFontName = tableFontName;
  }

  public int getTableFontSize() {
    return tableFontSize;
  }

  public void setTableFontSize(int tableFontSize) {
    this.tableFontSize = tableFontSize;
  }

  public String getTableFontStyle() {
    return tableFontStyle;
  }

  public void setTableFontStyle(String tableFontStyle) {
    this.tableFontStyle = tableFontStyle;
  }
  
  public boolean isTableUseVpsName() {
    return tableUseVpsName;
  }

  public void setTableUseVpsName(boolean tableUseVpsName) {
    this.tableUseVpsName = tableUseVpsName;
  }

  public boolean isTableRenderYear() {
    return tableRenderYear;
  }

  public void setTableRenderYear(boolean tableRenderYear) {
    this.tableRenderYear = tableRenderYear;
  }

  public boolean isTableRenderManufacturer() {
    return tableRenderManufacturer;
  }

  public void setTableRenderManufacturer(boolean tableRenderManufacturer) {
    this.tableRenderManufacturer = tableRenderManufacturer;
  }

  public boolean isTableUseDefaultColor() {
    return tableUseDefaultColor;
  }

  public void setTableUseDefaultColor(boolean tableUseDefaultColor) {
    this.tableUseDefaultColor = tableUseDefaultColor;
  }

  public String getTableColor() {
    return tableColor;
  }

  public void setTableColor(String tableColor) {
    this.tableColor = tableColor;
  }

  public double getTableX() {
    return tableX;
  }

  public void setTableX(double tableX) {
    this.tableX = tableX;
  }

  public double getTableY() {
    return tableY;
  }

  public void setTableY(double tableY) {
    this.tableY = tableY;
  }

  public double getTableWidth() {
    return tableWidth;
  }

  public void setTableWidth(double tableWidth) {
    this.tableWidth = tableWidth;
  }

  public double getTableHeight() {
    return tableHeight;
  }

  public void setTableHeight(double tableHeight) {
    this.tableHeight = tableHeight;
  }

  public String getTitleFontName() {
    return titleFontName;
  }

  public void setTitleFontName(String titleFontName) {
    this.titleFontName = titleFontName;
  }

  public int getTitleFontSize() {
    return titleFontSize;
  }

  public void setTitleFontSize(int titleFontSize) {
    this.titleFontSize = titleFontSize;
  }

  public String getTitleFontStyle() {
    return titleFontStyle;
  }

  public void setTitleFontStyle(String titleFontStyle) {
    this.titleFontStyle = titleFontStyle;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isTitleUseDefaultColor() {
    return titleUseDefaultColor;
  }

  public void setTitleUseDefaultColor(boolean titleUseDefaultColor) {
    this.titleUseDefaultColor = titleUseDefaultColor;
  }

  public String getTitleColor() {
    return titleColor;
  }

  public void setTitleColor(String titleColor) {
    this.titleColor = titleColor;
  }

  public double getTitleX() {
    return titleX;
  }

  public void setTitleX(double titleX) {
    this.titleX = titleX;
  }

  public double getTitleY() {
    return titleY;
  }

  public void setTitleY(double titleY) {
    this.titleY = titleY;
  }

  public double getTitleWidth() {
    return titleWidth;
  }

  public void setTitleWidth(double titleWidth) {
    this.titleWidth = titleWidth;
  }

  public double getTitleHeight() {
    return titleHeight;
  }

  public void setTitleHeight(double titleHeight) {
    this.titleHeight = titleHeight;
  }

  public String getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(String borderColor) {
    this.borderColor = borderColor;
  }

  public boolean isUseDefaultBackground() {
    return useDefaultBackground;
  }

  public void setUseDefaultBackground(boolean useDefaultBackground) {
    this.useDefaultBackground = useDefaultBackground;
  }

  public boolean isUseColoredBackground() {
    return useColoredBackground;
  }

  public void setUseColoredBackground(boolean useColorBackground) {
    this.useColoredBackground = useColorBackground;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardTemplate that = (CardTemplate) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public String getSettingsName() {
    return null;
  }

  //-------------------------------------- deprecated but kept to parse old template ---

  /**
   * @deprecated not used anymore
   */
  public int getPadding() {
    return padding;
  }
  /**
   * @deprecated not used anymore
   */
  public void setPadding(int padding) {
    this.padding = padding;
  }

  /**
   * @deprecated use isUseColoredBackground()
   */
  public boolean isTransparentBackground() {
    return isUseColoredBackground();
  }

  /**
   * @deprecated use setUseColoredBackground()
   */
  public void setTransparentBackground(boolean transparentBackground) {
    setUseColoredBackground(transparentBackground);
  }

  /**
   * @deprecated use getWheelRightPadding())
   */
  public int getWheelPadding() {
    return wheelPadding;
  }
  /**
   * @deprecated use setWheelRightPadding())
   */
  public void setWheelPadding(int wheelPadding) {
    this.wheelPadding = wheelPadding;
  }

  /**
   * @deprecated use isUseDefault())
   */
  public boolean isUseDirectB2S() {
    return isUseDefaultBackground();
  }
  /**
   * @deprecated use setUseDefault()
   */
  public void setUseDirectB2S(boolean useDirectB2S) {
    setUseDefaultBackground(useDirectB2S);
  }

}

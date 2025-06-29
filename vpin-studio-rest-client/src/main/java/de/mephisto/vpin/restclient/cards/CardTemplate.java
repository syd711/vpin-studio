package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.JsonSettings;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CardTemplate extends JsonSettings {
  public final static String DEFAULT = "Default";

  private Long id;
  private String name = DEFAULT;

  /** This dimensions are used as a reference to calculate the ratio between the template dimensions and the displayed dimensions 
   * Used to calculate ratioX = displayWidth / referenceWidth or 1 if referenceWidth < 0
  */
  private int referenceWidth = -1;
  private int referenceHeight = -1;


  private int alphaBlack = 33;
  private int alphaWhite = 1;
  private String background = "Old Bumbers";
  private int borderWidth = 1;
  private int padding = 10;
  private int marginTop = 10;
  private int marginRight = 10;
  private int marginBottom = 10;
  private int marginLeft = 10;
  private int wheelPadding = 32;
  private int rowMargin = 5;
  private int wheelSize = 200;
  private int blur = 6;
  private String fontColor = "#FFFFFF";
  private String friendsFontColor = "#CCCCCC";
  private boolean grayScale = false;
  private boolean rawScore = true;
  private int maxScores = 0;

  private String scoreFontName = "Monospaced";
  private int scoreFontSize = 90;
  private String scoreFontStyle = "Regular";
  private String tableFontName = "Impact";
  private int tableFontSize = 72;
  private String tableFontStyle = "Regular";
  private String titleFontName = "Cambria";
  private int titleFontSize = 120;
  private String titleFontStyle = "Regular";
  private String title = "Highscores";

  private boolean useDirectB2S = true;

  private boolean transparentBackground = false;
  private int transparentPercentage = 0;

  private boolean renderTableName = true;
  private boolean renderTitle = true;
  private boolean renderWheelIcon = true;
  private boolean renderCanvas = false;

  private int canvasX = 100;
  private int canvasY = 100;
  private int canvasWidth = 100;
  private int canvasHeight = 100;
  private String canvasBackground;
  private int canvasAlphaPercentage = 0;
  private int canvasBorderRadius = 0;

  private boolean renderFriends = true;
  private boolean renderPositions = true;
  private boolean renderScoreDates = true;

  private boolean overlayMode = false;
  private String overlayScreen = null;

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

  public int getCanvasX() {
    return canvasX;
  }

  public void setCanvasX(int canvasX) {
    this.canvasX = canvasX;
  }

  public int getCanvasY() {
    return canvasY;
  }

  public void setCanvasY(int canvasY) {
    this.canvasY = canvasY;
  }

  public int getCanvasWidth() {
    return canvasWidth;
  }

  public void setCanvasWidth(int canvasWidth) {
    this.canvasWidth = canvasWidth;
  }

  public int getCanvasHeight() {
    return canvasHeight;
  }

  public void setCanvasHeight(int canvasHeight) {
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

  public int getWheelSize() {
    return wheelSize;
  }

  public void setWheelSize(int wheelSize) {
    this.wheelSize = wheelSize;
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

  public boolean isTransparentBackground() {
    return transparentBackground;
  }

  public void setTransparentBackground(boolean transparentBackground) {
    this.transparentBackground = transparentBackground;
  }

  public int getTransparentPercentage() {
    return transparentPercentage;
  }

  public void setTransparentPercentage(int transparentPercentage) {
    this.transparentPercentage = transparentPercentage;
  }

  public boolean isRenderTableName() {
    return renderTableName;
  }

  public void setRenderTableName(boolean renderTableName) {
    this.renderTableName = renderTableName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public int getBorderWidth() {
    return borderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public int getPadding() {
    return padding;
  }

  public void setPadding(int padding) {
    this.padding = padding;
  }

  public int getWheelPadding() {
    return wheelPadding;
  }

  public void setWheelPadding(int wheelPadding) {
    this.wheelPadding = wheelPadding;
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

  public boolean isUseDirectB2S() {
    return useDirectB2S;
  }

  public void setUseDirectB2S(boolean useDirectB2S) {
    this.useDirectB2S = useDirectB2S;
  }

  public int getReferenceWidth() {
    return referenceWidth;
  }

  public void setReferenceWidth(int referenceWidth) {
    this.referenceWidth = referenceWidth;
  }

  public int getReferenceHeight() {
    return referenceHeight;
  }

  public void setReferenceHeight(int referenceHeight) {
    this.referenceHeight = referenceHeight;
  }

  @JsonIgnore
  public double getRatioXFor(double width) {
    return referenceWidth < 0 ? 1 : width / referenceWidth;
  }

  @JsonIgnore
  public double getRatioYFor(double height) {
    return referenceHeight < 0 ? 1 : height / referenceHeight;
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

}

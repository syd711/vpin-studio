package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.JsonSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardTemplate extends JsonSettings {
  public final static String DEFAULT = "Default";

  private long id;

  private String name = DEFAULT;
  private int alphaBlack = 33;
  private int alphaWhite = 1;
  private String background = "Old Bumbers";
  private int borderWidth = 1;
  private int padding = 10;
  private int wheelPadding = 32;
  private int rowMargin = 5;
  private int blur = 6;
  private String fontColor = "#FFFFFF";
  private boolean grayScale = false;
  private boolean rawScore = true;
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

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  private List<Integer> gameIds = new ArrayList<>();

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
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
}

package de.mephisto.vpin.restclient.cards;

import java.util.Objects;

public class CardTemplate {
  public final static String DEFAULT = "default";

  private String name = DEFAULT;
  private int alphaBlack = 33;
  private int alphaWhite = 1;
  private String background = "Old Bumbers";
  private int borderWidth = 1;
  private int padding = 10;
  private int wheelPadding = 32;
  private int rowMargin = 5;
  private int blur = 6;
  private String cardFontColor = "#FFFFFF";
  private boolean grayScale = false;
  private boolean rawScore = true;
  private int cardSampleTable = 1;
  private String cardScoreFontName = "Monospaced";
  private int cardScoreFontSize = 90;
  private String cardScoreFontStyle = "Regular";
  private String cardTableFontName = "Impact";
  private int cardTableFontSize = 72;
  private String cardTableFontStyle = "Regular";
  private String cardTitleFontName = "Cambria";
  private int cardTitleFontSize = 120;
  private String cardTitleFontStyle = "Regular";
  private String title = "Highscores";
  private boolean useDirectB2S = true;
  private boolean transparentBackground = false;
  private int transparentPercentage = 0;
  private boolean renderTableName = true;

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

  public String getCardFontColor() {
    return cardFontColor;
  }

  public void setCardFontColor(String cardFontColor) {
    this.cardFontColor = cardFontColor;
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

  public int getCardSampleTable() {
    return cardSampleTable;
  }

  public void setCardSampleTable(int cardSampleTable) {
    this.cardSampleTable = cardSampleTable;
  }

  public String getCardScoreFontName() {
    return cardScoreFontName;
  }

  public void setCardScoreFontName(String cardScoreFontName) {
    this.cardScoreFontName = cardScoreFontName;
  }

  public int getCardScoreFontSize() {
    return cardScoreFontSize;
  }

  public void setCardScoreFontSize(int cardScoreFontSize) {
    this.cardScoreFontSize = cardScoreFontSize;
  }

  public String getCardScoreFontStyle() {
    return cardScoreFontStyle;
  }

  public void setCardScoreFontStyle(String cardScoreFontStyle) {
    this.cardScoreFontStyle = cardScoreFontStyle;
  }

  public String getCardTableFontName() {
    return cardTableFontName;
  }

  public void setCardTableFontName(String cardTableFontName) {
    this.cardTableFontName = cardTableFontName;
  }

  public int getCardTableFontSize() {
    return cardTableFontSize;
  }

  public void setCardTableFontSize(int cardTableFontSize) {
    this.cardTableFontSize = cardTableFontSize;
  }

  public String getCardTableFontStyle() {
    return cardTableFontStyle;
  }

  public void setCardTableFontStyle(String cardTableFontStyle) {
    this.cardTableFontStyle = cardTableFontStyle;
  }

  public String getCardTitleFontName() {
    return cardTitleFontName;
  }

  public void setCardTitleFontName(String cardTitleFontName) {
    this.cardTitleFontName = cardTitleFontName;
  }

  public int getCardTitleFontSize() {
    return cardTitleFontSize;
  }

  public void setCardTitleFontSize(int cardTitleFontSize) {
    this.cardTitleFontSize = cardTitleFontSize;
  }

  public String getCardTitleFontStyle() {
    return cardTitleFontStyle;
  }

  public void setCardTitleFontStyle(String cardTitleFontStyle) {
    this.cardTitleFontStyle = cardTitleFontStyle;
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
}

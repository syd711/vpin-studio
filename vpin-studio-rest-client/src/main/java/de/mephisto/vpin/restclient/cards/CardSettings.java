package de.mephisto.vpin.restclient.cards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * #Sun Dec 10 08:30:57 CET 2023
 * card.alphacomposite.black=5
 * card.alphacomposite.white=10
 * card.background=Chalk Board
 * card.blur=6
 * card.font.color=\#FFFFFF
 * card.grayScale=true
 * card.highscores.row.padding.left=32
 * card.highscores.row.separator=5
 * card.padding=10
 * card.ratio=RATIO_16x9
 * card.rawHighscore=true
 * card.sampleTable=491
 * card.score.font.name=Monospaced
 * card.score.font.size=90
 * card.score.font.style=Regular
 * card.table.font.name=Impact
 * card.table.font.size=72
 * card.table.font.style=Regular
 * card.title.font.name=Cambria
 * card.title.font.size=120
 * card.title.font.style=Regular
 * card.title.text=Highscores
 * card.useDirectB2S=true
 * popper.screen=Other2
 */
public class CardSettings {
  private int cardAlphacompositeBlack = 33;
  private int cardAlphacompositeWhite = 1;
  private String cardBackground = "Old Bumbers";
  private int cardBorderWidth = 1;
  private String cardRatio = "RATIO_16x9";
  private int cardPadding = 10;
  private int cardHighscoresRowPaddingLeft = 32;
  private int cardHighscoresRowSeparator = 5;
  private int cardScaling = 1280;
  private int cardBlur = 6;
  private String cardFontColor = "#FFFFFF";
  private boolean cardGrayScale = true;
  private boolean cardRawHighscore = true;
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
  private String cardTitleText = "Highscores";
  private boolean cardUseDirectB2S = true;
  private String popperScreen = "Other2";
  private int notificationTime = 0;

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static CardSettings fromJson(String json) throws JsonProcessingException {
    return objectMapper.readValue(json, CardSettings.class);
  }

  public int getNotificationTime() {
    return notificationTime;
  }

  public void setNotificationTime(int notificationTime) {
    this.notificationTime = notificationTime;
  }

  public int getCardBorderWidth() {
    return cardBorderWidth;
  }

  public void setCardBorderWidth(int cardBorderWidth) {
    this.cardBorderWidth = cardBorderWidth;
  }

  public String getCardRatio() {
    return cardRatio;
  }

  public void setCardRatio(String cardRatio) {
    this.cardRatio = cardRatio;
  }

  public int getCardPadding() {
    return cardPadding;
  }

  public void setCardPadding(int cardPadding) {
    this.cardPadding = cardPadding;
  }

  public int getCardHighscoresRowPaddingLeft() {
    return cardHighscoresRowPaddingLeft;
  }

  public void setCardHighscoresRowPaddingLeft(int cardHighscoresRowPaddingLeft) {
    this.cardHighscoresRowPaddingLeft = cardHighscoresRowPaddingLeft;
  }

  public int getCardHighscoresRowSeparator() {
    return cardHighscoresRowSeparator;
  }

  public void setCardHighscoresRowSeparator(int cardHighscoresRowSeparator) {
    this.cardHighscoresRowSeparator = cardHighscoresRowSeparator;
  }

  public int getCardScaling() {
    return cardScaling;
  }

  public void setCardScaling(int cardScaling) {
    this.cardScaling = cardScaling;
  }

  public int getCardAlphacompositeBlack() {
    return cardAlphacompositeBlack;
  }

  public void setCardAlphacompositeBlack(int cardAlphacompositeBlack) {
    this.cardAlphacompositeBlack = cardAlphacompositeBlack;
  }

  public int getCardAlphacompositeWhite() {
    return cardAlphacompositeWhite;
  }

  public void setCardAlphacompositeWhite(int cardAlphacompositeWhite) {
    this.cardAlphacompositeWhite = cardAlphacompositeWhite;
  }

  public String getCardBackground() {
    return cardBackground;
  }

  public void setCardBackground(String cardBackground) {
    this.cardBackground = cardBackground;
  }

  public int getCardBlur() {
    return cardBlur;
  }

  public void setCardBlur(int cardBlur) {
    this.cardBlur = cardBlur;
  }

  public String getCardFontColor() {
    return cardFontColor;
  }

  public void setCardFontColor(String cardFontColor) {
    this.cardFontColor = cardFontColor;
  }

  public boolean isCardGrayScale() {
    return cardGrayScale;
  }

  public void setCardGrayScale(boolean cardGrayScale) {
    this.cardGrayScale = cardGrayScale;
  }

  public boolean isCardRawHighscore() {
    return cardRawHighscore;
  }

  public void setCardRawHighscore(boolean cardRawHighscore) {
    this.cardRawHighscore = cardRawHighscore;
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

  public String getCardTitleText() {
    return cardTitleText;
  }

  public void setCardTitleText(String cardTitleText) {
    this.cardTitleText = cardTitleText;
  }

  public boolean isCardUseDirectB2S() {
    return cardUseDirectB2S;
  }

  public void setCardUseDirectB2S(boolean cardUseDirectB2S) {
    this.cardUseDirectB2S = cardUseDirectB2S;
  }

  public String getPopperScreen() {
    return popperScreen;
  }

  public void setPopperScreen(String popperScreen) {
    this.popperScreen = popperScreen;
  }

  public String toJson() throws JsonProcessingException {
    return objectMapper.writeValueAsString(this);
  }
}

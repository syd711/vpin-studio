package de.mephisto.vpin.restclient.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.Objects;

public class CardTemplate extends JsonSettings {
  public final static String CARD_TEMPLATE_PREFIX = "__card_template_";

  public final static String DEFAULT = "Default";

  private Long id;
  private Long parentId;

  private String name = DEFAULT;

  private CardTemplateType templateType = CardTemplateType.HIGSCORE_CARD;

  private Integer version = null;

  private boolean renderBackground = false;
  private boolean lockBackground = false;
  private boolean renderFrame = false;
  private boolean lockFrame = false;
  private boolean renderTableName = false;
  private boolean lockTableName = false;
  private boolean renderTitle = false;
  private boolean lockTitle = false;
  private boolean renderWheelIcon = false;
  private boolean lockWheelIcon = false;
  private boolean renderManufacturerLogo = false;
  private boolean lockManufacturerLogo = false;
  private boolean renderOtherMedia = false;
  private boolean lockOtherMedia = false;
  private boolean renderCanvas = false;
  private boolean lockCanvas = false;
  private boolean renderScores = false;
  private boolean lockScores = false;

  private boolean lockOverlay = false;

  // Background images
  private boolean useDefaultBackground = true;
  private String background = "Old Bumbers";
  private boolean useColoredBackground = false;
  private String backgroundColor = "#000000";

  // BACKGROUND SETTINGS
  private double backgroundX = 0;
  private double backgroundY = 0;
  private double zoom = 100;
  private boolean useDmdPositions = false;
  private boolean fullScreen = true;

  private int transparentPercentage = 0;
  private int alphaBlack = 0;
  private int alphaWhite = 0;
  private int blur = 0;
  private boolean grayScale = false;

  // FRAME SETTINGS
  private int borderWidth = 0;
  public int borderRadius = 0;
  private String borderColor = "#FFFFFF";

  private int marginTop = 0;
  private int marginRight = 0;
  private int marginBottom = 0;
  private int marginLeft = 0;

  private String frame;

  // MANUFACTURER LOGO SETTINGS
  private boolean manufacturerLogoKeepAspectRatio = true;
  private boolean manufacturerLogoUseYear = true;
  private double manufacturerLogoX = 0.15;
  private double manufacturerLogoY = 0.05;
  private double manufacturerLogoWidth = 0.85;
  private double manufacturerLogoHeight = 0.2;

  // OTHER MEDIA SETTINGS
  private boolean otherMediaKeepAspectRatio = true;
  private VPinScreen otherMediaScreen = null;
  private double otherMediaX = 0.8;
  private double otherMediaY = 0.6;
  private double otherMediaWidth = 0.2;
  private double otherMediaHeight = 0.2;

  // WHEEL SETTINGS
  private double wheelX = 0.0;
  private double wheelY = 0.5;
  private double wheelSize = 0.3;

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

  public CardTemplateType getTemplateType() {
    return templateType;
  }

  public void setTemplateType(CardTemplateType templateType) {
    this.templateType = templateType;
  }
 
  //----------------------------------------

  @JsonIgnore
  public boolean isTemplate() {
    return parentId == null;
  }

  public boolean isLockOverlay() {
    return lockOverlay;
  }

  public void setLockOverlay(boolean lockOverlay) {
    this.lockOverlay = lockOverlay;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public boolean isLockBackground() {
    return lockBackground;
  }

  public void setLockBackground(boolean lockBackground) {
    this.lockBackground = lockBackground;
  }

  public boolean isLockFrame() {
    return lockFrame;
  }

  public void setLockFrame(boolean lockFrame) {
    this.lockFrame = lockFrame;
  }

  public boolean isLockTableName() {
    return lockTableName;
  }

  public void setLockTableName(boolean lockTableName) {
    this.lockTableName = lockTableName;
  }

  public boolean isLockTitle() {
    return lockTitle;
  }

  public void setLockTitle(boolean lockTitle) {
    this.lockTitle = lockTitle;
  }

  public boolean isLockWheelIcon() {
    return lockWheelIcon;
  }

  public void setLockWheelIcon(boolean lockWheelIcon) {
    this.lockWheelIcon = lockWheelIcon;
  }

  public boolean isLockManufacturerLogo() {
    return lockManufacturerLogo;
  }

  public void setLockManufacturerLogo(boolean lockManufacturerLogo) {
    this.lockManufacturerLogo = lockManufacturerLogo;
  }

  public boolean isLockOtherMedia() {
    return lockOtherMedia;
  }

  public void setLockOtherMedia(boolean lockOtherMedia) {
    this.lockOtherMedia = lockOtherMedia;
  }

  public boolean isLockCanvas() {
    return lockCanvas;
  }

  public void setLockCanvas(boolean lockCanvas) {
    this.lockCanvas = lockCanvas;
  }

  public boolean isLockScores() {
    return lockScores;
  }

  public void setLockScores(boolean lockScores) {
    this.lockScores = lockScores;
  }

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

  public boolean isManufacturerLogoKeepAspectRatio() {
    return manufacturerLogoKeepAspectRatio;
  }

  public void setManufacturerLogoKeepAspectRatio(boolean manufacturerKeepAspectRatio) {
    this.manufacturerLogoKeepAspectRatio = manufacturerKeepAspectRatio;
  }

  public boolean isManufacturerLogoUseYear() {
    return manufacturerLogoUseYear;
  }

  public void setManufacturerLogoUseYear(boolean manufacturerLogoUseYear) {
    this.manufacturerLogoUseYear = manufacturerLogoUseYear;
  }

  public double getManufacturerLogoX() {
    return manufacturerLogoX;
  }

  public void setManufacturerLogoX(double manufacturerLogoX) {
    this.manufacturerLogoX = manufacturerLogoX;
  }

  public double getManufacturerLogoY() {
    return manufacturerLogoY;
  }

  public void setManufacturerLogoY(double manufacturerLogoY) {
    this.manufacturerLogoY = manufacturerLogoY;
  }

  public double getManufacturerLogoWidth() {
    return manufacturerLogoWidth;
  }

  public void setManufacturerLogoWidth(double manufacturerLogoWidth) {
    this.manufacturerLogoWidth = manufacturerLogoWidth;
  }

  public double getManufacturerLogoHeight() {
    return manufacturerLogoHeight;
  }

  public void setManufacturerLogoHeight(double manufacturerLogoHeight) {
    this.manufacturerLogoHeight = manufacturerLogoHeight;
  }

  public VPinScreen getOtherMediaScreen() {
    return otherMediaScreen;
  }

  public void setOtherMediaScreen(VPinScreen otherMediaScreen) {
    this.otherMediaScreen = otherMediaScreen;
  }

  public boolean isOtherMediaKeepAspectRatio() {
    return otherMediaKeepAspectRatio;
  }

  public void setOtherMediaKeepAspectRatio(boolean other2MediaKeepAspectRatio) {
    this.otherMediaKeepAspectRatio = other2MediaKeepAspectRatio;
  }

  public double getOtherMediaX() {
    return otherMediaX;
  }

  public void setOtherMediaX(double other2MediaX) {
    this.otherMediaX = other2MediaX;
  }

  public double getOtherMediaY() {
    return otherMediaY;
  }

  public void setOtherMediaY(double other2MediaY) {
    this.otherMediaY = other2MediaY;
  }

  public double getOtherMediaWidth() {
    return otherMediaWidth;
  }

  public void setOtherMediaWidth(double other2MediaWidth) {
    this.otherMediaWidth = other2MediaWidth;
  }

  public double getOtherMediaHeight() {
    return otherMediaHeight;
  }

  public void setOtherMediaHeight(double other2MediaHeight) {
    this.otherMediaHeight = other2MediaHeight;
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

  public String getFrame() {
    return frame;
  }

  public void setFrame(String frame) {
    this.frame = frame;
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

  public boolean isRenderManufacturerLogo() {
    return renderManufacturerLogo;
  }

  public void setRenderManufacturerLogo(boolean renderManufacturerLogo) {
    this.renderManufacturerLogo = renderManufacturerLogo;
  }

  public boolean isRenderOtherMedia() {
    return renderOtherMedia;
  }

  public void setRenderOtherMedia(boolean renderOther2Media) {
    this.renderOtherMedia = renderOther2Media;
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

  @JsonIgnore
  public boolean isDefault() {
    return DEFAULT.equals(getName());
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
    if (o == null || getClass() != o.getClass()) return false;
    CardTemplate that = (CardTemplate) o;
    return Objects.equals(id, that.id) && Objects.equals(parentId, that.parentId) && Objects.equals(name, that.name) && templateType == that.templateType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, parentId, name, templateType);
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public String getSettingsName() {
    return null;
  }

  //-----------------------------------------------------------

  public void resetDefaultHighscoreCard() {
    renderBackground = true;
    renderFrame = true;
    renderTableName = true;
    renderTitle = true;
    renderWheelIcon = true;
    renderManufacturerLogo = false;
    renderOtherMedia = false;
    renderCanvas = false;
    renderScores = true;

    // BACKGROUND IMAGES
    useDefaultBackground = true;
    background = "Old Bumbers";
    useColoredBackground = true;
    backgroundColor = "#000000";

    transparentPercentage = 0;
    alphaBlack = 33;
    alphaWhite = 1;
    blur = 6;
    grayScale = false;

    // FRAME SETTINGS
    borderWidth = 1;
    borderRadius = 0;
    borderColor = "#FFFFFF";

    marginTop = 10;
    marginRight = 10;
    marginBottom = 10;
    marginLeft = 10;

    // MANUFACTURER LOGO SETTINGS
    manufacturerLogoKeepAspectRatio = true;
    manufacturerLogoUseYear = true;
    manufacturerLogoX = 0.15;
    manufacturerLogoY = 0.05;
    manufacturerLogoWidth = 0.85;
    manufacturerLogoHeight = 0.2;

    // OTHER MEDIA SETTINGS
    otherMediaKeepAspectRatio = true;
    otherMediaScreen = null;
    otherMediaX = 0.8;
    otherMediaY = 0.6;
    otherMediaWidth = 0.2;
    otherMediaHeight = 0.2;

    // WHEEL SETTINGS
    wheelX = 0.0;
    wheelY = 0.5;
    wheelSize = 0.3;

    // SCORES SETTINGS
    scoresX = 0.3;
    scoresY = 0.4;
    scoresWidth = 0.7;
    scoresHeight = 0.6;

    rowMargin = 5;
    rawScore = true;
    maxScores = 0;

    fontColor = "#FFFFFF";
    friendsFontColor = "#CCCCCC";

    scoreFontName = "Monospaced";
    scoreFontSize = 90;
    scoreFontStyle = "Regular";

    renderFriends = true;
    renderPositions = true;
    renderScoreDates = true;

    // TABLENAME SETTINGS
    tableUseVpsName = false;
    tableRenderManufacturer = true;
    tableRenderYear = true;
    tableFontName = "Impact";
    tableFontSize = 72;
    tableFontStyle = "Regular";
    tableUseDefaultColor = true;
    tableColor = "#FFFFFF";
    tableX = 0.0;
    tableY = 0.2;
    tableWidth = 1.0;
    tableHeight = 0.2;

    // TITLE SETTINGS
    title = "Highscores";
    titleFontName = "Cambria";
    titleFontSize = 120;
    titleFontStyle = "Regular";
    titleUseDefaultColor = true;
    titleColor = "#FFFFFF";
    titleX = 0;
    titleY = 0;
    titleWidth = 1.0;
    titleHeight = 0.2;

    // CANVAS SETTINGS
    canvasX = 0.1;
    canvasY = 0.1;
    canvasWidth = 0.8;
    canvasHeight = 0.8;
    canvasAlphaPercentage = 0;
    canvasBorderRadius = 0;

    // OVERLAY SETTINGS
    overlayMode = false;
    overlayScreen = null;
  }

  public void resetDefaultInstructionsCard() {
    //TODO find a good feault settings
  }

  public void resetDefaultWheel() {
    //TODO find a good default settings
    renderBackground = true;
    renderFrame = true;
    renderTableName = true;

    useDefaultBackground = true;

    borderRadius = 2000;
    marginTop = 80;
    marginBottom = 80;
    marginLeft = 80;
    marginRight = 80;
    frame = "wheel-tarcissio";

    tableFontName = "Arial";
    tableFontSize = 100;
    tableUseVpsName = true;
    tableRenderManufacturer = false;
    tableRenderYear = false;
    tableUseDefaultColor = false;
    tableColor = "#999999";
    tableX = 0;
    tableY = 0.85;
    tableWidth = 1;
    tableHeight = 0.15;
  }


}

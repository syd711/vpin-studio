package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.highscores.HighscoreCardResolution;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardGraphics {
  private final static Logger LOG = LoggerFactory.getLogger(CardGraphics.class);

  private final DefaultPictureService directB2SService;
  private final HighscoreCardResolution highscoreCardResolution;
  private final ScoreSummary summary;
  private final CardTemplate template;
  private final Game game;

  public CardGraphics(DefaultPictureService directB2SService, HighscoreCardResolution highscoreCardResolution, CardTemplate template, Game game, ScoreSummary summary) {
    this.directB2SService = directB2SService;
    this.highscoreCardResolution = highscoreCardResolution;
    this.template = template;
    this.game = game;
    this.summary = summary;
  }

  public BufferedImage draw() throws Exception {
    BufferedImage backgroundImage = getBackgroundImage();

    if (!template.isTransparentBackground()) {
      if (template.getBlur() > 0) {
        backgroundImage = ImageUtil.blurImage(backgroundImage, template.getBlur());
      }

      if (template.isGrayScale()) {
        backgroundImage = ImageUtil.grayScaleImage(backgroundImage);
      }

      float alphaWhite = template.getAlphaWhite();
      float alphaBlack = template.getAlphaBlack();
      ImageUtil.applyAlphaComposites(backgroundImage, alphaWhite, alphaBlack);
    }

    renderCardData(backgroundImage, game);
    int borderWidth = template.getBorderWidth();
    ImageUtil.drawBorder(backgroundImage, borderWidth);

    return backgroundImage;
  }

  private BufferedImage getBackgroundImage() throws IOException {
    if (template.isTransparentBackground()) {
      BufferedImage bufferedImage = new BufferedImage(highscoreCardResolution.toWidth(), highscoreCardResolution.toHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));

      int value = 255 - (255 * template.getTransparentPercentage() / 100);
      g2.setBackground(new Color(0, 0, 0, value));
      g2.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      g2.dispose();
      return bufferedImage;
    }

    File backgroundsFolder = new File(SystemService.RESOURCES + "backgrounds");
    File sourceImage = new File(backgroundsFolder, template.getBackground() + ".jpg");
    if (!sourceImage.exists()) {
      sourceImage = new File(backgroundsFolder, template.getBackground() + ".png");
    }
    if (!sourceImage.exists()) {
      File[] backgrounds = backgroundsFolder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
      if (backgrounds != null && backgrounds.length > 0) {
        sourceImage = backgrounds[0];
      }
    }
    if (!sourceImage.exists()) {
      throw new UnsupportedOperationException("No background images have been found, " +
          "make sure that folder " + backgroundsFolder.getAbsolutePath() + " contains valid images.");
    }

    File croppedDefaultPicture = directB2SService.generateCroppedDefaultPicture(game);
    BufferedImage backgroundImage = null;
    if (croppedDefaultPicture == null || !template.isUseDirectB2S()) {
      BufferedImage sImage = ImageUtil.loadImage(sourceImage);
      backgroundImage = ImageUtil.crop(sImage, DirectB2SImageRatio.RATIO_16X9.getXRatio(), DirectB2SImageRatio.RATIO_16X9.getYRatio());
    }
    else {
      try {
        backgroundImage = ImageUtil.loadImage(croppedDefaultPicture);
      }
      catch (Exception e) {
        LOG.info("Using default image as fallback instead of " + croppedDefaultPicture.getAbsolutePath());
        BufferedImage sImage = ImageUtil.loadImage(sourceImage);
        backgroundImage = ImageUtil.crop(sImage, DirectB2SImageRatio.RATIO_16X9.getXRatio(), DirectB2SImageRatio.RATIO_16X9.getYRatio());
      }
    }

    return backgroundImage;
  }

  /**
   * The upper section, usually with the three topscores.
   */
  private void renderCardData(BufferedImage image, Game game) throws Exception {
    Graphics g = image.getGraphics();
    int imageWidth = image.getWidth();

    if (template.isRenderCanvas()) {
      renderCanvas(image, g);
    }

    ImageUtil.setDefaultColor(g, template.getFontColor());
    int currentY = template.getMarginTop();
    if (template.isRenderTitle()) {
      String titleFontName = template.getTitleFontName();
      int titleFontStyle = ImageUtil.convertFontPosture(template.getTitleFontStyle());
      int titleFontSize = template.getTitleFontSize();
      g.setFont(new Font(titleFontName, titleFontStyle, titleFontSize));

      String title = template.getTitle();
      int titleWidth = g.getFontMetrics().stringWidth(title);
      currentY = currentY + titleFontSize;
      g.drawString(title, imageWidth / 2 - titleWidth / 2, currentY);
      g.setFont(new Font(titleFontName, titleFontStyle, titleFontSize));

      currentY = currentY + template.getPadding();
    }

    if (template.isRenderTableName()) {
      String tableFontName = template.getTableFontName();
      int tableFontStyle = ImageUtil.convertFontPosture(template.getTableFontStyle());
      int tableFontSize = template.getTableFontSize();
      g.setFont(new Font(tableFontName, tableFontStyle, tableFontSize));

      String tableName = game.getGameDisplayName();
      int width = g.getFontMetrics().stringWidth(tableName);
      while (width > highscoreCardResolution.toWidth() - template.getMarginLeft() - template.getMarginRight()) {
        tableFontSize = tableFontSize - 1;
        g.setFont(new Font(tableFontName, tableFontStyle, tableFontSize));
        width = g.getFontMetrics().stringWidth(tableName);
      }
      currentY = currentY + tableFontSize;
      int tableNameX = ((imageWidth - template.getMarginLeft() - template.getMarginRight()) / 2 - width / 2) + template.getMarginLeft();
      g.drawString(tableName, tableNameX, currentY);

      currentY = currentY + template.getPadding();
    }

    if (template.isRawScore()) {
      renderRawScore(game, image.getHeight(), image.getWidth(), g, currentY);
    }
    else {
      renderScorelist(game, g, template.getTitle(), currentY); //TODO why title?
    }
  }

  private void renderCanvas(BufferedImage image, Graphics g) {
    Color decode = Color.decode("#FFFFFF");
    if (template.getCanvasBackground() != null) {
      decode = Color.decode(template.getCanvasBackground());
    }
    int value = 255 - (255 * template.getCanvasAlphaPercentage() / 100);
    g.setColor(new Color(decode.getRed(), decode.getGreen(), decode.getBlue(), value));
    g.fillRoundRect(template.getCanvasX(), template.getCanvasY(), template.getCanvasWidth(), template.getCanvasHeight(), template.getCanvasBorderRadius(), template.getCanvasBorderRadius());
  }

  private void renderScorelist(Game game, Graphics g, String title, int currentY) throws IOException {
    g.setFont(new Font(template.getScoreFontName(), ImageUtil.convertFontPosture(template.getScoreFontStyle()), template.getScoreFontSize()));

    currentY = currentY + template.getTableFontSize() / 2;

    int wheelSize = 3 * template.getScoreFontSize() + 3 * template.getRowMargin();
    if (template.isRenderWheelIcon()) {
      //draw wheel icon
      int wheelY = currentY + template.getRowMargin();
      FrontendMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        File wheelIconFile = defaultMediaItem.getFile();
        WheelAugmenter augmenter = new WheelAugmenter(defaultMediaItem.getFile());
        if (augmenter.getBackupWheelIcon().exists()) {
          wheelIconFile = augmenter.getBackupWheelIcon();
        }
        BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        g.drawImage(wheelImage, template.getMarginLeft() + template.getWheelPadding(), wheelY, wheelSize, wheelSize, null);
      }
    }

    //the wheelsize should match the height of three score entries
    int scoreX = template.getMarginLeft();
    if (template.isRenderWheelIcon()) {
      scoreX = scoreX + template.getWheelPadding() + wheelSize + template.getWheelPadding();
    }
    int scoreY = currentY;

    int count = 0;

    int scoreLength = 0;
    List<Score> scores = summary.getScores();
    for (Score score : scores) {
      if (score.getFormattedScore().length() > scoreLength) {
        scoreLength = score.getFormattedScore().length();
      }
    }

    for (Score score : summary.getScores()) {
      int initialX = scoreX;
      scoreY = scoreY + template.getScoreFontSize() + template.getRowMargin();

      String renderString = score.getPlayerInitials() + " ";
      if (template.isRenderPositions()) {
        renderString = score.getPosition() + ". " + renderString;
      }
      int singleScoreWidth = g.getFontMetrics().stringWidth(renderString);
      g.drawString(renderString, initialX, scoreY);

      initialX = initialX + singleScoreWidth + template.getPadding();
      String scoreText = score.getFormattedScore();
      while (scoreText.length() < scoreLength) {
        scoreText = " " + scoreText;
      }
      g.drawString(scoreText, initialX, scoreY);

      count++;

      if (template.getMaxScores() > 0 && count == template.getMaxScores()) {
        break;
      }
    }
  }

  private void renderRawScore(Game game, int imageHeight, int imageWidth, Graphics g, int yStart) throws IOException {
    int wheelWidth = template.getWheelSize();
    if (!template.isRenderWheelIcon()) {
      wheelWidth = 0;
    }

    int remainingHeight = imageHeight - yStart - template.getMarginBottom();
    int remainingWidth = imageWidth - template.getMarginLeft() + template.getMarginRight() - wheelWidth;
    String raw = summary.getRaw().trim();
    String[] lines = raw.split("\n");

    int fontSize = remainingHeight / lines.length;
    if (fontSize > template.getScoreFontSize()) {
      fontSize = template.getScoreFontSize();
    }
    else if (fontSize < 20) {
      fontSize = 20;
    }
    g.setFont(new Font(template.getScoreFontName(), ImageUtil.convertFontPosture(template.getScoreFontStyle()), fontSize));

    //debug frame
//    g.drawRect(PADDING, yStart, remainingWidth, remainingHeight);

    List<TextBlock> textBlocks = createTextBlocks(Arrays.asList(lines), g);
    List<TextColumn> textColumns = createTextColumns(textBlocks, g, remainingHeight);
    while (fontSize > 20 && textColumns.size() > 1) {
      int downScale = g.getFont().getSize() - 1;
      g.setFont(new Font(template.getScoreFontName(), ImageUtil.convertFontPosture(template.getScoreFontStyle()), downScale));
      textColumns = createTextColumns(textBlocks, g, remainingHeight);
    }


    scaleDownToWidth(remainingWidth, g, textColumns);
//    yStart = centerYToRemainingSpace(textColumns, yStart, remainingHeight);
    int columnsWidth = getColumnsWidth(textColumns);
    int remainingXSpace = remainingWidth - columnsWidth;

    int x = 0;
    //file exists && there is place to render it
    FrontendMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    if (defaultMediaItem != null && defaultMediaItem.getFile().exists() && template.isRenderWheelIcon()) {
      File wheelIconFile = defaultMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIconFile);
      if (augmenter.getBackupWheelIcon().exists()) {
        wheelIconFile = augmenter.getBackupWheelIcon();
      }
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      x = (remainingXSpace) / 2;
      g.drawImage(wheelImage, x, yStart, wheelWidth, wheelWidth, null);
      x = x + wheelWidth + template.getPadding();
    }
    else {
      x = (imageWidth - columnsWidth) / 2;
    }

    yStart = yStart + g.getFont().getSize();
    for (TextColumn textColumn : textColumns) {
      textColumn.renderAt(g, x, yStart);
      x = x + textColumn.getWidth();
    }
  }

  private int getColumnsWidth(List<TextColumn> textColumns) {
    int width = 0;
    for (TextColumn textColumn : textColumns) {
      width = width + textColumn.getWidth();
    }
    return width;
  }

  private void scaleDownToWidth(int imageWidth, Graphics g, List<TextColumn> textColumns) {
    int width = computeTotalWidth(textColumns);
    while (width >= imageWidth) {
      int fontSize = g.getFont().getSize() - 1;
      g.setFont(new Font(template.getScoreFontName(), ImageUtil.convertFontPosture(template.getScoreFontStyle()), fontSize));
      width = computeTotalWidth(textColumns);
    }
  }

  private int computeTotalWidth(List<TextColumn> columns) {
    int width = 0;
    for (TextColumn column : columns) {
      int columnWidth = column.getWidth();
      width = width + columnWidth;
    }
    return width;
  }

  List<TextColumn> createTextColumns(List<TextBlock> blocks, Graphics g, int remainingHeight) {
    List<TextColumn> columns = new ArrayList<>();

    //scale down block until every one is matching
    for (TextBlock block : blocks) {
      while (block.getHeight() > remainingHeight) {
        int fontSize = g.getFont().getSize() - 1;
        g.setFont(new Font(template.getScoreFontName(), ImageUtil.convertFontPosture(template.getScoreFontStyle()), fontSize));
      }
    }

    TextColumn column = new TextColumn();
    int columnHeight = remainingHeight;
    for (TextBlock block : blocks) {
      int height = block.getHeight();
      if ((columnHeight - height) < 0) {
        columns.add(column);
        column = new TextColumn();
        columnHeight = remainingHeight;
      }

      columnHeight = columnHeight - height;
      column.addBlock(block);
    }
    columns.add(column);
    return columns;
  }

  List<TextBlock> createTextBlocks(List<String> lines, Graphics g) {
    List<TextBlock> result = new ArrayList<>();

    TextBlock textBlock = new TextBlock(g);
    for (String line : lines) {
      if (line.trim().equals("")) {
        if (!textBlock.isEmpty()) {
          result.add(textBlock);
        }
        textBlock = new TextBlock(g);
      }
      else {
        textBlock.addLine(line);
      }
    }

    if (!textBlock.isEmpty()) {
      result.add(textBlock);
    }

    return result;
  }

  static class TextColumn {
    private final List<TextBlock> blocks = new ArrayList<>();

    TextColumn() {
    }

    public void addBlock(TextBlock block) {
      this.blocks.add(block);
    }

    public int getWidth() {
      int width = 0;
      for (TextBlock block : blocks) {
        if (block.getWidth() > width) {
          width = block.getWidth();
        }
      }
      return width;
    }


    public void renderAt(Graphics g, int x, int y) {
      int startY = y;
      for (TextBlock block : blocks) {
        startY = block.renderAt(g, x, startY);
      }
    }

    public int getHeight() {
      int height = 0;
      for (TextBlock block : blocks) {
        height = height + block.getHeight();
      }
      return height;
    }
  }

  static class TextBlock {
    private final List<String> lines;
    private final Graphics g;

    TextBlock(Graphics g) {
      this(new ArrayList<>(), g);
    }

    TextBlock(List<String> lines, Graphics g) {
      this.lines = lines;
      this.g = g;
    }

    public void addLine(String line) {
      this.lines.add(line);
    }

    public int renderAt(Graphics g, int x, int y) {
      for (String line : lines) {
        //we add a whitespace for every line, nicer formatting for multi-column
        g.drawString(line, x + g.getFontMetrics().stringWidth(" "), y);
        y = y + g.getFont().getSize();
      }
      y = y + g.getFont().getSize(); //render extra blank line
      return y;
    }

    public int getHeight() {
      return (this.lines.size() + 1) * (g.getFont().getSize() - 1); //render extra blank line
    }

    public int getWidth() {
      int maxWidth = 0;
      for (String line : lines) {
        int width = g.getFontMetrics().stringWidth(line + "   ");
        if (width > maxWidth) {
          maxWidth = width;
        }
      }

      return maxWidth;
    }

    public boolean isEmpty() {
      return lines.isEmpty();
    }
  }
}

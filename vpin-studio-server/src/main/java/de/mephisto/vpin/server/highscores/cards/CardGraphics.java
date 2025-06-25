package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.highscores.HighscoreCardResolution;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.commons.fx.ImageUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;


/**
 * ON THE WAY TO commons.fx.CardGraphicsHighscore
 */
public class CardGraphics {
  private final static Logger LOG = LoggerFactory.getLogger(CardGraphics.class);

  private final DefaultPictureService directB2SService;
  private final HighscoreCardResolution highscoreCardResolution;
  private final FrontendService frontendService;

  private final ScoreSummary summary;
  private final CardTemplate template;
  private final Game game;

  public CardGraphics(DefaultPictureService directB2SService, FrontendService frontendService, 
      HighscoreCardResolution highscoreCardResolution, CardTemplate template, Game game, ScoreSummary summary) {
    this.directB2SService = directB2SService;
    this.frontendService = frontendService;
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

    return renderCardData(backgroundImage, game);
  }

  private BufferedImage getBackgroundImage() throws IOException {
    int width = highscoreCardResolution.toWidth();
    int height = highscoreCardResolution.toHeight();
    if (width == 0) {
      width = 1280;
    }
    if (height == 0) {
      height = 720;
    }

    if (template.isTransparentBackground()) {
      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));

      int value = 255 - (255 * template.getTransparentPercentage() / 100);
      g2.setBackground(new java.awt.Color(0, 0, 0, value));
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

    if (width != backgroundImage.getWidth()) {
      backgroundImage = ImageUtil.resizeImage(backgroundImage, width);
    }

    return backgroundImage;
  }

  /**
   * The upper section, usually with the three topscores.
   */
  private BufferedImage renderCardData(BufferedImage image, Game game) throws Exception {
    Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
    GraphicsContext g = canvas.getGraphicsContext2D();
    g.setFill(Paint.valueOf(template.getFontColor()));

    Image background = SwingFXUtils.toFXImage(image, null);
    g.drawImage(background, 0, 0);


    int borderWidth = template.getBorderWidth();
    ImageUtil.drawBorder(g, borderWidth, image.getWidth(), image.getHeight());

    int imageWidth = image.getWidth();

    if (template.isRenderCanvas()) {
      renderCanvas(image, g);
    }

    ImageUtil.setDefaultColor(g, template.getFontColor());
    int currentY = template.getMarginTop();
    if (template.isRenderTitle()) {
      String titleFontName = template.getTitleFontName();
      int titleFontSize = template.getTitleFontSize();
      g.setFont(createFont(titleFontName, template.getTitleFontStyle(), titleFontSize));

      String title = template.getTitle();
      int titleWidth = getTextWidth(title, g.getFont());
      currentY = currentY + titleFontSize;
      g.fillText(title, imageWidth / 2 - titleWidth / 2, currentY);
      g.setFont(createFont(titleFontName, template.getTitleFontStyle(), titleFontSize));

      currentY = currentY + template.getPadding();
    }


    if (template.isRawScore()) {
      renderRawScore(game, image.getHeight(), image.getWidth(), g, currentY);
    }
    else {
      renderScorelist(game, g, currentY, image.getWidth()); //TODO why title?
    }

    SnapshotParameters snapshotParameters = new SnapshotParameters();
    Rectangle2D rectangle2D = new Rectangle2D(0, 0, canvas.getWidth(), canvas.getHeight());
    snapshotParameters.setViewport(rectangle2D);
    snapshotParameters.setFill(Color.TRANSPARENT);
    WritableImage snapshot = canvas.snapshot(snapshotParameters, null);
    BufferedImage bufferedImage = new BufferedImage((int) rectangle2D.getWidth(), (int) rectangle2D.getHeight(), BufferedImage.TYPE_INT_ARGB);
    return SwingFXUtils.fromFXImage(snapshot, bufferedImage);
  }


  private void renderCanvas(BufferedImage image, GraphicsContext g) {
    int value = 255 - (255 * template.getCanvasAlphaPercentage() / 100);
    String hex = Integer.toHexString(value);
    String color = "#FFFFFF";
    if (template.getCanvasBackground() != null) {
      color = template.getCanvasBackground();
    }

    Paint paint = Paint.valueOf(color + hex);
    g.setFill(paint);

    boolean canvasCentered = template.getCanvasX() == 0;
    if (canvasCentered) {
      double x = (image.getWidth() / 2) - (template.getCanvasWidth() / 2);
      g.fillRoundRect(x, template.getCanvasY(), template.getCanvasWidth(), template.getCanvasHeight(), template.getCanvasBorderRadius(), template.getCanvasBorderRadius());
    }
    else {
      g.fillRoundRect(template.getCanvasX(), template.getCanvasY(), template.getCanvasWidth(), template.getCanvasHeight(), template.getCanvasBorderRadius(), template.getCanvasBorderRadius());
    }
    g.setFill(Paint.valueOf(template.getFontColor()));
  }

  private void renderScorelist(Game game, GraphicsContext g, int currentY, int imageWidth) throws IOException {
    if (template.isRenderTableName()) {
      String tableFontName = template.getTableFontName();
      String tableFontStyle = template.getTableFontStyle();
      int tableFontSize = template.getTableFontSize();

      Font font = createFont(tableFontName, tableFontStyle, tableFontSize);
      g.setFont(font);

      String tableName = game.getGameDisplayName();
      currentY = currentY + tableFontSize;
      int tableNameWidth = getTextWidth(tableName, font);
      g.fillText(tableName, imageWidth / 2 - tableNameWidth / 2, currentY);

      currentY = currentY + template.getPadding();
    }

    Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), template.getScoreFontSize());
    g.setFont(font);

    //calc max length of scores
    int scoreLength = 0;
    List<Score> scores = summary.getScores();
    for (Score score : scores) {
      if (score.getFormattedScore().length() > scoreLength) {
        scoreLength = score.getFormattedScore().length();
      }
    }

    //format score lines
    Map<String, Score> scoreLines = new LinkedHashMap<>();
    for (Score score : scores) {
      String renderString = score.getPlayerInitials() + "   ";
      if (template.isRenderPositions()) {
        renderString = score.getPosition() + ". " + renderString;
      }
      String scoreText = score.getFormattedScore();
      while (scoreText.length() < scoreLength) {
        scoreText = " " + scoreText;
      }
      renderString = renderString + scoreText;
      scoreLines.put(renderString, score);
    }


    //the wheelsize should match the height of three score entries
    int scoreX = template.getMarginLeft();
    currentY = currentY + template.getTableFontSize() / 2;

    int scoreY = currentY;
    //center scores
    double scoreStartX = scoreX;
    double wheelStartX = template.getMarginLeft() + template.getWheelPadding();

    if (template.getMarginLeft() == 0 && template.getMarginRight() == 0) {
      if (!scoreLines.isEmpty()) {
        String line = scoreLines.keySet().iterator().next();
        double textWidth = getTextWidth(line, font);
        scoreStartX = (imageWidth / 2) - (textWidth / 2);

        if (template.isRenderWheelIcon()) {
          double totalWheelWidth = template.getWheelSize() + template.getWheelPadding();
          double totalScoreAndWheelWidth = totalWheelWidth + textWidth;
          scoreStartX = (imageWidth / 2) - (totalScoreAndWheelWidth / 2) + totalWheelWidth;
          wheelStartX = (imageWidth / 2) - (totalScoreAndWheelWidth / 2);
        }
      }
    }
    else {
      scoreStartX = scoreStartX + template.getWheelSize() + template.getWheelPadding();
    }

    if (template.isRenderWheelIcon()) {
      //draw wheel icon
      int wheelY = currentY + template.getRowMargin();
      File wheelIconFile = frontendService.getWheelImage(game);
      if (wheelIconFile != null && wheelIconFile.exists()) {
        WheelAugmenter augmenter = new WheelAugmenter(wheelIconFile);
        if (augmenter.getBackupWheelIcon().exists()) {
          wheelIconFile = augmenter.getBackupWheelIcon();
        }
        BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        Image wImage = SwingFXUtils.toFXImage(wheelImage, null);
        g.drawImage(wImage, wheelStartX, wheelY, template.getWheelSize(), template.getWheelSize());
      }
    }

    int count = 0;
    for (Map.Entry<String, Score> entry : scoreLines.entrySet()) {
      String scoreLine = entry.getKey();
      Score score = entry.getValue();
      if (score.isExternal()) {
        g.setFill(Paint.valueOf(template.getFriendsFontColor()));
      }
      else {
        g.setFill(Paint.valueOf(template.getFontColor()));
      }

      scoreY = scoreY + template.getScoreFontSize() + template.getRowMargin();
      g.fillText(scoreLine, scoreStartX, scoreY);
      count++;

      if (template.getMaxScores() > 0 && count == template.getMaxScores()) {
        break;
      }
    }
    g.setFill(Paint.valueOf(template.getFontColor()));
  }

  private void renderRawScore(Game game, int imageHeight, int imageWidth, GraphicsContext g, int yStart) throws IOException {
    if (template.isRenderTableName()) {
      String tableFontName = template.getTableFontName();
      String tableFontStyle = template.getTableFontStyle();
      int tableFontSize = template.getTableFontSize();
      Font font = createFont(tableFontName, tableFontStyle, tableFontSize);
      g.setFont(font);

      String tableName = game.getGameDisplayName();
      int width = getTextWidth(tableName, font);
      while (width > highscoreCardResolution.toWidth() - template.getMarginLeft() - template.getMarginRight()) {
        tableFontSize = tableFontSize - 1;
        font = createFont(tableFontName, tableFontStyle, tableFontSize);
        g.setFont(font);
        width = getTextWidth(tableName, font);
      }

      yStart = yStart + tableFontSize;
      int tableNameX = ((imageWidth - template.getMarginLeft() - template.getMarginRight()) / 2 - width / 2) + template.getMarginLeft();
      g.fillText(tableName, tableNameX, yStart);

      yStart = yStart + template.getPadding();
    }

    int wheelWidth = template.getWheelSize();
    if (!template.isRenderWheelIcon()) {
      wheelWidth = 0;
    }

    int remainingHeight = imageHeight - yStart - template.getMarginBottom();
    int remainingWidth = imageWidth - template.getMarginLeft() + template.getMarginRight() - wheelWidth;
    String raw = ScoreFormatUtil.formatRaw(summary.getRaw());
    String[] lines = raw.split("\n");

    int fontSize = remainingHeight / lines.length;
    if (fontSize > template.getScoreFontSize()) {
      fontSize = template.getScoreFontSize();
    }
    else if (fontSize < 20) {
      fontSize = 20;
    }

    Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSize);
    g.setFont(font);

    //debug frame
//    g.drawRect(PADDING, yStart, remainingWidth, remainingHeight);

    List<TextBlock> textBlocks = createTextBlocks(Arrays.asList(lines), g);
    List<TextColumn> textColumns = createTextColumns(textBlocks, g, remainingHeight);
    while (fontSize > 20 && textColumns.size() > 1) {
      int downScale = (int) (g.getFont().getSize() - 1);
      font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), downScale);
      g.setFont(font);
      textColumns = createTextColumns(textBlocks, g, remainingHeight);
    }


    scaleDownToWidth(remainingWidth, g, textColumns);
//    yStart = centerYToRemainingSpace(textColumns, yStart, remainingHeight);
    int columnsWidth = getColumnsWidth(textColumns);
    int remainingXSpace = remainingWidth - columnsWidth;

    int x = 0;
    //file exists && there is place to render it
    File wheelIconFile = frontendService.getWheelImage(game);
    if (wheelIconFile != null && wheelIconFile.exists() && template.isRenderWheelIcon()) {
      WheelAugmenter augmenter = new WheelAugmenter(wheelIconFile);
      if (augmenter.getBackupWheelIcon().exists()) {
        wheelIconFile = augmenter.getBackupWheelIcon();
      }
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      Image wImage = SwingFXUtils.toFXImage(wheelImage, null);
      x = (remainingXSpace) / 2;
      g.drawImage(wImage, x, yStart, wheelWidth, wheelWidth);
      x = x + wheelWidth + template.getPadding();
    }
    else {
      x = (imageWidth - columnsWidth) / 2;
    }

    yStart = yStart + ((int) g.getFont().getSize());
    for (TextColumn textColumn : textColumns) {
      textColumn.renderAt(g, x, yStart);
      x = x + textColumn.getWidth();
    }
  }

  private static Font createFont(String family, String posture, int size) {
    FontWeight fontWeight = FontWeight.findByName(posture);
    FontPosture fontPosture = FontPosture.findByName(posture);
    if (posture != null && posture.contains(" ")) {
      String[] split = posture.split(" ");
      fontWeight = FontWeight.findByName(split[0]);
      fontPosture = FontPosture.findByName(split[1]);
    }
    return Font.font(family, fontWeight, fontPosture, size);
  }

  private int getColumnsWidth(List<TextColumn> textColumns) {
    int width = 0;
    for (TextColumn textColumn : textColumns) {
      width = width + textColumn.getWidth();
    }
    return width;
  }

  private void scaleDownToWidth(int imageWidth, GraphicsContext g, List<TextColumn> textColumns) {
    int width = computeTotalWidth(textColumns);
    while (width >= imageWidth) {
      int fontSize = ((int) g.getFont().getSize()) - 1;
      Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSize);
      g.setFont(font);
      width = computeTotalWidth(textColumns);
    }
  }

  private static int getTextWidth(String text, Font font) {
    Text theText = new Text(text);
    theText.setFont(font);
    return (int) theText.getBoundsInLocal().getWidth();
  }

  private int computeTotalWidth(List<TextColumn> columns) {
    int width = 0;
    for (TextColumn column : columns) {
      int columnWidth = column.getWidth();
      width = width + columnWidth;
    }
    return width;
  }

  List<TextColumn> createTextColumns(List<TextBlock> blocks, GraphicsContext g, int remainingHeight) {
    List<TextColumn> columns = new ArrayList<>();

    //scale down block until every one is matching
    for (TextBlock block : blocks) {
      while (block.getHeight() > remainingHeight) {
        int fontSize = ((int) g.getFont().getSize()) - 1;
        Font font = createFont(template.getScoreFontName(), template.getScoreFontStyle(), fontSize);
        g.setFont(font);
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

  List<TextBlock> createTextBlocks(List<String> lines, GraphicsContext g) {
    List<TextBlock> result = new ArrayList<>();

    TextBlock textBlock = new TextBlock(g);
    for (String line : lines) {
      if (line.trim().isEmpty()) {
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


    public void renderAt(GraphicsContext g, int x, int y) {
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
    private final GraphicsContext g;

    TextBlock(GraphicsContext g) {
      this(new ArrayList<>(), g);
    }

    TextBlock(List<String> lines, GraphicsContext g) {
      this.lines = lines;
      this.g = g;
    }

    public void addLine(String line) {
      this.lines.add(line);
    }

    public int renderAt(GraphicsContext g, int x, int y) {
      for (String line : lines) {
        //we add a whitespace for every line, nicer formatting for multi-column
        g.fillText(line, x + getTextWidth(" ", g.getFont()), y);
        y = y + (int) g.getFont().getSize();
      }
      y = y + (int) g.getFont().getSize(); //render extra blank line
      return y;
    }

    public int getHeight() {
      return (this.lines.size() + 1) * (((int) g.getFont().getSize()) - 1); //render extra blank line
    }

    public int getWidth() {
      int maxWidth = 0;
      for (String line : lines) {
        int width = getTextWidth(line + "   ", g.getFont());
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

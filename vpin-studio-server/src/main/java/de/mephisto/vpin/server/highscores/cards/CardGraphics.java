package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.directb2s.DirectB2SService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
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

  private final int ROW_SEPARATOR = Config.getCardGeneratorConfig().getInt("card.highscores.row.separator");
  private final int WHEEL_PADDING = Config.getCardGeneratorConfig().getInt("card.highscores.row.padding.left");

  private final String TITLE_TEXT = Config.getCardGeneratorConfig().getString("card.title.text", "Highscores");

  private final String SCORE_FONT_NAME = Config.getCardGeneratorConfig().getString("card.score.font.name");
  private final int SCORE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getCardGeneratorConfig().getString("card.score.font.style"));
  private final int SCORE_FONT_SIZE = Config.getCardGeneratorConfig().getInt("card.score.font.size", 24);

  private final String TITLE_FONT_NAME = Config.getCardGeneratorConfig().getString("card.title.font.name", "Arial");
  private final int TITLE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getCardGeneratorConfig().getString("card.title.font.style"));
  private final int TITLE_FONT_SIZE = Config.getCardGeneratorConfig().getInt("card.title.font.size", 28);

  private final String TABLE_FONT_NAME = Config.getCardGeneratorConfig().getString("card.table.font.name");
  private final int TABLE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getCardGeneratorConfig().getString("card.table.font.style"));
  private final int TABLE_FONT_SIZE = Config.getCardGeneratorConfig().getInt("card.table.font.size", 24);

  private final String FONT_COLOR = Config.getCardGeneratorConfig().getString("card.font.color", "#FFFFFF");

  private final int PADDING = Config.getCardGeneratorConfig().getInt("card.padding");

  private final boolean RAW_HIGHSCORE = Config.getCardGeneratorConfig().getBoolean("card.rawHighscore");
  private final boolean USE_DIRECTB2S = Config.getCardGeneratorConfig().getBoolean("card.useDirectB2S");
  private final boolean GRAY_SCALE = Config.getCardGeneratorConfig().getBoolean("card.grayScale");

  private final int BLUR_PIXELS = Config.getCardGeneratorConfig().getInt("card.blur");

  String cardRatio = Config.getCardGeneratorConfig().getString("card.ratio", DirectB2SImageRatio.RATIO_16X9.name());
  private final DirectB2SImageRatio DIRECTB2S_RATIO = DirectB2SImageRatio.valueOf(cardRatio.toUpperCase());


  private final DirectB2SService directB2SService;
  private final ScoreSummary summary;
  private final Game game;

  public CardGraphics(DirectB2SService directB2SService, Game game, ScoreSummary summary) {
    this.directB2SService = directB2SService;
    this.game = game;
    this.summary = summary;
  }

  public BufferedImage draw() throws Exception {
    File backgroundsFolder = new File(SystemService.RESOURCES + "backgrounds");
    File sourceImage = new File(backgroundsFolder, Config.getCardGeneratorConfig().get("card.background") + ".jpg");
    if (!sourceImage.exists()) {
      sourceImage = new File(backgroundsFolder, Config.getCardGeneratorConfig().get("card.background") + ".png");
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

    int scaling = Config.getCardGeneratorConfig().getInt("card.scaling", 1280);
    if (USE_DIRECTB2S) {

      File croppedDirectB2SBackgroundImage = game.getCroppedDirectB2SBackgroundImage();

      //check if the existing directb2s exists and has the correct scaling
      if (croppedDirectB2SBackgroundImage.exists()) {
        BufferedImage croppedDirectb2s = ImageUtil.loadImage(croppedDirectB2SBackgroundImage);
        int width = croppedDirectb2s.getWidth();
        if(width != scaling) {
          LOG.info("Deleting existing cropped directb2s background '" + croppedDirectB2SBackgroundImage.getAbsolutePath() + "', because is has the wrong ratio.");
          if(!croppedDirectB2SBackgroundImage.delete()) {
            LOG.error("Failed to delete " + croppedDirectB2SBackgroundImage.getAbsolutePath());
          }
          croppedDirectB2SBackgroundImage = directB2SService.generateCroppedB2SImage(game, DIRECTB2S_RATIO, scaling);
        }
      }
      else {
        croppedDirectB2SBackgroundImage = directB2SService.generateCroppedB2SImage(game, DIRECTB2S_RATIO, scaling);
      }

      if (croppedDirectB2SBackgroundImage != null && croppedDirectB2SBackgroundImage.exists()) {
        sourceImage = croppedDirectB2SBackgroundImage;
      }
    }

    BufferedImage backgroundImage = ImageUtil.loadImage(sourceImage);
    if (USE_DIRECTB2S) {
      backgroundImage = ImageUtil.crop(backgroundImage, DIRECTB2S_RATIO.getXRatio(), DIRECTB2S_RATIO.getYRatio());
      backgroundImage = ImageUtil.resizeImage(backgroundImage, scaling);
    }

    if (BLUR_PIXELS > 0) {
      backgroundImage = ImageUtil.blurImage(backgroundImage, BLUR_PIXELS);
    }

    if (GRAY_SCALE) {
      backgroundImage = ImageUtil.grayScaleImage(backgroundImage);
    }

    float alphaWhite = Config.getCardGeneratorConfig().getFloat("card.alphacomposite.white");
    float alphaBlack = Config.getCardGeneratorConfig().getFloat("card.alphacomposite.black");
    ImageUtil.applyAlphaComposites(backgroundImage, alphaWhite, alphaBlack);
    renderCardData(backgroundImage, game);

    int borderWidth = Config.getCardGeneratorConfig().getInt("card.border.width");
    ImageUtil.drawBorder(backgroundImage, borderWidth);

    return backgroundImage;
  }

  /**
   * The upper section, usually with the three topscores.
   */
  private void renderCardData(BufferedImage image, Game game) throws Exception {
    Graphics g = image.getGraphics();
    ImageUtil.setDefaultColor(g, FONT_COLOR);
    int imageWidth = image.getWidth();

    g.setFont(new Font(TITLE_FONT_NAME, TITLE_FONT_STYLE, TITLE_FONT_SIZE));

    String title = TITLE_TEXT;
    int titleWidth = g.getFontMetrics().stringWidth(title);
    int titleY = TITLE_FONT_SIZE + PADDING;
    g.drawString(title, imageWidth / 2 - titleWidth / 2, titleY);

    g.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE, TABLE_FONT_SIZE));
    String tableName = game.getGameDisplayName();
    int width = g.getFontMetrics().stringWidth(tableName);
    int tableNameY = titleY + TABLE_FONT_SIZE + TABLE_FONT_SIZE / 2;
    g.drawString(tableName, imageWidth / 2 - width / 2, tableNameY);

    if (RAW_HIGHSCORE) {
      int yStart = tableNameY + TABLE_FONT_SIZE;
      renderRawScore(game, image.getHeight(), image.getWidth(), g, yStart);
    }
    else {
      renderScorelist(game, g, title, tableNameY);
    }
  }

  private void renderScorelist(Game game, Graphics g, String title, int tableNameY) throws IOException {
    g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, SCORE_FONT_SIZE));
    int count = 0;
    int scoreWidth = 0;

    List<String> scores = new ArrayList<>();
    for (Score score : summary.getScores()) {
      String scoreString = score.getPosition() + ". " + score.getPlayerInitials() + " " + score.getScore();
      scores.add(scoreString);

      int singleScoreWidth = g.getFontMetrics().stringWidth(title);
      if (scoreWidth < singleScoreWidth) {
        scoreWidth = singleScoreWidth;
      }
      count++;
      if (count == 3) {
        break;
      }
    }

    tableNameY = tableNameY + TABLE_FONT_SIZE / 2;

    //draw wheel icon
    File wheelIconFile = game.getPinUPMedia(PopperScreen.Wheel);
    int wheelY = tableNameY + ROW_SEPARATOR;
    int wheelSize = 3 * SCORE_FONT_SIZE + 3 * ROW_SEPARATOR;
    if (wheelIconFile != null && wheelIconFile.exists()) {
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      g.drawImage(wheelImage, WHEEL_PADDING, wheelY, wheelSize, wheelSize, null);
    }


    //the wheelsize should match the height of three score entries
    int scoreX = WHEEL_PADDING + wheelSize + WHEEL_PADDING;
    int scoreY = tableNameY;
    for (String score : scores) {
      scoreY = scoreY + SCORE_FONT_SIZE + ROW_SEPARATOR;
      g.drawString(score, scoreX, scoreY);
    }
  }

  private void renderRawScore(Game game, int imageHeight, int imageWidth, Graphics g, int yStart) throws IOException {
    int remainingHeight = imageHeight - yStart - PADDING;
    int remainingWidth = imageWidth - 2 * PADDING;
    String raw = summary.getRaw().trim();
    String[] lines = raw.split("\n");

    int fontSize = remainingHeight / lines.length;
    if (fontSize > SCORE_FONT_SIZE) {
      fontSize = SCORE_FONT_SIZE;
    }
    else if (fontSize < 20) {
      fontSize = 20;
    }
    g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, fontSize));

    //debug frame
//    g.drawRect(PADDING, yStart, remainingWidth, remainingHeight);

    List<TextBlock> textBlocks = createTextBlocks(Arrays.asList(lines), g);
    List<TextColumn> textColumns = createTextColumns(textBlocks, g, remainingHeight);
    while (fontSize > 20 && textColumns.size() > 1) {
      int downScale = g.getFont().getSize() - 1;
      g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, downScale));
      textColumns = createTextColumns(textBlocks, g, remainingHeight);
    }


    scaleDownToWidth(remainingWidth, g, textColumns);
//    yStart = centerYToRemainingSpace(textColumns, yStart, remainingHeight);
    int columnsWidth = getColumnsWidth(textColumns);
    int remainingXSpace = remainingWidth - columnsWidth;

    int x = 0;
    int wheelWidth = PADDING * 2 + TABLE_FONT_SIZE * 2;
    File wheelIconFile = game.getPinUPMedia(PopperScreen.Wheel);
    boolean renderWheel = remainingXSpace > (wheelWidth + PADDING);
    if (remainingXSpace > 250) {
      wheelWidth = 250;
    }

    //file exists && there is place to render it
    if (wheelIconFile != null && wheelIconFile.exists() && renderWheel) {
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      x = (remainingXSpace - wheelWidth) / 2;
      g.drawImage(wheelImage, x, yStart, wheelWidth, wheelWidth, null);
      x = x + wheelWidth + PADDING;
    }
    else {
      x = (remainingWidth - columnsWidth) / 2;
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

  private int centerYToRemainingSpace(List<TextColumn> textColumns, int yStart, int remainingHeight) {
    int maxHeight = computeMaxHeight(textColumns);
    return yStart + ((remainingHeight - maxHeight) / 2);
  }

  private void scaleDownToWidth(int imageWidth, Graphics g, List<TextColumn> textColumns) {
    int width = computeTotalWidth(textColumns);
    while (width >= imageWidth) {
      int fontSize = g.getFont().getSize() - 1;
      g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, fontSize));
      width = computeTotalWidth(textColumns);
    }
  }

  private int computeMaxHeight(List<TextColumn> columns) {
    int height = 0;
    for (TextColumn column : columns) {
      if (column.getHeight() > height) {
        height = column.getHeight();
      }
    }
    return height;
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
        g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, fontSize));
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

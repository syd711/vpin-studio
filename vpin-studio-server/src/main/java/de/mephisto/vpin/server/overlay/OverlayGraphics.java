package de.mephisto.vpin.server.overlay;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import javafx.scene.text.FontPosture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OverlayGraphics {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayGraphics.class);

  private final String HIGHSCORE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.highscores.text", "Highscores");
  private final String TITLE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.title.text", "bubu");

  private final String SCORE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.score.font.name", "Arial");
  private final int SCORE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.style", FontPosture.REGULAR.ordinal());
  private final int SCORE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.size", 60);

  private final String TITLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.title.font.name", "Arial");
  private final int TITLE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.style", FontPosture.REGULAR.ordinal());
  private final int TITLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.size");

  private final String TABLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.table.font.name", "Arial");
  private final int TABLE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.style", FontPosture.REGULAR.ordinal());
  private final int TABLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.size", 60);

  private final int PADDING = Config.getOverlayGeneratorConfig().getInt("overlay.padding");
  private final int ROW_SEPARATOR = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.separator");
  private final int ROW_PADDING_LEFT = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.padding.left");
  private final int ROW_HEIGHT = TABLE_FONT_SIZE + ROW_SEPARATOR + SCORE_FONT_SIZE;

  private final int BLUR_PIXELS = Config.getOverlayGeneratorConfig().getInt("overlay.blur");

  private final String BACKGROUND_IMAGE_NAME = Config.getOverlayGeneratorConfig().getString("overlay.background");
  private final String FONT_COLOR = Config.getOverlayGeneratorConfig().getString("overlay.font.color", "#FFFFFF");

  private final OverlayService overlayService;
  private final GameService service;
  private final HighscoreService highscoreService;

  public OverlayGraphics(OverlayService overlayService, GameService service, HighscoreService highscoreService) {
    this.overlayService = overlayService;
    this.service = service;
    this.highscoreService = highscoreService;
  }

  public BufferedImage draw() throws Exception {
    File backgroundsFolder = overlayService.getOverlayBackgroundsFolder();
    File sourceImage = null;
    if (BACKGROUND_IMAGE_NAME != null) {
      sourceImage = new File(backgroundsFolder, BACKGROUND_IMAGE_NAME + ".jpg");
      if (!sourceImage.exists()) {
        sourceImage = new File(backgroundsFolder, BACKGROUND_IMAGE_NAME + ".png");
      }
    }

    if (sourceImage == null || !sourceImage.exists()) {
      File[] backgrounds = backgroundsFolder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
      if (backgrounds != null && backgrounds.length > 0) {
        sourceImage = backgrounds[0];
      }
    }

    if (sourceImage == null || !sourceImage.exists()) {
      throw new UnsupportedOperationException("No background images have been found, " +
          "make sure that folder " + backgroundsFolder.getAbsolutePath() + " contains valid images.");
    }

    BufferedImage backgroundImage = ImageUtil.loadImage(sourceImage);
    BufferedImage rotated = ImageUtil.rotateRight(backgroundImage);
    if (BLUR_PIXELS > 0) {
      rotated = ImageUtil.blurImage(rotated, BLUR_PIXELS);
    }

    float alphaWhite = Config.getOverlayGeneratorConfig().getFloat("overlay.alphacomposite.white");
    float alphaBlack = Config.getOverlayGeneratorConfig().getFloat("overlay.alphacomposite.black");
    ImageUtil.applyAlphaComposites(rotated, alphaWhite, alphaBlack);

    int highscoreListYOffset = PADDING + TITLE_FONT_SIZE;

    renderHighscoreList(rotated, highscoreListYOffset);

    return ImageUtil.rotateLeft(rotated);
  }

  /**
   * The upper section, usually with the three topscores.
   */
  private int renderTableChallenge(BufferedImage image, Game challengedGame) throws Exception {
    Highscore highscore = highscoreService.getHighscore(challengedGame);
    Graphics g = image.getGraphics();
    ImageUtil.setDefaultColor(g, FONT_COLOR);
    int imageWidth = image.getWidth();

    g.setFont(new Font(TITLE_FONT_NAME, TITLE_FONT_STYLE, TITLE_FONT_SIZE));

    String title = TITLE_TEXT;
    int titleWidth = g.getFontMetrics().stringWidth(title);
    int titleY = ROW_SEPARATOR + TITLE_FONT_SIZE + PADDING;
    g.drawString(title, imageWidth / 2 - titleWidth / 2, titleY);

    g.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE, TABLE_FONT_SIZE));
    String challengedTable = challengedGame.getGameDisplayName();
    int width = g.getFontMetrics().stringWidth(challengedTable);


    int tableNameY = titleY + (2 * ROW_SEPARATOR) + TITLE_FONT_SIZE;
    g.drawString(challengedTable, imageWidth / 2 - width / 2, tableNameY);

    g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, SCORE_FONT_SIZE));

    int count = 0;
    int scoreWidth = 0;


    List<String> scores = new ArrayList<>();
    if (highscore != null) {
//      for (Score score : highscore.toScores()) {
//        String scoreString = score.getPosition() + ". " + score.getUserInitials() + " " + score.getScore();
//        scores.add(scoreString);
//
//        int singleScoreWidth = g.getFontMetrics().stringWidth(scoreString);
//        if (scoreWidth < singleScoreWidth) {
//          scoreWidth = singleScoreWidth;
//        }
//        count++;
//        if (count == 3) {
//          break;
//        }
//      }
    }
    else {
      for (int i = 1; i <= 3; i++) {
        String scoreString = i + ". ??? 000.000.000";
        int singleScoreWidth = g.getFontMetrics().stringWidth(scoreString);
        if (scoreWidth < singleScoreWidth) {
          scoreWidth = singleScoreWidth;
        }
        scores.add(scoreString);
      }
    }

    int position = 0;
    int wheelWidth = (3 * SCORE_FONT_SIZE) + (3 * ROW_SEPARATOR);
    int totalScoreAndWheelWidth = scoreWidth + wheelWidth;

    tableNameY = tableNameY + ROW_SEPARATOR;
    for (String score : scores) {
      position++;
      int scoreY = tableNameY + (position * SCORE_FONT_SIZE) + (position * ROW_SEPARATOR);
      g.drawString(score, imageWidth / 2 - totalScoreAndWheelWidth / 2 + wheelWidth + ROW_SEPARATOR, scoreY);
    }

    File wheelIconFile = challengedGame.getEmulator().getPinUPMedia(PopperScreen.Wheel);
    int wheelY = tableNameY + ROW_SEPARATOR;

    if (wheelIconFile != null && wheelIconFile.exists()) {
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      g.drawImage(wheelImage, imageWidth / 2 - totalScoreAndWheelWidth / 2, wheelY, wheelWidth, wheelWidth, null);
    }

    return wheelY * 2 + SCORE_FONT_SIZE * 2;
  }

  private void renderHighscoreList(BufferedImage image, int highscoreListYOffset) throws Exception {
    Graphics g = image.getGraphics();
    ImageUtil.setDefaultColor(g, Config.getOverlayGeneratorConfig().getString("overlay.font.color"));
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    g.setFont(new Font(TITLE_FONT_NAME, Font.PLAIN, TITLE_FONT_SIZE));
    String text = HIGHSCORE_TEXT;
    int highscoreTextWidth = g.getFontMetrics().stringWidth(text);

    g.drawString(text, imageWidth / 2 - highscoreTextWidth / 2, highscoreListYOffset);

    int yStart = highscoreListYOffset + ROW_SEPARATOR + TITLE_FONT_SIZE / 2;

    List<Game> games = service.getGames();
    List<Game> gamesWithDate = games.stream().filter(game -> game.getLastPlayed() != null).collect(Collectors.toList());
    List<Game> gamesWithOutDate = games.stream().filter(game -> game.getLastPlayed() == null).collect(Collectors.toList());

    List<Game> sorted = new ArrayList<>();
    gamesWithDate.sort((o1, o2) -> Long.compare(o2.getLastPlayed().getTime(), o1.getLastPlayed().getTime()));
    sorted.addAll(gamesWithDate);
    sorted.addAll(gamesWithOutDate);

    for (Game game : sorted) {
      if (game.getScores().isEmpty()) {
        LOG.info("Skipped highscore rendering of " + game.getGameDisplayName() + ", no highscore info found");
        continue;
      }

      File wheelIconFile = game.getEmulator().getPinUPMedia(PopperScreen.Wheel);
      if ((wheelIconFile == null || !wheelIconFile.exists()) && Config.getOverlayGeneratorConfig().getBoolean("overlay.skipWithMissingWheels")) {
        continue;
      }

      LOG.info("Rendering row for table " + game + ", last played " + game.getLastPlayed());
      if (wheelIconFile != null && wheelIconFile.exists()) {
        BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        g.drawImage(wheelImage, ROW_PADDING_LEFT, yStart + 12, ROW_HEIGHT, ROW_HEIGHT, null);
      }

      int x = ROW_HEIGHT + ROW_PADDING_LEFT + ROW_HEIGHT / 3;
      g.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_SIZE, TABLE_FONT_SIZE));
      g.drawString(game.getGameDisplayName(), x, yStart + SCORE_FONT_SIZE);

      Score score = game.getScores().get(0);
      g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, SCORE_FONT_SIZE));
      g.drawString(score.getUserInitials() + " " + score.getScore(), x,
          yStart + SCORE_FONT_SIZE + ((ROW_HEIGHT - SCORE_FONT_SIZE) / 2) + SCORE_FONT_SIZE / 2);

      yStart = yStart + ROW_HEIGHT + ROW_SEPARATOR;
      if (!isRemainingSpaceAvailable(imageHeight, yStart)) {
        break;
      }
    }
  }

  private boolean isRemainingSpaceAvailable(int imageHeight, int positionY) {
    int remaining = imageHeight - positionY;
    return remaining > (ROW_HEIGHT + ROW_SEPARATOR + PADDING);
  }
}

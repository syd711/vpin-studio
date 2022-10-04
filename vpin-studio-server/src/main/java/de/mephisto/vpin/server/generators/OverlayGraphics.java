package de.mephisto.vpin.server.generators;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.jpa.Highscore;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OverlayGraphics {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayGraphics.class);

  private static String HIGHSCORE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.highscores.text");
  private static String TITLE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.title.text");

  private static String SCORE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.score.font.name");
  private static int SCORE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.style");
  private static int SCORE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.size");

  private static String TITLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.title.font.name");
  private static int TITLE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.style");
  private static int TITLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.size");

  private static String TABLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.table.font.name");
  private static int TABLE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.style");
  private static int TABLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.size");

  private static int TITLE_Y_OFFSET = Config.getOverlayGeneratorConfig().getInt("overlay.title.y.offset");
  private static int ROW_SEPARATOR = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.separator");
  private static int ROW_PADDING_LEFT = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.padding.left");
  private static int ROW_HEIGHT = TABLE_FONT_SIZE + ROW_SEPARATOR + SCORE_FONT_SIZE;

  private static int BLUR_PIXELS = Config.getOverlayGeneratorConfig().getInt("overlay.blur");

  private final GameService service;
  private final HighscoreService highscoreService;

  private static void initValues() {
    HIGHSCORE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.highscores.text");
    TITLE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.title.text");

    SCORE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.score.font.name");
    SCORE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.style");
    SCORE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.size");

    TITLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.title.font.name");
    TITLE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.style");
    TITLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.size");

    TABLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.table.font.name");
    TABLE_FONT_STYLE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.style");
    TABLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.size");

    TITLE_Y_OFFSET = Config.getOverlayGeneratorConfig().getInt("overlay.title.y.offset");
    ROW_SEPARATOR = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.separator");
    ROW_PADDING_LEFT = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.padding.left");
    ROW_HEIGHT = TABLE_FONT_SIZE + ROW_SEPARATOR + SCORE_FONT_SIZE;

    BLUR_PIXELS = Config.getOverlayGeneratorConfig().getInt("overlay.blur");
  }

  public OverlayGraphics(GameService service, HighscoreService highscoreService) {
    this.service = service;
    this.highscoreService = highscoreService;
  }

  public BufferedImage draw() throws Exception {
    initValues();
    int selection = Config.getOverlayGeneratorConfig().getInt("overlay.challengedTable");
    Game gameOfTheMonth = null;
    if (selection > 0) {
      gameOfTheMonth = service.getGame(selection);
    }

    BufferedImage backgroundImage = ImageUtil.loadBackground(new File(SystemService.RESOURCES, Config.getOverlayGeneratorConfig().getString("overlay.background")));
    BufferedImage rotated = ImageUtil.rotateRight(backgroundImage);
    if (BLUR_PIXELS > 0) {
      rotated = ImageUtil.blurImage(rotated, BLUR_PIXELS);
    }

    float alphaWhite = Config.getOverlayGeneratorConfig().getFloat("overlay.alphacomposite.white");
    float alphaBlack = Config.getOverlayGeneratorConfig().getFloat("overlay.alphacomposite.black");
    ImageUtil.applyAlphaComposites(rotated, alphaWhite, alphaBlack);

    int highscoreListYOffset = TITLE_Y_OFFSET + TITLE_FONT_SIZE;
    if (gameOfTheMonth != null) {
      highscoreListYOffset = renderTableChallenge(rotated, gameOfTheMonth);
    }

    renderHighscoreList(rotated, gameOfTheMonth, highscoreListYOffset);

    return ImageUtil.rotateLeft(rotated);
  }

  /**
   * The upper section, usually with the three topscores.
   */
  private int renderTableChallenge(BufferedImage image, Game challengedGame) throws Exception {
    Highscore highscore = highscoreService.getHighscore(challengedGame);
    Graphics g = image.getGraphics();
    ImageUtil.setDefaultColor(g, Config.getOverlayGeneratorConfig().getString("overlay.font.color"));
    int imageWidth = image.getWidth();

    g.setFont(new Font(TITLE_FONT_NAME, TITLE_FONT_STYLE, TITLE_FONT_SIZE));

    String title = TITLE_TEXT;
    int titleWidth = g.getFontMetrics().stringWidth(title);
    int titleY = ROW_SEPARATOR + TITLE_FONT_SIZE + TITLE_Y_OFFSET;
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
      for (Score score : highscore.toScores()) {
        String scoreString = score.getPosition() + ". " + score.getUserInitials() + " " + score.getScore();
        scores.add(scoreString);

        int singleScoreWidth = g.getFontMetrics().stringWidth(scoreString);
        if (scoreWidth < singleScoreWidth) {
          scoreWidth = singleScoreWidth;
        }
        count++;
        if (count == 3) {
          break;
        }
      }
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

    File wheelIconFile = challengedGame.getWheelIconFile();
    int wheelY = tableNameY + ROW_SEPARATOR;

    if (wheelIconFile.exists()) {
      BufferedImage wheelImage = ImageIO.read(wheelIconFile);
      g.drawImage(wheelImage, imageWidth / 2 - totalScoreAndWheelWidth / 2, wheelY, wheelWidth, wheelWidth, null);
    }

    return wheelY * 2 + SCORE_FONT_SIZE * 2;
  }

  private void renderHighscoreList(BufferedImage image, Game gameOfTheMonth, int highscoreListYOffset) throws Exception {
    Graphics g = image.getGraphics();
    ImageUtil.setDefaultColor(g, Config.getOverlayGeneratorConfig().getString("overlay.font.color"));
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    g.setFont(new Font(TITLE_FONT_NAME, Font.PLAIN, TITLE_FONT_SIZE));
    String text = HIGHSCORE_TEXT;
    int highscoreTextWidth = g.getFontMetrics().stringWidth(text);

    g.drawString(text, imageWidth / 2 - highscoreTextWidth / 2, highscoreListYOffset);

    int yStart = highscoreListYOffset + ROW_SEPARATOR + TITLE_FONT_SIZE / 2;

    List<Game> gamesWithDate = service.getGameInfos().stream().filter(game -> game.getLastPlayed() != null).collect(Collectors.toList());
    List<Game> gamesWithOutDate = service.getGameInfos().stream().filter(game -> game.getLastPlayed() == null).collect(Collectors.toList());

    List<Game> sorted = new ArrayList<>();
    gamesWithDate.sort((o1, o2) -> Long.compare(o2.getLastPlayed().getTime(), o1.getLastPlayed().getTime()));
    sorted.addAll(gamesWithDate);
    sorted.addAll(gamesWithOutDate);

    for (Game game : sorted) {
      Highscore highscore = highscoreService.getHighscore(game);
      if (highscore == null) {
        LOG.info("Skipped highscore rendering of " + game.getGameDisplayName() + ", no highscore info found");
        continue;
      }

      if (gameOfTheMonth != null && gameOfTheMonth.getGameDisplayName().equals(game.getGameDisplayName())) {
        continue;
      }

      File wheelIconFile = game.getWheelIconFile();
      if (!wheelIconFile.exists() && Config.getOverlayGeneratorConfig().getBoolean("overlay.skipWithMissingWheels")) {
        continue;
      }

      LOG.info("Rendering row for table " + game + ", last played " + game.getLastPlayed());
      if (wheelIconFile.exists()) {
        BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        g.drawImage(wheelImage, ROW_PADDING_LEFT, yStart + 12, ROW_HEIGHT, ROW_HEIGHT, null);
      }

      int x = ROW_HEIGHT + ROW_PADDING_LEFT + ROW_HEIGHT / 3;
      g.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_SIZE, TABLE_FONT_SIZE));
      g.drawString(game.getGameDisplayName(), x, yStart + SCORE_FONT_SIZE);

      g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, SCORE_FONT_SIZE));
      g.drawString(highscore.getInitials1() + " " + highscore.getScore1(), x,
          yStart + SCORE_FONT_SIZE + ((ROW_HEIGHT - SCORE_FONT_SIZE) / 2) + SCORE_FONT_SIZE / 2);

      yStart = yStart + ROW_HEIGHT + ROW_SEPARATOR;
      if (!isRemainingSpaceAvailable(imageHeight, yStart)) {
        break;
      }
    }
  }

  private static boolean isRemainingSpaceAvailable(int imageHeight, int positionY) {
    int remaining = imageHeight - positionY;
    return remaining > (ROW_HEIGHT + ROW_SEPARATOR + TITLE_Y_OFFSET);
  }
}

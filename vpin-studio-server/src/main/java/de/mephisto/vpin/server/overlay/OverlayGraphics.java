package de.mephisto.vpin.server.overlay;

import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.popper.PopperScreen;
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

  private final String TITLE_TEXT = Config.getOverlayGeneratorConfig().getString("overlay.title.text", "Highscores");

  private final String SCORE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.score.font.name", "Arial");
  private final int SCORE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getOverlayGeneratorConfig().getString("overlay.score.font.style"));
  private final int SCORE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.score.font.size", 60);

  private final String TITLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.title.font.name", "Arial");
  private final int TITLE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getOverlayGeneratorConfig().getString("overlay.title.font.style"));
  private final int TITLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.title.font.size");

  private final String TABLE_FONT_NAME = Config.getOverlayGeneratorConfig().getString("overlay.table.font.name", "Arial");
  private final int TABLE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getOverlayGeneratorConfig().getString("overlay.table.font.style"));
  private final int TABLE_FONT_SIZE = Config.getOverlayGeneratorConfig().getInt("overlay.table.font.size", 60);

  private final int PADDING = Config.getOverlayGeneratorConfig().getInt("overlay.padding");
  private final int ROW_SEPARATOR = Config.getOverlayGeneratorConfig().getInt("overlay.highscores.row.separator");
  private final int ROW_HEIGHT = TABLE_FONT_SIZE + ROW_SEPARATOR + SCORE_FONT_SIZE;

  private final int BLUR_PIXELS = Config.getOverlayGeneratorConfig().getInt("overlay.blur");
  private final boolean GRAY_SCALE = Config.getOverlayGeneratorConfig().getBoolean("overlay.grayScale");

  private final String BACKGROUND_IMAGE_NAME = Config.getOverlayGeneratorConfig().getString("overlay.background");
  private final String FONT_COLOR = Config.getOverlayGeneratorConfig().getString("overlay.font.color", "#FFFFFF");
  private final String RESOLUTION = Config.getOverlayGeneratorConfig().getString("overlay.resolution", "2560x1440");


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
    int targetWidth = Integer.parseInt(RESOLUTION.split("x")[0]);
    if(backgroundImage.getWidth() != targetWidth) {
      backgroundImage = ImageUtil.resizeImage(backgroundImage, targetWidth);
    }

    if (BLUR_PIXELS > 0) {
      backgroundImage = ImageUtil.blurImage(backgroundImage, BLUR_PIXELS);
    }

    if(GRAY_SCALE) {
      backgroundImage = ImageUtil.grayScaleImage(backgroundImage);
    }

    float alphaWhite = Config.getOverlayGeneratorConfig().getFloat("overlay.alphacomposite.white");
    float alphaBlack = Config.getOverlayGeneratorConfig().getFloat("overlay.alphacomposite.black");
    ImageUtil.applyAlphaComposites(backgroundImage, alphaWhite, alphaBlack);

    BufferedImage rotated = ImageUtil.rotateRight(backgroundImage);

    int highscoreListYOffset = PADDING + TITLE_FONT_SIZE;

    renderHighscoreList(rotated, highscoreListYOffset);

    return ImageUtil.rotateLeft(rotated);
  }

  private void renderHighscoreList(BufferedImage image, int highscoreListYOffset) throws Exception {
    Graphics g = image.getGraphics();
    ImageUtil.setDefaultColor(g, FONT_COLOR);
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    g.setFont(new Font(TITLE_FONT_NAME, TITLE_FONT_STYLE, TITLE_FONT_SIZE));
    String text = TITLE_TEXT;
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
      ScoreSummary summary = highscoreService.getHighscores(game);
      if (summary.getScores().isEmpty()) {
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
        g.drawImage(wheelImage, PADDING, yStart + 12, ROW_HEIGHT, ROW_HEIGHT, null);
      }

      int x = ROW_HEIGHT + PADDING + ROW_HEIGHT / 3;
      g.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE, TABLE_FONT_SIZE));
      g.drawString(game.getGameDisplayName(), x, yStart + SCORE_FONT_SIZE);

      Score score = summary.getScores().get(0);
      g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, SCORE_FONT_SIZE));
      g.drawString(score.getPlayerInitials() + " " + score.getScore(), x,
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

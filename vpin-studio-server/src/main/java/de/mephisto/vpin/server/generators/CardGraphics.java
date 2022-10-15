package de.mephisto.vpin.server.generators;

import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.directb2s.DirectB2SService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.jpa.Highscore;
import de.mephisto.vpin.server.popper.PopperScreen;
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

public class CardGraphics {
  private final static Logger LOG = LoggerFactory.getLogger(CardGraphics.class);

  private final HighscoreService highscoreService;
  private final DirectB2SService directB2SService;
  private final Game game;

  public CardGraphics(HighscoreService highscoreService, DirectB2SService directB2SService, Game game) {
    this.highscoreService = highscoreService;
    this.directB2SService = directB2SService;
    this.game = game;
  }

  public BufferedImage draw() throws Exception {
    Config.getCardGeneratorConfig().reload();

    boolean USE_DIRECTB2S = Config.getCardGeneratorConfig().getBoolean("card.useDirectB2S");
    String cardRatio = Config.getCardGeneratorConfig().getString("card.ratio", DirectB2SImageRatio.RATIO_16X9.name());
    DirectB2SImageRatio DIRECTB2S_RATIO = DirectB2SImageRatio.valueOf(cardRatio.toUpperCase());
    int BLUR_PIXELS = Config.getCardGeneratorConfig().getInt("card.blur");

    File sourceImage = new File(SystemService.RESOURCES + "backgrounds", Config.getCardGeneratorConfig().get("card.background") + ".jpg");
    if(!sourceImage.exists()) {
      sourceImage = new File(SystemService.RESOURCES + "backgrounds", Config.getCardGeneratorConfig().get("card.background") + ".png");
    }


    int scaling = Config.getCardGeneratorConfig().getInt("card.scaling", 1280);
    if (USE_DIRECTB2S && game.getDirectB2SFile().exists()) {
      File directB2SImage = game.getDirectB2SBackgroundImage();
      if (!directB2SImage.exists()) {
        directB2SImage = directB2SService.generateB2SImage(game, DIRECTB2S_RATIO, scaling);
      }
      if(directB2SImage != null && directB2SImage.exists()) {
        sourceImage = directB2SImage;
      }
    }

    BufferedImage backgroundImage = ImageUtil.loadBackground(sourceImage);
    if (USE_DIRECTB2S) {
      backgroundImage = ImageUtil.crop(backgroundImage, DIRECTB2S_RATIO.getXRatio(), DIRECTB2S_RATIO.getYRatio());
      backgroundImage = ImageUtil.resizeImage(backgroundImage, scaling);
    }

    if (BLUR_PIXELS > 0) {
      backgroundImage = ImageUtil.blurImage(backgroundImage, BLUR_PIXELS);
    }

    float alphaWhite = Config.getCardGeneratorConfig().getFloat("card.alphacomposite.white");
    float alphaBlack = Config.getCardGeneratorConfig().getFloat("card.alphacomposite.black");
    ImageUtil.applyAlphaComposites(backgroundImage, alphaWhite, alphaBlack);
    renderCardData(backgroundImage);

    int borderWidth = Config.getCardGeneratorConfig().getInt("card.border.width");
    ImageUtil.drawBorder(backgroundImage, borderWidth);

    return backgroundImage;
  }

  /**
   * The upper section, usually with the three topscores.
   */
  private void renderCardData(BufferedImage image) throws Exception {
    int ROW_SEPARATOR = Config.getCardGeneratorConfig().getInt("card.highscores.row.separator");
    int WHEEL_PADDING = Config.getCardGeneratorConfig().getInt("card.highscores.row.padding.left");

    String TITLE_TEXT = Config.getCardGeneratorConfig().getString("card.title.text");

    String SCORE_FONT_NAME = Config.getCardGeneratorConfig().getString("card.score.font.name");
    int SCORE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getCardGeneratorConfig().getString("card.score.font.style"));
    int SCORE_FONT_SIZE = Config.getCardGeneratorConfig().getInt("card.score.font.size");

    String TITLE_FONT_NAME = Config.getCardGeneratorConfig().getString("card.title.font.name");
    int TITLE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getCardGeneratorConfig().getString("card.title.font.style"));
    int TITLE_FONT_SIZE = Config.getCardGeneratorConfig().getInt("card.title.font.size");

    String TABLE_FONT_NAME = Config.getCardGeneratorConfig().getString("card.table.font.name");
    int TABLE_FONT_STYLE = ImageUtil.convertFontPosture(Config.getCardGeneratorConfig().getString("card.table.font.style"));
    int TABLE_FONT_SIZE = Config.getCardGeneratorConfig().getInt("card.table.font.size");

    int TITLE_Y_OFFSET = Config.getCardGeneratorConfig().getInt("card.title.y.offset");
    boolean RAW_SCORE_DATA = Config.getCardGeneratorConfig().getBoolean("card.rawScoreData");

    Highscore highscore = highscoreService.getHighscore(game);
    if (highscore != null) {
      Graphics g = image.getGraphics();
      ImageUtil.setDefaultColor(g, Config.getCardGeneratorConfig().getString("card.font.color"));
      int imageWidth = image.getWidth();

      g.setFont(new Font(TITLE_FONT_NAME, TITLE_FONT_STYLE, TITLE_FONT_SIZE));

      String title = TITLE_TEXT;
      int titleWidth = g.getFontMetrics().stringWidth(title);
      int titleY = TITLE_FONT_SIZE + TITLE_Y_OFFSET;
      g.drawString(title, imageWidth / 2 - titleWidth / 2, titleY);

      g.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE, TABLE_FONT_SIZE));
      String tableName = game.getGameDisplayName();
      int width = g.getFontMetrics().stringWidth(tableName);
      int tableNameY = titleY + TABLE_FONT_SIZE + TABLE_FONT_SIZE / 2;
      g.drawString(tableName, imageWidth / 2 - width / 2, tableNameY);


      g.setFont(new Font(SCORE_FONT_NAME, SCORE_FONT_STYLE, SCORE_FONT_SIZE));
      int count = 0;
      int scoreWidth = 0;

      List<String> scores = new ArrayList<>();
      for (Score score : highscore.toScores()) {
        String scoreString = score.getPosition() + ". " + score.getUserInitials() + " " + score.getScore();
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
      File wheelIconFile = game.getEmulator().getPinUPMedia(PopperScreen.Wheel);
      int wheelY = tableNameY + ROW_SEPARATOR;
      int wheelSize = 3 * SCORE_FONT_SIZE + 3 * ROW_SEPARATOR;
      if (wheelIconFile != null && wheelIconFile.exists()) {
        BufferedImage wheelImage = ImageIO.read(wheelIconFile);
        g.drawImage(wheelImage, WHEEL_PADDING, wheelY, wheelSize, wheelSize, null);
      }


      //the wheelsize should match the height of three score entries
      int scoreX = WHEEL_PADDING + wheelSize + WHEEL_PADDING;
      int scoreY = tableNameY;

      if(RAW_SCORE_DATA) {
        String raw = highscore.getRaw();
        String[] split = raw.split("\n");
        for (String s : split) {
          scoreY = scoreY + SCORE_FONT_SIZE + ROW_SEPARATOR;
          g.drawString(s, scoreX, scoreY);
        }
      }
      else {
        for (String score : scores) {
          scoreY = scoreY + SCORE_FONT_SIZE + ROW_SEPARATOR;
          g.drawString(score, scoreX, scoreY);
        }
      }
    }
  }
}

package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.ScoreHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class AssetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(AssetFactory.class);

  public static byte[] createCompetitionStartedCard(@NonNull Asset asset, @NonNull Game game, @NonNull Competition competition) {
    final int HEADLINE_SIZE = 18;
    final int SEPARATOR = 30;

    try {
      byte[] data = asset.getData();
      BufferedImage background = ImageIO.read(new ByteArrayInputStream(data));
      Graphics2D graphics = (Graphics2D) background.getGraphics();
      graphics.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setColor(Color.WHITE);

      String name = competition.getName();
      if (name.length() > 35) {
        name = name.substring(0, 34) + "...";
      }

      String table = game.getGameDisplayName();
      if(table.length() > 36) {
        table = table.substring(0, 35) + "...";
      }

      int yOffset = 6;
      int xOffset = 24;
      int imageY = 0;
      Font font = new Font("System", Font.BOLD, 38);
      graphics.setFont(font);
      graphics.drawString(name, xOffset, yOffset += 48);

      //TABLE
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("Table", xOffset, yOffset += 48);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(table, xOffset, yOffset += HEADLINE_SIZE + 12);
      imageY = yOffset;

      //START DATE
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("Start Date", xOffset, yOffset += SEPARATOR);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(DateUtil.formatDateTime(competition.getStartDate()), xOffset, yOffset += HEADLINE_SIZE + 12);

      //END DATE
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("End Date", xOffset, yOffset += SEPARATOR);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(DateUtil.formatDateTime(competition.getEndDate()), xOffset, yOffset += HEADLINE_SIZE + 12);

      //DURATION
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("Duration", xOffset, yOffset += SEPARATOR);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(DateUtil.formatDuration(competition.getStartDate(), competition.getEndDate()), xOffset, yOffset += HEADLINE_SIZE + 12);

      File wheelIconFile = game.getPinUPMedia(PopperScreen.Wheel);
      if(wheelIconFile != null && wheelIconFile.exists()) {
        BufferedImage image = ImageUtil.loadImage(wheelIconFile);
        BufferedImage resizedImage = ImageUtil.resizeImage(image, 190);
        graphics.drawImage(resizedImage, null, background.getWidth() - 200, imageY);
      }

      return ImageUtil.toBytes(background);
    } catch (Exception e) {
      LOG.error("Failed to get competition background " + e.getMessage(), e);
    }
    return null;
  }

  public static byte[] createCompetitionFinishedCard(Asset asset, @NonNull Game game, @NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary summary) {
    final int HEADLINE_SIZE = 18;
    final int SEPARATOR = 30;

    try {
      String winnerName = competition.getWinnerInitials();
      String winnerRaw = competition.getWinnerInitials();
      if (winner != null) {
        winnerName = winner.getName();
        winnerRaw = winner.getName();
        if (PlayerDomain.DISCORD.name().equals(winner.getDomain())) {
          winnerRaw = "<@" + winner.getId() + ">";
        }
      }

      String first = ScoreHelper.formatScoreEntry(summary, 1);
      String second = ScoreHelper.formatScoreEntry(summary, 1);
      String third = ScoreHelper.formatScoreEntry(summary, 2);


      byte[] data = asset.getData();
      BufferedImage background = ImageIO.read(new ByteArrayInputStream(data));
      Graphics2D graphics = (Graphics2D) background.getGraphics();
      graphics.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setColor(Color.WHITE);

      String name = competition.getName();
      if (name.length() > 35) {
        name = name.substring(0, 34) + "...";
      }

      String table = game.getGameDisplayName();
      if(table.length() > 36) {
        table = table.substring(0, 35) + "...";
      }

      int yOffset = 6;
      int xOffset = 24;
      int imageY = 0;
      Font font = new Font("System", Font.BOLD, 38);
      graphics.setFont(font);
      graphics.drawString(name, xOffset, yOffset += 48);

      //TABLE
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("Table", xOffset, yOffset += 48);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(winnerName, xOffset, yOffset += HEADLINE_SIZE + 12);
      imageY = yOffset;




      //1.
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("", xOffset, yOffset += SEPARATOR);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(first, xOffset, yOffset += HEADLINE_SIZE + 12);

      //2.
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("", xOffset, yOffset += SEPARATOR);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(second, xOffset, yOffset += HEADLINE_SIZE + 12);

      //3.
      font = new Font("System", Font.PLAIN, HEADLINE_SIZE);
      graphics.setFont(font);
      graphics.drawString("", xOffset, yOffset += SEPARATOR);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString(third, xOffset, yOffset += HEADLINE_SIZE + 12);


      File wheelIconFile = game.getPinUPMedia(PopperScreen.Wheel);
      if(wheelIconFile != null && wheelIconFile.exists()) {
        BufferedImage image = ImageUtil.loadImage(wheelIconFile);
        BufferedImage resizedImage = ImageUtil.resizeImage(image, 190);
        graphics.drawImage(resizedImage, null, background.getWidth() - 200, imageY);
      }

      return ImageUtil.toBytes(background);
    } catch (Exception e) {
      LOG.error("Failed to get competition background " + e.getMessage(), e);
    }
    return null;
  }
}

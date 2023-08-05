package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class AssetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(AssetFactory.class);

  static {
    try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, OverlayWindowFX.class.getResourceAsStream("digital_counter_7.ttf")));
    } catch (Exception e) {
      LOG.error("Error loading font: " + e.getMessage(), e);
    }
  }


  public static byte[] createSubscriptionCard(@NonNull Asset asset, @NonNull Game game, @NonNull Competition competition) {
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
      if (table.length() > 36) {
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

      GameMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        BufferedImage image = ImageUtil.loadImage(defaultMediaItem.getFile());
        BufferedImage resizedImage = ImageUtil.resizeImage(image, 190);
        graphics.drawImage(resizedImage, null, background.getWidth() - 200, imageY);
      }

      return ImageUtil.toBytes(background);
    } catch (Exception e) {
      LOG.error("Failed to get subscription background " + e.getMessage(), e);
    }
    return null;
  }

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
      if (table.length() > 36) {
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

      GameMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        BufferedImage image = ImageUtil.loadImage(defaultMediaItem.getFile());
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
    final int SEPARATOR = 10;
    final int IMAGE_WIDTH = 180;

    try {

      String winnerName = competition.getWinnerInitials();
      if (winner != null) {
        winnerName = winner.getName();
      }

      String first = "1. " + summary.getScores().get(0).getPlayerInitials() + "  " + summary.getScores().get(0).getScore();
      String second = "2. " + summary.getScores().get(1).getPlayerInitials() + "  " + summary.getScores().get(1).getScore();
      String third = "3. " + summary.getScores().get(2).getPlayerInitials() + "  " + summary.getScores().get(2).getScore();


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
      if (table.length() > 36) {
        table = table.substring(0, 35) + "...";
      }

      //wheel icon
      GameMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        BufferedImage image = ImageUtil.loadImage(defaultMediaItem.getFile());
        BufferedImage resizedImage = ImageUtil.resizeImage(image, IMAGE_WIDTH);
        graphics.drawImage(resizedImage, null, background.getWidth() - IMAGE_WIDTH, 0);
      }

      BufferedImage image = null;
      if (winner != null && !StringUtils.isEmpty(winner.getAvatarUrl())) {
        Image avatarFromUrl = CommonImageUtil.createAvatarFromUrl(winner.getAvatarUrl());
        image = SwingFXUtils.fromFXImage(avatarFromUrl, null);
        image = ImageUtil.resizeImage(image, IMAGE_WIDTH - 24);
      }
      else if (winner != null && winner.getAvatar() != null && winner.getAvatar().getData() != null) {
        Image avatarFromUrl = CommonImageUtil.createAvatarFromBytes(winner.getAvatar().getData());
        image = SwingFXUtils.fromFXImage(avatarFromUrl, null);
        image = ImageUtil.resizeImage(image, IMAGE_WIDTH - 24);
      }
      else {
        String initials = summary.getScores().get(0).getPlayerInitials();
        if (winner != null) {
          initials = winner.getInitials();
        }
        Image avatar = CommonImageUtil.createAvatar(initials);
        image = SwingFXUtils.fromFXImage(avatar, null);
        image = ImageUtil.resizeImage(image, IMAGE_WIDTH - 24);
      }
      graphics.drawImage(image, null, 12, 12);


      int yOffset = 0;
      Font font = new Font("System", Font.PLAIN, 20);
      graphics.setFont(font);
      int textWidth = graphics.getFontMetrics().stringWidth("Congratulations!");
      graphics.drawString("Congratulations!", background.getWidth() / 2 - textWidth / 2, yOffset += 20 + SEPARATOR);

      //Name
      int nameSize = 56;
      font = new Font("System", Font.BOLD, nameSize);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth(winnerName);
      while (textWidth > 400) {
        font = new Font("System", Font.BOLD, nameSize--);
        graphics.setFont(font);
        textWidth = graphics.getFontMetrics().stringWidth(winnerName);
      }
      graphics.drawString(winnerName, background.getWidth() / 2 - textWidth / 2, yOffset += nameSize + SEPARATOR - 8);

      //is the winner
      font = new Font("System", Font.PLAIN, 20);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth("is the winner of the");
      graphics.drawString("is the winner of the", background.getWidth() / 2 - textWidth / 2, yOffset += 20 + SEPARATOR + 8);

      //competitions name
      int competitionNameSize = 28;
      String cName = "\"" + competition.getName() + "\"";
      font = new Font("System", Font.BOLD, competitionNameSize);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth(cName);
      while (textWidth > 400) {
        font = new Font("System", Font.BOLD, competitionNameSize--);
        graphics.setFont(font);
        textWidth = graphics.getFontMetrics().stringWidth(cName);
      }
      graphics.drawString(cName, background.getWidth() / 2 - textWidth / 2, yOffset += competitionNameSize + SEPARATOR);

      //competition
      font = new Font("System", Font.PLAIN, 20);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth("competition!");
      graphics.drawString("competition!", background.getWidth() / 2 - textWidth / 2, yOffset += 20 + SEPARATOR);

      //1.
      font = new Font("Digital Counter 7", Font.PLAIN, 40);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth(first);
      graphics.drawString(first, background.getWidth() / 2 - textWidth / 2, yOffset += 40 + SEPARATOR);


      //2.
      font = new Font("Digital Counter 7", Font.PLAIN, 32);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth(second);
      graphics.drawString(second, background.getWidth() / 2 - textWidth / 2, yOffset += 32 + SEPARATOR);


      //3.
      font = new Font("Digital Counter 7", Font.PLAIN, 28);
      graphics.setFont(font);
      textWidth = graphics.getFontMetrics().stringWidth(third);
      graphics.drawString(third, background.getWidth() / 2 - textWidth / 2, yOffset += 28 + SEPARATOR);


      return ImageUtil.toBytes(background);
    } catch (Exception e) {
      LOG.error("Failed to get competition background " + e.getMessage(), e);
    }
    return null;
  }

  private static byte[] readImageUrl(String imageUrl) {
    try {
      URL url = new URL(imageUrl);
      ByteArrayOutputStream bis = new ByteArrayOutputStream();
      InputStream is = null;
      is = url.openStream();
      byte[] bytebuff = new byte[4096];
      int n;

      while ((n = is.read(bytebuff)) > 0) {
        bis.write(bytebuff, 0, n);
      }
      is.close();
      bis.close();

      return bis.toByteArray();
    } catch (Exception e) {
      LOG.error("Failed to read url " + imageUrl + ": " + e.getMessage());
    }
    return null;
  }
}

package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;
import static de.mephisto.vpin.commons.fx.ServerFX.client;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontSmall;

public class WidgetWeeklyCompetitionScoreItemController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

  private static int DEFAULT_AVATARSIZE = 60;
  private static int COMPACT_DEFAULT_AVATARSIZE = 40;

  @FXML
  private BorderPane root;

  @FXML
  private ImageView avatarImageView;

  @FXML
  private Label positionLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label scoreLabel;

  @FXML
  private Label statusLabel;

  @FXML
  private Label changeDateLabel;

  private boolean compactMode = false;

  // Add a public no-args constructor
  public WidgetWeeklyCompetitionScoreItemController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    positionLabel.managedProperty().bindBidirectional(positionLabel.visibleProperty());
    changeDateLabel.managedProperty().bindBidirectional(changeDateLabel.visibleProperty());
    scoreLabel.managedProperty().bindBidirectional(scoreLabel.visibleProperty());
    statusLabel.managedProperty().bindBidirectional(statusLabel.visibleProperty());
    statusLabel.setVisible(false);
    Font scoreFont = getScoreFont();
    scoreLabel.setFont(scoreFont);
  }

  public void setData(CompetitionScore score) {
    if (compactMode) {
      root.setPadding(new Insets(6, 6, 6, 6));
      Font posFont = Font.font("System", FontPosture.findByName("regular"), 16);
      positionLabel.setFont(posFont);
    }

    JFXFuture.supplyAsync(() -> {
      Image image = new Image(client.getCachedUrlImage(score.getAvatarUrl()));
      BufferedImage avatarImage = SwingFXUtils.fromFXImage(image, null);
      if (avatarImage.getWidth() == 40) {
        image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
        avatarImage = SwingFXUtils.fromFXImage(image, null);
      }
      avatarImage = ImageUtil.crop(avatarImage, (avatarImage.getWidth() - avatarImage.getHeight()) / 2, 0, avatarImage.getHeight(), avatarImage.getHeight());
      return SwingFXUtils.toFXImage(avatarImage, null);
    }).thenAcceptLater((image) -> {
      javafx.scene.shape.Rectangle clip = new Rectangle();
      clip.setWidth(DEFAULT_AVATARSIZE);
      clip.setHeight(DEFAULT_AVATARSIZE);

      clip.setArcHeight(DEFAULT_AVATARSIZE);
      clip.setArcWidth(DEFAULT_AVATARSIZE);
      clip.setStroke(javafx.scene.paint.Color.WHITE);
      clip.setStrokeWidth(0);

      avatarImageView.setPreserveRatio(true);
      avatarImageView.setSmooth(true);
      avatarImageView.setFitWidth(DEFAULT_AVATARSIZE);
      avatarImageView.setImage(image);
      avatarImageView.setClip(clip);

      if (score.isMyScore()) {
        if (compactMode) {
          root.setStyle("-fx-border-style: solid solid solid solid;\n" +
              "    -fx-border-color: transparent;\n" +
              "    -fx-border-width: 1 1 1 6;");

          Font posFont = Font.font("System", FontWeight.BOLD, FontPosture.findByName("bold"), 16);
          positionLabel.setFont(posFont);
        }
        else {
          root.setStyle("-fx-border-style: solid solid solid solid;\n" +
              "    -fx-border-color: #333366;\n" +
              "    -fx-border-width: 1 1 1 6;");
        }
      }
      else {
        root.setStyle("-fx-border-style: solid solid solid solid;\n" +
            "    -fx-border-color: transparent;\n" +
            "    -fx-border-width: 1 1 1 6;");
      }
    });

    if (score.getRank() > 0) {
      positionLabel.setText("#" + score.getRank());
    }
    else {
      positionLabel.setVisible(false);
    }
    nameLabel.setText(score.getParticipantName());

    if (score.getScore() > 0) {
      long l = new Double(score.getScore()).longValue();
      scoreLabel.setText(ScoreFormatUtil.formatScore(l, Locale.getDefault()));
    }
    else {
      scoreLabel.setVisible(false);
    }

    changeDateLabel.setText(score.getLeague() != null ? score.getLeague() : "");
    changeDateLabel.setVisible(!compactMode);

    if (score.isPending()) {
      statusLabel.setVisible(true);
      String tt = "Score has not been approved yet.";
      if (!StringUtils.isEmpty(score.getNote())) {
        tt = tt + "\n\n\"" + score.getNote() + "\"";
      }
      statusLabel.setTooltip(new Tooltip(tt));
    }

    JFXFuture.supplyAsync(() -> {
      try {
        BufferedImage bufferedImage = getFlagBackground(score);
        if (bufferedImage == null) {
          return null;
        }
        Image backgroundImage;
        Color end = new Color(0f, 0f, 0f, .1f);
        Color start = Color.decode("#111111");
        ImageUtil.gradient(bufferedImage, 200, 200, start, end);
        ImageUtil.gradient(bufferedImage, 200, 200, end, start);

        backgroundImage = SwingFXUtils.toFXImage(bufferedImage, null);
        BackgroundImage myBI = new BackgroundImage(backgroundImage,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            BackgroundSize.DEFAULT);
        return myBI;
      }
      catch (Exception e) {
        LOG.error("Failed to load flag file: {}", e.getMessage(), e);
      }
      return null;
    }).thenAcceptLater((image) -> {
      if (image != null) {
        root.setBackground(new Background(image));
      }
    });
  }

  @Nullable
  private static BufferedImage getFlagBackground(CompetitionScore score) throws MalformedURLException, FileNotFoundException {
    URL url = new URL(score.getFlagUrl());
    String flagFileName = FilenameUtils.getName(url.getFile());
    File flagsFolder = new File(RESOURCES, "flags/");
    if (!flagsFolder.exists() && !flagsFolder.mkdirs()) {
      LOG.error("Failed to create flags folder");
      return null;
    }
    File flagFile = new File(flagsFolder, flagFileName);
    if (!flagFile.exists()) {
      Updater.download(score.getFlagUrl(), flagFile, true);
    }
    Image backgroundImage = new Image(new FileInputStream(flagFile));
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(backgroundImage, null);
    if (backgroundImage == null && flagFile.delete()) {
      return getFlagBackground(score);
    }
    return bufferedImage;
  }

  public void setCompact() {
    compactMode = true;
    DEFAULT_AVATARSIZE = COMPACT_DEFAULT_AVATARSIZE;
    Font scoreFont = getScoreFontSmall();
    scoreLabel.setFont(scoreFont);
  }
}
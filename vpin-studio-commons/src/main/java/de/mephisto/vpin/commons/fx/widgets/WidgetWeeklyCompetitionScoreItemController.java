package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.ServerFX.client;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontSmall;

public class WidgetWeeklyCompetitionScoreItemController extends WidgetController implements Initializable {
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

  private final static int DEFAULT_AVATARSIZE = 60;

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
  private Label changeDateLabel;

  // Add a public no-args constructor
  public WidgetWeeklyCompetitionScoreItemController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(CompetitionScore score) {
    JFXFuture.supplyAsync(() -> {
      Image image = new Image(client.getCachedUrlImage(score.getAvatarUrl()));
      BufferedImage avatarImage = SwingFXUtils.fromFXImage(image, null);
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
    });

    positionLabel.setText("#" + score.getRank());
    nameLabel.setText(score.getParticipantName());

    Font scoreFont = getScoreFont();
    scoreLabel.setFont(scoreFont);

    long l = new Double(score.getScore()).longValue();
    scoreLabel.setText(ScoreFormatUtil.formatScore(l, Locale.getDefault()));
    changeDateLabel.setText(score.getLeague() != null ? score.getLeague() : "");

    JFXFuture.supplyAsync(() -> {
      Image backgroundImage = new Image(client.getCachedUrlImage(score.getAvatarUrl()));
      BufferedImage bufferedImage = SwingFXUtils.fromFXImage(backgroundImage, null);
      Color end = new Color(0f, 0f, 0f, .1f);
      Color start = Color.decode("#111111");
      ImageUtil.gradient(bufferedImage, 200, 200, start, end);
      ImageUtil.gradient(bufferedImage, 200, 200, end, start);

      backgroundImage = SwingFXUtils.toFXImage(bufferedImage, null);
      BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
          BackgroundSize.DEFAULT);
      return myBI;
    }).thenAcceptLater((image) -> {
      root.setBackground(new Background(image));
    });
  }
}
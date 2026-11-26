package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.ui.Studio.client;

public class WidgetPlayerScoreController extends WidgetController implements Initializable {

  private static final String[] RANK_CLASSES = {"sm-pill-rank-1", "sm-pill-rank-2", "sm-pill-rank-3"};

  @FXML
  private StackPane rootStack;

  @FXML
  private StackPane root;

  @FXML
  private ImageView wheelImageView;

  @FXML
  private Label tableLabel;

  @FXML
  private Label metaLabel;

  @FXML
  private Label positionLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label separatorLabel;

  @FXML
  private Label scoreLabel;

  @FXML
  private Label changeDateLabel;

  // Add a public no-args constructor
  public WidgetPlayerScoreController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // the background image is set on the inner pane, inside the 1px border of the tile
    roundCorners(root, TILE_RADIUS - 1);
    scoreLabel.setFont(getScoreFont());
    metaLabel.managedProperty().bind(metaLabel.visibleProperty());
    metaLabel.setVisible(false);
  }

  public void setData(GameRepresentation game, ScoreRepresentation score) {
    tableLabel.setText(game.getGameDisplayName());
    setRank(score.getPosition());
    nameLabel.setText(score.getPlayer() != null ? score.getPlayer().getName() : "");
    scoreLabel.setText(score.getFormattedScore());
    setDate(score.getCreatedAt());

    JFXFuture.supplyAsync(() -> {
      FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());
      return frontendMedia.getDefaultMediaItem(VPinScreen.Wheel) != null;
    }).thenAcceptLater(hasWheel -> {
      if (hasWheel) {
        JFXFuture.supplyAsync(() -> ServerFX.client.getWheelIcon(game.getId(), true))
            .thenAcceptLater(byteStream -> wheelImageView.setImage(new Image(byteStream)));
      }
      else {
        setBlankWheel();
      }
    });

    JFXFuture.supplyAsync(() -> client.getVpsService().getTableById(game.getExtTableId()))
        .thenAcceptLater(this::setMeta);

    JFXFuture.supplyAsync(() -> ServerFX.client.getCompetitionService().getCompetitionBackground(game.getId()))
        .thenAcceptLater(byteStream -> setBackgroundImage(new Image(byteStream)));
  }

  public void setData(@Nullable GameRepresentation game, VpsTable vpsTable, int position, TableScoreDetails tableScore) {
    if (game == null) {
      setBlankWheel();
    }
    else {
      FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());
      FrontendMediaItemRepresentation item = frontendMedia.getDefaultMediaItem(VPinScreen.Wheel);
      if (item != null) {
        ByteArrayInputStream gameMediaItem = ServerFX.client.getWheelIcon(game.getId(), true);
        wheelImageView.setImage(new Image(gameMediaItem));
      }
      else {
        setBlankWheel();
      }
    }

    if (game != null) {
      tableLabel.setText(game.getGameDisplayName());
    }
    else if (vpsTable != null) {
      tableLabel.setText(vpsTable.getName());
    }
    else {
      tableLabel.setText(Messages.get("players.widget_highscore.table_not_resolved"));
    }
    setMeta(vpsTable);

    setRank(position);
    nameLabel.setText(tableScore.getDisplayName() + " [" + tableScore.getInitials() + "]");
    scoreLabel.setText(tableScore.getScoreText());
    setDate(tableScore.getCreationDate() != null ? tableScore.getCreationDate().toInstant() : null);

    if (game != null) {
      setBackgroundImage(new Image(ServerFX.client.getCompetitionService().getCompetitionBackground(game.getId())));
    }
  }

  private void setBlankWheel() {
    wheelImageView.setImage(new Image(Studio.class.getResourceAsStream("avatar-blank.png")));
  }

  private void setBackgroundImage(Image image) {
    // cover the whole tile, independent of the image size
    BackgroundSize cover = new BackgroundSize(100, 100, true, true, false, true);
    BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, cover);
    root.setBackground(new Background(bi));
  }

  /**
   * "Manufacturer · Year" next to the table name, hidden when the table is not linked to VPS.
   */
  private void setMeta(@Nullable VpsTable vpsTable) {
    List<String> parts = new ArrayList<>();
    if (vpsTable != null) {
      if (!StringUtils.isBlank(vpsTable.getManufacturer())) {
        parts.add(vpsTable.getManufacturer());
      }
      if (vpsTable.getYear() != null && vpsTable.getYear() > 0) {
        parts.add(String.valueOf(vpsTable.getYear()));
      }
    }
    metaLabel.setText(String.join(" · ", parts));
    metaLabel.setVisible(!parts.isEmpty());
  }

  private void setRank(int position) {
    positionLabel.setText("#" + position);
    positionLabel.getStyleClass().removeAll(RANK_CLASSES);
    positionLabel.getStyleClass().remove("sm-pill-unknown");
    positionLabel.getStyleClass().add(position >= 1 && position <= RANK_CLASSES.length ? RANK_CLASSES[position - 1] : "sm-pill-unknown");
  }

  private void setDate(@Nullable Instant instant) {
    boolean visible = instant != null;
    separatorLabel.setVisible(visible);
    changeDateLabel.setVisible(visible);
    if (instant == null) {
      return;
    }
    ZonedDateTime date = instant.atZone(ZoneId.systemDefault());
    changeDateLabel.setText(formatRelative(date));
    changeDateLabel.setTooltip(new Tooltip(Messages.get("msg.updated", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(date))));
  }

  /**
   * "just now", "5 min ago", "3 h ago", "yesterday", "3 days ago", the date for anything older than 30 days.
   */
  static String formatRelative(ZonedDateTime date) {
    ZonedDateTime now = ZonedDateTime.now(date.getZone());
    long minutes = Duration.between(date, now).toMinutes();
    if (minutes < 1) {
      return Messages.get("players.widget_highscore.just_now");
    }
    if (minutes < 60) {
      return Messages.get("players.widget_highscore.minutes_ago", minutes);
    }
    if (minutes < 24 * 60) {
      return Messages.get("players.widget_highscore.hours_ago", minutes / 60);
    }
    long days = ChronoUnit.DAYS.between(date.toLocalDate(), LocalDate.now(date.getZone()));
    if (days <= 1) {
      return Messages.get("players.widget_highscore.yesterday");
    }
    if (days <= 30) {
      return Messages.get("players.widget_highscore.days_ago", days);
    }
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date);
  }
}

package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class WidgetPlayerScoreController extends WidgetController implements Initializable {

  @FXML
  private StackPane rootStack;

  @FXML
  private ImageView wheelImageView;

  @FXML
  private Label tableLabel;

  @FXML
  private Label positionLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label scoreLabel;

  @FXML
  private Label changeDateLabel;

  // Add a public no-args constructor
  public WidgetPlayerScoreController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(GameRepresentation game, ScoreRepresentation score) {
    JFXFuture.supplyAsync(() -> {
      FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());
      return frontendMedia.getDefaultMediaItem(VPinScreen.Wheel);
    }).thenAcceptLater(item -> {
      if (item != null) {
        ByteArrayInputStream gameMediaItem = ServerFX.client.getWheelIcon(game.getId(), true);
        Image image = new Image(gameMediaItem);
        wheelImageView.setImage(image);
      }
      else {
        Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        wheelImageView.setImage(wheel);
      }

      tableLabel.setText(game.getGameDisplayName());

      positionLabel.setText("#" + score.getPosition());
      nameLabel.setText(score.getPlayer().getName());

      scoreLabel.setFont(getScoreFont());
      scoreLabel.setText(score.getFormattedScore());

      String date = DateFormat.getDateTimeInstance().format(score.getCreatedAt());
      changeDateLabel.setText("Updated: " + date);

      Image backgroundImage = new Image(ServerFX.client.getCompetitionBackground(game.getId()));
      BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
      rootStack.setBackground(new Background(myBI));
    });
  }

  public void setData(@Nullable GameRepresentation game, VpsTable vpsTable, int position, TableScoreDetails tableScore) {
    if (game == null) {
      Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
      wheelImageView.setImage(wheel);
    }
    else {
      FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());
      FrontendMediaItemRepresentation item = frontendMedia.getDefaultMediaItem(VPinScreen.Wheel);
      if (item != null) {
        ByteArrayInputStream gameMediaItem = ServerFX.client.getWheelIcon(game.getId(), true);
        Image image = new Image(gameMediaItem);
        wheelImageView.setImage(image);
      }
      else {
        Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        wheelImageView.setImage(wheel);
      }
    }

    if (game != null) {
      tableLabel.setText(game.getGameDisplayName());
    }
    else if (vpsTable != null) {
      tableLabel.setText(vpsTable.getName());
    }
    else {
      tableLabel.setText("- Table not resolved - ");
    }

    positionLabel.setText("#" + position);
    nameLabel.setText(tableScore.getDisplayName() + " [" + tableScore.getInitials() + "]");

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(tableScore.getScoreText());

    String date = DateFormat.getDateTimeInstance().format(tableScore.getCreationDate());
    changeDateLabel.setText("Updated: " + date);

    if (game != null) {
      Image backgroundImage = new Image(ServerFX.client.getCompetitionBackground(game.getId()));
      BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
      rootStack.setBackground(new Background(myBI));
    }
  }
}
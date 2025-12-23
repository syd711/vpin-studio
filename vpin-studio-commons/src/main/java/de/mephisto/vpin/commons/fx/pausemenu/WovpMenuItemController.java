package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.wovp.models.WovpPlayer;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.restclient.wovp.ScoreSubmitResult;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.fx.ServerFX.client;

public class WovpMenuItemController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private ImageView sectionIcon;

  @FXML
  private Label nameLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Label authorsLabel;

  @FXML
  private Label errorMsg;

  @FXML
  private VBox errorContainer;

  @FXML
  private Label playerScoreLabel;

  @FXML
  private Label playerNameLabel;

  @FXML
  private Button submitBtn;

  @FXML
  private Button playerBtn;

  @FXML
  private Button rightBtn;

  @FXML
  private Button leftBtn;

  @FXML
  private VBox loadingIndicator;

  @FXML
  private BorderPane widgetPane;

  @FXML
  private ImageView screenshotView;

  private static List<WovpPlayer> players;
  private WovpPlayer wovpPlayer;
  private int playerSelectionIndex = 0;
  private static Image screenshotImage;

  public void setData(GameRepresentation game, VpsTable tableById, Image sectionImage) {
    this.nameLabel.setText(game.getGameDisplayName());
    this.versionLabel.setVisible(false);
    this.authorsLabel.setVisible(false);

    this.playerBtn.setVisible(false);
    this.sectionIcon.setImage(sectionImage);

    // when game is mapped to VPS Table
    if (tableById != null) {
      String extVersion = game.getExtTableVersionId();
      VpsTableVersion version = tableById.getTableVersionById(extVersion);
      if (version != null) {
        this.versionLabel.setText(version.getComment());
        List<String> authors = version.getAuthors();
        if (authors != null && !authors.isEmpty()) {
          this.authorsLabel.setText(String.join(", ", authors));
          this.authorsLabel.setVisible(true);
        }
      }
      else {
        this.versionLabel.setText(tableById.getManufacturer() + " (" + tableById.getYear() + ")");
        this.versionLabel.setVisible(true);
        List<String> designers = tableById.getDesigners();
        if (designers != null && !designers.isEmpty()) {
          this.authorsLabel.setText(String.join(", ", designers));
        }
      }
    }

    loadingIndicator.setVisible(true);

    JFXFuture.supplyAsync(() -> {
      if (players == null) {
        players = client.getWovpService().getPlayers();
      }
      return players;
    }).thenAcceptLater(players -> {
      if (this.wovpPlayer == null) {
        this.wovpPlayer = players.get(0);
      }
      this.playerBtn.setVisible(players.size() > 1);
      this.rightBtn.setVisible(players.size() > 1);

      if (this.playerBtn.isVisible()) {
        activate(this.playerBtn, this.submitBtn);
        activate(this.leftBtn, this.submitBtn);
        activate(this.rightBtn, this.submitBtn);
      }
      refreshViewForPlayer();
    });
  }

  private void activate(Button b1, Button b2) {
    b1.getStyleClass().remove("default-button");
    b1.getStyleClass().add("submit-button");

    b2.getStyleClass().removeAll("submit-button");
    b2.getStyleClass().add("default-button");
  }

  private void refreshViewForPlayer() {
    submitBtn.setDisable(true);
    loadingIndicator.setVisible(true);
    playerNameLabel.setText(this.wovpPlayer.getName());
    playerScoreLabel.setText("-");
    errorContainer.setVisible(false);
    errorMsg.setText("");

    playerBtn.setText((playerSelectionIndex + 1) + ". Player: " + wovpPlayer.getName());

    JFXFuture.supplyAsync(() -> {
      ScoreSubmitResult scoreSubmitResult = client.getCompetitionService().submitScore(wovpPlayer, true);
      return scoreSubmitResult;
    }).thenAcceptLater(result -> {
      loadingIndicator.setVisible(false);
      InputStream screenshot = client.getScreenshot();
      if (screenshotImage == null && screenshot != null) {
        screenshotImage = new Image(screenshot);
      }
      screenshotView.setImage(screenshotImage);

      submitBtn.setDisable(false);
      playerNameLabel.setText(wovpPlayer.getName());
      playerScoreLabel.setText(result.getLatestScore() > 0 ? ScoreFormatUtil.formatScore(result.getLatestScore(), Locale.getDefault()) : "-");
      submitBtn.setVisible(result.getErrorMessage() == null);

      if (result.getErrorMessage() != null) {
        errorContainer.setVisible(true);
        errorMsg.setText(result.getErrorMessage());
      }
    });
  }

  public boolean right() {
    if (submitBtn.isDisabled() || !submitBtn.isVisible() || players == null || players.size() <= 1) {
      return false;
    }
    if ((playerSelectionIndex + 1) < players.size()) {
      playerSelectionIndex++;
      leftBtn.setVisible(true);
      rightBtn.setVisible(playerSelectionIndex + 1 != players.size());
      wovpPlayer = players.get(playerSelectionIndex);
      refreshViewForPlayer();
      return true;
    }

    rightBtn.setVisible(false);
    return false;
  }

  public boolean left() {
    if (submitBtn.isDisabled() || !submitBtn.isVisible() || players == null || players.size() <= 1) {
      return false;
    }
    if (playerSelectionIndex > 0) {
      playerSelectionIndex--;
      rightBtn.setVisible(true);
      leftBtn.setVisible(playerSelectionIndex != 0);
      wovpPlayer = players.get(playerSelectionIndex);
      refreshViewForPlayer();
      return true;
    }

    leftBtn.setVisible(false);
    return false;
  }

  public void enter() {
    if (submitBtn.isDisabled() || !submitBtn.isVisible()) {
      return;
    }

    if (isActive(playerBtn)) {
      activate(this.submitBtn, this.playerBtn);
      activate(this.submitBtn, this.rightBtn);
      activate(this.submitBtn, this.leftBtn);
      return;
    }

    if (!isActive(submitBtn)) {
      return;
    }

    Transition blink = TransitionUtil.createBlink(submitBtn);
    blink.play();

    submitBtn.setText("Sending Highscores...");
    submitBtn.setDisable(true);
    JFXFuture.supplyAsync(() -> {
      return client.getCompetitionService().submitScore(wovpPlayer, false);
    }).thenAcceptLater((result) -> {
      blink.stop();
      submitBtn.setVisible(true);
      if (result.getErrorMessage() != null) {
        rightBtn.setVisible(false);
        leftBtn.setVisible(false);
        submitBtn.setVisible(false);
        playerBtn.setVisible(false);
        errorContainer.setVisible(true);
        errorMsg.setText(result.getErrorMessage());
      }
      else {
        submitBtn.setText("Your highscore has been submitted.");
      }
    });
  }

  private boolean isActive(Button submitBtn) {
    return submitBtn.getStyleClass().contains("submit-button");
  }

  public void reset() {
    screenshotImage = null;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    playerScoreLabel.setFont(WidgetFactory.getScoreFont());

    leftBtn.setVisible(false);
    rightBtn.setVisible(false);

    playerBtn.managedProperty().bindBidirectional(playerBtn.visibleProperty());
    loadingIndicator.managedProperty().bindBidirectional(loadingIndicator.visibleProperty());
    authorsLabel.managedProperty().bindBidirectional(authorsLabel.visibleProperty());
    versionLabel.managedProperty().bindBidirectional(versionLabel.visibleProperty());

    errorContainer.setVisible(false);
    playerBtn.setVisible(false);
  }
}

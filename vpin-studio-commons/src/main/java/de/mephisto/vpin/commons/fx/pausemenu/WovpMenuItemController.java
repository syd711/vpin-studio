package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
  private Label scoreInfoLabel;

  @FXML
  private Label errorMsg;

  @FXML
  private VBox errorContainer;

  @FXML
  private Label infoLabel;

  @FXML
  private Label playerScoreLabel;

  @FXML
  private Label playerNameLabel;

  @FXML
  private Button submitBtn;

  @FXML
  private VBox loadingIndicator;

  @FXML
  private BorderPane widgetPane;

  @FXML
  private ImageView screenshotView;
  private static Image screenshotImage;

  public void setData(GameRepresentation game, VpsTable tableById, Image sectionImage) {
    this.nameLabel.setText(game.getGameDisplayName());
    this.versionLabel.setVisible(false);
    this.authorsLabel.setVisible(false);
    this.scoreInfoLabel.setText("");

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
      ScoreSubmitResult scoreSubmitResult = client.getCompetitionService().submitScore(true);
      return scoreSubmitResult;
    }).thenAcceptLater(result -> {
      loadingIndicator.setVisible(false);
      InputStream screenshot = client.getScreenshot();
      if (screenshotImage == null && screenshot != null) {
        screenshotImage = new Image(screenshot);
      }
      screenshotView.setImage(screenshotImage);

      playerNameLabel.setText(result.getPlayerName() != null ? result.getPlayerName() : "-");
      playerScoreLabel.setText(result.getLatestScore() > 0 ? ScoreFormatUtil.formatScore(result.getLatestScore(), Locale.getDefault()) : "-");
      submitBtn.setVisible(result.getErrorMessage() == null);

      if (result.getErrorMessage() != null) {
        errorContainer.setVisible(true);
        errorMsg.setText(result.getErrorMessage());
      }
    });
  }

  public void enter() {
    if (submitBtn.isDisabled() || !submitBtn.isVisible()) {
      return;
    }

    Transition blink = TransitionUtil.createBlink(submitBtn);
    blink.play();

    submitBtn.setText("Sending Highscores...");
    submitBtn.setDisable(true);
    JFXFuture.supplyAsync(() -> {
      return client.getCompetitionService().submitScore(false);
    }).thenAcceptLater((result) -> {
      blink.stop();
      submitBtn.setVisible(true);
      if (result.getErrorMessage() != null) {
        submitBtn.setVisible(false);
        errorMsg.setText(result.getErrorMessage());
      }
      else {
        submitBtn.setText("Your highscore has been submitted.");
      }
    });
  }

  public void reset() {
    screenshotImage = null;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    playerScoreLabel.setFont(WidgetFactory.getScoreFont());

    loadingIndicator.managedProperty().bindBidirectional(loadingIndicator.visibleProperty());
    authorsLabel.managedProperty().bindBidirectional(authorsLabel.visibleProperty());
    versionLabel.managedProperty().bindBidirectional(versionLabel.visibleProperty());

    errorContainer.setVisible(false);
  }
}

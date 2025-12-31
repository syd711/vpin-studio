package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.widgets.WidgetWeeklyCompetitionScoreItemController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.wovp.models.WovpPlayer;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.wovp.ScoreSubmitResult;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
  private Pane playerSelectorBox;

  @FXML
  private Pane scoresBox;

  @FXML
  private Pane scoresLoader;

  @FXML
  private ImageView screenshotView;

  private static List<WovpPlayer> players;
  private WovpPlayer wovpPlayer;
  private int playerSelectionIndex = 0;
  private static Image screenshotImage;
  private PauseMenuItem pauseMenuItem;

  public void setData(GameRepresentation game, PauseMenuItem pauseMenuItem, VpsTable tableById, Image sectionImage) {
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
      this.playerSelectorBox.setVisible(players.size() > 1);
      this.rightBtn.setVisible(players.size() > 1);

      if (this.playerSelectorBox.isVisible()) {
        activate(this.playerBtn, this.submitBtn);
        activate(this.leftBtn, this.submitBtn);
        activate(this.rightBtn, this.submitBtn);
      }

      refreshScores(pauseMenuItem);
      refreshViewForPlayer();
    });
  }

  private void refreshScores(PauseMenuItem pauseMenuItem) {
    this.pauseMenuItem = pauseMenuItem;
    scoresLoader.setVisible(true);
    JFXFuture.supplyAsync(() -> {
      List<Pane> children = new ArrayList<>();
      try {
        CompetitionRepresentation competition = pauseMenuItem.getCompetition();
        List<CompetitionScore> weeklyCompetitionScores = client.getCompetitionService().getWeeklyCompetitionScores(competition.getUuid());
        Optional<CompetitionScore> myScore = weeklyCompetitionScores.stream().filter(s -> s.isMyScore()).findFirst();
        int myScoreIndex = -1;
        if (myScore.isPresent()) {
          myScoreIndex = weeklyCompetitionScores.indexOf(myScore.get());
        }

        for (CompetitionScore score : weeklyCompetitionScores) {
          Pane row = createScoreItem(score);
          children.add(row);

          if (children.size() == 2 && myScoreIndex > children.size()) {
            children.add(getPlaceholder());
            if (myScoreIndex - 1 != 2) {
              children.add(createScoreItem(weeklyCompetitionScores.get(myScoreIndex - 1)));
            }
            children.add(createScoreItem(weeklyCompetitionScores.get(myScoreIndex)));
            break;
          }

          if (children.size() > 4) {
            break;
          }
        }
      }
      catch (IOException e) {
        LOG.error("Failed to load competition score panel: {}", e.getMessage(), e);
      }
      return children;
    }).thenAcceptLater(children -> {
      scoresBox.getChildren().removeAll(scoresBox.getChildren());
      scoresBox.getChildren().addAll(children);
      scoresBox.setVisible(!children.isEmpty());
      scoresLoader.setVisible(false);
    });
  }

  @NotNull
  private static BorderPane createScoreItem(CompetitionScore score) throws IOException {
    FXMLLoader loader = new FXMLLoader(WidgetWeeklyCompetitionScoreItemController.class.getResource("widget-weekly-competition-score-item.fxml"));
    BorderPane row = loader.load();
    WidgetWeeklyCompetitionScoreItemController controller = loader.getController();
    row.setMaxWidth(Double.MAX_VALUE);
    row.setMaxHeight(100);
    controller.setCompact();
    controller.setData(score);
    return row;
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

//      refreshScores(pauseMenuItem);
    });
  }

  private boolean isActive(Button submitBtn) {
    return submitBtn.getStyleClass().contains("submit-button");
  }

  public void reset() {
    screenshotImage = null;
  }

  private Pane getPlaceholder() {
    Label label1 = new Label(".  .");
    label1.setWrapText(true);
    label1.setMaxWidth(3);
    label1.getStyleClass().add("default-title");

    VBox ph = new VBox();
    ph.setAlignment(Pos.CENTER);
    ph.getChildren().addAll(label1);
    ph.setMinHeight(100);
    ph.setMaxHeight(100);
    return ph;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    leftBtn.setVisible(false);
    rightBtn.setVisible(false);
    scoresLoader.setVisible(true);

    playerSelectorBox.managedProperty().bindBidirectional(playerSelectorBox.visibleProperty());
    playerBtn.managedProperty().bindBidirectional(playerBtn.visibleProperty());
    loadingIndicator.managedProperty().bindBidirectional(loadingIndicator.visibleProperty());
    authorsLabel.managedProperty().bindBidirectional(authorsLabel.visibleProperty());
    versionLabel.managedProperty().bindBidirectional(versionLabel.visibleProperty());

    errorContainer.setVisible(false);
    playerBtn.setVisible(false);
  }
}

package de.mephisto.vpin.ui.achievements;

import de.mephisto.vpin.ui.util.ThemeColors;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.AchievementsMigrationRequest;
import de.mephisto.vpin.restclient.achievements.AchievementsMigrationResult;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * Wizard for the one-time migration of VPX Achievement Watcher data ("claim once, then read").
 */
public class AchievementsMigrationDialog implements Initializable, DialogController {

  private static final String FONT_STYLE = "-fx-font-size: 14px;";

  @FXML
  private BorderPane root;

  private final ComboBox<PlayerRepresentation> playerCombo = new ComboBox<>();
  private Set<Long> linkedPlayerIds = Set.of();
  private final RadioButton firebaseRadio = new RadioButton(Messages.get("achievements.migration.source_cloud"));
  private final RadioButton localRadio = new RadioButton(Messages.get("achievements.migration.source_local"));
  private final TextField pidField = new TextField();
  private final PasswordField keyField = new PasswordField();
  private final TextField folderField = new TextField();
  private final Button previewBtn = new Button(Messages.get("achievements.migration.preview"));
  private final Button importBtn = new Button(Messages.get("achievements.migration.import"));
  private final VBox resultBox = new VBox(4);

  public static void open() {
    Stage stage = Dialogs.createStudioDialogStage(AchievementsMigrationDialog.class, "dialog-achievements-migration.fxml",
        Messages.get("achievements.migration.title"));
    stage.showAndWait();
  }

  public AchievementsMigrationDialog() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    VBox content = new VBox(10);
    content.setPadding(new Insets(16));
    content.setStyle(FONT_STYLE);
    content.setPrefWidth(640);

    Label intro = muted(Messages.get("achievements.migration.intro"));
    intro.setWrapText(true);
    content.getChildren().add(intro);

    playerCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(PlayerRepresentation p) {
        return p == null ? "" : p.getName() + (p.getInitials() != null ? " (" + p.getInitials() + ")" : "")
            + (!linkedPlayerIds.contains(p.getId()) ? " \u2013 " + Messages.get("pref.achievements.not_linked") : "");
      }

      @Override
      public PlayerRepresentation fromString(String s) {
        return null;
      }
    });
    playerCombo.setMaxWidth(Double.MAX_VALUE);

    ToggleGroup sources = new ToggleGroup();
    firebaseRadio.setToggleGroup(sources);
    localRadio.setToggleGroup(sources);
    firebaseRadio.setSelected(true);
    firebaseRadio.setStyle(FONT_STYLE + " -fx-text-fill: " + ThemeColors.WHITE + ";");
    localRadio.setStyle(FONT_STYLE + " -fx-text-fill: " + ThemeColors.WHITE + ";");
    sources.selectedToggleProperty().addListener((obs, o, n) -> updateFields());

    pidField.setPromptText("e.g. ab12");
    keyField.setPromptText(Messages.get("achievements.migration.key_prompt"));
    folderField.setPromptText("C:\\vPinball\\VPX Achievement Watcher");
    Button browse = new Button("...");
    browse.setOnAction(e -> {
      DirectoryChooser chooser = new DirectoryChooser();
      File current = new File(folderField.getText());
      if (current.exists()) {
        chooser.setInitialDirectory(current);
      }
      File dir = chooser.showDialog(getStage());
      if (dir != null) {
        folderField.setText(dir.getAbsolutePath());
      }
    });
    HBox folderRow = new HBox(6, folderField, browse);
    HBox.setHgrow(folderField, Priority.ALWAYS);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);
    grid.add(text(Messages.get("achievements.player")), 0, 0);
    grid.add(playerCombo, 1, 0);
    grid.add(text(Messages.get("achievements.migration.source")), 0, 1);
    grid.add(new VBox(4, firebaseRadio, localRadio), 1, 1);
    grid.add(text(Messages.get("achievements.migration.pid")), 0, 2);
    grid.add(pidField, 1, 2);
    grid.add(text(Messages.get("achievements.migration.key")), 0, 3);
    grid.add(keyField, 1, 3);
    grid.add(text(Messages.get("achievements.migration.folder")), 0, 4);
    grid.add(folderRow, 1, 4);
    GridPane.setHgrow(playerCombo, Priority.ALWAYS);
    content.getChildren().add(grid);

    Label folderHint = muted(Messages.get("achievements.migration.folder_hint"));
    folderHint.setWrapText(true);
    Label warning = colored(Messages.get("achievements.migration.warning"), ORANGE, 14, false);
    warning.setWrapText(true);
    content.getChildren().addAll(folderHint, warning);

    resultBox.setPadding(new Insets(10));
    resultBox.setStyle("-fx-background-color: " + CARD + ";");
    resultBox.setVisible(false);
    resultBox.setManaged(false);
    content.getChildren().add(resultBox);

    Button close = new Button(Messages.get("achievements.migration.close"));
    close.getStyleClass().add("default-button");
    close.setOnAction(e -> getStage().close());
    previewBtn.getStyleClass().add("default-button");
    importBtn.getStyleClass().add("submit-button");
    previewBtn.setOnAction(e -> run(true));
    importBtn.setOnAction(e -> run(false));
    importBtn.setDisable(true);
    HBox buttons = new HBox(8, spacer(), previewBtn, importBtn, close);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    content.getChildren().add(buttons);

    for (Control c : new Control[]{playerCombo, pidField, keyField, folderField, browse, previewBtn, importBtn, close}) {
      c.setStyle(FONT_STYLE);
    }

    root.setCenter(content);

    updateFields();
    JFXFuture.supplyAsync(() -> {
      linkedPlayerIds = AchievementsUI.getManiaLinkedPlayerIds();
      return client.getPlayerService().getPlayers();
    }).thenAcceptLater(players -> {
      playerCombo.getItems().setAll(players);
      players.stream().filter(PlayerRepresentation::isAdministrative).findFirst().ifPresentOrElse(playerCombo::setValue,
          () -> {
            if (!players.isEmpty()) {
              playerCombo.setValue(players.get(0));
            }
          });
    });
    JFXFuture.supplyAsync(() -> client.getAchievementsService().getDefaultWatcherFolder()).thenAcceptLater(folder -> {
      if (folder != null && !folder.isBlank()) {
        folderField.setText(folder);
      }
    });
  }

  private Stage getStage() {
    return (Stage) root.getScene().getWindow();
  }

  private void sizeToScene() {
    if (root.getScene() != null && root.getScene().getWindow() != null) {
      root.getScene().getWindow().sizeToScene();
    }
  }

  @Override
  public void onDialogCancel() {
  }

  private void updateFields() {
    boolean local = localRadio.isSelected();
    folderField.setDisable(!local);
    importBtn.setDisable(true);
    pidField.setPromptText(local ? Messages.get("achievements.migration.pid_optional") : "e.g. ab12");
  }

  private void run(boolean preview) {
    PlayerRepresentation player = playerCombo.getValue();
    if (player == null) {
      return;
    }
    AchievementsMigrationRequest request = new AchievementsMigrationRequest();
    request.playerId = player.getId();
    request.source = localRadio.isSelected() ? AchievementsMigrationRequest.SOURCE_LOCAL : AchievementsMigrationRequest.SOURCE_FIREBASE;
    request.firebasePid = pidField.getText();
    request.recoveryKey = keyField.getText();
    request.watcherFolder = folderField.getText();
    request.previewOnly = preview;

    previewBtn.setDisable(true);
    importBtn.setDisable(true);
    showResult(null, preview ? Messages.get("achievements.migration.reading") : Messages.get("achievements.migration.importing"));
    JFXFuture.supplyAsync(() -> client.getAchievementsService().migrate(request))
        .thenAcceptLater(result -> {
          previewBtn.setDisable(false);
          showResult(result, null);
          importBtn.setDisable(!preview || result == null || !result.success);
        })
        .onErrorLater(e -> {
          previewBtn.setDisable(false);
          showResult(null, e.getMessage());
        });
  }

  private void showResult(AchievementsMigrationResult result, String message) {
    resultBox.setVisible(true);
    resultBox.setManaged(true);
    resultBox.getChildren().clear();
    if (result == null) {
      resultBox.getChildren().add(muted(message));
      sizeToScene();
      return;
    }
    resultBox.getChildren().add(colored(result.message, result.success ? GREEN : RED, 14, true));
    if (result.success) {
      resultBox.getChildren().add(text(Messages.get("achievements.migration.summary",
          result.watcherPlayerName != null ? result.watcherPlayerName : "-", result.firebasePid != null ? result.firebasePid : "-",
          result.level, result.levelName, number(result.uniqueAchievements), result.romsPlayed, result.badges.size())));
      if ("Preview".equals(result.message)) {
        resultBox.getChildren().add(muted(result.cloudUpload ? Messages.get("achievements.migration.will_upload") : Messages.get("achievements.migration.local_only")));
      }
      else {
        resultBox.getChildren().add(muted(Messages.get("achievements.migration.imported", result.importedUnlocks, result.skippedUnlocks)));
      }
    }
    for (String w : result.warnings) {
      Label l = colored("\u26A0 " + w, ORANGE, 14, false);
      l.setWrapText(true);
      resultBox.getChildren().add(l);
    }
    sizeToScene();
  }
}

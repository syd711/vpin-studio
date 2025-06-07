package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarAltColorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAltColorController.class);

  @FXML
  private Button uploadBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button restoreBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label backupsLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label filesLabel;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private Pane altColorRoot;

  private AltColor altColor;
  private ValidationState validationState;

  private TablesSidebarController tablesSidebarController;
  private Optional<GameRepresentation> game = Optional.empty();

  // Add a public no-args constructor
  public TablesSidebarAltColorController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      TableDialogs.openAltColorUploadDialog(game.get(), null, null, null);
    }
  }


  @FXML
  private void onRestore() {
    if (game.isPresent()) {
      TableDialogs.openAltColorAdminDialog(tablesSidebarController, game.get());
    }
  }

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete ALT Color files for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Studio.client.getAltColorService().delete(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    }
  }

  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    Platform.runLater(() -> {
      new Thread(() -> {
        Studio.client.getDmdService().clearCache();
        Studio.client.getGameService().reload(this.game.get().getId());
        this.game.ifPresent(gameRepresentation -> EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom()));

        Platform.runLater(() -> {
          this.reloadBtn.setDisable(false);
          EventManager.getInstance().notifyRepositoryUpdate();
        });
      }).start();
    });
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    DismissalUtil.dismissValidation(g, this.validationState);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    dataBox.setVisible(false);
    restoreBtn.setDisable(true);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
    deleteBtn.setDisable(true);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }


  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.altColor = null;
    this.validationState = null;
    reloadBtn.setDisable(g.isEmpty());
    restoreBtn.setDisable(g.isEmpty());

    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
    deleteBtn.setDisable(true);

    lastModifiedLabel.setText("-");
    backupsLabel.setText("-");
    nameLabel.setText("-");
    typeLabel.setText("-");
    filesLabel.setText("-");
    restoreBtn.setText("Restore");
    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      AltColor altColor = Studio.client.getAltColorService().getAltColor(game.getId());
      boolean altColorAvailable = altColor.isAvailable();

      restoreBtn.setDisable(altColor.getBackedUpFiles().isEmpty());
      if (!altColor.getBackedUpFiles().isEmpty()) {
        restoreBtn.setText("Restore (" + altColor.getBackedUpFiles().size() + ")");
      }

      dataBox.setVisible(altColorAvailable);
      emptyDataBox.setVisible(!altColorAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
      deleteBtn.setDisable(!altColorAvailable);

      if (altColorAvailable) {
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altColor.getModificationDate()));
        typeLabel.setText(altColor.getAltColorType().name());
        nameLabel.setText(altColor.getName());
        altColor = Studio.client.getAltColorService().getAltColor(game.getId());
        backupsLabel.setText(String.valueOf(altColor.getBackedUpFiles().size()));

        List<String> files = altColor.getFiles();
        filesLabel.setText(String.join(", ", files));


        List<ValidationState> validationStates = altColor.getValidationStates();
        errorBox.setVisible(!validationStates.isEmpty());
        if (!validationStates.isEmpty()) {
          validationState = validationStates.get(0);
          LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);
          errorTitle.setText(validationResult.getLabel());
          errorText.setText(validationResult.getText());
        }
      }
    }
  }
}
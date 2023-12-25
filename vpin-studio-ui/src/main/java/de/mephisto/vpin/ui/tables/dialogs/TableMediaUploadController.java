package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TableMediaUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private ComboBox<PopperScreen> screenCombo;

  @FXML
  private Label titleLabel;

  private List<File> selection;

  private boolean result = false;
  private GameRepresentation game;
  private List<String> fileSelection = new ArrayList<>();

  public ComboBox<PopperScreen> getScreenCombo() {
    return screenCombo;
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && !selection.isEmpty() && screenCombo.getValue() != null) {
      result = false;
      stage.close();

      Platform.runLater(() -> {
        TableMediaUploadProgressModel model = new TableMediaUploadProgressModel(this.game.getId(),
            "Popper Media Upload", selection, "popperMedia", screenCombo.getValue());
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Media");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Files", fileSelection));

    this.selection = fileChooser.showOpenMultipleDialog(stage);
    this.uploadBtn.setDisable(selection == null);
    if (this.selection != null) {
      Platform.runLater(() -> {
        this.fileNameField.setText(this.selection.stream().map(f -> f.getName()).collect(Collectors.joining()));
        this.fileNameField.setDisable(false);
        this.fileBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
        this.uploadBtn.setDisable(false);
      });
    }
    else {
      this.fileNameField.setText("");
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);

    this.screenCombo.setItems(FXCollections.observableList(Arrays.asList(PopperScreen.values())));
    this.screenCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateMediaSelection(newValue));
  }

  private void updateMediaSelection(PopperScreen popperScreen) {
    fileSelection.clear();
    if (popperScreen.equals(PopperScreen.Audio) || popperScreen.equals(PopperScreen.AudioLaunch)) {
      fileSelection.add("*.mp3");
    }
    else if (popperScreen.equals(PopperScreen.GameInfo) || popperScreen.equals(PopperScreen.Wheel) || popperScreen.equals(PopperScreen.GameHelp)) {
      fileSelection.add("*.jpg");
      fileSelection.add("*.png");
      fileSelection.add("*.apng");
    }
    else {
      fileSelection.add("*.jpg");
      fileSelection.add("*.png");
      fileSelection.add("*.apng");
      fileSelection.add("*.mp4");
    }
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setGame(GameRepresentation game, PopperScreen screen) {
    this.game = game;
    this.screenCombo.setValue(screen);
    this.titleLabel.setText("Select media for table \"" + game.getGameDisplayName() + "\" and screen \"" + screen.name() + "\".");
  }
}

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MediaUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MediaUploadController.class);

  @FXML
  private Node root;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Label audioLabel;

  @FXML
  private Label audioLaunchLabel;

  @FXML
  private Label backglassLabel;

  @FXML
  private Label apronLabel;

  @FXML
  private Label dmdLabel;

  @FXML
  private Label helpLabel;

  @FXML
  private Label infoLabel;

  @FXML
  private Label loadingLabel;

  @FXML
  private Label playfieldLabel;

  @FXML
  private Label topperLabel;

  @FXML
  private Label wheelLabel;

  private File selection;
  private Stage stage;

  private boolean result = false;
  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      result = true;
      stage.close();

      Platform.runLater(() -> {
        MediaPackUploadProgressModel model = new MediaPackUploadProgressModel(this.game.getId(), "Media Pack Upload", selection);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Media Pack");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Media Pack", "*.zip"));
    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      refreshSelection(stage);
    }
  }

  private void refreshSelection(Stage stage) {
    this.fileNameField.setText(this.selection.getAbsolutePath());

    audioLabel.setText("-");
    audioLaunchLabel.setText("-");
    backglassLabel.setText("-");
    apronLabel.setText("-");
    dmdLabel.setText("-");
    helpLabel.setText("-");
    infoLabel.setText("-");
    loadingLabel.setText("-");
    playfieldLabel.setText("-");
    topperLabel.setText("-");
    wheelLabel.setText("-");


    UploaderAnalysis analysis = UploadAnalysisDispatcher.analyzeArchive(selection);
    String analyze = analysis.validateAssetType(AssetType.POPPER_MEDIA);
    if (analyze != null) {
      result = false;
      WidgetFactory.showAlert(stage, analyze);
      this.fileNameField.setText("");
      this.fileBtn.setDisable(false);
      this.fileNameField.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
    else {
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
      this.cancelBtn.setDisable(false);

      audioLabel.setText(formatReadable(audioLabel, analysis.getPopperMediaFiles(PopperScreen.Audio)));
      audioLaunchLabel.setText(formatReadable(audioLaunchLabel, analysis.getPopperMediaFiles(PopperScreen.AudioLaunch)));
      backglassLabel.setText(formatReadable(backglassLabel, analysis.getPopperMediaFiles(PopperScreen.BackGlass)));
      apronLabel.setText(formatReadable(apronLabel, analysis.getPopperMediaFiles(PopperScreen.Menu)));
      dmdLabel.setText(formatReadable(dmdLabel, analysis.getPopperMediaFiles(PopperScreen.DMD)));
      helpLabel.setText(formatReadable(helpLabel, analysis.getPopperMediaFiles(PopperScreen.GameHelp)));
      infoLabel.setText(formatReadable(infoLabel, analysis.getPopperMediaFiles(PopperScreen.GameInfo)));
      loadingLabel.setText(formatReadable(loadingLabel, analysis.getPopperMediaFiles(PopperScreen.Loading)));
      playfieldLabel.setText(formatReadable(playfieldLabel, analysis.getPopperMediaFiles(PopperScreen.PlayField)));
      topperLabel.setText(formatReadable(topperLabel, analysis.getPopperMediaFiles(PopperScreen.Topper)));
      wheelLabel.setText(formatReadable(wheelLabel, analysis.getPopperMediaFiles(PopperScreen.Wheel)));
    }
  }

  private String formatReadable(Label label, List<String> popperMediaFiles) {
    if (popperMediaFiles.isEmpty()) {
      return "-";
    }

    String result = popperMediaFiles.get(0);
    if (popperMediaFiles.size() > 1) {
      result = result + " (" + (popperMediaFiles.size() - 1) + " more)";
      label.setTooltip(new Tooltip(String.join("\n",popperMediaFiles)));
    }
    return result;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "zip"));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection(stage);
    }));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setData(GameRepresentation game, UploaderAnalysis analysis, File file, Stage stage) {
    this.game = game;
    this.selection = file;
    this.stage = stage;
    if (selection != null) {
      refreshSelection(stage);
    }
  }
}

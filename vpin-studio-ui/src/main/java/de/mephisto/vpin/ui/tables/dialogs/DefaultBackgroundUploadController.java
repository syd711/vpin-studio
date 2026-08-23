package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class DefaultBackgroundUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static boolean uploadTypeGeneratorSelectedLast;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Label titleLabel;

  private File selection;

  private boolean result = false;
  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null && selection.exists()) {
      Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
      result = true;
      try {
        uploadTypeGeneratorSelectedLast = false;
        DefaultBackgroundUploadProgressModel model = new DefaultBackgroundUploadProgressModel(this.game.getId(), "Default Background Upload", selection);
        ProgressDialog.createProgressDialog(model);
      }
      catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.uploading_default_background_failed"), Messages.get("dialog.please_check_the_log_file_for_details"), Messages.get("dialog.error") + e.getMessage());
      } finally {
        stage.close();
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Picture File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Pictures", "*.png", "*.jpg", "*.jpeg"));

    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      this.fileNameField.setText(this.selection.getAbsolutePath());
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
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setGame(@NonNull GameRepresentation game) {
    this.game = game;
      this.titleLabel.setText(Messages.get("tables.background_picture_upload.select_default_background_for", game.getGameDisplayName()));
  }
}

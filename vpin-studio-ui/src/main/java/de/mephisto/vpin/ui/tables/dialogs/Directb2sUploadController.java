package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.panels.PropperRenamingController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class Directb2sUploadController extends BaseUploadController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox uploadReplaceBox;

  @FXML
  private VBox uploadAppendBox;

  @FXML
  private RadioButton uploadAndReplaceRadio;

  @FXML
  private RadioButton uploadAndAppendRadio;

  @FXML
  private Label gameLabel;

  public Directb2sUploadController() {
    super(AssetType.DIRECTB2S, false, false, "zip", "7z", "rar", "directb2s");
  }

  private GameRepresentation game;


  @Override
  protected UploadProgressModel createUploadModel() {
    return null;
  }

  @Override
  protected void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    boolean append = uploadAndAppendRadio.isSelected();
    Platform.runLater(() -> {
      stage.close();
    });

    DirectB2SUploadProgressModel model = new DirectB2SUploadProgressModel(game.getId(), "DirectB2S Upload", getSelection(), append);
    ProgressDialog.createProgressDialog(model);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    ToggleGroup toggleGroup = new ToggleGroup();
    uploadAndReplaceRadio.setToggleGroup(toggleGroup);
    uploadAndAppendRadio.setToggleGroup(toggleGroup);

    uploadAndAppendRadio.setSelected(true);
    uploadAppendBox.getStyleClass().add("selection-panel-selected");

    uploadAndReplaceRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!uploadReplaceBox.getStyleClass().contains("selection-panel-selected")) {
            uploadReplaceBox.getStyleClass().add("selection-panel-selected");
          }
          uploadAppendBox.getStyleClass().remove("selection-panel-selected");
        }
        else {
          uploadReplaceBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    uploadAndAppendRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!uploadAndAppendRadio.getStyleClass().contains("selection-panel-selected")) {
            uploadAppendBox.getStyleClass().add("selection-panel-selected");
          }
          uploadReplaceBox.getStyleClass().remove("selection-panel-selected");
        }
        else {
          uploadAppendBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });
  }

  @Override
  public void setFile(Stage stage, File file, UploaderAnalysis analysis, Runnable finalizer) {
    super.setFile(stage, file, analysis, finalizer);
  }

  public void setData(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;
    this.gameLabel.setText(gameRepresentation.getGameDisplayName());
  }
}

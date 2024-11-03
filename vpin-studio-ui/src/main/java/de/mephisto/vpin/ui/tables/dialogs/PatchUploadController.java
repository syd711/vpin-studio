package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PatchUploadController extends BaseUploadController {
  public PatchUploadController() {
    super(AssetType.DIF, false, false, "zip", "7z", "rar", "dif");
  }

  @FXML
  private VBox uploadReplaceBox;

  @FXML
  private VBox uploadCloneBox;

  @FXML
  private RadioButton patchAndReplaceRadio;

  @FXML
  private RadioButton patchAndCloneRadio;

  @FXML
  private Label readmeLabel;

  @FXML
  private Button readmeBtn;

  @FXML
  private VBox assetsView;

  @FXML
  private VBox assetsBox;

  private UploadDescriptor uploadDescriptor = UploadDescriptorFactory.create();
  private GameRepresentation game;
  private UploaderAnalysis<?> analysis;

  @Override
  protected UploadProgressModel createUploadModel() {
    return null;//new RomUploadProgressModel("ROM Upload", getSelections(), getSelectedEmulatorId(), finalizer);
  }

  @FXML
  private void onAssetFilter() {
    TableDialogs.openMediaUploadDialog(this.game, getSelection(), analysis, true);
//    updateAnalysis();
  }

  @FXML
  private void onReadme(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String value = (String) ((Button) e.getSource()).getUserData();
    Dialogs.openTextEditor(stage, new TextFile(value), "README");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    assetsView.setVisible(false);
    readmeLabel.managedProperty().bindBidirectional(readmeLabel.visibleProperty());
    readmeBtn.managedProperty().bindBidirectional(readmeBtn.visibleProperty());

    ToggleGroup toggleGroup = new ToggleGroup();
    patchAndReplaceRadio.setToggleGroup(toggleGroup);
    patchAndReplaceRadio.setToggleGroup(toggleGroup);

    patchAndReplaceRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadReplaceBox.getStyleClass().add("selection-panel-selected");
          uploadDescriptor.setUploadType(UploadType.uploadAndReplace);
        }
        else {
          uploadCloneBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    patchAndCloneRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          uploadCloneBox.getStyleClass().add("selection-panel-selected");
          uploadDescriptor.setUploadType(UploadType.uploadAndClone);
        }
        else {
          uploadReplaceBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });
  }

  @Override
  protected void endAnalysis(String analysis, UploaderAnalysis<?> uploaderAnalysis) {
    super.endAnalysis(analysis, uploaderAnalysis);
    this.analysis = uploaderAnalysis;
  }

  public void setData(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;
  }
}

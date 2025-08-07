package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.assets.AssetLookupStrategy;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableAssetSourceFolderDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetSourceFolderDialogController.class);
  private static File lastFolderSelection;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField folderField;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private VBox screensPanel;

  @FXML
  private Button folderBtn;

  @FXML
  private RadioButton screensRadio;

  @FXML
  private RadioButton autoDetectRadio;

  @FXML
  private VBox autoDetectBox;

  @FXML
  private VBox screensBox;

  private List<CheckBox> screenCheckboxes = new ArrayList<>();
  private TableAssetSource source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    this.source.setType(TableAssetSourceType.FileSystem);
    this.source.setName(nameField.getText().trim());
    this.source.setLocation(folderField.getText().trim());
    this.source.setEnabled(enabledCheckbox.isSelected());
    this.source.setLookupStrategy(autoDetectRadio.isSelected() ? AssetLookupStrategy.autoDetect : AssetLookupStrategy.screens);

    List<String> screens = this.screenCheckboxes.stream().filter(CheckBox::isSelected).map(c -> {
      VPinScreen screen = (VPinScreen) c.getUserData();
      return screen.name();
    }).collect(Collectors.toList());
    this.source.setSupportedScreens(screens);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onFileSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    if (TableAssetSourceFolderDialogController.lastFolderSelection != null) {
      chooser.setInitialDirectory(TableAssetSourceFolderDialogController.lastFolderSelection);
    }
    chooser.setTitle("Select Folder");
    File targetFolder = chooser.showDialog(stage);
    if (targetFolder != null) {
      folderField.setText(targetFolder.getAbsolutePath());
      TableAssetSourceFolderDialogController.lastFolderSelection = targetFolder;

      if (StringUtils.isEmpty(nameField.getText())) {
        nameField.setText(targetFolder.getAbsolutePath());
      }
    }
    validateInput();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    folderBtn.setVisible(client.getSystemService().isLocal());

    nameField.textProperty().addListener((observableValue, s, t1) -> {
      source.setName(t1);
      validateInput();
    });
    this.validateInput();
    this.nameField.requestFocus();
  }

  private void setCheckboxesDisabled(boolean b) {
    screenCheckboxes.forEach(c -> c.setDisable(b));
  }

  private void validateInput() {
    String name = nameField.getText();
    String initials = folderField.getText();

    saveBtn.setDisable(StringUtils.isEmpty(name) || StringUtils.isEmpty(initials));
  }

  @Override
  public void onDialogCancel() {
    this.source = null;
  }

  public TableAssetSource getTableAssetSource() {
    return source;
  }

  public void setSource(TableAssetSource source) {
    this.source = source;
    nameField.setText(source.getName());
    folderField.setText(source.getLocation());
    enabledCheckbox.setSelected(source.isEnabled());


    ToggleGroup toggleGroup = new ToggleGroup();
    screensRadio.setToggleGroup(toggleGroup);
    autoDetectRadio.setToggleGroup(toggleGroup);

    autoDetectRadio.setSelected(true);

    if (source.getLookupStrategy().equals(AssetLookupStrategy.screens)) {
      screensBox.getStyleClass().add("selection-panel-selected");
    }
    else {
      autoDetectBox.getStyleClass().add("selection-panel-selected");
    }

    screensRadio.setSelected(source.getLookupStrategy().equals(AssetLookupStrategy.screens));
    screensRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!screensBox.getStyleClass().contains("selection-panel-selected")) {
            screensBox.getStyleClass().add("selection-panel-selected");
          }
          autoDetectBox.getStyleClass().remove("selection-panel-selected");
        }
        else {
          screensBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    autoDetectRadio.setSelected(source.getLookupStrategy().equals(AssetLookupStrategy.autoDetect));
    autoDetectRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!autoDetectBox.getStyleClass().contains("selection-panel-selected")) {
            autoDetectBox.getStyleClass().add("selection-panel-selected");
          }
          screensBox.getStyleClass().remove("selection-panel-selected");
        }
        else {
          autoDetectBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    for (VPinScreen screen : VPinScreen.values()) {
      CheckBox checkBox = new CheckBox(screen.name());
      checkBox.getStyleClass().add("default-text");
      checkBox.setUserData(screen);
      checkBox.setSelected(source.getSupportedScreens().contains(screen.name()));
      screensPanel.getChildren().add(checkBox);
      screenCheckboxes.add(checkBox);
    }

    validateInput();
  }
}

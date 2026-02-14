package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.assets.AssetLookupStrategy;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TableAssetSourceWebAssetDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetSourceWebAssetDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private VBox screensPanel;

  @FXML
  private VBox screensBox;

  private final List<CheckBox> screenCheckboxes = new ArrayList<>();
  private TableAssetSource source;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.source = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent event) {
    this.source.setName(nameField.getText().trim());
    this.source.setEnabled(enabledCheckbox.isSelected());
    this.source.setLookupStrategy(AssetLookupStrategy.screens);

    List<String> screens = this.screenCheckboxes.stream().filter(CheckBox::isSelected).map(c -> {
      VPinScreen screen = (VPinScreen) c.getUserData();
      return screen.name();
    }).collect(Collectors.toList());
    this.source.setSupportedScreens(screens);

    try {
      client.getAssetSourcesService().saveAssetSource(source);
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Error saving media source: " + e.getMessage());
    }

    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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
    saveBtn.setDisable(StringUtils.isEmpty(name));
  }

  @Override
  public void onDialogCancel() {
    this.source = null;
  }

  public void setSource(TableAssetSource source) {
    this.source = source;
    nameField.setText(source.getName());
    enabledCheckbox.setSelected(source.isEnabled());

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

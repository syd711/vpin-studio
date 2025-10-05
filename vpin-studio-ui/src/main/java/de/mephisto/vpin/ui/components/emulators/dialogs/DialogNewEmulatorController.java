package de.mephisto.vpin.ui.components.emulators.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class DialogNewEmulatorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DialogNewEmulatorController.class);

  @FXML
  private Button installBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private ComboBox<EmulatorTypeModel> emulatorTypeComboBox;

  @FXML
  private TextField nameField;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationText;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private VBox errorContainer;

  @FXML
  private VBox infoContainer;

  private GameEmulatorRepresentation validatedEmulator;

  @FXML
  private void onCancelClick(ActionEvent e) {
    validatedEmulator = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onInstall(ActionEvent event) {
    validatedEmulator.setName(nameField.getText());

    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    errorContainer.managedProperty().bindBidirectional(errorContainer.visibleProperty());
    infoContainer.managedProperty().bindBidirectional(infoContainer.visibleProperty());
    installBtn.setDisable(true);

    errorContainer.setVisible(false);

    ArrayList<EmulatorType> emulatorTypes = new ArrayList<>(Arrays.asList(EmulatorType.values()));
    emulatorTypes.remove(EmulatorType.OTHER);
    emulatorTypes.remove(EmulatorType.ZenFX2);
    List<EmulatorTypeModel> collect = emulatorTypes.stream().map(EmulatorTypeModel::new).collect(Collectors.toList());
    emulatorTypeComboBox.setItems(FXCollections.observableList(collect));

    emulatorTypeComboBox.valueProperty().addListener(new ChangeListener<EmulatorTypeModel>() {
      @Override
      public void changed(ObservableValue<? extends EmulatorTypeModel> observable, EmulatorTypeModel oldValue, EmulatorTypeModel emulatorTypeModel) {
        if(emulatorTypeModel == null) {
          return;
        }

        EmulatorType emulatorType = emulatorTypeModel.emulatorType;

        nameField.setText(emulatorTypeModel.toString());

        JFXFuture.supplyAsync(() -> {
          validatedEmulator = null;
          return client.getEmulatorService().validate(emulatorType);
        }).thenAcceptLater((validation) -> {
          try {
            installBtn.setDisable(validation.getErrorTitle() != null);

            if (validation.getErrorTitle() != null) {
              infoContainer.setVisible(false);
              errorContainer.setVisible(validation.getErrorTitle() != null);
              errorTitle.setText(validation.getErrorTitle());
              errorText.setText(validation.getErrorText());
            }
            else {
              validatedEmulator = validation.getGameEmulator();

              validatedEmulator.setName(nameField.getText());
              infoContainer.setVisible(true);
              if (emulatorType.equals(EmulatorType.ZenFX) || emulatorType.equals(EmulatorType.ZenFX3) || emulatorType.equals(EmulatorType.Zaccaria) || emulatorType.equals(EmulatorType.PinballM)) {
                validationText.setText("The games of this emulator type will be automatically imported.");
              }
              else {
                validationText.setText("You can setup the emulator and continue with the configuration.");
              }

              validationTitle.setText("Validation Successful");
            }

            nameField.selectAll();
            nameField.requestFocus();
          }
          catch (Exception e) {
            LOG.error("Emulator validation callback failed: {}", e.getMessage(), e);
          }
        });
      }
    });
  }

  @Override
  public void onDialogCancel() {
    validatedEmulator = null;
  }

  public GameEmulatorRepresentation getValidatedEmulator() {
    return validatedEmulator;
  }

  static class EmulatorTypeModel {
    private final EmulatorType emulatorType;

    EmulatorTypeModel(EmulatorType emulatorType) {
      this.emulatorType = emulatorType;
    }

    @Override
    public String toString() {
      return emulatorType.folderName();
    }
  }
}

package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CardGenerationPreferencesController implements Initializable {

  @FXML
  private CheckBox enableCardGenerationCheckbox;

  @FXML
  private ComboBox<String> popperScreenCombo;

  private VPinStudioClient client;
  private ObservedProperties properties;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();
    properties = client.getProperties("card-generator");

    BindingUtil.bindCheckbox(enableCardGenerationCheckbox, properties, "card.generation.enabled");
    enableCardGenerationCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> popperScreenCombo.setDisable(!t1));
    popperScreenCombo.setDisable(!enableCardGenerationCheckbox.selectedProperty().get());

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("Other2", "GameInfo", "GameHelp")));
    BindingUtil.bindComboBox(popperScreenCombo, properties, "popper.screen");
  }
}

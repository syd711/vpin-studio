package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CardGenerationPreferencesController implements Initializable {

  @FXML
  private ComboBox<String> popperScreenCombo;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ObservedProperties properties = Studio.client.getProperties("card-generator");

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("", "Other2", "GameInfo", "GameHelp")));
    BindingUtil.bindComboBox(popperScreenCombo, properties, "popper.screen");
  }
}

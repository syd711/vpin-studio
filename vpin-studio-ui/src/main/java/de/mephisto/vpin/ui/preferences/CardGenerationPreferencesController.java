package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CardGenerationPreferencesController implements Initializable {

  @FXML
  private ComboBox<String> popperScreenCombo;

  @FXML
  private Label validationError;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ObservedProperties properties = Studio.client.getProperties("card-generator");

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("", "Other2", "GameInfo", "GameHelp")));
    BindingUtil.bindComboBox(popperScreenCombo, properties, "popper.screen");
    popperScreenCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        onScreenChange();
      }
    });
    onScreenChange();
  }

  private void onScreenChange() {
    String selectedItem = popperScreenCombo.getSelectionModel().getSelectedItem();
    if (!StringUtils.isEmpty(selectedItem)) {
      PinUPControl fn = Studio.client.getPinUPControlFor(PopperScreen.valueOf(selectedItem));

      String msg = null;
      if (fn != null) {
        if (!fn.isActive()) {
          msg = "The screen has not been activated in PinUP Popper.";
        }

        if (fn.getCtrlKey() == 0) {
          msg = "The screen is not bound to any key in PinUP Popper.";
        }
      }

      validationError.setVisible(msg != null);
      validationError.setText(msg);
    }
    else {
      validationError.setVisible(false);
    }
  }
}

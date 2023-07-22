package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.SystemSummary;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PinVolPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolPreferencesController.class);

  @FXML
  private CheckBox toggleAutoStart;

  @FXML
  private Button openBtn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    toggleAutoStart.setSelected(Studio.client.getPinVolService().isAutoStartEnabled());
    toggleAutoStart.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        Studio.client.getPinVolService().toggleAutoStart();
      }
    });
  }

  @FXML
  private void onOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        Studio.client.getPinVolService().kill();
        File file = new File("resources", "PinVol.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find PinVol.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open Mame Setup: " + e.getMessage(), e);
      }
    }
  }
}

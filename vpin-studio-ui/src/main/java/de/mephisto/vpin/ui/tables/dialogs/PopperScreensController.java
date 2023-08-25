package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PopperScreensController implements Initializable, DialogController {

  @FXML
  private CheckBox useEmuDefaultsCheckbox;

  @FXML
  private CheckBox hideAllCheckbox;

  @FXML
  private CheckBox topperCheckbox;

  @FXML
  private CheckBox dmdCheckbox;

  @FXML
  private CheckBox backglassCheckbox;

  @FXML
  private CheckBox playfieldCheckbox;

  @FXML
  private CheckBox musicCheckbox;

  @FXML
  private CheckBox apronCheckbox;

  @FXML
  private CheckBox wheelbarCheckbox;

  @FXML
  private CheckBox loadingCheckbox;

  @FXML
  private CheckBox otherCheckbox;

  @FXML
  private CheckBox flyerCheckbox;

  @FXML
  private CheckBox helpCheckbox;

  private List<CheckBox> screenCheckboxes = new ArrayList<>();
  private TableDetails manifest;
  private GameRepresentation game;

  @FXML
  private void onSaveClick(ActionEvent e) {
    String value = "";
    if (useEmuDefaultsCheckbox.isSelected()) {
      //nothing, empty value for defaults
    }
    else if (hideAllCheckbox.isSelected()) {
      value = "NONE";
    }
    else {
      List<String> result = new ArrayList<>();
      if (topperCheckbox.isSelected()) result.add("" + 0);
      if (dmdCheckbox.isSelected()) result.add("" + 1);
      if (backglassCheckbox.isSelected()) result.add("" + 2);
      if (playfieldCheckbox.isSelected()) result.add("" + 3);
      if (musicCheckbox.isSelected()) result.add("" + 4);
      if (apronCheckbox.isSelected()) result.add("" + 5);
      if (wheelbarCheckbox.isSelected()) result.add("" + 6);
      if (loadingCheckbox.isSelected()) result.add("" + 7);
      if (otherCheckbox.isSelected()) result.add("" + 8);
      if (flyerCheckbox.isSelected()) result.add("" + 9);
      if (helpCheckbox.isSelected()) result.add("" + 10);

      value = String.join(",", result);
    }

    manifest.setKeepDisplays(value);
    try {
      Studio.client.getPinUPPopperService().saveTableDetails(manifest, game.getId());
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    } catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save table manifest: " + ex.getMessage());
    }

    this.onCancelClick(e);
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    screenCheckboxes = Arrays.asList(topperCheckbox, dmdCheckbox, backglassCheckbox, playfieldCheckbox, musicCheckbox,
        apronCheckbox, wheelbarCheckbox, loadingCheckbox, otherCheckbox, flyerCheckbox, helpCheckbox);

    useEmuDefaultsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        screenCheckboxes.stream().forEach(check -> check.setSelected(false));
        hideAllCheckbox.setSelected(false);
      }
    });

    hideAllCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        screenCheckboxes.stream().forEach(check -> check.setSelected(false));
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });

    topperCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    dmdCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    backglassCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    playfieldCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    musicCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    apronCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    wheelbarCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    loadingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    otherCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    flyerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    helpCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue)  {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.manifest = Studio.client.getPinUPPopperService().getTableDetails(game.getId());

    String keepDisplays = manifest.getKeepDisplays();
    if (StringUtils.isEmpty(keepDisplays)) {
      useEmuDefaultsCheckbox.setSelected(true);
    }
    else if (keepDisplays.equalsIgnoreCase("NONE")) {
      hideAllCheckbox.setSelected(true);
    }
    else {
      String[] split = keepDisplays.split(",");
      for (String screen : split) {
        if (StringUtils.isEmpty(screen)) {
          continue;
        }

        int id = Integer.parseInt(screen);
        switch (id) {
          case 0: {
            topperCheckbox.setSelected(true);
            break;
          }
          case 1: {
            dmdCheckbox.setSelected(true);
            break;
          }
          case 2: {
            backglassCheckbox.setSelected(true);
            break;
          }
          case 3: {
            playfieldCheckbox.setSelected(true);
            break;
          }
          case 4: {
            musicCheckbox.setSelected(true);
            break;
          }
          case 5: {
            apronCheckbox.setSelected(true);
            break;
          }
          case 6: {
            wheelbarCheckbox.setSelected(true);
            break;
          }
          case 7: {
            loadingCheckbox.setSelected(true);
            break;
          }
          case 8: {
            otherCheckbox.setSelected(true);
            break;
          }
          case 9: {
            flyerCheckbox.setSelected(true);
            break;
          }
          case 10: {
            helpCheckbox.setSelected(true);
            break;
          }
        }
      }
    }
  }
}

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.TableDetails;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class TableDataTabScreensController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataTabScreensController.class);

  //screens
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

  @FXML
  private VBox root;

  private List<CheckBox> screenCheckboxes = new ArrayList<>();
  private GameRepresentation game;
  private TableDetails tableDetails;

  public void setGame(GameRepresentation game, TableDetails tableDetails) {
    this.game = game;
    this.tableDetails = tableDetails;

    //displays
    String keepDisplays = tableDetails.getKeepDisplays();
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

  public void save() {
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
    tableDetails.setKeepDisplays(value);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //screens
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
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    dmdCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    backglassCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    playfieldCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    musicCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    apronCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    wheelbarCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    loadingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    otherCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    flyerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    helpCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
  }
}

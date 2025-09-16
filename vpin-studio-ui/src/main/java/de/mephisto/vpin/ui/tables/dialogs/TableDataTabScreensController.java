package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

import static de.mephisto.vpin.ui.Studio.client;

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
  private TableDetails tableDetails;

  private BooleanProperty dirty = new SimpleBooleanProperty(false);


  public void setGame(GameRepresentation game, TableDetails tableDetails) {
    this.tableDetails = tableDetails;

    //displays
    String keepDisplays = tableDetails!=null? tableDetails.getKeepDisplays(): null;
    if (StringUtils.isEmpty(keepDisplays)) {
      useEmuDefaultsCheckbox.setSelected(true);
    }
    else if (StringUtils.equalsIgnoreCase(keepDisplays, "NONE")) {
      hideAllCheckbox.setSelected(true);
    }
    else {
      List<VPinScreen> screens = Arrays.asList(VPinScreen.keepDisplaysToScreens(keepDisplays));
      for (CheckBox checkbox : screenCheckboxes) {
        VPinScreen screen = (VPinScreen) checkbox.getUserData();
        checkbox.setSelected(screens.contains(screen));
      }
    }

    dirty.set(false);
  }

  public boolean save() {
    String value = "";
    if (useEmuDefaultsCheckbox.isSelected()) {
      //nothing, empty value for defaults
    }
    else if (hideAllCheckbox.isSelected()) {
      value = "NONE";
    }
    else {
      StringBuilder bld = new StringBuilder();
      for (CheckBox checkbox : screenCheckboxes) {
        if (checkbox.isSelected()) {
          if (bld.length() > 0) {
            bld.append(",");
          }
          bld.append(((VPinScreen) checkbox.getUserData()).getCode());
        }
      }
      value = bld.toString();
    }
    if (tableDetails !=  null) {
      tableDetails.setKeepDisplays(value);
    }
    dirty.set(false);
    return true;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    List<VPinScreen> supportedScreens = frontend.getSupportedScreens();

    screenCheckboxes = Arrays.asList(topperCheckbox, dmdCheckbox, backglassCheckbox, playfieldCheckbox, musicCheckbox, 
          apronCheckbox, wheelbarCheckbox, loadingCheckbox, otherCheckbox, flyerCheckbox, helpCheckbox);
    //screens, in exact same order as checkboxes
    VPinScreen[] screens = { VPinScreen.Topper, VPinScreen.DMD, VPinScreen.BackGlass, VPinScreen.PlayField, VPinScreen.Audio,   
          VPinScreen.Menu, VPinScreen.Wheel, VPinScreen.Loading, VPinScreen.Other2, VPinScreen.GameInfo, VPinScreen.GameHelp };

    for (int i = 0; i < screens.length; i++) {
      CheckBox checkbox = screenCheckboxes.get(i);
      checkbox.setUserData(screens[i]);

      checkbox.managedProperty().bindBidirectional(checkbox.visibleProperty());
      checkbox.setVisible(supportedScreens.contains(screens[i]));

      checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue) {
          hideAllCheckbox.setSelected(false);
          useEmuDefaultsCheckbox.setSelected(false);
        }
        dirty.set(true);
      });
    }

    useEmuDefaultsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        screenCheckboxes.stream().forEach(check -> check.setSelected(false));
        hideAllCheckbox.setSelected(false);
      }
      dirty.set(true);
    });

    hideAllCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        screenCheckboxes.stream().forEach(check -> check.setSelected(false));
        useEmuDefaultsCheckbox.setSelected(false);
      }
      dirty.set(true);
    });
  }

  public BooleanProperty dirtyProperty() {
    return dirty;
  } 
}

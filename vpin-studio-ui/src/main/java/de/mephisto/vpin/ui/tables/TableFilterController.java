package de.mephisto.vpin.ui.tables;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TableFilterController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TableFilterController.class);

  @FXML
  private VBox filterPanel;

  @FXML
  private CheckBox missingAssetsCheckBox;

  @FXML
  private CheckBox vpsUpdatesCheckBox;

  @FXML
  private CheckBox versionUpdatesCheckBox;

  @FXML
  private CheckBox notPlayedCheckBox;

  @FXML
  private CheckBox noHighscoreSettingsCheckBox;

  @FXML
  private CheckBox noHighscoreSupportCheckBox;

  @FXML
  private CheckBox withBackglassCheckBox;

  @FXML
  private CheckBox withPupPackCheckBox;

  @FXML
  private CheckBox withAltSoundCheckBox;

  @FXML
  private CheckBox withAltColorCheckBox;

  @FXML
  private CheckBox withPovIniCheckBox;

  private TableOverviewController tableOverviewController;

  public void setTableController(TableOverviewController tableOverviewController) {

    this.tableOverviewController = tableOverviewController;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    filterPanel.setPrefWidth(0);
    filterPanel.setVisible(false);
  }

  public void toggle() {
    filterPanel.setPrefWidth(0);
    filterPanel.setVisible(false);
  }
}

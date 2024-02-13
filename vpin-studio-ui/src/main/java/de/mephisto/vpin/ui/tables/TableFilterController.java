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

  @FXML
  private VBox filterRoot;

  private TableOverviewController tableOverviewController;

  public void setTableController(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
    this.tableOverviewController.getTableStack().getChildren().add(filterRoot);

    filterRoot.setPrefWidth(0);
    filterRoot.setVisible(false);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  public void toggle() {
    filterRoot.setPrefWidth(200);
    filterRoot.setVisible(true);
  }
}

package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

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

  @FXML
  private VBox titlePaneRoot;

  private boolean visible = false;
  private FilterSettings filterSettings;
  private TableOverviewController tableOverviewController;


  @FXML
  private void onReset() {
    updateSettings(new FilterSettings());
  }

  public void setTableController(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
    this.tableOverviewController.getTableStack().setAlignment(Pos.TOP_LEFT);
    this.tableOverviewController.getTableStack().getChildren().add(0, filterRoot);
    filterRoot.prefHeightProperty().bind(this.tableOverviewController.getTableStack().heightProperty());
    titlePaneRoot.prefHeightProperty().bind(this.tableOverviewController.getTableStack().heightProperty());
  }

  public void toggle() {
    if(!visible) {
      visible = true;
      filterRoot.setVisible(true);
      TranslateTransition translateByXTransition = TransitionUtil.createTranslateByXTransition(this.tableOverviewController.getTableView(), 300, 250);
      translateByXTransition.play();
    }
    else {
      visible = false;
      filterRoot.setVisible(true);
      TranslateTransition translateByXTransition = TransitionUtil.createTranslateByXTransition(this.tableOverviewController.getTableView(), 300, -250);
      translateByXTransition.play();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    filterRoot.setVisible(false);

    filterSettings = new FilterSettings();
    missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
    missingAssetsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setMissingAssets(newValue);
      applyFilter();
    });
    vpsUpdatesCheckBox.setSelected(filterSettings.isVpsUpdates());
    vpsUpdatesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setVpsUpdates(newValue);
      applyFilter();
    });
    versionUpdatesCheckBox.setSelected(filterSettings.isVersionUpdates());
    versionUpdatesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setVersionUpdates(newValue);
      applyFilter();
    });
    notPlayedCheckBox.setSelected(filterSettings.isNotPlayed());
    notPlayedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNotPlayed(newValue);
      applyFilter();
    });
    noHighscoreSettingsCheckBox.setSelected(filterSettings.isNoHighscoreSettings());
    noHighscoreSettingsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoHighscoreSettings(newValue);
      applyFilter();
    });
    noHighscoreSupportCheckBox.setSelected(filterSettings.isNoHighscoreSupport());
    noHighscoreSupportCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoHighscoreSupport(newValue);
      applyFilter();
    });
    withBackglassCheckBox.setSelected(filterSettings.isWithBackglass());
    withBackglassCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithBackglass(newValue);
      applyFilter();
    });
    withPupPackCheckBox.setSelected(filterSettings.isWithPupPack());
    withPupPackCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPupPack(newValue);
      applyFilter();
    });
    withAltSoundCheckBox.setSelected(filterSettings.isWithAltSound());
    withAltSoundCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltSound(newValue);
      applyFilter();
    });
    withAltColorCheckBox.setSelected(filterSettings.isWithAltColor());
    withAltColorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltColor(newValue);
      applyFilter();
    });
    withPovIniCheckBox.setSelected(filterSettings.isWithPovIni());
    withPovIniCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPovIni(newValue);
      applyFilter();
    });
  }

  private void updateSettings(FilterSettings filterSettings) {
    missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
    vpsUpdatesCheckBox.setSelected(filterSettings.isVpsUpdates());
    versionUpdatesCheckBox.setSelected(filterSettings.isVersionUpdates());
    notPlayedCheckBox.setSelected(filterSettings.isNotPlayed());
    noHighscoreSettingsCheckBox.setSelected(filterSettings.isNoHighscoreSettings());
    noHighscoreSupportCheckBox.setSelected(filterSettings.isNoHighscoreSupport());
    withBackglassCheckBox.setSelected(filterSettings.isWithBackglass());
    withPupPackCheckBox.setSelected(filterSettings.isWithPupPack());
    withAltSoundCheckBox.setSelected(filterSettings.isWithAltSound());
    withAltColorCheckBox.setSelected(filterSettings.isWithAltColor());
    withPovIniCheckBox.setSelected(filterSettings.isWithPovIni());
  }

  private void applyFilter() {
    try {
      List<Integer> integers = client.getGameService().filterGames(this.filterSettings);
      tableOverviewController.setFilterIds(integers);
    } catch (Exception e) {
      LOG.error("Error filtering tables: " + e.getMessage());
      WidgetFactory.showAlert(Studio.stage, "Error", "Error filtering tables: " + e.getMessage());
    }
  }
}

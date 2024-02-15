package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableFilterController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TableFilterController.class);

  @FXML
  private VBox filterPanel;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

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

  @FXML
  private ComboBox<TableStatus> statusCombo;

  private boolean visible = false;
  private boolean updatesDisabled = false;
  private FilterSettings filterSettings;
  private TableOverviewController tableOverviewController;


  @FXML
  private void onReset() {
    updateSettings(new FilterSettings());
    applyFilter();
  }

  public void setTableController(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
//    tableOverviewController.getFilterButton();
//    this.tableOverviewController.getTableStack().setAlignment(Pos.TOP_LEFT);
//    this.tableOverviewController.getTableStack().getChildren().add(0, filterRoot);
//    filterRoot.prefHeightProperty().bind(this.tableOverviewController.getTableStack().heightProperty());
//    titlePaneRoot.prefHeightProperty().bind(this.tableOverviewController.getTableStack().heightProperty());
//    tableOverviewController.getTableStack().widthProperty().addListener(new ChangeListener<Number>() {
//      @Override
//      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
////        System.out.println(newValue);
//      }
//    });
  }

  @FXML
  public void toggle() {
    if (!visible) {
      visible = true;
      filterRoot.setVisible(true);
      TranslateTransition filterTransition = TransitionUtil.createTranslateByXTransition(this.tableOverviewController.getTableView(), 300, 250);
      filterTransition.play();
    }
    else {
      visible = false;
      TranslateTransition translateByXTransition = TransitionUtil.createTranslateByXTransition(this.tableOverviewController.getTableView(), 300, -250);
      translateByXTransition.statusProperty().addListener(new ChangeListener<Animation.Status>() {
        @Override
        public void changed(ObservableValue<? extends Animation.Status> observable, Animation.Status oldValue, Animation.Status newValue) {
          if (newValue == Animation.Status.STOPPED) {
            filterRoot.setVisible(false);
          }
        }
      });
      translateByXTransition.play();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    filterRoot.setVisible(false);

    List<GameEmulatorRepresentation> gameEmulators = new ArrayList<>(Studio.client.getPinUPPopperService().getGameEmulators());
    gameEmulators.add(0, null);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      if (t1 == null) {
        filterSettings.setEmulatorId(-1);
      }
      else {
        filterSettings.setEmulatorId(t1.getId());
      }
      applyFilter();
    });

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

    List<TableStatus> statuses = new ArrayList<>(TableDataController.TABLE_STATUSES_15);
    statuses.add(0, null);
    statusCombo.setItems(FXCollections.observableList(statuses));
    statusCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        filterSettings.setGameStatus(-1);
      }
      else {
        filterSettings.setGameStatus(newValue.getValue());
      }
      applyFilter();
    });
  }

  private void updateSettings(FilterSettings filterSettings) {
    updatesDisabled = true;
    emulatorCombo.setValue(null);
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
    updatesDisabled = false;
  }

  private void applyFilter() {
    if (updatesDisabled) {
      return;
    }

    try {
      List<Integer> integers = client.getGameService().filterGames(this.filterSettings);
      tableOverviewController.setFilterIds(integers);
      tableOverviewController.onReload();//TODO
    } catch (Exception e) {
      LOG.error("Error filtering tables: " + e.getMessage());
      WidgetFactory.showAlert(Studio.stage, "Error", "Error filtering tables: " + e.getMessage());
    }
  }
}

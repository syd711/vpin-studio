package de.mephisto.vpin.ui.vps;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import de.mephisto.vpin.ui.tables.panels.BaseFilterController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VpsTablesFilterController extends BaseFilterController implements Initializable {
  @FXML
  private VBox filterRoot;

  @FXML
  private VBox tableFilters;

  @FXML
  private CheckBox installedOnlyCheckbox;

  @FXML
  private TextField author;

  @FXML
  private CheckBox withAuthorInOtherAssetsToo;

  @FXML
  private TextField manufacturer;

  @FXML
  private TextField theme;

  //-------------------------------
  @FXML
  private VBox featuresPanel;

  private LinkedHashMap<String, CheckBox> featureCheckboxes;

  //-------------------------------
  // Presence of assets
  @FXML
  private CheckBox withDirectB2s;

  @FXML
  private CheckBox withPuppack;

  @FXML
  private CheckBox withRom;

  @FXML
  private CheckBox withTopper;

  @FXML
  private CheckBox withWheel;

  @FXML
  private CheckBox withAltSound;

  @FXML
  private CheckBox withAltColor;

  @FXML
  private CheckBox withTutorial;

  //------------------------------------------------------

  private boolean updatesDisabled = false;
  
  private VpsFilterSettings filterSettings = new VpsFilterSettings();
  
  private VpsTablesController vpsTablesController;

  //------------------------------------------------------

  
  @FXML
  private void onReset() {
    resetFilters();
    applyFilters();
  }


  public void setVpsTablesController(VpsTablesController vpsTablesController, Button filterButton, StackPane pane, TableView<?> tableView) {
    this.vpsTablesController = vpsTablesController;
    super.setupDrawer(filterRoot, filterButton, pane, tableView);
    super.toggleFilterButton(!filterSettings.isResetted());
  }

  @FXML
  public void toggle() {
    super.toggleDrawer();
  }

  private void resetFilters() {
    this.filterSettings = new VpsFilterSettings();

    updatesDisabled = true;

    installedOnlyCheckbox.setSelected(false);

    author.setText(null);
    withAuthorInOtherAssetsToo.setSelected(false);
    manufacturer.setText(null);
    theme.setText(null);

    if (featureCheckboxes != null) {
      for (CheckBox cb : featureCheckboxes.values())  {
        cb.setSelected(false);
      }
    }

    withDirectB2s.setSelected(false);
    withPuppack.setSelected(false);
    withRom.setSelected(false);
    withTopper.setSelected(false);
    withAltSound.setSelected(false);
    withAltColor.setSelected(false);
    withTutorial.setSelected(false);

    updatesDisabled = false;
  }

  protected void applyFilters() {
    super.toggleFilterButton(!filterSettings.isResetted());
    if (updatesDisabled) {
      return;
    }

    Platform.runLater(() -> {
      vpsTablesController.applyFilters();
    });
  }

  public VpsFilterSettings getFilterSettings() {
    return filterSettings;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tableFilters.managedProperty().bindBidirectional(tableFilters.visibleProperty());

    installedOnlyCheckbox.setSelected(filterSettings.isInstalledOnly());
    installedOnlyCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setInstalledOnly(newValue);
      applyFilters();
    });

    author.textProperty().setValue(filterSettings.getAuthor());
    author.textProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setAuthor(newValue);
      applyFilters();
    });
    withAuthorInOtherAssetsToo.setSelected(filterSettings.isSearchAuthorInOtherAssetsToo());
    withAuthorInOtherAssetsToo.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setSearchAuthorInOtherAssetsToo(newValue);
      applyFilters();
    });

    manufacturer.textProperty().setValue(filterSettings.getManufacturer());
    manufacturer.textProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setManufacturer(newValue);
      applyFilters();
    });
    theme.textProperty().setValue(filterSettings.getTheme());
    theme.textProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setTheme(newValue);
      applyFilters();
    });

    withDirectB2s.setSelected(filterSettings.isWithBackglass());
    withDirectB2s.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithBackglass(newValue);
      applyFilters();
    });
    withPuppack.setSelected(filterSettings.isWithPupPack());
    withPuppack.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPupPack(newValue);
      applyFilters();
    });
    withRom.setSelected(filterSettings.isWithRom());
    withRom.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithRom(newValue);
      applyFilters();
    });
    withTopper.setSelected(filterSettings.isWithTopper());
    withTopper.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithTopper(newValue);
      applyFilters();
    });
    withWheel.setSelected(filterSettings.isWithWheel());
    withTopper.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithWheel(newValue);
      applyFilters();
    });
    withAltSound.setSelected(filterSettings.isWithAltSound());
    withAltSound.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltSound(newValue);
      applyFilters();
    });
    withAltColor.setSelected(filterSettings.isWithAltColor());
    withAltColor.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltColor(newValue);
      applyFilters();
    });
    withTutorial.setSelected(filterSettings.isWithTutorial());
    withTutorial.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithTutorial(newValue);
      applyFilters();
    });
  }

  public void bindSearchField(TextField searchTextField) {
    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      vpsTablesController.clearSelection();

      // reset the Predicate to trigger the table refiltering
      filterSettings.setFilterTerm(filterValue);
      applyFilters();
    });
  }


}

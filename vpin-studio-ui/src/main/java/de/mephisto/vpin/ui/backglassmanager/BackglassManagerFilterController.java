package de.mephisto.vpin.ui.backglassmanager;

import static de.mephisto.vpin.ui.Studio.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController.DirectB2SEntryModel;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BackglassManagerFilterController extends BaseFilterController implements Initializable {

  @FXML
  private VBox filterPanel;

  @FXML
  private CheckBox missingDMDImageCheckBox;

  @FXML
  private CheckBox notFullDMDRatioCheckBox;

  @FXML
  private CheckBox scoresAvailableCheckBox;

  @FXML
  private CheckBox missingTableCheckBox;

  @FXML
  private ComboBox<B2SVisibility> grillVisibilityComboBox;

  @FXML
  private CheckBox backglassVisibilityCheckBox;

  @FXML
  private CheckBox b2sdmdVisibilityCheckBox;

  @FXML
  private ComboBox<B2SVisibility> dmdVisibilityComboBox;

  @FXML
  private VBox filterRoot;

  @FXML
  private VBox emulatorFilters;

  private List<CheckBox> emulatorCheckboxes = new ArrayList<>();


  @FXML
  private Node backglassFilters;

  @FXML
  private Node settingFilters;

  private boolean updatesDisabled = false;

  private BackglassManagerController backglassManagerController;

  private BackglassManagerPredicateFactory predicateFactory;


  @FXML
  private void onReset() {
    updatesDisabled = true;

    for (CheckBox cb : emulatorCheckboxes) {
      cb.setSelected(true);
    }

    missingDMDImageCheckBox.setSelected(false);
    notFullDMDRatioCheckBox.setSelected(false);
    scoresAvailableCheckBox.setSelected(false);
    missingTableCheckBox.setSelected(false);

    grillVisibilityComboBox.getSelectionModel().clearSelection();
    b2sdmdVisibilityCheckBox.setSelected(false);
    backglassVisibilityCheckBox.setSelected(false);
    dmdVisibilityComboBox.getSelectionModel().clearSelection();

    updatesDisabled = false;
    applyFilter();
  }

  @Override
  protected void initStackPaneListener(StackPane stackPane) {
    stackPane.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (newValue!=null) {
          stackPane.setMinWidth(newValue.doubleValue());
          stackPane.setMaxWidth(newValue.doubleValue());
        }
        refreshState();
      }
    });
  }

  @FXML
  public void toggle() {
    toggleDrawer();
  }

  public void applyFilter() {
    if (updatesDisabled) {
      return;
    }
    updatesDisabled = true;

    boolean hasFilter = missingDMDImageCheckBox.isSelected()
      || notFullDMDRatioCheckBox.isSelected()
      || scoresAvailableCheckBox.isSelected()
      || missingTableCheckBox.isSelected()
      || isNotEmpty(grillVisibilityComboBox)
      || b2sdmdVisibilityCheckBox.isSelected()
      || backglassVisibilityCheckBox.isSelected()
      || isNotEmpty(dmdVisibilityComboBox);

    for (CheckBox cb : emulatorCheckboxes) {
      hasFilter |= !cb.isSelected();
    }

    toggleFilterButton(hasFilter);

    // add filter check on selected emulators

    Platform.runLater(() -> {
      backglassManagerController.applyFilter();
      updatesDisabled = false;
    });
  }
  private boolean isNotEmpty(ComboBox<B2SVisibility> comboBox) {
    return comboBox.getValue()!=null && comboBox.getValue().getId()>=0;
  }

  public Predicate<? super DirectB2SEntryModel> buildPredicate() {
    return predicateFactory.buildPredicate();
  }

  //--------------------------------
  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  public void setTableController(BackglassManagerController backglassManagerController,
                                 Button filterButton, TextField searchField, StackPane stackPane, TableView<?> filteredTable) {

    this.backglassManagerController = backglassManagerController;
    super.setupDrawer(filterRoot, filterButton, stackPane, filteredTable);

    predicateFactory = new BackglassManagerPredicateFactory(backglassManagerController);

    searchField.textProperty().addListener((observable, oldValue, filterValue) -> {
      filteredTable.getSelectionModel().clearSelection();
    
      // reset the Predicate to trigger the table refiltering
      predicateFactory.setFilterTerm(filterValue);

      backglassManagerController.applyFilter();
    });

    List<GameEmulatorRepresentation> emulators = client.getFrontendService().getVpxGameEmulators();
    for (GameEmulatorRepresentation gameEmulator: emulators) {
      CheckBox checkBox = new CheckBox(gameEmulator.getName());
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(true);
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (newValue) {
            predicateFactory.selectEmulator(gameEmulator.getId());
          } else {
            predicateFactory.unselectEmulator(gameEmulator.getId());
          }
          applyFilter();
        }
      });
      emulatorCheckboxes.add(checkBox);
      emulatorFilters.getChildren().add(checkBox);
    }

    setupCheckbox(missingDMDImageCheckBox, backglassManagerController.missingDMDImageFilter);
    setupCheckbox(notFullDMDRatioCheckBox, backglassManagerController.notFullDMDRatioFilter);
    setupCheckbox(scoresAvailableCheckBox, backglassManagerController.scoresAvailableFilter);
    setupCheckbox(missingTableCheckBox, backglassManagerController.missingTableFilter);

    List<B2SVisibility> visibilities = new ArrayList<>();
    visibilities.add(new B2SVisibility(-1, ""));
    visibilities.addAll(TablesSidebarDirectB2SController.VISIBILITIES);
    ObservableList<B2SVisibility> visibilitiesModel = FXCollections.observableList(visibilities);

    setupComboBox(grillVisibilityComboBox, visibilitiesModel, backglassManagerController.grillVisibilityFilter);
    setupCheckbox(b2sdmdVisibilityCheckBox, backglassManagerController.b2sdmdVisibilityFilter);
    setupCheckbox(backglassVisibilityCheckBox, backglassManagerController.backglassVisibilityFilter);
    setupComboBox(dmdVisibilityComboBox, visibilitiesModel, backglassManagerController.dmdVisibilityFilter);
  }

  private void setupCheckbox(CheckBox cb, Property<Boolean> model) {
    cb.selectedProperty().bindBidirectional(model);
    cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
      applyFilter();
    });
  }

  private void setupComboBox(ComboBox<B2SVisibility> combo, ObservableList<B2SVisibility> visibilitiesModel, Property<B2SVisibility> model) {
    combo.setItems(visibilitiesModel);
    combo.valueProperty().bindBidirectional(model);
    combo.valueProperty().addListener((observable, oldValue, newValue) -> {
      applyFilter();
    });
  }
}

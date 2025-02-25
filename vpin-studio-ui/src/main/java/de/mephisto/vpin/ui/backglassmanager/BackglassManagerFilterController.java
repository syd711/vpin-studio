package de.mephisto.vpin.ui.backglassmanager;

import static de.mephisto.vpin.ui.Studio.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SOption;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class BackglassManagerFilterController extends BaseFilterController<DirectB2S, DirectB2SModel> implements Initializable {

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
  private VBox emulatorFilters;

  private List<CheckBox> emulatorCheckboxes = new ArrayList<>();

  @FXML
  private Node backglassFilters;

  @FXML
  private Node settingFilters;

  private BackglassManagerPredicateFactory predicateFactory;
  

  protected void resetFilters() {
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
  }

  @Override
  protected boolean hasFilter() {
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
    return hasFilter;
  }
  private boolean isNotEmpty(ComboBox<B2SVisibility> comboBox) {
    return comboBox.getValue()!=null && comboBox.getValue().getId()>=0;
  }

  @Override
  public Predicate<DirectB2SModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    return predicateFactory.buildPredicate(searchTerm, playlist);
  }

  //--------------------------------
  @Override
  public void initialize(URL location, ResourceBundle resources) {

    predicateFactory = new BackglassManagerPredicateFactory();

    List<GameEmulatorRepresentation> emulators = client.getEmulatorService().getBackglassGameEmulators();
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
          applyFilters();
        }
      });
      emulatorCheckboxes.add(checkBox);
      emulatorFilters.getChildren().add(checkBox);
    }

    setupCheckbox(missingDMDImageCheckBox, predicateFactory.missingDMDImageFilter);
    setupCheckbox(notFullDMDRatioCheckBox, predicateFactory.notFullDMDRatioFilter);
    setupCheckbox(scoresAvailableCheckBox, predicateFactory.scoresAvailableFilter);
    setupCheckbox(missingTableCheckBox, predicateFactory.missingTableFilter);

    List<B2SVisibility> visibilities = new ArrayList<>();
    visibilities.add(new B2SVisibility(-1, ""));
    visibilities.addAll(TablesSidebarDirectB2SController.VISIBILITIES);
    ObservableList<B2SVisibility> visibilitiesModel = FXCollections.observableList(visibilities);

    setupComboBox(grillVisibilityComboBox, visibilitiesModel, predicateFactory.grillVisibilityFilter);
    setupCheckbox(b2sdmdVisibilityCheckBox, predicateFactory.b2sdmdVisibilityFilter);
    setupCheckbox(backglassVisibilityCheckBox, predicateFactory.backglassVisibilityFilter);
    setupComboBox(dmdVisibilityComboBox, visibilitiesModel, predicateFactory.dmdVisibilityFilter);
  }

  private void setupCheckbox(CheckBox cb, Property<Boolean> model) {
    cb.selectedProperty().bindBidirectional(model);
    cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
      applyFilters();
    });
  }

  private <T extends B2SOption> void setupComboBox(ComboBox<T> combo, ObservableList<T> visibilitiesModel, Property<T> model) {
    combo.setItems(visibilitiesModel);
    combo.valueProperty().bindBidirectional(model);
    combo.valueProperty().addListener((observable, oldValue, newValue) -> {
      applyFilters();
    });
  }
}

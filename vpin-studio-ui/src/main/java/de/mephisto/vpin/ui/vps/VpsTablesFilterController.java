package de.mephisto.vpin.ui.vps;

import static de.mephisto.vpin.ui.Studio.client;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import de.mephisto.vpin.ui.vps.VpsTablesController.VpsTableFormat;
import de.mephisto.vpin.ui.vps.VpsTablesController.VpsTableModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;

public class VpsTablesFilterController extends BaseFilterController<VpsTable, VpsTableModel> implements Initializable {

  @FXML
  private VBox tableFilters;

  @FXML
  private CheckBox installedOnlyCheckbox;

  @FXML
  private CheckBox notInstalledOnlyCheckbox;

  @FXML
  private DatePicker lastUpdateDate;

  @FXML
  private Button clearDateBtn;

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
  private FlowPane featuresPanel;

  private LinkedHashMap<String, Label> featureCheckboxes;

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

  @FXML
  private CheckBox withPov;

  //------------------------------------------------------

  private VpsTablesPredicateFactory predicateFactory = new VpsTablesPredicateFactory();
  
  //------------------------------------------------------

  public VpsTablesPredicateFactory getPredicateFactory() {
    return predicateFactory;
  } 

  @FXML
  private void onClearDate() {
    lastUpdateDate.setValue(null);
  }

  @Override
  protected void resetFilters() {
    this.predicateFactory = new VpsTablesPredicateFactory();

    installedOnlyCheckbox.setSelected(false);
    notInstalledOnlyCheckbox.setSelected(false);
    lastUpdateDate.setValue(null);
    author.setText(null);
    withAuthorInOtherAssetsToo.setSelected(false);
    manufacturer.setText(null);
    theme.setText(null);

    if (featureCheckboxes != null) {
      for (Label badge : featureCheckboxes.values())  {
        String feature = badge.getText();
        predicateFactory.registerFeature(feature);
        badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature, false) + ";");
      }
    }

    withDirectB2s.setSelected(false);
    withPuppack.setSelected(false);
    withRom.setSelected(false);
    withTopper.setSelected(false);
    withWheel.setSelected(false);
    withAltSound.setSelected(false);
    withAltColor.setSelected(false);
    withTutorial.setSelected(false);
    withPov.setSelected(false);
  }

  protected boolean hasFilter() {
    return !predicateFactory.isResetted();
  }

  @Override
  public Predicate<VpsTableModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    boolean noVPX = client.getEmulatorService().getVpxGameEmulators().isEmpty();
    return predicateFactory.buildPredicate(noVPX, searchTerm);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tableFilters.managedProperty().bindBidirectional(tableFilters.visibleProperty());

    installedOnlyCheckbox.setSelected(predicateFactory.isInstalledOnly());
    installedOnlyCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setInstalledOnly(newValue);
      // cannot select two opposite checkboxes
      notInstalledOnlyCheckbox.setDisable(newValue);
      applyFilters();
    });
    notInstalledOnlyCheckbox.setSelected(predicateFactory.isNotInstalledOnly());
    notInstalledOnlyCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setNotInstalledOnly(newValue);
      // cannot select two opposite checkboxes
      installedOnlyCheckbox.setDisable(newValue);
      applyFilters();
    });

    lastUpdateDate.valueProperty().setValue(predicateFactory.getLastUpdateDate());
    clearDateBtn.setVisible(predicateFactory.getLastUpdateDate() != null);
    lastUpdateDate.valueProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setLastUpdateDate(newValue);
      applyFilters();
      clearDateBtn.setVisible(newValue != null);
    });
    // prevent selection of future dates
    lastUpdateDate.setDayCellFactory(param -> new DateCell() {
        @Override
        public void updateItem(LocalDate date, boolean empty) {
          super.updateItem(date, empty);
          setDisable(empty || date.compareTo(LocalDate.now()) > 0 );
        }
    });
    // install specific formatter
    LocalDateStringConverter dsc = new LocalDateStringConverter(FormatStyle.MEDIUM);
    lastUpdateDate.setConverter(dsc);
    // prevent modification in the text field, and force user to use the picker
    lastUpdateDate.getEditor().setEditable(false);
    // click on textfield opens popup
    lastUpdateDate.getEditor().setOnMouseClicked((e) -> lastUpdateDate.show());

    author.textProperty().setValue(predicateFactory.getAuthor());
    author.textProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setAuthor(newValue);
      applyFilters();
    });
    withAuthorInOtherAssetsToo.setSelected(predicateFactory.isSearchAuthorInOtherAssetsToo());
    withAuthorInOtherAssetsToo.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setSearchAuthorInOtherAssetsToo(newValue);
      applyFilters();
    });

    manufacturer.textProperty().setValue(predicateFactory.getManufacturer());
    manufacturer.textProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setManufacturer(newValue);
      applyFilters();
    });
    theme.textProperty().setValue(predicateFactory.getTheme());
    theme.textProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setTheme(newValue);
      applyFilters();
    });

    withDirectB2s.setSelected(predicateFactory.isWithBackglass());
    withDirectB2s.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithBackglass(newValue);
      applyFilters();
    });
    withPuppack.setSelected(predicateFactory.isWithPupPack());
    withPuppack.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithPupPack(newValue);
      applyFilters();
    });
    withRom.setSelected(predicateFactory.isWithRom());
    withRom.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithRom(newValue);
      applyFilters();
    });
    withTopper.setSelected(predicateFactory.isWithTopper());
    withTopper.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithTopper(newValue);
      applyFilters();
    });
    withWheel.setSelected(predicateFactory.isWithWheel());
    withWheel.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithWheel(newValue);
      applyFilters();
    });
    withAltSound.setSelected(predicateFactory.isWithAltSound());
    withAltSound.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithAltSound(newValue);
      applyFilters();
    });
    withAltColor.setSelected(predicateFactory.isWithAltColor());
    withAltColor.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithAltColor(newValue);
      applyFilters();
    });
    withTutorial.setSelected(predicateFactory.isWithTutorial());
    withTutorial.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithTutorial(newValue);
      applyFilters();
    });
    withPov.setSelected(predicateFactory.isWithPov());
    withPov.selectedProperty().addListener((observable, oldValue, newValue) -> {
      predicateFactory.setWithPov(newValue);
      applyFilters();
    });

    // create Checkboxes for features
    featureCheckboxes = new LinkedHashMap<>();
    for (String feature: VpsFeatures.forFilter()) {

      Label badge = new Label(feature);
      badge.getStyleClass().add("white-label");
      badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
      badge.getStyleClass().add("vps-badge-button");

      badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature, predicateFactory.isSelectedFeature(feature)) + ";");

      badge.setOnMouseClicked(e -> {
        boolean selected = predicateFactory.toggleFeature(feature);
        badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature, selected) + ";");
        applyFilters();
      });
      predicateFactory.registerFeature(feature);
      featureCheckboxes.put(feature, badge);
      featuresPanel.getChildren().add(badge);
    }
  }

  public void bindEmulatorCombo(ComboBox<VpsTableFormat> emulatorCombo) {
    emulatorCombo.valueProperty().addListener(new ChangeListener<VpsTableFormat>() {
      @Override
      public void changed(ObservableValue<? extends VpsTableFormat> observable, VpsTableFormat oldValue, VpsTableFormat newValue) {
        predicateFactory.setTableFormats(newValue.getAbbrev());
        applyFilters();
      }
    });
  }
}

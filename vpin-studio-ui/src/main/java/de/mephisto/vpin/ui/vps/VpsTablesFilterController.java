package de.mephisto.vpin.ui.vps;

import static de.mephisto.vpin.ui.Studio.client;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import de.mephisto.vpin.ui.vps.VpsTablesController.VpsTableModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class VpsTablesFilterController extends BaseFilterController<VpsTable, VpsTableModel> implements Initializable {

  @FXML
  private VBox tableFilters;

  @FXML
  private CheckBox installedOnlyCheckbox;
  @FXML
  private CheckBox notInstalledOnlyCheckbox;

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

  //------------------------------------------------------

  private VpsTablesPredicateFactory predicateFactory = new VpsTablesPredicateFactory();
  
  //------------------------------------------------------

  @Override
  protected void resetFilters() {
    this.predicateFactory = new VpsTablesPredicateFactory();

    installedOnlyCheckbox.setSelected(false);
    notInstalledOnlyCheckbox.setSelected(false);

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
  }

  protected boolean hasFilter() {
    return !predicateFactory.isResetted();
  }

  @Override
  public Predicate<VpsTableModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    boolean noVPX = client.getFrontendService().getVpxGameEmulators().isEmpty();
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
}

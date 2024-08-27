package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarVpsController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class VpsTablesSidebarController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTablesSidebarController.class);

  @FXML
  private Accordion vpsTableAccordion;

  @FXML
  private TitledPane tableDetailsPane;

  @FXML
  private Hyperlink ipdbLink;

  @FXML
  private Label nameLabel;

  @FXML
  private Label manufacturer;

  @FXML
  private Label year;

  @FXML
  private Label updated;

  @FXML
  private Label players;

  @FXML
  private Label typeLabel;

  @FXML
  private Label theme;

  @FXML
  private FlowPane features;

  @FXML
  private VBox dataRoot;

  @FXML
  private VBox detailsBox;

  @FXML
  private Button openBtn;

  private Optional<VpsTable> selection;

  private boolean initialized = false;


  @FXML
  private void onVpsBtn() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        if (selection.isPresent()) {
          String url = VPS.getVpsTableUrl(selection.get().getId());
          Desktop.getDesktop().browse(new URI(url));
        }
      }
      catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  private void init() {
    //TODO mpf!
    if (!initialized) {
      initialized = true;
      detailsBox.setPrefHeight(Studio.stage.getHeight() - 150);
      Studio.stage.heightProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
          detailsBox.setPrefHeight(newValue.intValue() - 150);
        }
      });
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpsTableAccordion.managedProperty().bindBidirectional(vpsTableAccordion.visibleProperty());
    vpsTableAccordion.setExpandedPane(tableDetailsPane);
  }

  public void setVisible(boolean b) {
    this.vpsTableAccordion.setVisible(b);
  }

  @FXML
  private void onIpdbLink() {
    if (selection.isPresent()) {
      Studio.browse(selection.get().getIpdbUrl());
    }
  }


  public void setTable(Optional<VpsTable> selection) {
    this.init();
    this.openBtn.setDisable(selection.isEmpty());

    this.selection = selection;

    ipdbLink.setText("-");
    nameLabel.setText("-");
    manufacturer.setText("-");
    typeLabel.setText("-");
    theme.setText("-");
    features.getChildren().removeAll(features.getChildren());

    year.setText("-");
    updated.setText("-");
    players.setText("-");

    dataRoot.getChildren().removeAll(dataRoot.getChildren());

    if (selection.isPresent()) {
      VpsTable table = selection.get();
      updated.setText(DateFormat.getDateInstance().format(new Date(table.getUpdatedAt())));

      if (table.getFeatures() != null) {
        for (String feature : table.getFeatures()) {
          if (feature.equalsIgnoreCase("fp") || feature.equalsIgnoreCase("vpx")) {
            continue;
          }

          Label badge = new Label(feature);
          badge.getStyleClass().add("white-label");
          badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
          badge.getStyleClass().add("vps-badge");
          badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature) + ";");
          features.getChildren().add(badge);
        }
      }

      typeLabel.setText(StringUtils.isEmpty(table.getType()) ? "-" : table.getType());
      theme.setText(table.getTheme() == null ? "-" : String.join(", ", table.getTheme()));
      nameLabel.setText(StringUtils.isEmpty(table.getDisplayName()) ? "-" : table.getDisplayName());
      manufacturer.setText(StringUtils.isEmpty(table.getManufacturer()) ? "-" : table.getManufacturer());
      year.setText(String.valueOf(table.getYear()));
      players.setText(String.valueOf(table.getPlayers()));
      ipdbLink.setText(StringUtils.isEmpty(table.getIpdbUrl()) ? "-" : table.getIpdbUrl());

      TablesSidebarVpsController.addTablesSection(dataRoot, "Table Version", null, VpsDiffTypes.tableNewVersionVPX, table, table.getTableFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "Backglasses", null, VpsDiffTypes.b2s, table.getB2sFiles(), false);

      TablesSidebarVpsController.addSection(dataRoot, "ALT Sound", null, VpsDiffTypes.altSound, table.getAltSoundFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "ALT Color", null, VpsDiffTypes.altColor, table.getAltColorFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "PUP Pack", null, VpsDiffTypes.pupPack, table.getPupPackFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "ROM", null, VpsDiffTypes.rom, table.getRomFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "Sound", null, VpsDiffTypes.sound, table.getSoundFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "Topper", null, VpsDiffTypes.topper, table.getTopperFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "Tutorials", null, VpsDiffTypes.tutorial, table.getTutorialFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "POV", null, VpsDiffTypes.pov, table.getPovFiles(), false);
      TablesSidebarVpsController.addSection(dataRoot, "Wheel Art", null, VpsDiffTypes.wheel, table.getWheelArtFiles(), false);
    }
  }
}

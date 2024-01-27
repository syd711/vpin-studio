package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.tables.TablesSidebarVpsController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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
  private HBox features;

  @FXML
  private VBox dataRoot;

  @FXML
  private VBox detailsBox;

  private Optional<VpsTable> selection;

  private boolean initialized = false;

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
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(selection.get().getIpdbUrl()));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }


  public void setTable(Optional<VpsTable> selection) {
    this.init();

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

      if(table.getFeatures()!= null) {
        for (String feature : table.getFeatures()) {
          Label badge = new Label(feature);
          badge.getStyleClass().add("white-label");
          badge.setTooltip(new Tooltip(feature));
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


      TablesSidebarVpsController.addSection(dataRoot, "PUP Pack", table.getPupPackFiles());
      TablesSidebarVpsController.addSection(dataRoot, "Backglasses", table.getB2sFiles());
      TablesSidebarVpsController.addSection(dataRoot, "ALT Sound", table.getAltSoundFiles());
      TablesSidebarVpsController.addSection(dataRoot, "ALT Color", table.getAltColorFiles());
      TablesSidebarVpsController.addSection(dataRoot, "ROM", table.getRomFiles());
      TablesSidebarVpsController.addSection(dataRoot, "Sound", table.getSoundFiles());
      TablesSidebarVpsController.addSection(dataRoot, "Topper", table.getTopperFiles());

      TablesSidebarVpsController.addSection(dataRoot, "Wheel Art", table.getWheelArtFiles());
      TablesSidebarVpsController.addSection(dataRoot, "POV", table.getPovFiles());
      TablesSidebarVpsController.addTutorialsSection(dataRoot, "Tutorials", table.getTutorialFiles());
    }
  }
}

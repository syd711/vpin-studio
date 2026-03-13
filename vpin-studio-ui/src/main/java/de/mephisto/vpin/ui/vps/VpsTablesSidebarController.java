package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TablesSidebarVpsController;
import de.mephisto.vpin.ui.tables.panels.BaseSideBarController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static de.mephisto.vpin.ui.Studio.client;

public class VpsTablesSidebarController extends BaseSideBarController<VpsTable> implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Debouncer debouncer = new Debouncer();

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
  private TextArea commentsArea;

  @FXML
  private Button openBtn;


  private Optional<VpsTable> selection;

  private boolean initialized = false;
  private CommentChangeListener commentChangeListener;


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

    WidgetFactory.addToTextListener(nameLabel);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpsTableAccordion.managedProperty().bindBidirectional(vpsTableAccordion.visibleProperty());
    vpsTableAccordion.setExpandedPane(tableDetailsPane);

    commentChangeListener = new CommentChangeListener();
  }

  @Override
  public void setVisible(boolean b) {
    this.vpsTableAccordion.setVisible(b);
  }

  @FXML
  private void onIpdbLink() {
    if (selection.isPresent()) {
      Studio.browse(selection.get().getIpdbUrl());
    }
  }


  public void setTable(Optional<VpsTable> selection, VpsTablesPredicateFactory predicate) {
    this.init();
    this.openBtn.setDisable(selection.isEmpty());

    this.commentsArea.textProperty().removeListener(commentChangeListener);
    this.commentsArea.setDisable(selection.isEmpty());
    this.commentsArea.setText("");

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
      this.commentsArea.setText(table.getComment());
      this.commentsArea.textProperty().addListener(commentChangeListener);

      for (String feature : table.getAllFeatures()) {
        Label badge = new Label(feature);
        badge.getStyleClass().add("white-label");
        badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
        badge.getStyleClass().add("vps-badge");

        if (predicate.isFeaturesFilterEmpty()) {
          badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature) + ";");
        }
        else {
          boolean isFilter = predicate.isSelectedFeature(feature);
          badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature, isFilter) + ";");
        }

        features.getChildren().add(badge);
      }


      typeLabel.setText(StringUtils.isEmpty(table.getType()) ? "-" : table.getType());
      theme.setText(table.getTheme() == null ? "-" : String.join(", ", table.getTheme()));
      nameLabel.setText(StringUtils.isEmpty(table.getDisplayName()) ? "-" : table.getDisplayName());
      manufacturer.setText(StringUtils.isEmpty(table.getManufacturer()) ? "-" : table.getManufacturer());
      year.setText(String.valueOf(table.getYear()));
      players.setText(String.valueOf(table.getPlayers()));
      ipdbLink.setText(StringUtils.isEmpty(table.getIpdbUrl()) ? "-" : table.getIpdbUrl());

      TablesSidebarVpsController.addTablesSection(dataRoot, "Table Version", null, VpsDiffTypes.tableNewVersionVPX, table, false, predicate.buildTableVersionPredicate());

      Predicate<VpsAuthoredUrls> authoredUrlsPredicate = predicate.buildAuthoredUrlsPredicate();
      TablesSidebarVpsController.addSection(dataRoot, "Backglasses", null, VpsDiffTypes.b2s, table.getB2sFiles(), false, authoredUrlsPredicate);

      TablesSidebarVpsController.addSection(dataRoot, "ALT Sound", null, VpsDiffTypes.altSound, table.getAltSoundFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "ALT Color", null, VpsDiffTypes.altColor, table.getAltColorFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "PUP Pack", null, VpsDiffTypes.pupPack, table.getPupPackFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "ROM", null, VpsDiffTypes.rom, table.getRomFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "Sound", null, VpsDiffTypes.sound, table.getSoundFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "Topper", null, VpsDiffTypes.topper, table.getTopperFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "Tutorials", null, VpsDiffTypes.tutorial, table.getTutorialFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "POV", null, VpsDiffTypes.pov, table.getPovFiles(), false, authoredUrlsPredicate);
      TablesSidebarVpsController.addSection(dataRoot, "Wheel Art", null, VpsDiffTypes.wheel, table.getWheelArtFiles(), false, authoredUrlsPredicate);
    }
  }

  class CommentChangeListener implements ChangeListener<String> {
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      debouncer.debounce("vpsComment", () -> {
        if (selection.isPresent()) {
          VpsTable vpsTable = selection.get();
          vpsTable.setComment(newValue);
          client.getVpsService().saveVpsData(vpsTable);
          EventManager.getInstance().notifyVpsTableChange(vpsTable.getId());
        }
      }, 300);
    }
  }
}

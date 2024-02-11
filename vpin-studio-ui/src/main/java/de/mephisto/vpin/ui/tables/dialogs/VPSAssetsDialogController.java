package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarVpsController;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class VPSAssetsDialogController implements DialogController, AutoCompleteTextFieldChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPSAssetsDialogController.class);

  @FXML
  private TextField nameField;

  @FXML
  private Button openBtn;

  @FXML
  private VBox dataRoot;

  @FXML
  private Button okButton;

  private GameRepresentation game;
  private AutoCompleteTextField autoCompleteNameField;


  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE) && !StringUtils.isEmpty(game.getExtTableId())) {
      try {
        desktop.browse(new URI(VPS.getVpsTableUrl(game.getExtTableId())));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }


  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  public void setGame(GameRepresentation game) {
    this.game = game;

    List<VpsTable> tables = VPS.getInstance().getTables();
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    if (!StringUtils.isEmpty(game.getExtTableId())) {
      VpsTable tableById = VPS.getInstance().getTableById(game.getExtTableId());
      if (tableById != null) {
        refreshTableView(tableById);
        return;
      }
    }

    String term = game.getGameDisplayName();
    List<VpsTable> vpsTables = VPS.getInstance().find(term, game.getRom());
    if (!vpsTables.isEmpty()) {
      VpsTable vpsTable = vpsTables.get(0);
      refreshTableView(vpsTable);
    }
  }

  private void refreshTableView(VpsTable vpsTable) {
    dataRoot.getChildren().removeAll(dataRoot.getChildren());
    autoCompleteNameField.reset();

    TablesSidebarVpsController.addSection(dataRoot, "PUP Pack", vpsTable.getPupPackFiles());

    TablesSidebarVpsController.addSection(dataRoot, "Backglasses", vpsTable.getB2sFiles());

    TablesSidebarVpsController.addSection(dataRoot, "ALT Sound", vpsTable.getAltSoundFiles());

    TablesSidebarVpsController.addSection(dataRoot, "ALT Color", vpsTable.getAltColorFiles());

    TablesSidebarVpsController.addSection(dataRoot, "Sound", vpsTable.getSoundFiles());

    TablesSidebarVpsController.addSection(dataRoot, "Topper", vpsTable.getTopperFiles());

    TablesSidebarVpsController.addSection(dataRoot, "ROM", vpsTable.getRomFiles());

    TablesSidebarVpsController.addSection(dataRoot, "Wheel Art", vpsTable.getWheelArtFiles());

    TablesSidebarVpsController.addSection(dataRoot, "POV", vpsTable.getPovFiles());


    autoCompleteNameField.setText(vpsTable.getDisplayName());

    if (dataRoot.getChildren().isEmpty()) {
      Label emptyLabel = WidgetFactory.createDefaultLabel("No additional assets found.");
      dataRoot.getChildren().add(emptyLabel);
    }
  }

  @Override
  public void onChange(String value) {
    List<VpsTable> tables = VPS.getInstance().getTables();
    Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    if (selectedEntry.isPresent()) {
      VpsTable vpsTable = selectedEntry.get();
      refreshTableView(vpsTable);
    }
  }

}

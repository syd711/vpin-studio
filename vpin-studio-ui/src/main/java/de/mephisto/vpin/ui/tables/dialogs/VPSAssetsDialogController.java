package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class VPSAssetsDialogController implements DialogController, AutoCompleteTextFieldChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    Studio.browse(VPS.getVpsTableUrl(game.getExtTableId()));
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

  public void setGame(Stage stage, GameRepresentation game) {
    this.game = game;

    List<VpsTable> tables = client.getVpsService().getTables();
    List<String> collect = new ArrayList<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    if (!StringUtils.isEmpty(game.getExtTableId())) {
      VpsTable tableById = client.getVpsService().getTableById(game.getExtTableId());
      if (tableById != null) {
        refreshTableView(tableById);
        return;
      }
    }

    String term = game.getGameDisplayName();
    List<VpsTable> vpsTables = client.getVpsService().find(term, game.getRom());
    if (!vpsTables.isEmpty()) {
      VpsTable vpsTable = vpsTables.get(0);
      refreshTableView(vpsTable);
    }
  }

  private void refreshTableView(VpsTable vpsTable) {
    dataRoot.getChildren().removeAll(dataRoot.getChildren());
    autoCompleteNameField.reset();

    TablesSidebarVpsController.addSection(dataRoot, "PUP Pack", game, VpsDiffTypes.pupPack, vpsTable.getPupPackFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "Backglasses", game, VpsDiffTypes.b2s, vpsTable.getB2sFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "ALT Sound", game, VpsDiffTypes.altSound, vpsTable.getAltSoundFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "ALT Color", game, VpsDiffTypes.altColor, vpsTable.getAltColorFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "Sound", game, VpsDiffTypes.sound, vpsTable.getSoundFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "Topper", game, VpsDiffTypes.topper, vpsTable.getTopperFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "ROM", game, VpsDiffTypes.rom, vpsTable.getRomFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "Wheel Art", game, VpsDiffTypes.wheel, vpsTable.getWheelArtFiles(), false, null);
    TablesSidebarVpsController.addSection(dataRoot, "POV", game, VpsDiffTypes.pov, vpsTable.getPovFiles(), false, null);

    autoCompleteNameField.setText(vpsTable.getDisplayName());

    if (dataRoot.getChildren().isEmpty()) {
      Label emptyLabel = WidgetFactory.createDefaultLabel("No additional assets found.");
      dataRoot.getChildren().add(emptyLabel);
    }
  }

  @Override
  public void onChange(String value) {
    List<VpsTable> tables = client.getVpsService().getTables();
    Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    if (selectedEntry.isPresent()) {
      VpsTable vpsTable = selectedEntry.get();
      refreshTableView(vpsTable);
    }
  }

}

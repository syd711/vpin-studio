package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarVpsController;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

public class VPSTableSelectorDialogController implements DialogController, AutoCompleteTextFieldChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPSTableSelectorDialogController.class);

  @FXML
  private TextField nameField;

  @FXML
  private VBox dataRoot;

  @FXML
  private Button okButton;

  private AutoCompleteTextField autoCompleteNameField;

  @FXML
  private ComboBox<VpsTableVersion> versionsCombo;


  @Override
  public void onDialogCancel() {

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

  private void refreshTableView(VpsTable vpsTable) {
    dataRoot.getChildren().removeAll(dataRoot.getChildren());
    autoCompleteNameField.reset();

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

      List<VpsTableVersion> tableFiles = vpsTable.getTableFiles();
      versionsCombo.setItems(FXCollections.observableList(tableFiles));
      if(!tableFiles.isEmpty()) {
        versionsCombo.getSelectionModel().select(0);
      }

      refreshTableView(vpsTable);
    }
  }

  public void setTournament(ManiaTournamentRepresentation tournamentRepresentation) {
    List<VpsTable> tables = VPS.getInstance().getTables();
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);
  }

  public List<VpsTableVersion> getSelection() {
    return null;
  }
}

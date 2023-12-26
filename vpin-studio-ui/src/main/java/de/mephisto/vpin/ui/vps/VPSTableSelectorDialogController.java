package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class VPSTableSelectorDialogController implements DialogController, AutoCompleteTextFieldChangeListener, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPSTableSelectorDialogController.class);

  @FXML
  private TextField nameField;

  @FXML
  private Button okButton;

  private AutoCompleteTextField autoCompleteNameField;

  @FXML
  private ComboBox<VpsTableVersion> versionsCombo;
  private VpsTable vpsTable;


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
    autoCompleteNameField.reset();
    autoCompleteNameField.setText(vpsTable.getDisplayName());
    okButton.setDisable(versionsCombo.getValue() == null);
  }

  @Override
  public void onChange(String value) {
    List<VpsTable> tables = VPS.getInstance().getTables();
    Optional<VpsTable> first = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    if (first.isPresent()) {
      vpsTable = first.get();

      List<VpsTableVersion> tableFiles = vpsTable.getTableFiles();
      versionsCombo.setItems(FXCollections.emptyObservableList());
      versionsCombo.setItems(FXCollections.observableList(tableFiles));
      if (!tableFiles.isEmpty()) {
        versionsCombo.getSelectionModel().select(0);
      }

      refreshTableView(vpsTable);
    }
  }

  public VpsSelection getSelection() {
    return new VpsSelection(vpsTable, versionsCombo.getValue());
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    List<VpsTable> tables = VPS.getInstance().getTables();
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    versionsCombo.setCellFactory(c -> new VpsTableVersionCell());
    versionsCombo.setButtonCell(new VpsTableVersionCell());
    versionsCombo.valueProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue == null));

    Platform.runLater(() -> nameField.requestFocus());
  }
}

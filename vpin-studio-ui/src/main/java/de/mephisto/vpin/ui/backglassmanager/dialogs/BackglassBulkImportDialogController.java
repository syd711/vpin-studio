package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class BackglassBulkImportDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassBulkImportDialogController.class);

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private TableView<BackglassBulkImportItem> tableView;

  @FXML
  private TableColumn<BackglassBulkImportItem, Boolean> columnSelected;

  @FXML
  private TableColumn<BackglassBulkImportItem, String> columnDisplayName;

  @FXML
  private TableColumn<BackglassBulkImportItem, String> columnBackglass;

  @FXML
  private Label statusLabel;

  @FXML
  private Button importBtn;

  private CheckBox selectAllCheckbox;

  private Stage stage;

  /**
   * Incremented on every load so that outdated results are ignored.
   */
  private int loadCounter = 0;

  /**
   * The emulators are loaded asynchronously, the preselection is applied once they are available.
   */
  private int preselectedEmulatorId = -1;

  @FXML
  private RadioButton backglassRadio;

  @FXML
  private RadioButton dmdRadio;

  @FXML
  private ToggleGroup importTypeGroup;

  @FXML
  private Label descriptionLabel;

  private BackglassBulkImportType type = BackglassBulkImportType.BACKGLASS;

  @FXML
  private void onCancelClick(ActionEvent e) {
    stage.close();
  }

  @FXML
  private void onImportClick(ActionEvent e) {
    List<BackglassBulkImportItem> selection = tableView.getItems().stream().filter(BackglassBulkImportItem::isSelected).collect(Collectors.toList());
    if (selection.isEmpty()) {
      return;
    }
    ProgressDialog.createProgressDialog(stage, new BackglassBulkImportProgressModel(selection, type));
    stage.close();
  }

  @Override
  public void onDialogCancel() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setEditable(true);
    updateTypeTexts();

    selectAllCheckbox = new CheckBox();
    selectAllCheckbox.getStyleClass().add("default-text");
    selectAllCheckbox.setSelected(true);
    selectAllCheckbox.setOnAction(e -> {
      boolean selected = selectAllCheckbox.isSelected();
      tableView.getItems().forEach(item -> item.selectedProperty().set(selected));
    });
    columnSelected.setGraphic(selectAllCheckbox);
    columnSelected.setCellValueFactory(cell -> cell.getValue().selectedProperty());
    columnSelected.setCellFactory(col -> new TableCell<>() {
      private final CheckBox checkBox = new CheckBox();
      private BooleanProperty bound;

      {
        checkBox.getStyleClass().add("default-text");
        setAlignment(Pos.CENTER);
      }

      @Override
      protected void updateItem(Boolean selected, boolean empty) {
        super.updateItem(selected, empty);
        if (bound != null) {
          checkBox.selectedProperty().unbindBidirectional(bound);
          bound = null;
        }
        if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
          setGraphic(null);
          return;
        }
        bound = getTableView().getItems().get(getIndex()).selectedProperty();
        checkBox.selectedProperty().bindBidirectional(bound);
        setGraphic(checkBox);
      }
    });
    columnDisplayName.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getDisplayName()));
    columnBackglass.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getBackglass().getName()));

    importTypeGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
      if (newValue == null) {
        return;
      }
      BackglassBulkImportType selected = newValue == dmdRadio ? BackglassBulkImportType.DMD : BackglassBulkImportType.BACKGLASS;
      if (selected != type) {
        type = selected;
        updateTypeTexts();
        GameEmulatorRepresentation emulator = emulatorCombo.getValue();
        if (emulator != null) {
          loadTables(emulator);
        }
      }
    });

    importBtn.setDisable(true);
    emulatorCombo.setDisable(true);
    emulatorCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
      if (newValue != null) {
        loadTables(newValue);
      }
    });

    JFXFuture.supplyAsync(() -> client.getEmulatorService().getValidatedGameEmulators().stream()
        .filter(e -> e.isVpxEmulator() || e.isFpEmulator() || e.isFxEmulator())
        .collect(Collectors.toList()))
        .thenAcceptLater(emulators -> {
          emulatorCombo.setItems(FXCollections.observableList(emulators));
          emulatorCombo.setDisable(false);
          emulators.stream().filter(e -> e.getId() == preselectedEmulatorId).findFirst()
              .or(() -> emulators.stream().findFirst())
              .ifPresent(emulatorCombo::setValue);
        });
  }

  public void setData(Stage stage, int preselectedEmulatorId, BackglassBulkImportType type) {
    this.stage = stage;
    this.preselectedEmulatorId = preselectedEmulatorId;
    // selecting the radio triggers the table search if an emulator is already selected
    (type == BackglassBulkImportType.DMD ? dmdRadio : backglassRadio).setSelected(true);
  }

  private void updateTypeTexts() {
    boolean dmd = type == BackglassBulkImportType.DMD;
    descriptionLabel.setText(Messages.get(dmd ? "backglass.bulk_import.description_dmd" : "backglass.bulk_import.description"));
    tableView.setPlaceholder(new Label(Messages.get(dmd ? "backglass.bulk_import.no_tables_dmd" : "backglass.bulk_import.no_tables")));
  }

  private void updateSelectAllCheckbox() {
    List<BackglassBulkImportItem> items = tableView.getItems();
    selectAllCheckbox.setSelected(!items.isEmpty() && items.stream().allMatch(BackglassBulkImportItem::isSelected));
  }

  private void loadTables(GameEmulatorRepresentation emulator) {
    int counter = ++loadCounter;
    importBtn.setDisable(true);
    tableView.setItems(FXCollections.observableArrayList());
    statusLabel.setText(Messages.get("backglass.bulk_import.loading"));

    JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getBackglasses().stream()
        .filter(b -> b.getEmulatorId() == emulator.getId() && b.getGameId() > 0)
        .collect(Collectors.toList())
    ).thenAcceptLater(candidates -> {
      if (counter != loadCounter) {
        return;
      }
      BackglassBulkImportSearchModel model = new BackglassBulkImportSearchModel(candidates, type);
      ProgressDialog.createProgressDialog(stage, model);
      if (counter != loadCounter) {
        return; // another emulator has been selected while searching
      }

      List<BackglassBulkImportItem> items = model.getResult();
      items.sort((i1, i2) -> i1.getDisplayName().compareToIgnoreCase(i2.getDisplayName()));
      items.forEach(item -> item.selectedProperty().addListener((obs, o, n) -> updateSelectAllCheckbox()));
      tableView.setItems(FXCollections.observableList(items));
      importBtn.setDisable(items.isEmpty());
      statusLabel.setText(Messages.get("backglass.bulk_import.found", items.size()));
      updateSelectAllCheckbox();
    }).onErrorLater(ex -> {
      LOG.error("Failed to load tables for backglass bulk import: {}", ex.getMessage(), ex);
      statusLabel.setText("");
    });
  }
}

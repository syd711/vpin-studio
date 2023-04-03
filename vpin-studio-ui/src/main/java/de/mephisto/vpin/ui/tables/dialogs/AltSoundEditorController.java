package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.AltSoundEntry;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AltSoundEditorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundEditorController.class);

  private AltSound altSound;
  private boolean result = false;

  @FXML
  private TableView<AltSoundEntryModel> tableView;


  @FXML
  private TableColumn<AltSoundEntryModel, String> columnId;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnName;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnChannel;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnDuck;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnGain;

  @FXML
  private TableColumn<AltSoundEntryModel, Boolean> columnLoop;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnFilename;

  @FXML
  private TableColumn<AltSoundEntryModel, Boolean> checkColumn;

  @FXML
  private CheckBox allCheckbox;

  @FXML
  private ComboBox<Integer> channelFilterCombo;

  @FXML
  private ComboBox<String> nameFilterCombo;

  @FXML
  private ComboBox<String> filenameFilterCombo;

  @FXML
  private ComboBox<String> loopedFilterCombo;

  @FXML
  private Spinner<Integer> channelSpinner;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label duckLabel;

  @FXML
  private Label gainLabel;

  @FXML
  private Slider duckVolume;

  @FXML
  private Slider gainVolume;

  @FXML
  private CheckBox loopedCheckbox;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onRestoreClick() {

  }

  @FXML
  private void onSaveClick() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    columnId.setCellValueFactory(cellData -> cellData.getValue().id);
    columnName.setCellValueFactory(cellData -> cellData.getValue().name);
    columnChannel.setCellValueFactory(cellData -> cellData.getValue().channel);
    columnDuck.setCellValueFactory(cellData -> cellData.getValue().duck);
    columnGain.setCellValueFactory(cellData -> cellData.getValue().gain);
    columnFilename.setCellValueFactory(cellData -> cellData.getValue().filename);

    columnLoop.setCellValueFactory(cd -> cd.getValue().looped);
    columnLoop.setCellFactory(CheckBoxTableCell.forTableColumn(columnLoop));
    checkColumn.setCellValueFactory(cd -> cd.getValue().active);
    checkColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkColumn));

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    loopedFilterCombo.setItems(FXCollections.observableList(Arrays.asList(null, "Yes", "No")));
    loopedFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    channelFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    nameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    filenameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());


    allCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        tableView.getSelectionModel().selectAll();
      }
      else {
        tableView.getSelectionModel().clearSelection();
      }
      refreshEditorForSelection();
    });

    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<AltSoundEntryModel>) c -> refreshEditorForSelection());

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10);
    channelSpinner.setValueFactory(factory);
    channelSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.channel.set(newValue));
      tableView.refresh();
    });

    loopedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.looped.set(newValue));
      tableView.refresh();
    });

    duckVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      int value1 = ((Double) newValue).intValue();
      selectedItems.forEach(i -> {
        i.duck.set(value1);
      });
      duckLabel.setText(String.valueOf(value1));
      tableView.refresh();
    });

    gainVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      int value1 = ((Double) newValue).intValue();
      selectedItems.forEach(i -> {
        i.gain.set(value1);
      });
      gainLabel.setText(String.valueOf(value1));
      tableView.refresh();
    });
  }

  public void setAltSound(AltSound altSound) {
    this.altSound = altSound;

    List<Integer> channels = new ArrayList<>(altSound.getChannels());
    channels.add(0, null);
    channelFilterCombo.setItems(FXCollections.observableList(channels));

    List<String> names = new ArrayList<>();
    names.add(null);
    List<String> filenames = new ArrayList<>();
    filenames.add(null);

    for (AltSoundEntry entry : altSound.getEntries()) {
      if (!names.contains(entry.getName())) {
        names.add(entry.getName());
      }
      if (!filenames.contains(entry.getFilename())) {
        filenames.add(entry.getFilename());
      }
    }

    nameFilterCombo.setItems(FXCollections.observableList(names));
    filenameFilterCombo.setItems(FXCollections.observableList(filenames));

    refresh();
  }

  private void refresh() {
    List<AltSoundEntryModel> filtered = new ArrayList<>();
    List<AltSoundEntry> allEntries = altSound.getEntries();

    String nameFilter = this.nameFilterCombo.getValue();
    Integer channelFilter = this.channelFilterCombo.getValue();
    String filenameFiler = this.filenameFilterCombo.getValue();
    String loopedFilter = this.loopedFilterCombo.getValue();

    for (AltSoundEntry entry : allEntries) {
      if (nameFilter != null && !entry.getName().equals(nameFilter)) {
        continue;
      }

      if (filenameFiler != null && !entry.getFilename().equals(filenameFiler)) {
        continue;
      }

      if (loopedFilter != null) {
        if (loopedFilter.equals("No") && entry.getLoop() != 0) {
          continue;
        }
      }

      if (channelFilter != null && entry.getChannel() != channelFilter) {
        continue;
      }

      filtered.add(new AltSoundEntryModel(entry.getId(), entry.getName(), entry.getFilename(), entry.getChannel(), entry.getDuck(), entry.getGain(), entry.getLoop() != 0));
    }

    ObservableList<AltSoundEntryModel> entries = FXCollections.observableList(filtered);
    tableView.setItems(entries);
    tableView.refresh();

    tableView.getSelectionModel().selectAll();
    refreshEditorForSelection();
  }

  private void refreshEditorForSelection() {
    ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    entriesLabel.setText(String.valueOf(selectedItems.size()));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  private static class AltSoundEntryModel {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty filename;
    private final IntegerProperty duck;
    private final IntegerProperty gain;
    private final IntegerProperty channel;
    private final BooleanProperty looped;
    private final BooleanProperty active;

    private AltSoundEntryModel(String id, String name, String filename, int channel, int duck, int gain, boolean looped) {
      this.id = new SimpleStringProperty(id);
      this.name = new SimpleStringProperty(name);
      this.filename = new SimpleStringProperty(filename);
      this.looped = new SimpleBooleanProperty(looped);
      this.channel = new SimpleIntegerProperty(channel);
      this.duck = new SimpleIntegerProperty(duck);
      this.gain = new SimpleIntegerProperty(gain);
      this.active = new SimpleBooleanProperty(true);
    }
  }
}

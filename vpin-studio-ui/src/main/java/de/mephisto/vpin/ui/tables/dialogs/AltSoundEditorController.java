package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.AltSoundEntry;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class AltSoundEditorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundEditorController.class);

  private GameRepresentation game;
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
  private TableColumn<AltSoundEntryModel, String> columnLoop;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnStop;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnFilename;

  @FXML
  private TextField searchText;

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
  private CheckBox stopCheckbox;

  @FXML
  private Button saveBtn;

  @Override
  public void onDialogCancel() {
    result = false;
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onRestoreClick() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Revert Changes?", "Revert all changes and reload original ALT sound data?", null, "Yes, revert changes");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      AltSound orig = Studio.client.getAltSound(this.game.getId());
      this.altSound.setEntries(orig.getEntries());
      this.refresh();
    }
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    try {
      Studio.client.saveAltSound(game.getId(), this.altSound);
    } catch (Exception ex) {
      LOG.error("Failed to save ALT sound: " + ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save ALT sound: " + ex.getMessage());
    }
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    columnId.setCellValueFactory(cellData -> cellData.getValue().id);
    columnName.setCellValueFactory(cellData -> cellData.getValue().name);
    columnChannel.setCellValueFactory(cellData -> cellData.getValue().channel);
    columnDuck.setCellValueFactory(cellData -> cellData.getValue().duck);
    columnGain.setCellValueFactory(cellData -> cellData.getValue().gain);
    columnFilename.setCellValueFactory(cellData -> cellData.getValue().filename);

    columnLoop.setCellValueFactory(cellData -> {
      AltSoundEntryModel value = cellData.getValue();
      if (value.looped.get()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    columnStop.setCellValueFactory(cellData -> {
      AltSoundEntryModel value = cellData.getValue();
      if (value.stop.get()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    loopedFilterCombo.setItems(FXCollections.observableList(Arrays.asList(null, "Yes", "No")));
    loopedFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    channelFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    nameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    filenameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    searchText.textProperty().addListener((observable, oldValue, newValue) -> refresh());

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

    stopCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.stop.set(newValue));
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

    tableView.setPlaceholder(new Label("               No matching entries found!\n" +
        "Adapt the filter criteria to find matching entries."));
  }

  public void setAltSound(GameRepresentation game, AltSound altSound) {
    this.game = game;
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
    tableView.getSelectionModel().clearSelection();
  }

  private void refresh() {
    List<AltSoundEntryModel> filtered = new ArrayList<>();
    List<AltSoundEntry> allEntries = altSound.getEntries();

    String nameFilter = this.nameFilterCombo.getValue();
    Integer channelFilter = this.channelFilterCombo.getValue();
    String filenameFiler = this.filenameFilterCombo.getValue();
    String loopedFilter = this.loopedFilterCombo.getValue();
    String term = this.searchText.getText();

    for (AltSoundEntry entry : allEntries) {
      if (!StringUtils.isEmpty(term) && !entry.getFilename().toLowerCase().contains(term) && !entry.getName().toLowerCase().contains(term)) {
        continue;
      }

      if (nameFilter != null && !entry.getName().equals(nameFilter)) {
        continue;
      }

      if (filenameFiler != null && !entry.getFilename().equals(filenameFiler)) {
        continue;
      }

      if (loopedFilter != null) {
        if (loopedFilter.equals("Yes") && entry.getLoop() != 100) {
          continue;
        }
      }

      if (channelFilter != null && entry.getChannel() != channelFilter) {
        continue;
      }

      filtered.add(new AltSoundEntryModel(entry));
    }

    ObservableList<AltSoundEntryModel> entries = FXCollections.observableList(filtered);
    tableView.setItems(entries);
    tableView.refresh();

    refreshEditorForSelection();
  }

  private void refreshEditorForSelection() {
    ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    entriesLabel.setText(String.valueOf(selectedItems.size()));

    gainVolume.setDisable(selectedItems.isEmpty());
    duckLabel.setDisable(selectedItems.isEmpty());
    loopedCheckbox.setDisable(selectedItems.isEmpty());
    stopCheckbox.setDisable(selectedItems.isEmpty());
    channelSpinner.setDisable(selectedItems.isEmpty());

    if (!selectedItems.isEmpty()) {
      AltSoundEntryModel altSoundEntryModel = selectedItems.get(0);
      gainVolume.valueProperty().set(altSoundEntryModel.gain.get());
      gainLabel.setText("" + altSoundEntryModel.gain.getValue());
      duckVolume.valueProperty().set(altSoundEntryModel.duck.get());
      duckLabel.setText("" + altSoundEntryModel.duck.getValue());
    }
    else {
      gainVolume.valueProperty().set(0);
      gainLabel.setText("-");
      duckVolume.valueProperty().set(0);
      duckLabel.setText("-");
    }
  }

  private static class AltSoundEntryModel {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty filename;
    private final IntegerProperty duck;
    private final IntegerProperty gain;
    private final IntegerProperty channel;
    private final BooleanProperty looped;
    private final BooleanProperty stop;

    private AltSoundEntryModel(AltSoundEntry entry) {
      this.id = new SimpleStringProperty(entry.getId());
      this.name = new SimpleStringProperty(entry.getName());
      this.filename = new SimpleStringProperty(entry.getFilename());
      this.looped = new SimpleBooleanProperty(entry.getLoop() == 100);
      this.stop = new SimpleBooleanProperty(entry.getStop() == 1);
      this.looped.addListener((observable, oldValue, newValue) -> {
        if (newValue) {
          entry.setLoop(100);
        }
        else {
          entry.setLoop(0);
        }
      });

      this.stop.addListener((observable, oldValue, newValue) -> {
        if (newValue) {
          entry.setStop(1);
        }
        else {
          entry.setStop(0);
        }
      });

      this.channel = new SimpleIntegerProperty(entry.getChannel());
      this.channel.addListener((observable, oldValue, newValue) -> {
        entry.setChannel((Integer) newValue);
      });


      this.duck = new SimpleIntegerProperty(entry.getDuck());
      this.duck.addListener((observable, oldValue, newValue) -> entry.setDuck((Integer) newValue));

      this.gain = new SimpleIntegerProperty(entry.getGain());
      this.gain.addListener((observable, oldValue, newValue) -> entry.setGain((Integer) newValue));
    }
  }
}

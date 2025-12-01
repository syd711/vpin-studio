package de.mephisto.vpin.ui.tables.editors;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;

public class AltSoundEditorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private GameRepresentation game;
  private AltSound altSound;

  @FXML
  private BorderPane root;

  @FXML
  private TableView<AltSoundEntryModel> tableView;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnId;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnName;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnChannel;

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
  private TableColumn<AltSoundEntryModel, String> columnFilesize;

  @FXML
  private TextField searchText;

  @FXML
  private ComboBox<String> nameFilterCombo;

  @FXML
  private ComboBox<String> filenameFilterCombo;

  @FXML
  private ComboBox<String> loopedFilterCombo;

  @FXML
  private TextField channelField;

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

  private ChangeListener<String> channelFieldChangeListener;
  private ChangeListener<Boolean> loopCheckboxChangeListener;
  private ChangeListener<Boolean> stopCheckboxChangeListener;
  private TablesController tablesController;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.tablesController.getEditorRootStack().getChildren().remove(root);
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    try {
      Studio.client.getAltSoundService().saveAltSound(game.getId(), this.altSound);
      EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
    } catch (Exception ex) {
      LOG.error("Failed to save ALT sound: " + ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save ALT sound: " + ex.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    columnId.setCellValueFactory(cellData -> cellData.getValue().id);
    columnName.setCellValueFactory(cellData -> cellData.getValue().name);
    columnChannel.setCellValueFactory(cellData -> {
      AltSoundEntryModel entry = cellData.getValue();
      if (entry.channel.get() == null) {
        return new SimpleObjectProperty("");
      }
      return new SimpleObjectProperty(entry.channel.get());
    });
    columnDuck.setCellValueFactory(cellData -> cellData.getValue().duck);
    columnGain.setCellValueFactory(cellData -> cellData.getValue().gain);
    columnFilesize.setCellValueFactory(cellData -> cellData.getValue().size);
    columnFilename.setCellValueFactory(cellData -> {
      AltSoundEntryModel entry = cellData.getValue();
      Label label = new Label(entry.filename.getValue());

      if (!entry.exists.getValue()) {
        label.setStyle(ERROR_STYLE + "-fx-font-weight: bold;");
      }
      return new SimpleObjectProperty(label);
    });

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
    nameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    filenameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    searchText.textProperty().addListener((observable, oldValue, newValue) -> refresh());

    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<AltSoundEntryModel>) c -> refreshEditorForSelection());

    channelFieldChangeListener = (observable, oldValue, newValue) -> {
      try {
        int v = Integer.parseInt(newValue);
        if (v > 256) {
          channelField.setText(String.valueOf(oldValue));
          return;
        }
        ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
        selectedItems.forEach(i -> i.channel.set(String.valueOf(v)));
        tableView.refresh();
      } catch (Exception e) {
        String value = String.valueOf(oldValue);
        if (String.valueOf(newValue).equals("")) {
          value = "";
        }

        channelField.setText(value);
        ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
        selectedItems.forEach(i -> i.channel.set(newValue));
        tableView.refresh();
      }
    };
    channelField.textProperty().addListener(channelFieldChangeListener);

    loopCheckboxChangeListener = (observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.looped.set(newValue));
      tableView.refresh();
    };
    loopedCheckbox.selectedProperty().addListener(loopCheckboxChangeListener);

    stopCheckboxChangeListener = (observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.stop.set(newValue));
      tableView.refresh();
    };
    stopCheckbox.selectedProperty().addListener(stopCheckboxChangeListener);

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
    String filenameFiler = this.filenameFilterCombo.getValue();
    String loopedFilter = this.loopedFilterCombo.getValue();
    String term = this.searchText.getText();

    for (AltSoundEntry entry : allEntries) {
      if (!StringUtils.isEmpty(term) && !entry.getFilename().toLowerCase().contains(term) && !entry.getName().toLowerCase().contains(term)) {
        continue;
      }

      if (nameFilter != null && !entry.getName().equalsIgnoreCase(nameFilter)) {
        continue;
      }

      if (filenameFiler != null && !entry.getFilename().equalsIgnoreCase(filenameFiler)) {
        continue;
      }

      if (loopedFilter != null) {
        if (loopedFilter.equals("Yes") && entry.getLoop() != 100) {
          continue;
        }
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

    channelField.textProperty().removeListener(channelFieldChangeListener);
    loopedCheckbox.selectedProperty().removeListener(loopCheckboxChangeListener);
    stopCheckbox.selectedProperty().removeListener(stopCheckboxChangeListener);

    gainVolume.setDisable(selectedItems.isEmpty());
    gainLabel.setDisable(selectedItems.isEmpty());
    duckVolume.setDisable(selectedItems.isEmpty());
    duckLabel.setDisable(selectedItems.isEmpty());

    loopedCheckbox.setDisable(selectedItems.isEmpty());
    stopCheckbox.setDisable(selectedItems.isEmpty());
    channelField.setDisable(selectedItems.isEmpty());

    if (!selectedItems.isEmpty()) {
      AltSoundEntryModel altSoundEntryModel = selectedItems.get(0);
      gainVolume.valueProperty().set(altSoundEntryModel.gain.get());
      gainLabel.setText("" + altSoundEntryModel.gain.getValue());
      duckVolume.valueProperty().set(altSoundEntryModel.duck.get());
      duckLabel.setText("" + altSoundEntryModel.duck.getValue());

      channelField.setText(altSoundEntryModel.channel.get() == null ? "" : altSoundEntryModel.channel.getValue());
      loopedCheckbox.selectedProperty().set(altSoundEntryModel.looped.getValue());
      stopCheckbox.selectedProperty().set(altSoundEntryModel.stop.getValue());
    }
    else {
      gainVolume.valueProperty().set(0);
      gainLabel.setText("-");
      duckVolume.valueProperty().set(0);
      duckLabel.setText("-");

      channelField.setText("");
      loopedCheckbox.setSelected(false);
      stopCheckbox.setSelected(false);
    }

    channelField.textProperty().addListener(channelFieldChangeListener);
    loopedCheckbox.selectedProperty().addListener(loopCheckboxChangeListener);
    stopCheckbox.selectedProperty().addListener(stopCheckboxChangeListener);
  }

  private static class AltSoundEntryModel {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty filename;
    private final IntegerProperty duck;
    private final IntegerProperty gain;
    private final SimpleStringProperty channel;
    private final BooleanProperty looped;
    private final BooleanProperty stop;
    private final BooleanProperty exists;
    private final StringProperty size;

    private AltSoundEntryModel(AltSoundEntry entry) {
      this.id = new SimpleStringProperty(entry.getId());
      this.name = new SimpleStringProperty(entry.getName());
      this.filename = new SimpleStringProperty(entry.getFilename());
      this.looped = new SimpleBooleanProperty(entry.getLoop() == 100);
      this.stop = new SimpleBooleanProperty(entry.getStop() == 1);
      this.size= new SimpleStringProperty(FileUtils.readableFileSize(entry.getSize()));
      this.exists = new SimpleBooleanProperty(entry.isExists());
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

      this.channel = new SimpleStringProperty(entry.getChannel());
      this.channel.addListener((observable, oldValue, newValue) -> {
        entry.setChannel(newValue);
      });

      this.duck = new SimpleIntegerProperty(entry.getDuck());
      this.duck.addListener((observable, oldValue, newValue) -> entry.setDuck((Integer) newValue));

      this.gain = new SimpleIntegerProperty(entry.getGain());
      this.gain.addListener((observable, oldValue, newValue) -> entry.setGain((Integer) newValue));
    }
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }
}

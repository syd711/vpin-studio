package de.mephisto.vpin.ui.tables.editors;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class AltSound2EditorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(AltSound2EditorController.class);

  private GameRepresentation game;
  private AltSound altSound;

  @FXML
  private BorderPane root;

  @FXML
  private TableView<AltSoundEntryModel> tableView;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnId;

  @FXML
  private TableColumn<AltSoundEntryModel, StringProperty> columnType;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnDuck;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnGain;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnFilename;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnFilesize;

  @FXML
  private TextField searchText;

  @FXML
  private ComboBox<String> typeFilterCombo;

  @FXML
  private ComboBox<String> filenameFilterCombo;

  @FXML
  private ComboBox<AltSound2DuckingProfile> profilesCombo;

  @FXML
  private TextField channelField;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label gainLabel;

  @FXML
  private Slider gainVolume;

  @FXML
  private Button saveBtn;

  private ChangeListener<String> channelFieldChangeListener;
  private TablesController tablesController;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.tablesController.getEditorRootStack().getChildren().remove(root);
  }

  @FXML
  private void onRestoreClick() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Revert Changes?", "Revert all changes and restore initial ALT sound data?", null, "Yes, revert changes");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      AltSound orig = Studio.client.getAltSoundService().getAltSound(this.game.getId());
      this.altSound.setEntries(orig.getEntries());
      this.refresh();
    }
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    try {
      Studio.client.getAltSoundService().saveAltSound(game.getId(), this.altSound);
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
    columnType.setCellValueFactory(cellData -> {
      AltSoundEntryModel entry = cellData.getValue();
      final StringProperty value = entry.channel;
      return Bindings.createObjectBinding(() -> value);
    });
    columnType.setCellFactory(col -> {
      TableCell<AltSoundEntryModel, StringProperty> c = new TableCell<>();
      final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableList(AltSound2SampleType.toStringValues()));
      c.itemProperty().addListener((observable, oldValue, newValue) -> {
        if (oldValue != null) {
          comboBox.valueProperty().unbindBidirectional(oldValue);
        }
        if (newValue != null) {
          comboBox.valueProperty().bindBidirectional(newValue);
        }
      });
      c.graphicProperty().bind(Bindings.when(c.emptyProperty()).then((Node) null).otherwise(comboBox));
      return c;
    });

    columnDuck.setCellValueFactory(cellData -> cellData.getValue().duck);
    columnGain.setCellValueFactory(cellData -> cellData.getValue().gain);
    columnFilesize.setCellValueFactory(cellData -> cellData.getValue().size);
    columnFilename.setCellValueFactory(cellData -> {
      AltSoundEntryModel entry = cellData.getValue();
      Label label = new Label(entry.filename.getValue());

      if (!entry.exists.getValue()) {
        label.setStyle("-fx-font-color: #FF3333;-fx-text-fill:#FF3333; -fx-font-weight: bold;");
      }
      return new SimpleObjectProperty(label);
    });
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    profilesCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    typeFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
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

    typeFilterCombo.setItems(FXCollections.observableList(AltSound2SampleType.toStringValues()));
    filenameFilterCombo.setItems(FXCollections.observableList(filenames));

    refreshProfiles();
    refresh();
    tableView.getSelectionModel().clearSelection();
  }

  private void refreshProfiles() {
    List<AltSound2DuckingProfile> profiles = new ArrayList<>();
    profiles.addAll(altSound.getCalloutDuckingProfiles());
    profiles.addAll(altSound.getMusicDuckingProfiles());
    profiles.addAll(altSound.getOverlayDuckingProfiles());
    profiles.addAll(altSound.getSfxDuckingProfiles());
    profiles.addAll(altSound.getSoloDuckingProfiles());
    profilesCombo.setItems(FXCollections.observableList(profiles));
  }

  private void refresh() {
    List<AltSoundEntryModel> filtered = new ArrayList<>();
    List<AltSoundEntry> allEntries = altSound.getEntries();

    String typeFilter = this.typeFilterCombo.getValue();
    String filenameFiler = this.filenameFilterCombo.getValue();
    AltSound2DuckingProfile value = this.profilesCombo.getValue();
    String term = this.searchText.getText();

    for (AltSoundEntry entry : allEntries) {
      if (!StringUtils.isEmpty(term) && !entry.getFilename().toLowerCase().contains(term) && !entry.getName().toLowerCase().contains(term)) {
        continue;
      }

      if (typeFilter != null && !entry.getChannel().equalsIgnoreCase(typeFilter)) {
        continue;
      }

      if (filenameFiler != null && !entry.getFilename().equals(filenameFiler)) {
        continue;
      }

      if (value != null) {
        entry.getChannel();
//        if (entry.getDuck()) {
//          continue;
//        }
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

    gainVolume.setDisable(selectedItems.isEmpty());
    gainLabel.setDisable(selectedItems.isEmpty());

    channelField.setDisable(selectedItems.isEmpty());

    if (!selectedItems.isEmpty()) {
      AltSoundEntryModel altSoundEntryModel = selectedItems.get(0);
      gainVolume.valueProperty().set(altSoundEntryModel.gain.get());
      gainLabel.setText("" + altSoundEntryModel.gain.getValue());

      channelField.setText(altSoundEntryModel.channel.get() == null ? "" : altSoundEntryModel.channel.getValue());
    }
    else {
      gainVolume.valueProperty().set(0);
      gainLabel.setText("-");
      channelField.setText("");
    }

    channelField.textProperty().addListener(channelFieldChangeListener);
  }

  private static class AltSoundEntryModel {
    private final StringProperty id;
    private final StringProperty filename;
    private final IntegerProperty duck;
    private final IntegerProperty gain;
    private final SimpleStringProperty channel;
    private final BooleanProperty exists;
    private final StringProperty size;

    private AltSoundEntryModel(AltSoundEntry entry) {
      this.id = new SimpleStringProperty(entry.getId());
      this.filename = new SimpleStringProperty(entry.getFilename());
      this.size= new SimpleStringProperty(FileUtils.readableFileSize(entry.getSize()));
      this.exists = new SimpleBooleanProperty(entry.isExists());

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

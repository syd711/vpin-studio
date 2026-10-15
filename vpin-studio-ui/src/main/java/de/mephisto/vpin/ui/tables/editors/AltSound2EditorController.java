package de.mephisto.vpin.ui.tables.editors;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSound2SampleType;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class AltSound2EditorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private GameRepresentation game;
  private AltSound altSound;

  @FXML
  private BorderPane root;

  @FXML
  private ComboBox<String> channelCombo;

  @FXML
  private TableView<AltSoundEntryModel> tableView;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnId;

  @FXML
  private TableColumn<AltSoundEntryModel, String> columnType;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnDuck;

  @FXML
  private TableColumn<AltSoundEntryModel, Number> columnGain;

  @FXML
  private TableColumn<AltSoundEntryModel, AltSoundEntryModel> columnFilename;

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
  private ComboBox<AltSound2DuckingProfile> duckingProfileCombo;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label gainLabel;

  @FXML
  private Slider gainVolume;

  @FXML
  private Button editBtn;

  @FXML
  private Button editGroupBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editFileBtn;

  @FXML
  private CheckBox romVolCtrlCheckbox;

  @FXML
  private CheckBox recordSoundCmdsCheckbox;

  @FXML
  private Spinner<Integer> skipCountSpinner;

  private ChangeListener<String> channelComboChangeListener;
  private ChangeListener<AltSound2DuckingProfile> duckingProfileComboChangeListener;
  private TablesController tablesController;

  private MediaPlayer mediaPlayer;
  private String playingFilename;

  @FXML
  private void onCancelClick(ActionEvent e) {
    stopPlayback();
    this.tablesController.getEditorRootStack().getChildren().remove(root);
  }

  @FXML
  private void onSampleTypes() {
    String value = this.typeFilterCombo.getValue();
    if (value != null) {
      TableDialogs.openAltSound2SampleTypeDialog(altSound, AltSound2SampleType.valueOf(value.toLowerCase()));
      this.refreshProfiles();
      this.refresh();
    }
  }

  @FXML
  private void onFileEdit() {
    AltSoundEntryModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String updatedName = WidgetFactory.showInputDialog(Studio.stage, Messages.get("dialog.rename_entry"), Messages.get("dialog.enter_the_updated_filename"), Messages.get("dialog.the_itself_won_t_re_renamed_only"), null, selectedItem.filename.get());
      if (!StringUtils.isEmpty(updatedName) && FileUtils.isValidFilename(updatedName)) {
        selectedItem.filename.setValue(updatedName);
        this.refresh();
      }
    }
  }

  @FXML
  private void onProfileDelete() {
    AltSound2DuckingProfile value = this.profilesCombo.getValue();
    if (value != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_profile") + value.getType().name().toUpperCase() + "\" " + value.getId() + "?",
          "All related audio files will be resetted to the default profile.",
          "If no other profile exists for this sample type, it will be removed from all ducking lists.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//        altSound.getMusicDuckingProfiles().remove(value);
//        altSound.getSoloDuckingProfiles().remove(value);
        altSound.getSfxDuckingProfiles().remove(value);
        altSound.getOverlayDuckingProfiles().remove(value);
        altSound.getCalloutDuckingProfiles().remove(value);

        if (altSound.getCalloutDuckingProfiles().isEmpty()) {
          altSound.getSfx().removeDuck(value.getType());
          altSound.getSfxDuckingProfiles().forEach(p -> p.removeProfileValue(value.getType()));
          altSound.getOverlay().removeDuck(value.getType());
          altSound.getOverlayDuckingProfiles().forEach(p -> p.removeProfileValue(value.getType()));
        }

        if (altSound.getSfxDuckingProfiles().isEmpty()) {
          altSound.getCallout().removeDuck(value.getType());
          altSound.getCalloutDuckingProfiles().forEach(p -> p.removeProfileValue(value.getType()));
          altSound.getOverlay().removeDuck(value.getType());
          altSound.getOverlayDuckingProfiles().forEach(p -> p.removeProfileValue(value.getType()));
        }

        if (altSound.getOverlayDuckingProfiles().isEmpty()) {
          altSound.getCallout().removeDuck(value.getType());
          altSound.getCalloutDuckingProfiles().forEach(p -> p.removeProfileValue(value.getType()));
          altSound.getSfx().removeDuck(value.getType());
          altSound.getSfxDuckingProfiles().forEach(p -> p.removeProfileValue(value.getType()));
        }

        List<AltSoundEntry> entries = altSound.getEntries();
        for (AltSoundEntry entry : entries) {
          if (entry.getChannel().equalsIgnoreCase(value.getType().name()) && entry.getDuck() == value.getId()) {
            entry.setDuck(0);
            LOG.info("Setting " + entry.getFilename() + " to ducking profile 0");
          }
        }

        refreshProfiles();
        refresh();
      }
    }
  }

  @FXML
  private void onProfileEdit() {
    TableDialogs.openAltSound2ProfileEditor(altSound, this.profilesCombo.getValue());
    refresh();
  }

  @FXML
  private void onProfileAdd() {
    AltSound2DuckingProfile profile = TableDialogs.openAltSound2ProfileEditor(altSound, null);
    if (profile != null) {
      altSound.addProfile(profile);
      refreshProfiles();
      refresh();
    }
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    try {
      client.getAltSoundService().saveAltSound(game.getId(), this.altSound);
      EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
      this.altSound = client.getAltSoundService().getAltSound(game.getId());
      this.refresh();
    } catch (Exception ex) {
      LOG.error(Messages.get("dialog.failed_to_save_alt_sound") + ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to_save_alt_sound") + ex.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    columnId.setCellValueFactory(cellData -> cellData.getValue().id);
    columnType.setCellValueFactory(cellData -> cellData.getValue().channel);


    columnDuck.setCellValueFactory(cellData -> cellData.getValue().duck);
    columnGain.setCellValueFactory(cellData -> cellData.getValue().gain);
    columnFilesize.setCellValueFactory(cellData -> cellData.getValue().size);
    columnFilename.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    columnFilename.setCellFactory(param -> new TableCell<>() {
      private final FontIcon playIcon = new FontIcon();
      private final Button playBtn = new Button();
      private final Label label = new Label();
      private final HBox box = new HBox(6, playBtn, label);

      {
        playIcon.setIconSize(14);
        playIcon.setIconColor(Paint.valueOf("#FFFFFF"));
        playBtn.setGraphic(playIcon);
        playBtn.getStyleClass().add("ghost-button-tiny");
        box.setAlignment(Pos.CENTER_LEFT);
      }

      @Override
      protected void updateItem(AltSoundEntryModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          return;
        }

        String filename = item.filename.get();
        boolean playing = filename != null && filename.equals(playingFilename);
        playIcon.setIconLiteral(playing ? "bi-stop" : "bi-play");
        playBtn.setTooltip(new Tooltip(playing ? Messages.get("tables.editors.editor_altsound.stop") : Messages.get("tables.editors.editor_altsound2.play")));
        playBtn.setDisable(!item.exists.get());
        playBtn.setOnAction(event -> togglePlayback(filename));

        label.setText(filename);
        label.setStyle(item.exists.get() ? null : ERROR_STYLE + "-fx-font-weight: bold;");
        setGraphic(box);
      }
    });
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


    List<String> altSoundSampleTypes = new ArrayList<>(AltSound2SampleType.toStringValues());
    altSoundSampleTypes.addFirst(null);
    typeFilterCombo.setItems(FXCollections.observableList(altSoundSampleTypes));

    profilesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      editBtn.setDisable(newValue == null);
      deleteBtn.setDisable(newValue == null);
      refresh();
    });

    editGroupBtn.setDisable(true);
    typeFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      editGroupBtn.setDisable(newValue == null);
      refresh();
    });
    filenameFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    searchText.textProperty().addListener((observable, oldValue, newValue) -> refresh());

    channelCombo.setItems(FXCollections.observableList(AltSound2SampleType.toStringValues()));
    channelComboChangeListener = (observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.channel.set(newValue));
      tableView.refresh();
      refreshEditorForSelection();
    };
    channelCombo.valueProperty().addListener(channelComboChangeListener);

    duckingProfileComboChangeListener = (observableValue, altSound2DuckingProfile, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      selectedItems.forEach(i -> i.duck.set(newValue != null ? newValue.getId() : 0));
      tableView.refresh();
    };
    duckingProfileCombo.valueProperty().addListener(duckingProfileComboChangeListener);


    gainVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      int value1 = ((Double) newValue).intValue();
      selectedItems.forEach(i -> {
        i.gain.set(value1);
      });
      gainLabel.setText(String.valueOf(value1));
      tableView.refresh();
    });

    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<AltSoundEntryModel>) c -> refreshEditorForSelection());
    tableView.setPlaceholder(new Label("               " + Messages.get("tables.editors.altsound2.no_matching_entries_found")));
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
    filenameFilterCombo.setItems(FXCollections.observableList(filenames));

    romVolCtrlCheckbox.setSelected(altSound.isRomVolumeControl());
    romVolCtrlCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> altSound.setRomVolumeControl(t1));

    recordSoundCmdsCheckbox.setSelected(altSound.isRecordSoundCmds());
    recordSoundCmdsCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> altSound.setRecordSoundCmds(t1));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, altSound.getCommandSkipCount());
    skipCountSpinner.setValueFactory(factory);
    skipCountSpinner.valueProperty().addListener((observable, oldValue, newValue) -> altSound.setCommandSkipCount(Integer.parseInt(String.valueOf(newValue))));

    refreshProfiles();
    refresh();

    if (!filenames.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  private void refreshProfiles() {
    List<AltSound2DuckingProfile> profiles = new ArrayList<>();
    profiles.add(null);
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
    AltSound2DuckingProfile profile = this.profilesCombo.getValue();
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

      if (profile != null) {
        String channel = entry.getChannel();
        if (!profile.getType().name().equalsIgnoreCase(channel)) {
          continue;
        }

        if (entry.getDuck() != profile.getId()) {
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
    duckingProfileCombo.valueProperty().removeListener(duckingProfileComboChangeListener);
    channelCombo.valueProperty().removeListener(channelComboChangeListener);

    ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    entriesLabel.setText(String.valueOf(selectedItems.size()));
    editFileBtn.setDisable(selectedItems.size() != 1);

    gainVolume.setDisable(selectedItems.isEmpty());
    gainLabel.setDisable(selectedItems.isEmpty());

    if (!selectedItems.isEmpty()) {
      AltSoundEntryModel altSoundEntryModel = selectedItems.getFirst();

      boolean hasMismatch = false;
      if (selectedItems.size() > 1) {
        hasMismatch = selectedItems.stream().anyMatch(item -> !item.channel.get().equalsIgnoreCase(altSoundEntryModel.channel.get()));
      }
      gainVolume.setDisable(hasMismatch);
      channelCombo.setDisable(hasMismatch);
      gainLabel.setDisable(hasMismatch);
      duckingProfileCombo.setDisable(hasMismatch);

      gainVolume.valueProperty().set(altSoundEntryModel.gain.get());
      if (!hasMismatch) {
        gainLabel.setText("" + altSoundEntryModel.gain.getValue());
        channelCombo.setValue(altSoundEntryModel.channel.getValue().toUpperCase());

        String name = altSoundEntryModel.channel.get();
        AltSound2SampleType altSound2SampleType = AltSound2SampleType.valueOf(name.toLowerCase());
        List<AltSound2DuckingProfile> profiles = new ArrayList<>(altSound.getProfiles(altSound2SampleType));
        duckingProfileCombo.setDisable(profiles.isEmpty());
        profiles.addFirst(null);
        duckingProfileCombo.setItems(FXCollections.observableList(profiles));

        Optional<AltSound2DuckingProfile> first = profiles.stream().filter(p -> p != null && p.getId() == altSoundEntryModel.duck.get()).findFirst();
        if (first.isPresent()) {
          duckingProfileCombo.valueProperty().set(first.get());
        }
      }
      else {
        gainLabel.setText("-");
      }

    }
    else {
      duckingProfileCombo.setValue(null);
      duckingProfileCombo.setDisable(true);
      channelCombo.setValue(null);
      channelCombo.setDisable(true);
      gainVolume.valueProperty().set(0);
      gainLabel.setText("-");
    }

    duckingProfileCombo.valueProperty().addListener(duckingProfileComboChangeListener);
    channelCombo.valueProperty().addListener(channelComboChangeListener);
  }

  private void togglePlayback(String filename) {
    boolean wasPlaying = filename.equals(playingFilename);
    stopPlayback();
    if (wasPlaying) {
      return;
    }

    String url = client.getAltSoundService().getAudioUrl(game.getId(), filename);
    try {
      Media media = new Media(url);
      MediaPlayer player = new MediaPlayer(media);
      player.setCycleCount(1);
      player.setMute(false);
      player.setOnError(() -> {
        LOG.warn("Media player error: {}, URL: {}", player.getError(), url);
        if (player == mediaPlayer) {
          stopPlayback();
        }
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), "Failed to play \"" + filename + "\": " + player.getError().getMessage());
      });
      player.setOnEndOfMedia(() -> {
        if (player == mediaPlayer) {
          stopPlayback();
        }
      });
      player.setAutoPlay(true);

      mediaPlayer = player;
      playingFilename = filename;
    }
    catch (Exception e) {
      LOG.error("Failed to play ALT sound file {}: {}", url, e.getMessage(), e);
    }
    tableView.refresh();
  }

  private void stopPlayback() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.dispose();
      mediaPlayer = null;
    }
    playingFilename = null;
    tableView.refresh();
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
      this.size = new SimpleStringProperty(FileUtils.readableFileSize(entry.getSize()));
      this.exists = new SimpleBooleanProperty(entry.isExists());

      this.channel = new SimpleStringProperty(entry.getChannel());
      this.channel.addListener((observable, oldValue, newValue) -> {
        entry.setChannel(newValue);
      });

      this.duck = new SimpleIntegerProperty(entry.getDuck());
      this.duck.addListener((observable, oldValue, newValue) -> entry.setDuck((Integer) newValue));

      this.gain = new SimpleIntegerProperty(entry.getGain());
      this.gain.addListener((observable, oldValue, newValue) -> entry.setGain((Integer) newValue));

      this.filename.addListener((observableValue, s, newValue) -> entry.setFilename(newValue));
    }

  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }
}

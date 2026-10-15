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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class AltSound2EditorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Pattern ID_PREFIX_PATTERN = Pattern.compile("^0x[0-9a-fA-F]+[-_ ]*");

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
  private ChangeListener<Number> gainVolumeChangeListener;
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
    int index = tableView.getSelectionModel().getSelectedIndex();
    if (index >= 0) {
      tableView.edit(index, columnFilename);
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
      this.refreshFilenameFilter();
      this.refreshProfiles();
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
    columnFilename.setCellFactory(param -> new FilenameCell());
    columnId.setEditable(false);
    columnType.setEditable(false);
    columnDuck.setEditable(false);
    columnGain.setEditable(false);
    columnFilesize.setEditable(false);
    tableView.setEditable(true);
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


    gainVolumeChangeListener = (observable, oldValue, newValue) -> {
      ObservableList<AltSoundEntryModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
      int value1 = newValue.intValue();
      selectedItems.forEach(i -> {
        i.gain.set(value1);
      });
      gainLabel.setText(String.valueOf(value1));
      tableView.refresh();
    };
    gainVolume.valueProperty().addListener(gainVolumeChangeListener);

    // registered once, they always write into the currently edited ALT sound (which is reloaded on save)
    romVolCtrlCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> altSound.setRomVolumeControl(t1));
    recordSoundCmdsCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> altSound.setRecordSoundCmds(t1));
    skipCountSpinner.valueProperty().addListener((observable, oldValue, newValue) -> altSound.setCommandSkipCount(newValue));

    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<AltSoundEntryModel>) c -> refreshEditorForSelection());
    tableView.setPlaceholder(new Label("               " + Messages.get("tables.editors.altsound2.no_matching_entries_found")));
  }

  public void setAltSound(GameRepresentation game, AltSound altSound) {
    this.game = game;
    this.altSound = altSound;

    refreshFilenameFilter();

    romVolCtrlCheckbox.setSelected(altSound.isRomVolumeControl());
    recordSoundCmdsCheckbox.setSelected(altSound.isRecordSoundCmds());
    skipCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, altSound.getCommandSkipCount()));

    refreshProfiles();
    refresh();

    if (!tableView.getItems().isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  private void refreshFilenameFilter() {
    String selection = filenameFilterCombo.getValue();
    List<String> filenames = new ArrayList<>();
    filenames.add(null);
    for (AltSoundEntry entry : altSound.getEntries()) {
      if (!filenames.contains(entry.getFilename())) {
        filenames.add(entry.getFilename());
      }
    }
    filenameFilterCombo.setItems(FXCollections.observableList(filenames));
    filenameFilterCombo.setValue(filenames.contains(selection) ? selection : null);
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
    String term = StringUtils.lowerCase(StringUtils.trimToNull(this.searchText.getText()));

    for (AltSoundEntry entry : allEntries) {
      if (term != null && !StringUtils.containsIgnoreCase(entry.getFilename(), term) && !StringUtils.containsIgnoreCase(entry.getName(), term)
          && !StringUtils.containsIgnoreCase(entry.getId(), term)) {
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
    gainVolume.valueProperty().removeListener(gainVolumeChangeListener);

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
        duckingProfileCombo.valueProperty().set(first.orElse(null));
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
    gainVolume.valueProperty().addListener(gainVolumeChangeListener);
  }

  /**
   * Renames the audio file of the given entry on the server, the ID prefix and the file extension are kept.
   */
  private void renameAudioFile(AltSoundEntryModel item, String newBaseName) {
    String[] parts = splitFilename(item);
    String baseName = StringUtils.trimToEmpty(newBaseName);
    if (baseName.equals(parts[1])) {
      return;
    }
    if (baseName.isEmpty() || !FileUtils.isValidFilename(baseName)) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("tables.editors.editor_altsound2.invalid_filename", baseName));
      return;
    }

    String filename = item.filename.get();
    String newFilename = parts[0] + baseName + parts[2];
    if (filename.equals(playingFilename)) {
      stopPlayback();
    }

    try {
      client.getAltSoundService().renameAudioFile(game.getId(), filename, newFilename);
      // the server has updated the CSV, apply the rename to the unsaved editor state too
      for (AltSoundEntry entry : altSound.getEntries()) {
        if (filename.equalsIgnoreCase(entry.getFilename())) {
          entry.setFilename(newFilename);
        }
      }
      refreshFilenameFilter();
      refresh();
    }
    catch (Exception e) {
      LOG.error("Failed to rename ALT sound file {}: {}", filename, e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, Messages.get("tables.editors.editor_altsound2.rename_failed", filename), e.getMessage());
    }
  }

  /**
   * Splits the filename of the entry into [folder + ID prefix, editable name, extension],
   * e.g. "0x0049-end_of_missions.ogg" into ["0x0049-", "end_of_missions", ".ogg"].
   */
  private static String[] splitFilename(AltSoundEntryModel item) {
    String filename = StringUtils.defaultString(item.filename.get());
    int folderIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1;
    String folder = filename.substring(0, folderIndex);
    String name = filename.substring(folderIndex);

    int extIndex = name.lastIndexOf('.');
    String extension = extIndex > 0 ? name.substring(extIndex) : "";
    String baseName = name.substring(0, name.length() - extension.length());

    String idPrefix = "";
    String id = item.id.get();
    if (!StringUtils.isEmpty(id) && StringUtils.startsWithIgnoreCase(baseName, id)) {
      int end = id.length();
      while (end < baseName.length() && "-_ ".indexOf(baseName.charAt(end)) >= 0) {
        end++;
      }
      idPrefix = baseName.substring(0, end);
    }
    else {
      Matcher matcher = ID_PREFIX_PATTERN.matcher(baseName);
      if (matcher.find()) {
        idPrefix = matcher.group();
      }
    }
    return new String[]{folder + idPrefix, baseName.substring(idPrefix.length()), extension};
  }

  @Nullable
  private static String getValidationMessage(AltSoundEntryModel item) {
    if (!item.exists.get()) {
      return Messages.get("tables.editors.editor_altsound2.file_not_found", item.filename.get());
    }
    if (item.rawSize == 0) {
      return Messages.get("tables.editors.editor_altsound2.file_empty", item.filename.get());
    }
    return null;
  }

  /**
   * Shows the play button and the filename, while editing only the name between the ID prefix and the extension is editable.
   */
  private class FilenameCell extends TableCell<AltSoundEntryModel, AltSoundEntryModel> {
    private final FontIcon playIcon = new FontIcon();
    private final Button playBtn = new Button();
    private final Label label = new Label();
    private final HBox box = new HBox(6, playBtn, label);

    private final Label prefixLabel = new Label();
    private final TextField textField = new TextField();
    private final Label extensionLabel = new Label();
    private final HBox editBox = new HBox(2, prefixLabel, textField, extensionLabel);

    FilenameCell() {
      playIcon.setIconSize(14);
      playIcon.setIconColor(Paint.valueOf("#FFFFFF"));
      playBtn.setGraphic(playIcon);
      playBtn.getStyleClass().add("ghost-button-tiny");
      box.setAlignment(Pos.CENTER_LEFT);

      prefixLabel.getStyleClass().add("default-text");
      extensionLabel.getStyleClass().add("default-text");
      editBox.setAlignment(Pos.CENTER_LEFT);
      HBox.setHgrow(textField, Priority.ALWAYS);

      textField.setOnAction(e -> commitRename());
      textField.setOnKeyPressed(e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
          e.consume();
        }
      });
      textField.focusedProperty().addListener((obs, oldValue, focused) -> {
        if (!focused && isEditing()) {
          commitRename();
        }
      });
    }

    @Override
    public void startEdit() {
      AltSoundEntryModel item = getItem();
      // only existing files can be renamed
      if (item == null || !item.exists.get()) {
        return;
      }
      super.startEdit();
      if (!isEditing()) {
        return;
      }

      String[] parts = splitFilename(item);
      prefixLabel.setText(parts[0]);
      textField.setText(parts[1]);
      extensionLabel.setText(parts[2]);
      setGraphic(editBox);
      textField.requestFocus();
      textField.selectAll();
    }

    @Override
    public void cancelEdit() {
      super.cancelEdit();
      render(getItem());
    }

    private void commitRename() {
      AltSoundEntryModel item = getItem();
      String value = textField.getText();
      cancelEdit();
      if (item != null) {
        renameAudioFile(item, value);
      }
    }

    @Override
    protected void updateItem(AltSoundEntryModel item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setGraphic(null);
        setTooltip(null);
        return;
      }
      if (!isEditing()) {
        render(item);
      }
    }

    private void render(@Nullable AltSoundEntryModel item) {
      if (item == null) {
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
      String validationMessage = getValidationMessage(item);
      if (validationMessage != null) {
        label.setStyle(ERROR_STYLE + "-fx-font-weight: bold;");
        setTooltip(new Tooltip(validationMessage));
      }
      else {
        label.setStyle(null);
        setTooltip(new Tooltip(Messages.get("tables.editors.editor_altsound2.rename_tooltip", filename)));
      }
      setGraphic(box);
    }
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
    private final long rawSize;

    private AltSoundEntryModel(AltSoundEntry entry) {
      this.id = new SimpleStringProperty(entry.getId());
      this.filename = new SimpleStringProperty(entry.getFilename());
      this.rawSize = entry.getSize();
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

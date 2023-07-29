package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.ValidationCode;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.restclient.representations.ValidationState;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.LocalizedValidation;
import de.mephisto.vpin.ui.tables.validation.ValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class TableOverviewController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  @FXML
  private TableColumn<GameRepresentation, String> columnId;

  @FXML
  private TableColumn<GameRepresentation, String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, String> columnRom;

  @FXML
  private TableColumn<GameRepresentation, String> columnEmulator;

  @FXML
  private TableColumn<GameRepresentation, String> columnB2S;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private TableColumn<GameRepresentation, String> columnPUPPack;

  @FXML
  private TableColumn<GameRepresentation, String> columnAltSound;

  @FXML
  private TableColumn<GameRepresentation, String> columnAltColor;

  @FXML
  private TableColumn<GameRepresentation, String> columnPOV;

  @FXML
  private TableColumn<GameRepresentation, String> columnHSType;

  @FXML
  private TableColumn<GameRepresentation, String> columnPlaylists;

  @FXML
  private TableView<GameRepresentation> tableView;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  private Node validationError;

  @FXML
  private Label labelTableCount;

  @FXML
  private Button validateBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button scanBtn;

  @FXML
  private Button scanAllBtn;

  @FXML
  private Button playBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button importBtn;

  @FXML
  private Button backupBtn;

  @FXML
  private Button uploadTableBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private ComboBox<String> emulatorTypeCombo;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  @FXML
  private StackPane tableStack;

  private Parent tablesLoadingOverlay;
  private TablesController tablesController;
  private List<PlaylistRepresentation> playlists;

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onRomUpload() {
    this.tablesController.getTablesSideBarController().getTablesSidebarMetadataController().onRomUpload();
  }

  @FXML
  private void onBackup() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    Dialogs.openTablesBackupDialog(selectedItems);
  }

  @FXML
  private void onPlay() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Start playing table \"" + game.getGameDisplayName() + "\"?",
          "All existing VPX and Popper processes will be terminated.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getVpxService().playGame(game.getId());
      }
    }
  }

  @FXML
  private void onStop() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Stop all VPX and PinUP Popper processes?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getPinUPPopperService().terminatePopper();
      }
    }
  }

  @FXML
  private void onEmulatorSelect() {

  }

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    if (e.getCode().equals(KeyCode.ENTER)) {
      tableView.getSelectionModel().select(0);
      tableView.requestFocus();
    }
  }

  @FXML
  private void onTableUpload() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      Optional<ButtonType> buttonType = Dialogs.openPopperRunningWarning(Studio.stage);
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        client.getPinUPPopperService().terminatePopper();
        openUploadDialog();
      }
      return;
    }

    openUploadDialog();
  }

  private void openUploadDialog() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    boolean updated = Dialogs.openTableUploadDialog(game);
    if (updated) {
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      Optional<ButtonType> buttonType = Dialogs.openPopperRunningWarning(Studio.stage);
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        client.getPinUPPopperService().terminatePopper();
        deleteSelection();
      }
      return;
    }

    deleteSelection();
  }

  private void deleteSelection() {
    List<GameRepresentation> selectedGames = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    if (selectedGames != null && !selectedGames.isEmpty()) {
      for (GameRepresentation game : selectedGames) {
        if (client.getCompetitionService().isGameReferencedByCompetitions(game.getId())) {
          WidgetFactory.showAlert(Studio.stage, "The table \"" + game.getGameDisplayName()
              + "\" is used by at least one competition.", "Delete all competitions for this table first.");
          return;
        }
      }

      tableView.getSelectionModel().clearSelection();
      boolean b = Dialogs.openTableDeleteDialog(selectedGames, this.games);
      if (b) {
        this.onReload();
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
//        TransitionUtil.createTranslateByXTransition(main, 300, 600).playFromStart();
      }
    }
  }

  @FXML
  private void onTablesScan() {
    List<GameRepresentation> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    String title = "Re-scan selected tables?";
    if (selectedItems.size() == 1) {
      title = "Re-scan table \"" + selectedItems.get(0).getGameDisplayName() + "\"?";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        "Re-scanning will overwrite some of the existing metadata properties.", null, "Start Scan");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Dialogs.createProgressDialog(new TableScanProgressModel("Scanning Tables", selectedItems));
      this.onReload();
    }
  }

  @FXML
  private void onTablesScanAll() {
    String title = "Re-scan all " + games.size() + " tables?";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
        "Re-scanning will overwrite some of the existing metadata properties.", null, "Start Scan");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.clearCache();
      Dialogs.createProgressDialog(new TableScanProgressModel("Scanning Tables", this.games));
      this.onReload();
    }
  }

  @FXML
  private void onImport() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      Optional<ButtonType> buttonType = Dialogs.openPopperRunningWarning(Studio.stage);
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        client.getPinUPPopperService().terminatePopper();
        Dialogs.openTableImportDialog();
      }
    }
    else {
      Dialogs.openTableImportDialog();
    }
  }

  @FXML
  private void onValidate() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-validate table \"" + game.getGameDisplayName() + "\"?",
        "This will reset the dismissed validations for this table too.", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      game.setIgnoredValidations(null);

      try {
        client.getGameService().saveGame(game);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    ValidationState validationState = game.getValidationState();
    dismissValidation(game, validationState);
  }

  public void dismissValidation(@NonNull GameRepresentation game, @NonNull ValidationState validationState) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?",
        "The warning can be re-enabled by validating the table again.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      List<Integer> ignoredValidations = game.getIgnoredValidations();
      if (ignoredValidations == null) {
        ignoredValidations = new ArrayList<>();
      }

      if (!ignoredValidations.contains(validationState.getCode())) {
        ignoredValidations.add(validationState.getCode());
      }

      game.setIgnoredValidations(ignoredValidations);

      try {
        client.getGameService().saveGame(game);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }


  public void reload(String rom) {
    List<GameRepresentation> gamesByRom = client.getGameService().getGamesByRom(rom);
    Platform.runLater(() -> {
      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();

      for (GameRepresentation g : gamesByRom) {
        GameRepresentation refreshedGame = client.getGameService().getGame(g.getId());
        int index = data.indexOf(refreshedGame);
        data.remove(index);
        data.add(index, refreshedGame);
      }

      if (selection != null) {
        tableView.getSelectionModel().select(selection);
      }
      tableView.refresh();
    });
  }

  public void reload(int id) {
    GameRepresentation refreshedGame = client.getGameService().getGame(id);

    Platform.runLater(() -> {
      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();

      int index = data.indexOf(refreshedGame);
      if(index != -1) {
        data.remove(index);
        data.add(index, refreshedGame);
      }

      if (selection != null) {
        tableView.getSelectionModel().select(refreshedGame);
      }
      tableView.refresh();
    });
  }

  public void showEditor(GameRepresentation game) {
    String tableSource = client.getVpxService().getTableSource(game);
    if (!StringUtils.isEmpty(tableSource)) {
      try {
        FXMLLoader loader = new FXMLLoader(VpxEditorController.class.getResource("vpx-editor.fxml"));
        BorderPane root = loader.load();
        root.setMaxWidth(Double.MAX_VALUE);

        StackPane editorRootStack = tablesController.getEditorRootStack();
        editorRootStack.getChildren().add(root);

        VpxEditorController editorController = loader.getController();

        String source = new String(Base64.getDecoder().decode(tableSource), Charset.forName("utf8"));
        editorController.setGame(game, source);
        editorController.setTablesController(tablesController);
      } catch (IOException e) {
        LOG.error("Failed to load VPX Editor: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  public void onReload() {
    refreshPlaylists();

    this.textfieldSearch.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.scanAllBtn.setDisable(true);
    this.playBtn.setDisable(true);
    this.validateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.uploadTableBtn.setDisable(true);
    this.backupBtn.setDisable(true);
    this.importBtn.setDisable(true);
    this.stopBtn.setDisable(true);

    tableView.setVisible(false);
    tableStack.getChildren().add(tablesLoadingOverlay);

    new Thread(() -> {

      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      games = client.getGameService().getGames();
      filterGames(games);

      Platform.runLater(() -> {
        tableView.setItems(data);
        tableView.refresh();

        if (selection != null) {
          final GameRepresentation updatedGame = client.getGame(selection.getId());
          if (updatedGame != null) {
            tableView.getSelectionModel().select(updatedGame);
            this.playBtn.setDisable(!updatedGame.isGameFileAvailable());
            this.backupBtn.setDisable(!updatedGame.isGameFileAvailable());
          }
        }
        else if (!games.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }

        tableStack.getChildren().remove(tablesLoadingOverlay);

        if (!games.isEmpty()) {
          this.validateBtn.setDisable(false);
          this.deleteBtn.setDisable(false);
        }

        this.importBtn.setDisable(false);
        this.stopBtn.setDisable(false);
        this.textfieldSearch.setDisable(false);
        this.reloadBtn.setDisable(false);
        this.scanBtn.setDisable(false);
        this.scanAllBtn.setDisable(false);
        this.uploadTableBtn.setDisable(false);

        tableView.setVisible(true);
        labelTableCount.setText(games.size() + " tables");
      });
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }


    playlistCombo.setCellFactory(c -> new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.setButtonCell(new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.valueProperty().addListener(new ChangeListener<PlaylistRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends PlaylistRepresentation> observableValue, PlaylistRepresentation playlistRepresentation, PlaylistRepresentation t1) {
        tableView.getSelectionModel().clearSelection();
        refreshView(Optional.empty());

        filterGames(games);
        tableView.setItems(data);

        if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
      }
    });

    bindTable();
    bindSearchField();
  }

  private void refreshPlaylists() {
    this.playlistCombo.setDisable(true);
    playlists = new ArrayList<>(client.getPlaylistsService().getPlaylists());
    List<PlaylistRepresentation> pl = new ArrayList<>(playlists);
    pl.add(0, null);
    playlistCombo.setItems(FXCollections.observableList(pl));
    this.playlistCombo.setDisable(false);
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());

      filterGames(games);
      tableView.setItems(data);
    });
  }

  private void bindTable() {
    data = FXCollections.observableArrayList(Collections.emptyList());
    labelTableCount.setText(data.size() + " tables");
    tableView.setPlaceholder(new Label("No matching tables found."));

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    columnDisplayName.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      return new SimpleStringProperty(value.getGameDisplayName());
    });

    columnId.setCellValueFactory(
        new PropertyValueFactory<>("id")
    );

    columnRom.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String rom = value.getRom();
      List<Integer> ignoredValidations = Collections.emptyList();
      if (value.getIgnoredValidations() != null) {
        ignoredValidations = value.getIgnoredValidations();
      }
      if (!value.isRomExists() && value.isRomRequired() && !ignoredValidations.contains(ValidationCode.CODE_ROM_NOT_EXISTS)) {
        Label label = new Label(rom);
        String color = "#FF3333";
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
        return new SimpleObjectProperty(label);
      }

      return new SimpleStringProperty(rom);
    });

    columnEmulator.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      return new SimpleStringProperty(value.getEmulator().getName());
    });

    columnHSType.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      return new SimpleStringProperty(value.getHighscoreType());
    });

    columnB2S.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isDirectB2SAvailable()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    columnPOV.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isPovAvailable()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    columnAltSound.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isAltSoundAvailable()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    columnAltColor.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isAltColorAvailable()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });


    columnPUPPack.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isPupPackAvailable()) {
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon());
      }
      return new SimpleStringProperty("");
    });

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      ValidationState validationState = value.getValidationState();
      if (validationState.getCode() > 0) {
        return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
      }

      return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
    });

    columnPlaylists.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      HBox box = new HBox();
      List<PlaylistRepresentation> matches = new ArrayList<>();
      for (PlaylistRepresentation playlist : playlists) {
        if (playlist.getGameIds().contains(value.getId())) {
          matches.add(playlist);
        }
      }

      for (PlaylistRepresentation match : matches) {
        box.getChildren().add(WidgetFactory.createPlaylistIcon(match));
        if (box.getChildren().size() == 3 && matches.size() > box.getChildren().size()) {
          Label label = new Label("+" + (matches.size() - box.getChildren().size()));
          label.setStyle("-fx-font-size: 14px;-fx-font-weight: bold; -fx-padding: 1 0 0 0;");
          box.getChildren().add(label);
          break;
        }
      }
      return new SimpleObjectProperty(box);
    });


    tableView.setItems(data);
    tableView.setEditable(true);
    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<GameRepresentation>) c -> {
      boolean disable = c.getList().isEmpty() || c.getList().size() > 1;
      validateBtn.setDisable(disable);
      deleteBtn.setDisable(c.getList().isEmpty());
      backupBtn.setDisable(true);
      playBtn.setDisable(disable);
      scanBtn.setDisable(c.getList().isEmpty());

      if (c.getList().isEmpty()) {
        refreshView(Optional.empty());
      }
      else {
        GameRepresentation gameRepresentation = c.getList().get(0);
        backupBtn.setDisable(!gameRepresentation.isGameFileAvailable());
        playBtn.setDisable(!gameRepresentation.isGameFileAvailable());
        refreshView(Optional.ofNullable(gameRepresentation));
      }
    });

//    tableView.setRowFactory(
//        tableView -> {
//          final TableRow<GameRepresentation> row = new TableRow<>();
//          final ContextMenu rowMenu = new ContextMenu();
//          MenuItem editItem = new MenuItem("Edit");
//          MenuItem removeItem = new MenuItem("Delete");
//          rowMenu.getItems().addAll(editItem, removeItem);
//
//          // only display context menu for non-empty rows:
//          row.contextMenuProperty().bind(
//              Bindings.when(row.emptyProperty())
//                  .then((ContextMenu) null)
//                  .otherwise(rowMenu));
//          return row;
//        });


//    emulatorTypeCombo.setItems(FXCollections.observableList(Arrays.asList("", EmulatorTypes.VISUAL_PINBALL_X, EmulatorTypes.PINBALL_FX3, EmulatorTypes.FUTURE_PINBALL)));
//    emulatorTypeCombo.setItems(FXCollections.observableList(Arrays.asList("", EmulatorTypes.VISUAL_PINBALL_X)));
//    emulatorTypeCombo.setItems(FXCollections.observableList(Arrays.asList(EmulatorTypes.VISUAL_PINBALL_X)));
//    emulatorTypeCombo.valueProperty().setValue(EmulatorTypes.VISUAL_PINBALL_X);
//    emulatorTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> onReload());

  }

  private void filterGames(List<GameRepresentation> games) {
    List<GameRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();

    PlaylistRepresentation playlist = playlistCombo.getValue();

    for (GameRepresentation game : games) {
      String gameEmuType = game.getEmulator().getName();
      if (!gameEmuType.equals(EmulatorType.VISUAL_PINBALL_X) && !gameEmuType.equals(EmulatorType.VISUAL_PINBALL)) {
        continue;
      }

      if (playlist != null && !playlist.getGameIds().contains(game.getId())) {
        continue;
      }

      if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(game);
      }
      else if (!StringUtils.isEmpty(game.getRom()) && game.getRom().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(game);
      }
    }
    data = FXCollections.observableList(filtered);
  }

  private void refreshView(Optional<GameRepresentation> g) {
    validationError.setVisible(false);
    validationErrorLabel.setText("");
    validationErrorText.setText("");
    this.tablesController.getTablesSideBarController().setGame(g);
    if (g.isPresent()) {
      GameRepresentation game = g.get();
      validationError.setVisible(game.getValidationState().getCode() > 0);
      if (game.getValidationState().getCode() > 0) {
        LocalizedValidation validationMessage = ValidationTexts.validate(game);
        validationErrorLabel.setText(validationMessage.getLabel());
        validationErrorText.setText(validationMessage.getText());
      }
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
    else {
      validationError.setVisible(false);
      NavigationController.setBreadCrumb(Arrays.asList("Tables"));
    }
  }

  @Override
  public void onViewActivated() {

  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
    this.onReload();
  }

  public List<GameRepresentation> getGames() {
    return games;
  }

  public void initSelection() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
  }

  public GameRepresentation getSelection() {
    return tableView.getSelectionModel().getSelectedItem();
  }

  public List<PlaylistRepresentation> getPlaylists() {
    return this.playlists;
  }

  public void updatePlaylist(PlaylistRepresentation update) {
    GameRepresentation selectedItem = this.tableView.getSelectionModel().getSelectedItem();
    if(selectedItem != null) {
      EventManager.getInstance().notifyTableChange(selectedItem.getId(), null);
    }

    int pos = this.playlists.indexOf(update);
    this.playlists.remove(update);
    this.playlists.add(pos, update);

    List<PlaylistRepresentation> refreshedData = new ArrayList<>(this.playlists);
    refreshedData.add(0, null);
    this.playlistCombo.setItems(FXCollections.observableList(refreshedData));

    filterGames(games);
    tableView.setItems(data);
  }
}
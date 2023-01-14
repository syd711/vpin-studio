package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.EmulatorTypes;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.ValidationCode;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.tables.dialogs.ScriptDownloadProgressModel;
import de.mephisto.vpin.ui.tables.validation.ValidationResult;
import de.mephisto.vpin.ui.tables.validation.ValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;

public class TablesController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TablesController.class);

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
  private TableColumn<GameRepresentation, String> columnMediaB2S;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private TableColumn<GameRepresentation, String> columnPUPPack;

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
  private Button inspectBtn;

  @FXML
  private Button scanBtn;

  @FXML
  private Button importBtn;

  @FXML
  private Button exportBtn;

  @FXML
  private MenuItem uploadTableItem;

  @FXML
  private MenuItem uploadRomItem;

  @FXML
  private MenuItem uploadDirectB2SItem;

  @FXML
  private Button reloadBtn;

  @FXML
  private ComboBox<String> emulatorTypeCombo;

  @FXML
  private Parent tablesSideBar;

  @FXML
  private StackPane tableStack;

  @FXML
  private TablesSidebarController tablesSideBarController; //fxml magic! Not unused

  private Parent tablesLoadingOverlay;

  // Add a public no-args constructor
  public TablesController() {
  }

  private VPinStudioClient client;
  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onPlayClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    MediaView mediaView = (MediaView) borderPane.getCenter();

    FontIcon icon = (FontIcon) source.getChildrenUnmodifiable().get(0);
    String iconLiteral = icon.getIconLiteral();
    if (iconLiteral.equals("bi-play")) {
      mediaView.getMediaPlayer().setMute(false);
      mediaView.getMediaPlayer().setCycleCount(1);
      mediaView.getMediaPlayer().play();
      icon.setIconLiteral("bi-stop");
    }
    else {
      mediaView.getMediaPlayer().stop();
      icon.setIconLiteral("bi-play");
    }
  }

  @FXML
  private void onRomUpload() {
    this.tablesSideBarController.onRomUpload();
  }

  @FXML
  private void onDirectb2sUpload() {
    this.tablesSideBarController.onDirectb2sUpload();
  }

  @FXML
  private void onExport() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      Dialogs.openTableExportDialog(game);
    }
  }

  @FXML
  private void onImport() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      Dialogs.openTableImportDialog(game);
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
    if (client.isPinUPPopperRunning()) {
      Dialogs.openPopperRunningWarning(Studio.stage);
      return;
    }

    boolean updated = Dialogs.openTableUploadDialog();
    if (updated) {
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    if (client.isPinUPPopperRunning()) {
      Dialogs.openPopperRunningWarning(Studio.stage);
      return;
    }

    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      tableView.getSelectionModel().clearSelection();
      boolean b = Dialogs.openTableDeleteDialog(game);
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
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-scan selected tables?",
        "Re-scanning will overwrite some of the existing metadata properties.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Dialogs.createProgressDialog(new TableScanProgressModel(client, "Scanning Tables", tableView.getSelectionModel().getSelectedItems()));
      this.onReload();
    }
  }

  @FXML
  private void onValidate() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-validate table '" + game.getGameDisplayName() + "'?",
        "This will reset the dismissed validations for this table too.", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      game.setIgnoredValidations(null);

      try {
        client.saveGame(game);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  private void onInspect() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      ProgressResultModel resultModel = Dialogs.createProgressDialog(new ScriptDownloadProgressModel(client, "Extracting Table Script", game));
      if (!resultModel.getResults().isEmpty()) {
        File file = (File) resultModel.getResults().get(0);
        try {
          Desktop.getDesktop().open(file);
        } catch (IOException e) {
          WidgetFactory.showAlert(Studio.stage, "Failed to open script file " + file.getAbsolutePath() + ": " + e.getMessage());
        }
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Script extraction failed, check log for details.");
      }
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?",
        "The warning can be re-enabled by validating the table again.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      String validationState = String.valueOf(game.getValidationState());
      String ignoredValidations = game.getIgnoredValidations();
      if (ignoredValidations == null) {
        ignoredValidations = "";
      }
      List<String> gameIgnoreList = new ArrayList<>(Arrays.asList(ignoredValidations.split(",")));
      if (!gameIgnoreList.contains(validationState)) {
        gameIgnoreList.add(validationState);
      }

      game.setIgnoredValidations(StringUtils.join(gameIgnoreList, ","));

      try {
        client.saveGame(game);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      onReload();
    }
  }

  @FXML
  public void onReload() {
    this.textfieldSearch.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.validateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.uploadTableItem.setDisable(true);
    this.uploadRomItem.setDisable(true);
    this.uploadDirectB2SItem.setDisable(true);
    this.inspectBtn.setDisable(true);
    this.exportBtn.setDisable(true);
    this.importBtn.setDisable(true);

    tableView.setVisible(false);
    tableStack.getChildren().add(tablesLoadingOverlay);

    new Thread(() -> {
      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      games = client.getGames();
      filterGames(games);

      Platform.runLater(() -> {
        tableView.setItems(data);
        tableView.refresh();

        if (selection != null) {
          final GameRepresentation updatedGame = client.getGame(selection.getId());
          tableView.getSelectionModel().select(updatedGame);
        }
        else if (!games.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }

        tableStack.getChildren().remove(tablesLoadingOverlay);

        if (!games.isEmpty()) {
          this.validateBtn.setDisable(false);
          this.deleteBtn.setDisable(false);
          this.inspectBtn.setDisable(false);

          this.uploadDirectB2SItem.setDisable(false);
          this.uploadRomItem.setDisable(false);

          this.exportBtn.setDisable(false);
        }

        this.importBtn.setDisable(false);
        this.textfieldSearch.setDisable(false);
        this.reloadBtn.setDisable(false);
        this.scanBtn.setDisable(false);
        this.uploadTableItem.setDisable(false);

        tableView.setVisible(true);
        labelTableCount.setText(games.size() + " tables");
      });
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
    tablesSideBarController.setTablesController(this);

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    bindTable();
    bindSearchField();
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
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        rom = value.getOriginalRom();
      }

      List<String> ignoredValidations = Collections.emptyList();
      if (value.getIgnoredValidations() != null) {
        ignoredValidations = Arrays.asList(value.getIgnoredValidations().split(","));
      }
      if (!value.isRomExists() && !ignoredValidations.contains(String.valueOf(ValidationCode.CODE_ROM_NOT_EXISTS))) {
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

    columnB2S.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isDirectB2SAvailable()) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Paint.valueOf("#66FF66"));
        fontIcon.setIconLiteral("bi-check-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleStringProperty("");
    });

    columnMediaB2S.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isDirectB2SAsMediaAvailable()) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Paint.valueOf("#66FF66"));
        fontIcon.setIconLiteral("bi-check-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleStringProperty("");
    });

    columnPUPPack.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isPupPackAvailable()) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Paint.valueOf("#66FF66"));
        fontIcon.setIconLiteral("bi-check-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleStringProperty("");
    });

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      int validationState = value.getValidationState();
      if (validationState > 0) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setCursor(Cursor.HAND);
        fontIcon.setIconColor(Paint.valueOf("#FF3333"));
        fontIcon.setIconLiteral("bi-exclamation-circle");
        return new SimpleObjectProperty(fontIcon);
      }

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(18);
      fontIcon.setIconColor(Paint.valueOf("#66FF66"));
      fontIcon.setIconLiteral("bi-check-circle");
      return new SimpleObjectProperty(fontIcon);
    });


    tableView.setItems(data);
    tableView.setEditable(true);
    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<GameRepresentation>() {
      @Override
      public void onChanged(Change<? extends GameRepresentation> c) {
        boolean disable = c.getList().isEmpty() ||c.getList().size() > 1;
        validateBtn.setDisable(disable);
        deleteBtn.setDisable(disable);
        inspectBtn.setDisable(disable);
        exportBtn.setDisable(disable);
        uploadDirectB2SItem.setDisable(disable);

        if(c.getList().isEmpty()) {
          refreshView(Optional.empty());
        }
        else {
          refreshView(Optional.ofNullable(c.getList().get(0)));
        }
      }
    });


//    emulatorTypeCombo.setItems(FXCollections.observableList(Arrays.asList("", EmulatorTypes.VISUAL_PINBALL_X, EmulatorTypes.PINBALL_FX3, EmulatorTypes.FUTURE_PINBALL)));
//    emulatorTypeCombo.setItems(FXCollections.observableList(Arrays.asList("", EmulatorTypes.VISUAL_PINBALL_X)));
//    emulatorTypeCombo.setItems(FXCollections.observableList(Arrays.asList(EmulatorTypes.VISUAL_PINBALL_X)));
//    emulatorTypeCombo.valueProperty().setValue(EmulatorTypes.VISUAL_PINBALL_X);
//    emulatorTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> onReload());

    refreshView(Optional.empty());
    this.onReload();
  }

  private void filterGames(List<GameRepresentation> games) {
    List<GameRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();
    String emulatorValue = EmulatorTypes.VISUAL_PINBALL_X;//emulatorTypeCombo.getValue();
    for (GameRepresentation game : games) {
      if (!StringUtils.isEmpty(emulatorValue) && !game.getEmulator().getName().equals(emulatorValue)) {
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
    tablesSideBarController.setGame(g);
    if (g.isPresent()) {
      GameRepresentation game = g.get();
      validationError.setVisible(game.getValidationState() > 0);
      if (game.getValidationState() > 0) {
        ValidationResult validationMessage = ValidationTexts.validate(game);
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
}
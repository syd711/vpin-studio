package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.tables.validation.TableValidation;
import de.mephisto.vpin.ui.tables.validation.ValidationTexts;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
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

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class TablesController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TablesController.class);

  @FXML
  private TableColumn<GameRepresentation, String> columnId;

  @FXML
  private TableColumn<GameRepresentation, String> columnActive;

  @FXML
  private TableColumn<GameRepresentation, String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, String> columnRom;

  @FXML
  private TableColumn<GameRepresentation, String> columnRomAlias;

  @FXML
  private TableColumn<GameRepresentation, String> columnNVOffset;

  @FXML
  private TableColumn<GameRepresentation, String> columnB2S;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private TableColumn<GameRepresentation, String> columnPUPPack;

  @FXML
  private TableColumn<GameRepresentation, String> columnHsFile;

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
  private Button scanBtn;

  @FXML
  private Button scanAllBtn;

  @FXML
  private Button uploadTableBtn;

  @FXML
  private Button reloadBtn;

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
  private void onOpenDirectB2SBackground() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    MediaUtil.openDirectB2SBackground(game);
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
    boolean updated = WidgetFactory.openTableUploadDialog();
    if (updated) {
      onReload();
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
  private void onTableScan() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    WidgetFactory.createProgressDialog(new TableScanProgressModel(client, "Scanning Table '" + game + "'", game));
    this.onReload();
  }

  @FXML
  private void onTablesScan() {
    WidgetFactory.createProgressDialog(new TablesScanProgressModel(client, "Scanning Tables"));
    this.onReload();
  }

  @FXML
  private void onValidate() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    Optional<ButtonType> result = WidgetFactory.showConfirmation("Re-validate table '" + game.getGameDisplayName() + "?\nThis will reset the dismissed validations for this table too.", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      game.setIgnoredValidations(null);
      client.saveGame(game);
      onReload();
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    Optional<ButtonType> result = WidgetFactory.showConfirmation("Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?", null);
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
      client.saveGame(game);
      onReload();
    }
  }

  @FXML
  public void onReload() {
    this.textfieldSearch.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanAllBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.validateBtn.setDisable(true);
    this.uploadTableBtn.setDisable(true);

    tableView.setVisible(false);
    validationError.setVisible(false);
    tableStack.getChildren().add(tablesLoadingOverlay);

    new Thread(() -> {
      GameRepresentation gameRepresentation = tableView.getSelectionModel().selectedItemProperty().get();
      List<GameRepresentation> games = client.getGames();
      List<GameRepresentation> filtered = new ArrayList<>();
      String filterValue = textfieldSearch.textProperty().getValue();
      for (GameRepresentation game : games) {
        if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
          filtered.add(game);
        }
      }
      data = FXCollections.observableList(filtered);

      Platform.runLater(() -> {
        tableView.setItems(data);
        tableView.refresh();
        tableView.getSelectionModel().select(gameRepresentation);


        tableStack.getChildren().remove(tablesLoadingOverlay);

        this.textfieldSearch.setDisable(false);
        this.reloadBtn.setDisable(false);
        this.scanAllBtn.setDisable(false);
        this.scanBtn.setDisable(false);
        this.validateBtn.setDisable(false);
        this.uploadTableBtn.setDisable(false);

        tableView.setVisible(true);
        validationError.setVisible(true);
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
      ctrl.setLoadingMessage("Reloading Tables...");
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

      List<GameRepresentation> filtered = new ArrayList<>();
      for (GameRepresentation game : games) {
        if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
          filtered.add(game);
        }
      }
      data = FXCollections.observableArrayList(filtered);
      tableView.setItems(data);
    });
  }

  private void bindTable() {
    games = client.getGames();
    data = FXCollections.observableArrayList(games);
    labelTableCount.setText(data.size() + " tables");
    tableView.setPlaceholder(new Label("No matching tables found."));


    columnDisplayName.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("gameDisplayName")
    );

    columnId.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("id")
    );


    columnRom.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String rom = value.getRom();
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        rom = value.getOriginalRom();
      }

      if (value.isRomExists()) {
        return new SimpleStringProperty(rom);
      }

      Label label = new Label(rom);
      String color = "#FF3333";
      label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
      return new SimpleObjectProperty(label);
    });

    columnRomAlias.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        return new SimpleStringProperty(value.getRom());
      }
      return new SimpleStringProperty("-");
    });

    columnNVOffset.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.getNvOffset() > 0) {
        return new SimpleStringProperty(String.valueOf(value.getNvOffset()));
      }
      return new SimpleStringProperty("");
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

    columnHsFile.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String hsFileName = value.getHsFileName();
      return new SimpleStringProperty(hsFileName);
    });


    tableView.setItems(data);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      this.scanBtn.setDisable(disable);
      this.validateBtn.setDisable(disable);
      refreshView(Optional.ofNullable(newSelection));
    });

    if (!data.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  private void refreshView(Optional<GameRepresentation> g) {
    tablesSideBarController.setGame(g);
    if (g.isPresent()) {
      GameRepresentation game = g.get();
      validationError.setVisible(game.getValidationState() > 0);
      if (game.getValidationState() > 0) {
        TableValidation validationMessage = ValidationTexts.validate(game);
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
}
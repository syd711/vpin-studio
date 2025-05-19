package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tournaments.*;
import de.mephisto.vpin.ui.tournaments.view.TournamentTableGameCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentEditDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentEditDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> tournamentBadgeCombo;

  @FXML
  private CheckBox highscoreReset;

  @FXML
  private ComboBox<String> startTime;

  @FXML
  private ComboBox<String> endTime;

  @FXML
  private VBox avatarPane;

  @FXML
  private Button saveBtn;

  @FXML
  private Button addTableBtn;

  @FXML
  private Button editTableBtn;

  @FXML
  private Button deleteTableBtn;

  @FXML
  private CheckBox visibilityCheckbox;

  @FXML
  private Button editBtn;

  @FXML
  private ComboBox<IScoredGameRoom> gameRoomsCombo;

  @FXML
  private TextField nameField;

  @FXML
  private TextField discordLinkText;

  @FXML
  private TextField websiteLinkText;

  @FXML
  private TextArea descriptionText;

  @FXML
  private Label durationLabel;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Button openDiscordBtn;

  @FXML
  private Button openWesiteBtn;

  @FXML
  private StackPane rootStack;

  @FXML
  private TableView<TournamentTreeModel> tableView;

  @FXML
  private TableColumn<TournamentTreeModel, Label> statusColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> tableColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> vpsTableColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> vpsTableVersionColumn;

  private Stage stage;
  private Tournament tournament;
  private boolean joinMode;

  private final List<TournamentTreeModel> tableSelection = new ArrayList<>();
  private Node loadingOverlay;
  private Cabinet cabinet;
  private TreeItem<TournamentTreeModel> result;

  @FXML
  private void onCancelClick(ActionEvent e) {
    result = null;
    this.tournament = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDiscordOpen() {
    String discordLink = this.discordLinkText.getText();
    Studio.browse(discordLink);
  }

  @FXML
  private void onWebsiteOpen() {
    String link = this.websiteLinkText.getText();
    Studio.browse(link);
  }

  @FXML
  private void onTableAdd(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    TournamentTable tournamentTable = new TournamentTable();
    tournamentTable.setEnabled(true);
    TournamentTable result = TournamentDialogs.openTableSelectionDialog(stage, this.tournament, tournamentTable);
    if (result != null) {
      tournamentTable.setTournamentId(this.tournament.getId());

      VpsTable vpsTable = client.getVpsService().getTableById(tournamentTable.getVpsTableId());
      VpsTableVersion vpsTableVersion = null;
      if (vpsTable != null) {
        vpsTableVersion = vpsTable.getTableVersionById(tournamentTable.getVpsVersionId());
      }

      GameRepresentation game = client.getGameService().getGameByVpsTable(tournamentTable.getVpsTableId(), tournamentTable.getVpsVersionId());
      TournamentTreeModel tournamentTreeModel = new TournamentTreeModel(null, game, tournamentTable, vpsTable, vpsTableVersion);
      if (!tableSelection.contains(tournamentTreeModel)) {
        tableSelection.add(tournamentTreeModel);
        reloadTables();
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        TournamentTreeModel selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
          doTableEdit(stage, selectedItem);
        }
      }
    }
  }

  @FXML
  private void onTableEdit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    TournamentTreeModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      doTableEdit(stage, selectedItem);
    }
  }

  @FXML
  private void onEdit() {
    IScoredGameRoom value = gameRoomsCombo.getValue();
    if (value != null) {
      IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      boolean result = TableDialogs.openIScoredGameRoomDialog(iScoredSettings, value);
      if (result) {
        loadIScoredTables();
      }
    }
  }

  @FXML
  private void doTableEdit(Stage stage, TournamentTreeModel selection) {
    TournamentTable updatedTournamentTable = TournamentDialogs.openTableSelectionDialog(stage, this.tournament, selection.getTournamentTable());
    if (updatedTournamentTable != null) {
      GameRepresentation game = client.getGameService().getGameByVpsTable(selection.getTournamentTable().getVpsTableId(), selection.getTournamentTable().getVpsVersionId());
      selection.setGame(game);

      VpsTable vpsTable = client.getVpsService().getTableById(selection.getTournamentTable().getVpsTableId());
      VpsTableVersion vpsTableVersion = null;
      if (vpsTable != null) {
        vpsTableVersion = vpsTable.getTableVersionById(selection.getTournamentTable().getVpsVersionId());
      }
      selection.setVpsTable(vpsTable);
      selection.setVpsTableVersion(vpsTableVersion);
      reloadTables();
    }
  }

  @FXML
  private void onTableRemove(ActionEvent e) {
    TournamentTreeModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      tableSelection.remove(selectedItem);
    }
    reloadTables();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    if (TournamentHelper.isOwner(this.tournament, cabinet)) {
      List<TournamentTable> children = new ArrayList<>();
      for (TournamentTreeModel tournamentTreeModel : this.tableSelection) {
        if (!tournamentTreeModel.isTournamentNode()) {
          children.add(tournamentTreeModel.getTournamentTable());
        }
      }

      this.result = TournamentTreeModel.create(this.tournament, children);
    }
    else if (joinMode) {
      try {
        PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
        if (defaultPlayer != null) {
          String tournamentUserUuid = defaultPlayer.getTournamentUserUuid();
          Account account = maniaClient.getAccountClient().getAccountByUuid(tournamentUserUuid);
          if (account != null) {
            maniaClient.getTournamentClient().addMember(this.tournament, account);
          }
        }
      }
      catch (Exception ex) {
        LOG.error("Error joining tournament: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, ex.getMessage());
      }
    }

    stage.close();
  }

  private void reloadTables() {
    tableView.setItems(FXCollections.observableList(tableSelection));
    tableView.refresh();
    rootStack.getChildren().remove(loadingOverlay);
    this.validate();
  }

  private void refreshGameRoomsCombo(List<IScoredGameRoom> validGameRooms) {
    editBtn.setDisable(true);

    IScoredGameRoom value = gameRoomsCombo.getValue();
    List<IScoredGameRoom> gameRoomsComboValues = new ArrayList<>(validGameRooms);
    gameRoomsComboValues.add(0, null);
    gameRoomsCombo.setItems(FXCollections.observableList(gameRoomsComboValues));
    gameRoomsCombo.setValue(value);
    gameRoomsCombo.setDisable(validGameRooms.isEmpty());

    editBtn.setDisable(gameRoomsCombo.getValue() == null);
  }

  private void validate() {
    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);
    this.openDiscordBtn.setDisable(true);
    this.openWesiteBtn.setDisable(true);

    if (!StringUtils.isEmpty(this.discordLinkText.getText())) {
      openDiscordBtn.setDisable(false);
    }
    if (!StringUtils.isEmpty(this.websiteLinkText.getText())) {
      openWesiteBtn.setDisable(false);
    }

    Date startDate = tournament.getStartDate();
    Date endDate = tournament.getEndDate();
    this.durationLabel.setText(DateUtil.formatDuration(startDate, endDate));

    if (StringUtils.isEmpty(tournament.getDisplayName())) {
      validationTitle.setText("No tournament name set: Define a meaningful tournament name.");
      return;
    }

    if (startDate == null || endDate == null || startDate.getTime() >= endDate.getTime()) {
      validationTitle.setText("Invalid start/end date set: The end date must be after the start date.");
      return;
    }

    ObservableList<TournamentTreeModel> items = this.tableView.getItems();
    if (items.isEmpty()) {
      validationTitle.setText("Not tables selected: A tournament must have at least one table to be played.");
      return;
    }

    for (TournamentTreeModel item : items) {
      if (!item.isValid()) {
        validationTitle.setText("Table not valid: One or more tables are invalid.");
      }
    }

    this.saveBtn.setDisable(false);
    validationContainer.setVisible(false);
  }

  @Override
  public void onDialogCancel() {
    this.result = null;
  }

  public void setTournament(Stage stage, Tournament selectedTournament, boolean joinMode) {
    this.stage = stage;
    this.tournament = selectedTournament;
    this.joinMode = joinMode;
    this.visibilityCheckbox.setSelected(this.tournament.getVisibility().equals(TournamentVisibility.privateTournament));

    boolean isOwner = TournamentHelper.isOwner(selectedTournament, cabinet);
    boolean editable = isOwner && !selectedTournament.isActive();


    this.gameRoomsCombo.setDisable(tournament.getUuid() != null);
    editBtn.setDisable(true);
    List<IScoredGameRoom> items = gameRoomsCombo.getItems();
    for (IScoredGameRoom item : items) {
      if (item != null && item.getUrl().equalsIgnoreCase(selectedTournament.getDashboardUrl())) {
        gameRoomsCombo.setValue(item);
        editBtn.setDisable(tournament.getUuid() != null);
        break;
      }
    }


    InputStream in = null;
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    if (tournament.getUuid() == null) {
      editable = true;
      in = client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue());
    }
    else {
      in = client.getCachedUrlImage(maniaClient.getTournamentClient().getBadgeUrl(tournament));
    }

    if (in == null) {
      in = Studio.class.getResourceAsStream("avatar-default.png");
    }

    Image image = new Image(in);
    Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(UIDefaults.DEFAULT_AVATARSIZE * 2, UIDefaults.DEFAULT_AVATARSIZE * 2)
        .backgroundColor(Color.TRANSPARENT)
        .image(image)
        .imageMask(Tile.ImageMask.ROUND)
        .text("")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();

    avatarPane.getChildren().add(avatar);

    this.addTableBtn.setDisable(!isOwner);
    this.deleteTableBtn.setDisable(!isOwner || selectedTournament.getId() > 0);

    tournamentBadgeCombo.setDisable(isOwner && tournament.getUuid() != null);
    this.nameField.setText(selectedTournament.getDisplayName());
    this.nameField.setDisable(!isOwner);
    this.descriptionText.setText(selectedTournament.getDescription() != null && !selectedTournament.getDescription().equals("null") ? selectedTournament.getDescription() : "");
    this.descriptionText.setDisable(!isOwner);
    this.discordLinkText.setText(selectedTournament.getDiscordLink());
    this.discordLinkText.setDisable(!isOwner);
    this.websiteLinkText.setText(selectedTournament.getWebsite());
    this.websiteLinkText.setDisable(!isOwner);

    this.visibilityCheckbox.setDisable(!isOwner);

    this.startDatePicker.setValue(selectedTournament.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    this.startDatePicker.setDisable(!editable);
    this.startTime.setValue(DateUtil.formatTimeString(selectedTournament.getStartDate()));
    this.startTime.setDisable(!isOwner || (tournament.getUuid() != null && tournament.isActive()));

    this.endDatePicker.setValue(selectedTournament.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    this.endDatePicker.setDisable(!editable);
    this.endTime.setValue(DateUtil.formatTimeString(selectedTournament.getEndDate()));
    this.endTime.setDisable(!isOwner || (tournament.getUuid() != null && tournament.isFinished()));

    this.validationContainer.setVisible(editable);

    this.tournament = selectedTournament;

    List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(this.tournament.getId());
    for (TournamentTable tournamentTable : tournamentTables) {
      VpsTable vpsTable = client.getVpsService().getTableById(tournamentTable.getVpsTableId());
      VpsTableVersion vpsVersion = vpsTable.getTableVersionById(tournamentTable.getVpsVersionId());
      GameRepresentation game = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
      TournamentTreeModel tournamentTreeModel = new TournamentTreeModel(tournament, game, tournamentTable, vpsTable, vpsVersion);
      if (!this.tableSelection.contains(tournamentTreeModel)) {
        this.tableSelection.add(tournamentTreeModel);
      }
    }

    if (!isOwner) {
      this.saveBtn.setText("Join Tournament");
    }
    else if (isOwner && tournament.getUuid() != null) {
      this.saveBtn.setText("Update Tournament");
    }

    Platform.runLater(() -> {
      rootStack.getChildren().add(loadingOverlay);
      if (isOwner) {
        loadIScoredTables();
      }
      reloadTables();
      rootStack.getChildren().remove(loadingOverlay);
    });
  }

  private void loadIScoredTables() {
    this.tableSelection.clear();
    this.tableView.refresh();

    IScoredGameRoom iScoredGameRoom = gameRoomsCombo.getValue();
    if (iScoredGameRoom != null) {
      GameRoom gameRoom = IScored.getGameRoom(iScoredGameRoom.getUrl(), false);
      List<IScoredGame> games = gameRoom.getTaggedGames();
      for (IScoredGame game : games) {
        List<String> tags = game.getTags();
        Optional<String> first = tags.stream().filter(t -> VPS.isVpsTableUrl(t)).findFirst();
        if (first.isPresent()) {
          try {
            String vpsUrl = first.get();
            URL url = new URL(vpsUrl);
            String idSegment = url.getQuery();

            String tableId = idSegment.substring(idSegment.indexOf("=") + 1);
            if (tableId.contains("&")) {
              tableId = tableId.substring(0, tableId.indexOf("&"));
            }
            else if (tableId.contains("#")) {
              tableId = tableId.substring(0, tableId.indexOf("#"));
            }

            VpsTable vpsTable = client.getVpsService().getTableById(tableId);

            String[] split = vpsUrl.split("#");
            VpsTableVersion vpsVersion = null;
            if (vpsTable != null && split.length > 1) {
              vpsVersion = vpsTable.getTableVersionById(split[1]);
            }
            GameRepresentation gameRep = null;
            if (vpsTable != null) {
              gameRep = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
            }

            TournamentTable tournamentTable = new TournamentTable();
            tournamentTable.setEnabled(!game.isDisabled());
            tournamentTable.setVpsTableId(vpsTable.getId());
            tournamentTable.setVpsVersionId(vpsVersion.getId());
            tournamentTable.setTournamentId(this.tournament.getId());
            tournamentTable.setDisplayName(vpsTable.getDisplayName());

            TournamentTreeModel tournamentTreeModel = new TournamentTreeModel(tournament, gameRep, tournamentTable, vpsTable, vpsVersion);
            if (!this.tableSelection.contains(tournamentTreeModel)) {
              this.tableSelection.add(tournamentTreeModel);
            }
          }
          catch (Exception e) {
            LOG.error("Failed to parse table list: " + e.getMessage(), e);
            WidgetFactory.showAlert(stage, "Error", "Failed to parse table list: " + e.getMessage());
          }
        }
      }
    }
    this.tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    cabinet = maniaClient.getCabinetClient().getCabinetCached();

    editTableBtn.setDisable(true);
    validationContainer.setVisible(false);
    tableView.setPlaceholder(new Label("                     No tables selected!\nUse the '+' button to add tables to this tournament."));

    IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);

    ProgressDialog.createProgressDialog(new IScoredGameRoomLoadingProgressModel(iScoredSettings.getGameRooms(), false));
    List<IScoredGameRoom> validGameRooms = new ArrayList<>();
    List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
    for (IScoredGameRoom gameRoom : gameRooms) {
      GameRoom gr = IScored.getGameRoom(gameRoom.getUrl(), false);
      if (gr != null) {
        validGameRooms.add(gameRoom);
      }
    }
    refreshGameRoomsCombo(validGameRooms);

    gameRoomsCombo.valueProperty().addListener(new ChangeListener<IScoredGameRoom>() {
      @Override
      public void changed(ObservableValue<? extends IScoredGameRoom> observable, IScoredGameRoom oldValue, IScoredGameRoom newValue) {
        loadIScoredTables();
      }
    });


    deleteTableBtn.setDisable(true);
    saveBtn.setDisable(true);

    nameField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("nameField", () -> {
      Platform.runLater(() -> {
        if (nameField.getText().length() > 40) {
          String sub = nameField.getText().substring(0, 40);
          nameField.setText(sub);
        }
        tournament.setDisplayName(nameField.getText());
        validate();
      });
    }, 500));

    websiteLinkText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("websiteLinkText", () -> {
      Platform.runLater(() -> {
        if (websiteLinkText.getText() != null) {
          if (websiteLinkText.getText().length() > 1024) {
            String sub = websiteLinkText.getText().substring(0, 1024);
            websiteLinkText.setText(sub);
          }
          tournament.setWebsite(websiteLinkText.getText());
        }
        validate();
      });
    }, 500));


    startDatePicker.setValue(LocalDate.now());
    startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      tournament.setStartDate(date);
      validate();
    });
    startTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    startTime.setValue("00:00");
    startTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      tournament.setStartDate(date);
      validate();
    });

    endDatePicker.setValue(LocalDate.now().plus(7, ChronoUnit.DAYS));
    endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      tournament.setEndDate(date);
      validate();
    });
    endTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    endTime.setValue("00:00");
    endTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      tournament.setEndDate(date);
      validate();
    });

    discordLinkText.textProperty().addListener((observable, oldValue, newValue) -> {
      tournament.setDiscordLink(newValue);
    });

    descriptionText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("descriptionText", () -> {
      String value = String.valueOf(t1);
      if (!StringUtils.isEmpty(String.valueOf(value)) && value.length() > 4096) {
        value = value.substring(0, 4000);
      }
      tournament.setDescription(value);
    }, 300));

    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    tournamentBadgeCombo.setItems(imageList);
    tournamentBadgeCombo.setCellFactory(c -> new TournamentImageCell(client));
    tournamentBadgeCombo.setButtonCell(new TournamentImageCell(client));
    this.visibilityCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tournament.setVisibility(newValue ? TournamentVisibility.privateTournament : TournamentVisibility.publicTournament);
      validate();
    });

    tableColumn.setCellValueFactory(cellData -> {
      GameRepresentation game = cellData.getValue().getGame();
      return new SimpleObjectProperty(new TournamentTableGameCellContainer(game, tournament, cellData.getValue().getTournamentTable()));
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      VpsTable vpsTable = cellData.getValue().getVpsTable();
      String customStyle = TournamentHelper.getLabelCss(tournament, cellData.getValue().getTournamentTable());
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable, customStyle));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      VpsTable vpsTable = cellData.getValue().getVpsTable();
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      String customStyle = TournamentHelper.getLabelCss(tournament, cellData.getValue().getTournamentTable());
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTable, vpsTableVersion, customStyle, true));
    });

    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TournamentTreeModel>() {
      @Override
      public void onChanged(Change<? extends TournamentTreeModel> c) {
        deleteTableBtn.setDisable(!TournamentHelper.isOwner(tournament, cabinet) || c == null || c.getList().isEmpty());
        editTableBtn.setDisable(!TournamentHelper.isOwner(tournament, cabinet) || c == null || c.getList().isEmpty());
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tournament Data...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public TournamentCreationModel getTournamentData() {
    if (result != null) {
      TournamentCreationModel model = new TournamentCreationModel();
      model.setNewTournamentModel(this.result);
      model.setResetHighscore(highscoreReset.isSelected());
      model.setBadge(tournamentBadgeCombo.getValue());
      return model;
    }
    return null;
  }

  static class TournamentImageCell extends ListCell<String> {
    private final VPinStudioClient client;

    public TournamentImageCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getCompetitionService().getCompetitionBadge(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }
}

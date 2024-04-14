package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.*;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.Game;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.connectors.mania.model.TournamentVisibility;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.tournaments.TournamentDialogs;
import de.mephisto.vpin.ui.tournaments.TournamentHelper;
import de.mephisto.vpin.ui.tournaments.view.GameCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.vps.containers.VpsSelection;
import de.mephisto.vpin.ui.vps.containers.VpsTableContainer;
import de.mephisto.vpin.ui.vps.containers.VpsVersionContainer;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentEditDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentEditDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> tournamentBadgeCombo;

  @FXML
  private ComboBox<String> startTime;

  @FXML
  private ComboBox<String> endTime;

  @FXML
  private VBox avatarPane;

  @FXML
  private Button saveBtn;

  @FXML
  private Button iscoredReloadBtn;

  @FXML
  private Button addTableBtn;

  @FXML
  private Button deleteTableBtn;

  @FXML
  private CheckBox visibilityCheckbox;

  @FXML
  private CheckBox iscoredScoresEnabled;

  @FXML
  private TextField nameField;

  @FXML
  private TextField discordLinkText;

  @FXML
  private TextField websiteLinkText;

  @FXML
  private TextArea descriptionText;

  @FXML
  private TextField dashboardUrlField;

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
  private TableColumn<TournamentTreeModel, String> statusColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> tableColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> vpsTableColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> vpsTableVersionColumn;

  private Tournament tournament;

  private List<TournamentTreeModel> tableSelection = new ArrayList<>();
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
    if (!StringUtils.isEmpty(discordLink)) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(discordLink));
        } catch (Exception e) {
          LOG.error("Failed to open discord link: " + e.getMessage(), e);
        }
      }
    }
  }

  @FXML
  private void onWebsiteOpen() {
    String link = this.websiteLinkText.getText();
    if (!StringUtils.isEmpty(link)) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(link));
        } catch (Exception e) {
          LOG.error("Failed to open discord link: " + e.getMessage(), e);
        }
      }
    }
  }

  @FXML
  private void onTableAdd(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    VpsSelection vpsSelection = TournamentDialogs.openTableSelectionDialog(stage);
    if (vpsSelection.getTable() != null) {
      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(vpsSelection.getTable(), vpsSelection.getVersion());

      TournamentTable tournamentTable = new TournamentTable();
      tournamentTable.setVpsTableId(vpsSelection.getTable().getId());
      tournamentTable.setVpsVersionId(vpsSelection.getVersion().getId());
      tournamentTable.setTournamentId(this.tournament.getId());
      tournamentTable.setDisplayName(vpsSelection.getTable().getDisplayName());
      tableSelection.add(new TournamentTreeModel(null, gameByVpsTable, tournamentTable, vpsSelection.getTable(), vpsSelection.getVersion()));
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
    stage.close();
  }

  private void reloadTables() {
    tableView.setItems(FXCollections.observableList(tableSelection));
    tableView.refresh();
    rootStack.getChildren().remove(loadingOverlay);
    this.validate();
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

  public void setTournament(Tournament selectedTournament) {
    this.tournament = selectedTournament;
    this.visibilityCheckbox.setSelected(this.tournament.getVisibility().equals(TournamentVisibility.privateTournament));

    boolean isOwner = TournamentHelper.isOwner(selectedTournament, cabinet);
    boolean editable = isOwner && !selectedTournament.isActive();
    if (tournament.getUuid() == null) {
      editable = true;
    }

    this.iscoredReloadBtn.setDisable(!isOwner);
    this.addTableBtn.setDisable(!isOwner);
    this.deleteTableBtn.setDisable(!isOwner || selectedTournament.getId() > 0);

    tournamentBadgeCombo.setDisable(isOwner && tournament.getUuid() != null);
    this.nameField.setText(selectedTournament.getDisplayName());
    this.nameField.setDisable(!isOwner);
    this.descriptionText.setText(selectedTournament.getDescription());
    this.descriptionText.setDisable(!isOwner);
    this.discordLinkText.setText(selectedTournament.getDiscordLink());
    this.discordLinkText.setDisable(!isOwner);
    this.dashboardUrlField.setText(selectedTournament.getDashboardUrl());
    this.dashboardUrlField.setDisable(!isOwner);
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
      VpsTable vpsTable = VPS.getInstance().getTableById(tournamentTable.getVpsTableId());
      VpsTableVersion vpsVersion = vpsTable.getVersion(tournamentTable.getVpsVersionId());
      GameRepresentation game = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
      this.tableSelection.add(new TournamentTreeModel(tournament, game, tournamentTable, vpsTable, vpsVersion));
    }

    if (!isOwner) {
      this.saveBtn.setText("Join Tournament");
    }
    else if (isOwner && tournament.getUuid() != null) {
      this.saveBtn.setText("Update Tournament");
    }

    Platform.runLater(() -> {
      rootStack.getChildren().add(loadingOverlay);
      if (isOwner && this.tableSelection.isEmpty()) {
        loadIScoredTables();
      }
      else {
        reloadTables();
      }
    });
  }

  @FXML
  private void loadIScoredTables() {
    new Thread(() -> {
      Platform.runLater(() -> {
        String dashboardUrl = this.dashboardUrlField.getText();
        iscoredScoresEnabled.setSelected(false);
        this.tableSelection.clear();
        this.tableView.refresh();

        if (!StringUtils.isEmpty(dashboardUrl)) {
          try {
            GameRoom gameRoom = IScored.loadGameRoom(dashboardUrl);
            iscoredScoresEnabled.setSelected(gameRoom.getSettings().isPublicScoresEnabled());

            List<Game> games = gameRoom.getGames();
            for (Game game : games) {
              List<String> tags = game.getTags();
              Optional<String> first = tags.stream().filter(t -> t.startsWith(VPS.BASE_URL)).findFirst();
              if (first.isPresent()) {
                String vpsUrl = first.get();
                String idSegment = vpsUrl.substring(vpsUrl.lastIndexOf("=") + 1);
                String[] split = idSegment.split("#");
                VpsTable vpsTable = VPS.getInstance().getTableById(split[0]);
                VpsTableVersion vpsVersion = null;
                if (vpsTable != null && split.length > 1) {
                  vpsVersion = vpsTable.getVersion(split[1]);
                }
                GameRepresentation gameRep = null;
                if (vpsTable != null) {
                  gameRep = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
                }

                TournamentTable tournamentTable = new TournamentTable();
                tournamentTable.setVpsTableId(vpsTable.getId());
                tournamentTable.setVpsVersionId(vpsVersion.getId());
                tournamentTable.setTournamentId(this.tournament.getId());
                tournamentTable.setDisplayName(vpsTable.getDisplayName());

                this.tableSelection.add(new TournamentTreeModel(tournament, gameRep, tournamentTable, vpsTable, vpsVersion));
                this.tableView.refresh();
              }
            }
          } catch (Exception e) {
            LOG.warn("Failed to load iscored dashboard: " + e.getMessage());
          }
        }
        reloadTables();
      });
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    cabinet = maniaClient.getCabinetClient().getCabinet();

    validationContainer.setVisible(false);
    tableView.setPlaceholder(new Label("                     No tables selected!\nUse the '+' button to add tables to this tournament."));

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    AssetRepresentation asset = defaultPlayer.getAvatar();
    ByteArrayInputStream in = client.getAsset(AssetType.AVATAR, asset.getUuid());

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
        if(websiteLinkText.getText() != null) {
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

    dashboardUrlField.textProperty().addListener((observable, oldValue, newValue) -> tournament.setDashboardUrl(newValue));


    statusColumn.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue();
      if (value.getGame() == null) {
        return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
      }

      return new SimpleObjectProperty(WidgetFactory.createCheckIcon(null));
    });

    tableColumn.setCellValueFactory(cellData -> {
      GameRepresentation game = cellData.getValue().getGame();
      if (game != null) {
        return new SimpleObjectProperty(new GameCellContainer(game));
      }

      HBox cell = new HBox(12);
      cell.setAlignment(Pos.CENTER_LEFT);
      Image gameIcon = new Image(OverlayWindowFX.class.getResourceAsStream("avatar-blank.png"));
      ImageView imageView = new ImageView(gameIcon);
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(UIDefaults.DEFAULT_AVATARSIZE);
      cell.getChildren().add(imageView);

      Label label = new Label("Table not installed");
      label.setStyle("-fx-padding: 3 6 3 6;");
      label.getStyleClass().add("error-title");
      cell.getChildren().add(label);
      return new SimpleObjectProperty(cell);
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      VpsTable vpsTable = cellData.getValue().getVpsTable();
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion));
    });

    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TournamentTreeModel>() {
      @Override
      public void onChanged(Change<? extends TournamentTreeModel> c) {
        deleteTableBtn.setDisable(!TournamentHelper.isOwner(tournament, cabinet) || c == null || c.getList().isEmpty());
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tournament Data...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public TreeItem<TournamentTreeModel> getTournament() {
    return result;
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

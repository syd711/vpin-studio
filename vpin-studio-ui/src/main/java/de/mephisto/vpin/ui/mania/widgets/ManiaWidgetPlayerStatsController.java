package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.TarcisioWheelsDB;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.*;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontSmall;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaWidgetPlayerStatsController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetPlayerStatsController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<TableScoreModel> tableView;

  @FXML
  private TableColumn<TableScoreModel, String> columnName;

  @FXML
  private TableColumn<TableScoreModel, String> columnVersion;

  @FXML
  private TableColumn<TableScoreModel, String> columnScore;

  @FXML
  private TableColumn<TableScoreModel, String> columnPosition;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button denyBtn;

  @FXML
  private Button tableStatsBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private Label sub1Label;

  @FXML
  private Label subScore1Label;
  @FXML
  private Label subScore2Label;
  @FXML
  private Label subScore3Label;

  @FXML
  private VBox avatarPane;

  @FXML
  private Label rankLabel;

  @FXML
  private ImageView avatarView;

  @FXML
  private ToolBar toolbar;

  @FXML
  private Separator reloadSeparator;

  private Parent loadingOverlay;
  private Account account;
  private ManiaController maniaController;
  private RankedAccount rankedAccount;
  private PlayButtonController playButtonController;

  // Add a public no-args constructor
  public ManiaWidgetPlayerStatsController() {
  }


  public void onViewActivated(@Nullable NavigationOptions options) {
    if (options == null || options.getModel() == null) {
      if (this.account != null) {
        refresh();
        return;
      }

      PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
      if (defaultPlayer == null || defaultPlayer.getTournamentUserUuid() == null) {
        return;
      }
      account = maniaClient.getAccountClient().getAccountByUuid(defaultPlayer.getTournamentUserUuid());
      if (account != null) {
        refresh();
      }
      return;
    }

    if (options != null && options.getModel() != null && options.getModel() instanceof Account) {
      this.account = (Account) options.getModel();
      refresh();
    }
  }

  @FXML
  private void onDenyListAdd() {
    List<TableScoreModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<DeniedScore> update = new ArrayList<>();
      for (TableScoreModel selectedItem : selectedItems) {
        VpsTable vpsTable = selectedItem.getVpsTable();

        DeniedScore deniedScore = new DeniedScore();
        deniedScore.setDeniedByAccountUuid(ManiaPermissions.getAccount().getUuid());
        deniedScore.setDeniedDate(new Date());
        deniedScore.setScore(selectedItem.getScore());
        deniedScore.setInitials(account.getInitials());
        deniedScore.setVpsTableId(selectedItem.getVpsTable().getId());
        deniedScore.setVpsVersionId(selectedItem.getVpsTableVersion().getId());
        deniedScore.setTableName(vpsTable.getDisplayName());

        update.add(deniedScore);
      }

      boolean b = ManiaDialogs.openDenyListDialog(update);
      if (b) {
        tableView.getItems().removeAll(selectedItems);
        tableView.refresh();
      }
    }
  }


  @FXML
  private void onDelete() {
    List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
    List<PlayerRepresentation> collect = players.stream().filter(p -> !StringUtils.isEmpty(p.getTournamentUserUuid())).collect(Collectors.toList());
    if (collect.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "No Accounts", "None of your players is registered on VPin Mania.", "The highscores are registered based on players that have marked as VPin Mania account.");
      return;
    }

    ObservableList<TableScoreModel> selectedItems = this.tableView.getSelectionModel().getSelectedItems();
    if (selectedItems.isEmpty()) {
      return;
    }

    String msg = "This will delete the recorded highscores of the " + selectedItems.size() + " selected table.";
    if (selectedItems.size() == 1) {
      msg = "This will delete your recorded highscore for the table \"" + selectedItems.get(0).getName() + "\"";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Table Highscores", msg, null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      for (PlayerRepresentation playerRepresentation : collect) {
        Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(playerRepresentation.getTournamentUserUuid());
        if (accountByUuid != null) {
          for (TableScoreModel selectedItem : selectedItems) {
            String vpsTableId = selectedItem.getVpsTable().getId();
            String vpsVersionId = selectedItem.getVpsTableVersion().getId();
            maniaClient.getHighscoreClient().deleteHighscore(accountByUuid.getId(), vpsTableId, vpsVersionId);
            LOG.info("Deleted score " + vpsTableId + "/" + vpsVersionId + " from account " + accountByUuid.getId());
          }
        }
      }
      onReload();
    }
  }

  @FXML
  private void onPlayerSearch() {
    Account account = ManiaDialogs.openAccountSearchDialog();
    if (account != null) {
      this.account = account;
      setData(this.account);
    }
  }

  @FXML
  private void onReload() {
    ManiaPermissions.invalidate();
    maniaClient.getHighscoreClient().clearTableHighscoresCache();
    this.refresh();
  }

  @FXML
  private void onTableStats() {
    ObservableList<TableScoreModel> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      TableScoreModel tableScoreModel = selectedItems.get(0);
      if (tableScoreModel.getVpsTable() != null) {
        maniaController.selectVpsTable(tableScoreModel.getVpsTable());
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        onTableStats();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("No player selected."));
    denyBtn.managedProperty().bindBidirectional(denyBtn.visibleProperty());
    titleLabel.setText("Player Statistics");
    deleteBtn.setDisable(true);
    deleteBtn.setVisible(false);


    columnName.setCellValueFactory(cellData -> {
      VpsTable tableById = cellData.getValue().getVpsTable();
      if (tableById == null) {
        return new SimpleStringProperty("Invalid VPS Table");
      }

      HBox hBox = new HBox(3);
      hBox.setAlignment(Pos.CENTER_LEFT);

      InputStream imageInput = TarcisioWheelsDB.getWheelImage(Studio.class, client, tableById.getId());
      Image image = new Image(imageInput);
      ImageView imageView = new ImageView(image);
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(100);
      imageView.setFitWidth(100);
      hBox.getChildren().add(imageView);

      VpsTableContainer c = new VpsTableContainer(tableById, "");
      hBox.getChildren().add(c);
      return new SimpleObjectProperty(hBox);
    });

    columnVersion.setCellValueFactory(cellData -> {
      TableScoreModel value = cellData.getValue();
      VpsTableVersion tableVersionById = value.getVpsTableVersion();
      if (tableVersionById == null) {
        return new SimpleObjectProperty("-not available-");
      }

      VpsVersionContainer vpsVersionContainer = new VpsVersionContainer(value.getVpsTable(), tableVersionById, "", false);
      return new SimpleObjectProperty(vpsVersionContainer);
    });

    columnScore.setCellValueFactory(cellData -> {
      TableScoreModel value = cellData.getValue();
      Label label = new Label(ScoreFormatUtil.formatScore(value.getScore()));
      label.getStyleClass().add("default-text-color");
      label.setFont(getScoreFontSmall());
      return new SimpleObjectProperty(label);
    });


    columnPosition.setCellValueFactory(cellData -> {
      TableScoreModel value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label("#" + String.valueOf(value.getPosition()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnPosition.setSortable(false);
    columnName.setSortable(false);
    columnVersion.setSortable(false);
    columnScore.setSortable(false);


    tableStatsBtn.setDisable(true);

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableScoreModel>() {
      @Override
      public void changed(ObservableValue<? extends TableScoreModel> observable, TableScoreModel oldValue, TableScoreModel newValue) {
        tableStatsBtn.setDisable(newValue == null);
        deleteBtn.setDisable(newValue == null);
        denyBtn.setDisable(newValue == null);

        if (newValue != null) {
          GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(newValue.getVpsTable(), newValue.getVpsTableVersion());
          playButtonController.setData(gameByVpsTable);
        }
        else {
          playButtonController.setData(null);
        }
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay-plain.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Player Data...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }


    try {
      FXMLLoader loader = new FXMLLoader(PlayButtonController.class.getResource("play-btn.fxml"));
      SplitMenuButton playBtn = loader.load();
      playButtonController = loader.getController();
      int i = toolbar.getItems().indexOf(reloadSeparator);
      toolbar.getItems().add(i + 1, playBtn);
    }
    catch (IOException e) {
      LOG.error("failed to load play button: " + e.getMessage(), e);
    }
  }

  public void refresh() {
    try {
      deleteBtn.setVisible(account != null);
      if (account == null) {
        return;
      }

      denyBtn.setVisible(ManiaPermissions.isAdmin() || ManiaPermissions.isEditor());

      List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
      String accountUUID = account.getUuid();
      for (PlayerRepresentation player : players) {
        String maniaUUID = player.getTournamentUserUuid();
        if (!StringUtils.isEmpty(accountUUID) && accountUUID.equals(maniaUUID)) {
          deleteBtn.setVisible(true);
          break;
        }
      }

      tableView.getSelectionModel().clearSelection();

      rankedAccount = null;
      Image image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      avatarView.setImage(image);
      int rank = 0;
      new Thread(() -> {
        try {
          rankedAccount = maniaClient.getAccountClient().getRankedAccount(account.getUuid());
        }
        catch (Exception e) {
          LOG.error("Failed to load VPin Mania account: {}", e.getMessage(), e);
        }
        Platform.runLater(() -> {
          if (rankedAccount != null) {
            avatarView.setImage(ManiaAvatarCache.getAvatarImage(rankedAccount.getUuid()));
            rankLabel.setText("#" + rankedAccount.getRanking());
            CommonImageUtil.setClippedImage(avatarView, (int) (avatarView.getFitHeight()));
            subScore1Label.setText("#1 Places: " + rankedAccount.getPlace1());
            subScore2Label.setText("#2 Places: " + rankedAccount.getPlace2());
            subScore3Label.setText("#3 Places: " + rankedAccount.getPlace3());
          }
        });
      }).start();

      this.reloadBtn.setDisable(true);
      this.tableView.setVisible(false);

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TableScoreLoadingProgressModel(account));
      List<Object> results = progressDialog.getResults();
      if(results.isEmpty()) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to load player data, please try again later.");
        return;
      }

      List<ManiaWidgetPlayerStatsController.TableScoreModel> models = (List<TableScoreModel>) results.get(0);
      titleLabel.setText("\"" + account.getDisplayName() + "\" [" + account.getInitials() + "]");
      sub1Label.setText("Recorded Scores: " + models.size());
      ObservableList<TableScoreModel> data = FXCollections.observableList(models);

      Platform.runLater(() -> {
        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);
        tableView.setItems(data);
        tableView.refresh();
        this.reloadBtn.setDisable(false);
      });
    }
    catch (Exception e) {
      this.reloadBtn.setDisable(false);
      LOG.error("Failed to refresh player stats: " + e.getMessage(), e);
    }
  }

  public void setData(Account account) {
    this.account = account;
    this.deleteBtn.setVisible(account == null);
    if (account == null) {
      tableView.setPlaceholder(new Label("No player selected."));
      titleLabel.setText("-");
      sub1Label.setText("");
      subScore1Label.setText("");
      subScore2Label.setText("");
      subScore3Label.setText("");
      tableView.setItems(FXCollections.emptyObservableList());
      return;
    }
    refresh();
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
  }

  public static class TableScoreModel {
    private VpsTable vpsTable;
    private VpsTableVersion vpsTableVersion;
    //private String formattedScore;
    private long score;
    private String name = "???";
    private int position = -1;

    public TableScoreModel(TableScore tableScore) {
      this.score = tableScore.getScore();
      this.position = tableScore.getPosition();
      this.vpsTable = client.getVpsService().getTableById(tableScore.getVpsTableId());
      if (vpsTable != null) {
        this.name = vpsTable.getName().trim();
        this.vpsTableVersion = vpsTable.getTableVersionById(tableScore.getVpsVersionId());
      }
    }

    public String getName() {
      return name;
    }

    public VpsTable getVpsTable() {
      return vpsTable;
    }

    public VpsTableVersion getVpsTableVersion() {
      return vpsTableVersion;
    }

    public long getScore() {
      return score;
    }

    public int getPosition() {
      return position;
    }
  }
}
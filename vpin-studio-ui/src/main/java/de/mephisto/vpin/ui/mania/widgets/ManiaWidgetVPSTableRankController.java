package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.DeniedScore;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaHighscoreSyncResult;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.*;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontSmall;
import static de.mephisto.vpin.ui.Studio.maniaClient;
import static de.mephisto.vpin.ui.Studio.stage;

public class ManiaWidgetVPSTableRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetVPSTableRankController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<TableScoreDetails> tableView;

  @FXML
  private TableView<DeniedScore> denyListView;

  @FXML
  private TableColumn<TableScoreDetails, String> columnRank;

  @FXML
  private TableColumn<TableScoreDetails, String> columnScore;

  @FXML
  private TableColumn<TableScoreDetails, String> columnName;

  @FXML
  private TableColumn<TableScoreDetails, String> columnVersion;

  @FXML
  private TableColumn<DeniedScore, String> columnDenyVersion;

  @FXML
  private TableColumn<DeniedScore, String> columnDenyInitials;

  @FXML
  private TableColumn<DeniedScore, String> columnDenyAccount;

  @FXML
  private TableColumn<DeniedScore, String> columnDenyScore;

  @FXML
  private TableColumn<TableScoreDetails, String> columnDate;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button openBtn;

  @FXML
  private Button syncBtn;

  @FXML
  private Button denyBtn;

  @FXML
  private Separator deleteSeparator;

  @FXML
  private Button showPlayerBtn;

  @FXML
  private Button removeFromDenyListBtn;

  @FXML
  private Separator reloadSeparator;

  @FXML
  private Label titleLabel;

  @FXML
  private ToolBar toolbar;

  @FXML
  private Tab denyListTab;

  @FXML
  private Tab highscoresTab;

  private Parent loadingOverlay;
  private List<TableScoreDetails> tableScores;
  private VpsTable vpsTable;

  private ManiaController maniaController;
  private PlayButtonController playButtonController;

  // Add a public no-args constructor
  public ManiaWidgetVPSTableRankController() {
  }

  @FXML
  private void onDenyListRemove() {
    DeniedScore selectedItem = denyListView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithCheckbox(stage, "Deny List", null,
          "Remove From Deny List", "Remove the selected score from the deny list?",
          "Remove the highscore from the deny list will allow future submission of the score for all users with the given initials and value again.",
          "Understood, remove the highscore from the deny list",
          true);
      if (confirmationResult.isOkClicked()) {
        try {
          maniaClient.getHighscoreClient().removeFromDenyList(selectedItem);
          onReload();
        }
        catch (Exception e) {
          LOG.error("Failed to remove score from deny list: {}", e.getMessage(), e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(stage, "Error", "Failed to remove score from deny list: " + e.getMessage());
          });
        }
      }
    }
  }


  @FXML
  private void onDenyListAdd() {
    List<TableScoreDetails> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      List<DeniedScore> update = new ArrayList<>();
      for (TableScoreDetails selectedItem : selectedItems) {
        DeniedScore deniedScore = selectedItem.toDeniedScore(ManiaPermissions.getAccount().getUuid(), vpsTable.getDisplayName());
        update.add(deniedScore);
      }

      boolean b = ManiaDialogs.openDenyListDialog(update);
      if (b) {
        onReload();
      }
    }
  }

  @FXML
  private void onDenyListReload() {
    JFXFuture.supplyAsync(() -> {
      return maniaClient.getHighscoreClient().getDeniedScoresByTableId(this.vpsTable.getId());
    }).thenAcceptLater(deniedScores -> {
      ObservableList<DeniedScore> denyListData = FXCollections.observableList(deniedScores);
      denyListView.setItems(denyListData);
      denyListTab.setText("Deny List (" + denyListData.size() + ")");
    });
  }

  @FXML
  private void onPlayerView() {
    TableScoreDetails selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(selectedItem.getAccountUUID());
      if (accountByUuid != null) {
        maniaController.selectPlayer(accountByUuid);
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        onPlayerView();
      }
    }
  }

  @FXML
  private void onScoreSync() {
    List<PlayerRepresentation> players = Studio.client.getPlayerService().getPlayers();
    List<PlayerRepresentation> collect = players.stream().filter(p -> !StringUtils.isEmpty(p.getTournamentUserUuid())).collect(Collectors.toList());
    if (collect.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "No Accounts", "None of your players is registered on VPin Mania.", "The highscores are registered based on players that have marked as VPin Mania account.");
      return;
    }


    if (vpsTable != null) {
      GameRepresentation gameByVpsTable = Studio.client.getGameService().getGameByVpsTable(vpsTable, null);
      if (gameByVpsTable == null) {
        WidgetFactory.showAlert(Studio.stage, "No VPS Mapping", "This table is not installed on your cabinet or has no valid VPS mapping.");
        return;
      }

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new HighscoreSynchronizeProgressModel("Highscore Synchronization", Arrays.asList(vpsTable)));
      List<Object> results = progressDialog.getResults();
      int count = 0;
      for (Object result : results) {
        ManiaHighscoreSyncResult syncResult = (ManiaHighscoreSyncResult) result;
        count += syncResult.getTableScores().size();
      }
      WidgetFactory.showConfirmation(Studio.stage, "Synchronization Result", count + " highscore(s) have been submitted to vpin-mania.net.");
      onReload();
    }
  }

  @FXML
  private void onOpen() {
    if (vpsTable != null) {
      Studio.browse(VPS.getVpsTableUrl(vpsTable.getId()));
    }
  }

  @FXML
  private void onReload() {
    ManiaAvatarCache.clear();
    ManiaPermissions.invalidate();
    maniaClient.getHighscoreClient().clearTableHighscoresCache();
    doReload();
  }

  private void doReload() {
    this.setData(this.vpsTable);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    denyBtn.managedProperty().bindBidirectional(denyBtn.visibleProperty());
    deleteSeparator.managedProperty().bindBidirectional(deleteSeparator.visibleProperty());

    syncBtn.setDisable(true);
    showPlayerBtn.setDisable(true);
    tableView.setPlaceholder(new Label("         No scores listed here?\nBe the first and create a highscore!"));
    denyListView.setPlaceholder(new Label("No scores have been added to this deny list yet."));

    columnRank.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label("#" + (tableScores.indexOf(value) + 1));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnScore.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      Label label = new Label(ScoreFormatUtil.formatScore(value.getScore()));
      label.getStyleClass().add("default-text-color");
      label.setFont(getScoreFontSmall());
      return new SimpleObjectProperty(label);
    });

    columnVersion.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      VpsTable tableById = Studio.client.getVpsService().getTableById(value.getVpsTableId());
      if (tableById != null) {
        VpsTableVersion tableVersionById = tableById.getTableVersionById(value.getVpsVersionId());
        if (tableVersionById != null) {
          VpsVersionContainer vpsVersionContainer = new VpsVersionContainer(tableById, tableVersionById, "", false);
          return new SimpleObjectProperty(vpsVersionContainer);
        }

      }
      return new SimpleObjectProperty("-not available-");
    });

    columnName.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      HBox hBox = new HBox();

      Image image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      ImageView view = new ImageView(image);

      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);

      hBox.setAlignment(Pos.CENTER_LEFT);
      hBox.getChildren().add(view);
      hBox.setSpacing(6);

      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(value.getDisplayName() + " [" + value.getInitials() + "]");
      label.setFont(defaultFont);
      label.getStyleClass().add("default-text-color");
      hBox.getChildren().add(label);

      new Thread(() -> {
        Platform.runLater(() -> {
          Image i = ManiaAvatarCache.getAvatarImage(value.getAccountUUID());
          view.setImage(i);
          CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
        });
      }).start();
      return new SimpleObjectProperty(hBox);
    });

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    if (screenBounds.getWidth() < 2600) {
      columnName.setPrefWidth(280);
    }
    if (screenBounds.getWidth() < 2000) {
      columnName.setPrefWidth(260);
    }

    columnDate.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(DateUtil.formatDateTime(value.getCreationDate()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    //deny list table
    columnDenyVersion.setCellValueFactory(cellData -> {
      DeniedScore deniedScore = cellData.getValue();
      VpsTable tableById = Studio.client.getVpsService().getTableById(deniedScore.getVpsTableId());
      if (tableById != null) {
        VpsTableVersion tableVersionById = tableById.getTableVersionById(deniedScore.getVpsVersionId());
        if (tableVersionById != null) {
          VpsVersionContainer vpsVersionContainer = new VpsVersionContainer(tableById, tableVersionById, "", false);
          return new SimpleObjectProperty(vpsVersionContainer);
        }

      }
      return new SimpleObjectProperty("-not available-");
    });

    columnDenyScore.setCellValueFactory(cellData -> {
      DeniedScore value = cellData.getValue();
      Label label = new Label(ScoreFormatUtil.formatScore(value.getScore()));
      label.getStyleClass().add("default-text-color");
      label.setFont(getScoreFontSmall());
      return new SimpleObjectProperty(label);
    });

    columnDenyInitials.setCellValueFactory(cellData -> {
      DeniedScore value = cellData.getValue();
      Label label = new Label(value.getInitials());
      label.getStyleClass().add("default-text-color");
      label.setFont(getScoreFontSmall());
      return new SimpleObjectProperty(label);
    });

    columnDenyAccount.setCellValueFactory(cellData -> {
      DeniedScore value = cellData.getValue();

      HBox hBox = new HBox();

      Image image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      ImageView view = new ImageView(image);

      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);

      hBox.setAlignment(Pos.CENTER_LEFT);
      hBox.getChildren().add(view);
      hBox.setSpacing(6);

      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label("");
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      hBox.getChildren().add(label);

      Account authorAcc = maniaClient.getAccountClient().getAccountByUuid(value.getDeniedByAccountUuid());
      if (authorAcc != null) {
        label.setText(authorAcc.getDisplayName());
        Image avatarImage = ManiaAvatarCache.getAvatarImage(authorAcc.getUuid());
        view.setImage(avatarImage);
        CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
      }
      else {
        label.setText("- invalid account -");
      }
      return new SimpleObjectProperty(hBox);
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Table Ranking...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TableScoreDetails>() {
      @Override
      public void onChanged(Change<? extends TableScoreDetails> c) {
        denyBtn.setDisable(c.getList().isEmpty());
      }
    });

    denyListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    denyListView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<DeniedScore>() {
      @Override
      public void onChanged(Change<? extends DeniedScore> c) {
        removeFromDenyListBtn.setDisable(c.getList().isEmpty());
      }
    });

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

    denyBtn.setVisible(false);
    deleteSeparator.setVisible(false);
    denyBtn.setDisable(true);
    openBtn.setDisable(true);

    denyBtn.setVisible(ManiaPermissions.isAdmin() || ManiaPermissions.isEditor());
    deleteSeparator.setVisible(ManiaPermissions.isAdmin() || ManiaPermissions.isEditor());
  }


  public void setData(VpsTable vpsTable) {
    openBtn.setDisable(vpsTable == null);
    syncBtn.setDisable(vpsTable == null);
    showPlayerBtn.setDisable(true);
    this.vpsTable = vpsTable;

    refreshButtons();
    onDenyListReload();

    if (vpsTable != null) {
      GameRepresentation gameByVpsTable = Studio.client.getGameService().getGameByVpsTable(vpsTable.getId(), null);
      playButtonController.setData(gameByVpsTable);
    }
    else {
      playButtonController.setData(null);
    }

    if (vpsTable == null) {
      Platform.runLater(() -> {
        titleLabel.setText("Ranking");
        ObservableList<TableScoreDetails> data = FXCollections.emptyObservableList();
        tableView.setItems(data);
        highscoresTab.setText("Highscores (" + data.size() + ")");
        tableView.refresh();
      });
      return;
    }

    titleLabel.setText("Ranking for \"" + vpsTable.getDisplayName() + "\"");

    JFXFuture.supplyAsync(() -> {
          return maniaClient.getHighscoreClient().getHighscoresByTable(vpsTable.getId());
        })
        .onErrorSupply(e -> {
          LOG.error("Loading ranked accounts: {}", e.getMessage(), e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(stage, "Error", "Loading ranked accounts: " + e.getMessage());
          });
          return Collections.emptyList();
        })
        .thenAcceptLater(searchResult -> {
          tableScores = searchResult;
          ObservableList<TableScoreDetails> data = FXCollections.observableList(tableScores);
          tableView.setItems(data);
          highscoresTab.setText("Highscores (" + data.size() + ")");
          tableView.refresh();
        });
  }

  private void refreshButtons() {
    denyBtn.setVisible(ManiaPermissions.isAdmin() || ManiaPermissions.isEditor());
    deleteSeparator.setVisible(ManiaPermissions.isAdmin() || ManiaPermissions.isEditor());
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
  }
}
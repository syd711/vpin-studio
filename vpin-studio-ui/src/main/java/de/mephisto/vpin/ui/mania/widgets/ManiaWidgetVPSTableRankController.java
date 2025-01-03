package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
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
import de.mephisto.vpin.ui.mania.HighscoreSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.ManiaController;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.fx.ServerFX.client;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontSmall;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaWidgetVPSTableRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetVPSTableRankController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<TableScoreDetails> tableView;

  @FXML
  private TableColumn<TableScoreDetails, String> columnRank;

  @FXML
  private TableColumn<TableScoreDetails, String> columnScore;

  @FXML
  private TableColumn<TableScoreDetails, String> columnName;

  @FXML
  private TableColumn<TableScoreDetails, String> columnVersion;

  @FXML
  private TableColumn<TableScoreDetails, String> columnDate;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button openBtn;

  @FXML
  private Button syncBtn;

  @FXML
  private Button showPlayerBtn;

  @FXML
  private Separator reloadSeparator;

  @FXML
  private Label titleLabel;

  @FXML
  private ToolBar toolbar;

  private Parent loadingOverlay;
  private List<TableScoreDetails> tableScores;
  private VpsTable vpsTable;

  private ManiaController maniaController;
  private PlayButtonController playButtonController;

  // Add a public no-args constructor
  public ManiaWidgetVPSTableRankController() {
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
    maniaClient.getHighscoreClient().clearTableHighscoresCache();
    this.setData(this.vpsTable);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    syncBtn.setDisable(true);
    showPlayerBtn.setDisable(true);
    tableView.setPlaceholder(new Label("         No scores listed here?\nBe the first and create a highscore!"));

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
      Label label = new Label(ScoreFormatUtil.formatScore(String.valueOf(value.getScore())));
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
      Label label = new Label(value.getDisplayName());
      label.setFont(defaultFont);
      label.getStyleClass().add("default-text-color");
      hBox.getChildren().add(label);

      new Thread(() -> {
        InputStream in = client.getCachedUrlImage(maniaClient.getAccountClient().getAvatarUrl(value.getAccountUUID()));
        if (in == null) {
          in = ServerFX.class.getResourceAsStream("avatar-blank.png");
        }
        final InputStream data = in;
        if (data != null) {
          Platform.runLater(() -> {
            Image i = new Image(data);
            view.setImage(i);
            CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
          });
        }
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

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Table Ranking...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableScoreDetails>() {
      @Override
      public void changed(ObservableValue<? extends TableScoreDetails> observable, TableScoreDetails oldValue, TableScoreDetails newValue) {
        showPlayerBtn.setDisable(newValue == null);
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
  }


  public void setData(VpsTable vpsTable) {
    openBtn.setDisable(vpsTable == null);
    syncBtn.setDisable(vpsTable == null);
    showPlayerBtn.setDisable(true);
    this.vpsTable = vpsTable;

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
        tableView.refresh();
      });
      return;
    }

    titleLabel.setText("Ranking for \"" + vpsTable.getDisplayName() + "\"");
    new Thread(() -> {
      tableScores = maniaClient.getHighscoreClient().getHighscoresByTable(vpsTable.getId());

      Platform.runLater(() -> {
        ObservableList<TableScoreDetails> data = FXCollections.observableList(tableScores);
        tableView.setItems(data);
        tableView.refresh();
      });
    }).start();
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
  }
}
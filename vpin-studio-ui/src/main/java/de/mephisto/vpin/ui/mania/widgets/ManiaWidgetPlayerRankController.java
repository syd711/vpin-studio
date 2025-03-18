package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.RankedAccount;
import de.mephisto.vpin.connectors.mania.model.RankedAccountPagingResult;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.mania.ManiaHighscoreSyncResult;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.ManiaAvatarCache;
import de.mephisto.vpin.ui.mania.VPinManiaSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.ManiaController;
import de.mephisto.vpin.commons.utils.JFXFuture;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.*;

public class ManiaWidgetPlayerRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetPlayerRankController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<RankedAccount> tableView;

  @FXML
  private TableColumn<RankedAccount, String> columnRank;

  @FXML
  private TableColumn<RankedAccount, String> columnPoints;

  @FXML
  private TableColumn<RankedAccount, String> columnName;

  @FXML
  private TableColumn<RankedAccount, String> columnFirst;

  @FXML
  private TableColumn<RankedAccount, String> columnSecond;

  @FXML
  private TableColumn<RankedAccount, String> columnThird;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button showPlayerBtn;

  @FXML
  private Button synchronizeBtn;

  @FXML
  private Button nextBtn;

  @FXML
  private Button previousBtn;

  @FXML
  private Label pagingInfo;

  private Parent loadingOverlay;


  private int page = 0;
  private ManiaController maniaController;

  private RankedAccountPagingResult searchResult;

  // Add a public no-args constructor
  public ManiaWidgetPlayerRankController() {
  }

  @FXML
  private void onPlayerView() {
    RankedAccount selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null && selectedItem.getUuid() != null) {
      String uuid = selectedItem.getUuid();
      Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(uuid);
      maniaController.selectPlayer(accountByUuid);
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

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Highscores", "Delete Highscores?", "This will delete all registered scores from all your accounts on VPin-Mania.");
    boolean deleted = true;
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      int count = 0;
      for (PlayerRepresentation playerRepresentation : collect) {
        Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(playerRepresentation.getTournamentUserUuid());
        if (accountByUuid != null) {
          if (!maniaClient.getHighscoreClient().deleteHighscores(accountByUuid.getId())) {
            deleted = false;
          }
          count++;
        }
      }

      if (deleted) {
        WidgetFactory.showInformation(Studio.stage, "Information", "Highscore deletion successful.", "Deleted highscores of " + count + " account(s).");
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Deletion Failed", "One or more highscore deletions failed");
      }
      onReload();
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
    List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
    List<PlayerRepresentation> collect = players.stream().filter(p -> !StringUtils.isEmpty(p.getTournamentUserUuid())).collect(Collectors.toList());
    if (collect.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "No Accounts", "None of your players is registered on VPin Mania.", "The highscores are registered based on players that have marked as VPin Mania account.");
      return;
    }

    Optional<ButtonType> b = WidgetFactory.showConfirmation(Studio.stage, "Highscore Synchronization", "This will synchronize all highscores from all tables from all your VPin-Mania players.", "Only tables with a valid Virtual Pinball Spreadsheet mapping will be synchronized.", "Start Synchronization");
    if (b.get().equals(ButtonType.OK)) {
      List<VpsTable> vpsTables = Studio.client.getGameService().getInstalledVpsTables();

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new VPinManiaSynchronizeProgressModel(vpsTables));
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
  private void onReload() {
    ManiaAvatarCache.clear();
    refresh();
  }

  @FXML
  private void onHelp() {
    Studio.browse("https://github.com/syd711/vpin-studio/wiki/Mania#Player-Ranking");
  }

  @FXML
  private void onNext() {
    page = searchResult.getPage() + 1;
    doSearch();
  }

  @FXML
  private void onPrevious() {
    page = page - searchResult.getPage();
    if (page < 0) {
      page = 0;
    }
    doSearch();
  }

  private void doSearch() {
    Platform.runLater(() -> {
      try {
        this.synchronizeBtn.setDisable(true);
        this.reloadBtn.setDisable(true);
        this.tableView.setVisible(false);
        if (!tableStack.getChildren().contains(loadingOverlay)) {
          tableStack.getChildren().add(loadingOverlay);
        }

        JFXFuture.supplyAsync(() -> {
              try {
                return searchResult = maniaClient.getAccountClient().getRankedAccounts(page, UIDefaults.PLAYERS_PAGE_SIZE);
              }
              catch (Exception e) {
                LOG.error("Loading ranked accounts: {}", e.getMessage(), e);
              }
              return new RankedAccountPagingResult();
            })
            .onErrorSupply(e -> {
              LOG.error("Loading ranked accounts: {}", e.getMessage(), e);
              Platform.runLater(() -> {
                WidgetFactory.showAlert(stage, "Error", "Loading ranked accounts: " + e.getMessage());
              });
              return new RankedAccountPagingResult();
            })
            .thenAcceptLater(searchResult -> {
              this.searchResult = searchResult;
              List<RankedAccount> rankedAccounts = searchResult.getResults();

              int from = searchResult.getPage() * UIDefaults.PLAYERS_PAGE_SIZE;
              int to = from + UIDefaults.PLAYERS_PAGE_SIZE;
              if (to > searchResult.getTotal()) {
                to = searchResult.getTotal();
              }

              pagingInfo.setText((from + 1) + " to " + to + " of " + searchResult.getTotal());
              pagingInfo.setVisible(searchResult.getTotal() > 0);
              previousBtn.setDisable(true);
              nextBtn.setDisable(true);

              if (!rankedAccounts.isEmpty()) {
                boolean hasNext = to < searchResult.getTotal();
                boolean hasPrevious = searchResult.getPage() > 0;
                nextBtn.setDisable(!hasNext);
                previousBtn.setDisable(!hasPrevious);
              }

              tableStack.getChildren().remove(loadingOverlay);
              tableView.setVisible(true);

              ObservableList<RankedAccount> data = FXCollections.observableList(rankedAccounts);
              tableView.setItems(data);
              tableView.refresh();
            });

      }
      catch (Exception e) {
        LOG.error("Player fetch failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Player fetch failed: " + e.getMessage());
      }
      finally {
        Platform.runLater(() -> {
          tableStack.getChildren().remove(loadingOverlay);
          tableView.setVisible(true);

          this.reloadBtn.setDisable(false);
          this.synchronizeBtn.setDisable(false);
        });
      }
    });
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                     No players listed here?\nCreate players to match their initials with highscores."));

    columnRank.setCellValueFactory(cellData -> {
      RankedAccount value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label("#" + value.getRanking());
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnPoints.setCellValueFactory(cellData -> {
      RankedAccount value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label(String.valueOf(value.getPoints()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnName.setCellValueFactory(cellData -> {
      RankedAccount value = cellData.getValue();
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
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      hBox.getChildren().add(label);

      new Thread(() -> {
        Image avatarImage = ManiaAvatarCache.getAvatarImage(value.getUuid());
        if (avatarImage != null) {
          Platform.runLater(() -> {
            view.setImage(avatarImage);
            CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
          });
        }
      }).start();
      return new SimpleObjectProperty(hBox);
    });

    columnFirst.setCellValueFactory(cellData -> {
      RankedAccount value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getPlace1()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnSecond.setCellValueFactory(cellData -> {
      RankedAccount value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getPlace2()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnThird.setCellValueFactory(cellData -> {
      RankedAccount value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getPlace3()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });


    this.showPlayerBtn.setDisable(true);
    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RankedAccount>() {
      @Override
      public void changed(ObservableValue<? extends RankedAccount> observable, RankedAccount oldValue, RankedAccount newValue) {
        showPlayerBtn.setDisable(newValue == null);
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay-plain.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Ranking...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void refresh() {
    this.page = 0;
    doSearch();
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
  }
}
package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.RankedAccount;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaHighscoreSyncResult;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.HighscoreSynchronizeProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaWidgetPlayerRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetPlayerRankController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<RankedPlayer> tableView;

  @FXML
  private TableColumn<RankedPlayer, String> columnRank;

  @FXML
  private TableColumn<RankedPlayer, String> columnPoints;

  @FXML
  private TableColumn<RankedPlayer, String> columnName;

  @FXML
  private TableColumn<RankedPlayer, String> columnFirst;

  @FXML
  private TableColumn<RankedPlayer, String> columnSecond;

  @FXML
  private TableColumn<RankedPlayer, String> columnThird;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button synchronizeBtn;

  private Parent loadingOverlay;
  private List<RankedPlayer> rankedPlayers;

  private Map<String, Image> rankedPlayersAvatarCache = new HashMap<>();

  // Add a public no-args constructor
  public ManiaWidgetPlayerRankController() {
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
      List<GameRepresentation> gamesCached = Studio.client.getGameService().getGamesCached(-1);
      List<VpsTable> vpsTables = new ArrayList<>();
      for (GameRepresentation game : gamesCached) {
        if (!StringUtils.isEmpty(game.getExtTableId())) {
          VpsTable tableById = Studio.client.getVpsService().getTableById(game.getExtTableId());
          if (tableById != null) {
            vpsTables.add(tableById);
          }
        }
      }

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new HighscoreSynchronizeProgressModel("Highscore Synchronization", vpsTables));
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
    rankedPlayersAvatarCache.clear();
    refresh();
  }

  @FXML
  private void onHelp() {
    Studio.browse("https://github.com/syd711/vpin-studio/wiki/Mania#Player-Ranking");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                     No players listed here?\nCreate players to match their initials with highscores."));

    columnRank.setCellValueFactory(cellData -> {
      RankedPlayer value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label("#" + (rankedPlayers.indexOf(value) + 1));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnPoints.setCellValueFactory(cellData -> {
      RankedPlayer value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label(String.valueOf(value.getPoints()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnName.setCellValueFactory(cellData -> {
      RankedPlayer value = cellData.getValue();
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
        Image avatarImage = getAvatarImage(value);
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
      RankedPlayer value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getPlace1()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnSecond.setCellValueFactory(cellData -> {
      RankedPlayer value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getPlace2()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnThird.setCellValueFactory(cellData -> {
      RankedPlayer value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getPlace3()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
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

  private Image getAvatarImage(RankedPlayer value) {
    Image avatarImage = null;
    if (rankedPlayersAvatarCache.containsKey(value.getUuid())) {
      return rankedPlayersAvatarCache.get(value.getUuid());
    }

    InputStream in = client.getCachedUrlImage(maniaClient.getAccountClient().getAvatarUrl(value.getUuid()));
    if (in != null) {
      rankedPlayersAvatarCache.put(value.getUuid(), avatarImage);
      avatarImage = new Image(in);
    }
    else {
      avatarImage = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
    }
    rankedPlayersAvatarCache.put(value.getUuid(), avatarImage);
    return avatarImage;
  }

  public void refresh() {
    this.synchronizeBtn.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.tableView.setVisible(false);
    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    new Thread(() -> {
      try {
        List<RankedAccount> rankedAccounts = maniaClient.getAccountClient().getRankedAccounts();
        if(rankedAccounts != null) {
          rankedPlayers = rankedAccounts.stream().map(r -> new RankedPlayer(r)).collect(Collectors.toList());
          Collections.sort(rankedPlayers, new Comparator<RankedPlayer>() {
            @Override
            public int compare(RankedPlayer o1, RankedPlayer o2) {
              return o2.points - o1.getPoints();
            }
          });

          Platform.runLater(() -> {
            tableStack.getChildren().remove(loadingOverlay);
            tableView.setVisible(true);

            ObservableList<RankedPlayer> data = FXCollections.observableList(rankedPlayers);
            tableView.setItems(data);
            tableView.refresh();
          });
        }
        else {
          LOG.error("Failed to load player stats.");
        }
      }
      catch (Exception e) {
        LOG.error("Failed to load player stats: " + e.getMessage(), e);
      }
      finally {
        Platform.runLater(() -> {
          tableStack.getChildren().remove(loadingOverlay);
          tableView.setVisible(true);

          this.reloadBtn.setDisable(false);
          this.synchronizeBtn.setDisable(false);
        });
      }
    }).start();
  }


  class RankedPlayer {
    private final int points;
    private final RankedAccount account;
    private final String displayName;
    private final String uuid;
    private final int place1;
    private final int place2;
    private final int place3;

    RankedPlayer(RankedAccount account) {
      this.account = account;
      this.points = account.getPlace1() * 4 + account.getPlace2() * 2 + account.getPlace3();
      this.displayName = account.getDisplayName();
      this.uuid = account.getUuid();
      this.place1 = account.getPlace1();
      this.place2 = account.getPlace2();
      this.place3 = account.getPlace3();
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getUuid() {
      return uuid;
    }

    public int getPlace1() {
      return place1;
    }

    public int getPlace2() {
      return place2;
    }

    public int getPlace3() {
      return place3;
    }

    public int getPoints() {
      return points;
    }

    public RankedAccount getAccount() {
      return account;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RankedPlayer that = (RankedPlayer) o;
      return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(uuid);
    }
  }
}
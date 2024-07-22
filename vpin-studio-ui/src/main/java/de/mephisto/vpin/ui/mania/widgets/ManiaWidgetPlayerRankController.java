package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.connectors.mania.model.RankedAccount;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.fx.ServerFX.client;
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

  private Parent loadingOverlay;
  private List<RankedPlayer> rankedPlayers;

  // Add a public no-args constructor
  public ManiaWidgetPlayerRankController() {
  }

  @FXML
  private void onReload() {
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
        InputStream in = client.getCachedUrlImage(maniaClient.getAccountClient().getAvatarUrl(value.getUuid()));
        if (in != null) {
          Platform.runLater(() -> {
            Image i = new Image(in);
            view.setImage(i);
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

  public void refresh() {
    this.tableView.setVisible(false);
    if(!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    new Thread(() -> {
      List<RankedAccount> rankedAccounts = maniaClient.getAccountClient().getRankedAccounts();
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
package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.mania.ManiaController;
import de.mephisto.vpin.ui.mania.ManiaDialogs;
import de.mephisto.vpin.ui.mania.TarcisioWheelsDB;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.Nullable;
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
  private Button tableStatsBtn;

  @FXML
  private Label titleLabel;

  private Parent loadingOverlay;
  private Account account;
  private ManiaController maniaController;

  // Add a public no-args constructor
  public ManiaWidgetPlayerStatsController() {
  }


  public void onViewActivated(@Nullable NavigationOptions options) {
    if (options == null || options.getModel() == null) {
      PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
      if (defaultPlayer.getTournamentUserUuid() == null) {
        return;
      }
      account = maniaClient.getAccountClient().getAccountByUuid(defaultPlayer.getTournamentUserUuid());
      if (account != null) {
        refresh();
      }
      return;
    }

    if (options.getModel() != null && options.getModel() instanceof Account) {
      this.account = (Account) options.getModel();
      refresh();
    }
  }

  @FXML
  private void onPlayerSearch() {
    this.account = ManiaDialogs.openAccountSearchDialog();
    setData(this.account);
  }

  @FXML
  private void onReload() {
    maniaClient.getHighscoreClient().clearTableHighscoresCache();
    this.refresh();
  }

  @FXML
  private void onTableStats() {
    TableScoreModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null && selectedItem.getVpsTable() != null) {
      maniaController.selectVpsTable(selectedItem.getVpsTable());
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
    titleLabel.setText("Player Statistics");

    columnName.setCellValueFactory(cellData -> {
      VpsTable tableById = cellData.getValue().getVpsTable();
      if (tableById == null) {
        return new SimpleStringProperty("Invalid VPS Table");
      }

      HBox hBox = new HBox(3);
      hBox.setAlignment(Pos.CENTER_LEFT);

      InputStream imageInput = TarcisioWheelsDB.getWheelImage(tableById.getId());
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
      Label label = new Label(ScoreFormatUtil.formatScore(String.valueOf(value.getScore())));
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

    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableScoreModel>() {
      @Override
      public void changed(ObservableValue<? extends TableScoreModel> observable, TableScoreModel oldValue, TableScoreModel newValue) {
        tableStatsBtn.setDisable(newValue == null);
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
  }

  public void refresh() {
    try {
      if (account == null) {
        return;
      }

      this.reloadBtn.setDisable(true);
      this.tableView.setVisible(false);

      if (!tableStack.getChildren().contains(loadingOverlay)) {
        tableStack.getChildren().add(loadingOverlay);
      }

      new Thread(() -> {
        List<TableScore> highscoresByAccount = new ArrayList<>(maniaClient.getHighscoreClient().getHighscoresByAccount(account.getId()));
        Platform.runLater(()-> {
          titleLabel.setText("Player Statistics for \"" + account.getDisplayName() + "\" [" + account.getInitials() + "] - (" + highscoresByAccount.size() + " scores)");
        });

        List<TableScoreModel> models = highscoresByAccount.stream().map(score -> new TableScoreModel(score, account)).collect(Collectors.toList());
        Collections.sort(models, Comparator.comparing(TableScoreModel::getName));
        ObservableList<TableScoreModel> data = FXCollections.observableList(models);

        Platform.runLater(() -> {
          tableStack.getChildren().remove(loadingOverlay);
          tableView.setVisible(true);
          tableView.setItems(data);
          tableView.refresh();
          this.reloadBtn.setDisable(false);
        });
      }).start();
    }
    catch (Exception e) {
      LOG.error("Failed to refresh player stats: " + e.getMessage(), e);
    }
  }

  public void setData(Account account) {
    this.account = account;
    if (account == null) {
      tableView.setPlaceholder(new Label("No player selected."));
      titleLabel.setText("Player Statistics");
      tableView.setItems(FXCollections.emptyObservableList());
      return;
    }

    refresh();
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
  }

  class TableScoreModel {
    private VpsTable vpsTable;
    private VpsTableVersion vpsTableVersion;
    private String score;
    private String name = "???";
    private int position = -1;

    public TableScoreModel(TableScore tableScore, Account account) {
      this.score = String.valueOf(tableScore.getScore());
      this.vpsTable = client.getVpsService().getTableById(tableScore.getVpsTableId());
      if (vpsTable != null) {
        this.name = vpsTable.getName().trim();
        this.vpsTableVersion = vpsTable.getTableVersionById(tableScore.getVpsVersionId());
      }

      List<TableScoreDetails> highscoresByTable = maniaClient.getHighscoreClient().getHighscoresByTable(vpsTable.getId());
      Collections.sort(highscoresByTable, (o1, o2) -> Long.compare(o2.getScore(), o1.getScore()));
      for (int i = 0; i < highscoresByTable.size(); i++) {
        TableScoreDetails tableScoreDetails = highscoresByTable.get(i);
        if (tableScoreDetails.getAccountUUID().equals(account.getUuid())) {
          position = i + 1;
          break;
        }
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

    public String getScore() {
      return score;
    }

    public int getPosition() {
      return position;
    }
  }
}
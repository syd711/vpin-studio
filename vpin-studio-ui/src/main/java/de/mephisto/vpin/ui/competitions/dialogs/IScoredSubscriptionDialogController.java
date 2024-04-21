package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.Game;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tournaments.TournamentHelper;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.tournaments.dialogs.IScoredGameRoomProgressModel;
import de.mephisto.vpin.ui.tournaments.view.TournamentTableGameCellContainer;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredSubscriptionDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSubscriptionDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> badgeCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private Button iscoredReloadBtn;

  @FXML
  private Button deleteTableBtn;

  @FXML
  private CheckBox iscoredScoresEnabled;

  @FXML
  private TextField dashboardUrlField;

  @FXML
  private Pane validationContainer;

  @FXML
  private TableView<IScoredSubscription> tableView;

  @FXML
  private TableColumn<IScoredSubscription, Label> statusColumn;

  @FXML
  private TableColumn<IScoredSubscription, String> tableColumn;

  @FXML
  private TableColumn<IScoredSubscription, String> vpsTableColumn;

  @FXML
  private TableColumn<IScoredSubscription, String> vpsTableVersionColumn;

  private Stage stage;

  private List<CompetitionRepresentation> result = new ArrayList<>();

  @FXML
  private void onCancelClick(ActionEvent e) {
    result.clear();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onTableRemove(ActionEvent e) {
    IScoredSubscription selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      result.remove(selectedItem);
    }
    tableView.refresh();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    this.result.clear();
  }

  public void setCompetition(Stage stage, CompetitionRepresentation competition) {
    this.stage = stage;
    this.validationContainer.setVisible(false);

    if (competition != null) {
      this.deleteTableBtn.setDisable(true);
      IScoredSubscription sub = new IScoredSubscription();

    }
  }

  @FXML
  private void loadIScoredTables() {
    String dashboardUrl = this.dashboardUrlField.getText();
    iscoredScoresEnabled.setSelected(false);
    this.tableView.setItems(FXCollections.emptyObservableList());
    this.tableView.refresh();
    this.result.clear();

    if (!StringUtils.isEmpty(dashboardUrl)) {
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new IScoredGameRoomProgressModel(dashboardUrl));
      if (!progressDialog.getResults().isEmpty()) {
        GameRoom gameRoom = (GameRoom) progressDialog.getResults().get(0);
        iscoredScoresEnabled.setSelected(gameRoom.getSettings().isPublicScoresEnabled());

        List<IScoredSubscription> subs = new ArrayList<>();
        List<Game> games = gameRoom.getGames();
        for (Game game : games) {
          List<String> tags = game.getTags();
          Optional<String> first = tags.stream().filter(t -> t.startsWith(VPS.BASE_URL)).findFirst();
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

              VpsTable vpsTable = VPS.getInstance().getTableById(tableId);

              String[] split = vpsUrl.split("#");
              VpsTableVersion vpsVersion = null;
              if (vpsTable != null && split.length > 1) {
                vpsVersion = vpsTable.getVersion(split[1]);
              }
              GameRepresentation gameRep = null;
              if (vpsTable != null) {
                gameRep = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
              }

              GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
              IScoredSubscription sub = new IScoredSubscription();
              sub.setVpsTable(vpsTable);
              sub.setiScoredGame(game);
              sub.setVpsTableVersion(vpsVersion);
              if (gameByVpsTable != null) {
                sub.setGameId(gameByVpsTable.getId());
              }

              subs.add(sub);
            } catch (Exception e) {
              LOG.error("Failed to parse table list: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to parse table list: " + e.getMessage());
            }
          }
        }

        this.tableView.setItems(FXCollections.observableList(subs));
        this.tableView.refresh();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validationContainer.setVisible(false);
    tableView.setPlaceholder(new Label("                     No tables selected!\nEnter an iScored Game Room URL to add tables."));

    deleteTableBtn.setDisable(true);
    saveBtn.setDisable(true);


    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    badgeCombo.setItems(imageList);
//    badgeCombo.setCellFactory(c -> new TournamentImageCell(client));
//    badgeCombo.setButtonCell(new TournamentImageCell(client));

    dashboardUrlField.textProperty().addListener((observable, oldValue, newValue) -> loadIScoredTables());


    statusColumn.setCellValueFactory(cellData -> {
      IScoredSubscription value = cellData.getValue();
      Label label = new Label();
      if (value.getGameId() == 0) {
        label.setGraphic(WidgetFactory.createExclamationIcon());
      }
      else {
        label.setGraphic(WidgetFactory.createCheckIcon(null));
      }
      return new SimpleObjectProperty<>(label);
    });

    tableColumn.setCellValueFactory(cellData -> {
      IScoredSubscription value = cellData.getValue();
      return new SimpleObjectProperty(new IScoredGameCellContainer(value));
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      IScoredSubscription value = cellData.getValue();
      VpsTable vpsTable = value.getVpsTable();
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion, "", true));
    });

    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<IScoredSubscription>() {
      @Override
      public void onChanged(Change<? extends IScoredSubscription> c) {
        deleteTableBtn.setDisable(c == null);
      }
    });
  }

  public List<CompetitionRepresentation> getResult() {
    return result;
  }


  class IScoredSubscription {
    private int gameId;
    private VpsTable vpsTable;
    private VpsTableVersion vpsTableVersion;
    private Game iScoredGame;

    public Game getiScoredGame() {
      return iScoredGame;
    }

    public void setiScoredGame(Game iScoredGame) {
      this.iScoredGame = iScoredGame;
    }

    public int getGameId() {
      return gameId;
    }

    public void setGameId(int gameId) {
      this.gameId = gameId;
    }

    public VpsTable getVpsTable() {
      return vpsTable;
    }

    public void setVpsTable(VpsTable vpsTable) {
      this.vpsTable = vpsTable;
    }

    public VpsTableVersion getVpsTableVersion() {
      return vpsTableVersion;
    }

    public void setVpsTableVersion(VpsTableVersion vpsTableVersion) {
      this.vpsTableVersion = vpsTableVersion;
    }
  }
}

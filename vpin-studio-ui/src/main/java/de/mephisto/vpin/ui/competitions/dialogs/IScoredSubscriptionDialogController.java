package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.LocalUISettings;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.Game;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.tournaments.dialogs.IScoredGameRoomProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
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
  private CheckBox iscoredScoresEnabled;

  @FXML
  private TextField dashboardUrlField;

  @FXML
  private Pane validationContainer;

  @FXML
  private CheckBox selectAllCheckbox;

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, CheckBox> selectionColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, Label> statusColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> tableColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> vpsTableColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> vpsTableVersionColumn;

  private Stage stage;

  private List<CompetitionRepresentation> result = new ArrayList<>();
  private List<CompetitionRepresentation> selection = new ArrayList<>();

  @FXML
  private void onCancelClick(ActionEvent e) {
    result.clear();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
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

  @FXML
  private void loadIScoredTables() {
    String dashboardUrl = this.dashboardUrlField.getText();
    iscoredScoresEnabled.setSelected(false);
    this.tableView.setItems(FXCollections.emptyObservableList());
    this.tableView.refresh();
    this.result.clear();

    this.selectAllCheckbox.setSelected(true);

    if (!StringUtils.isEmpty(dashboardUrl)) {
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new IScoredGameRoomProgressModel(dashboardUrl));
      if (!progressDialog.getResults().isEmpty()) {
        LocalUISettings.saveProperty(LocalUISettings.LAST_ISCORED_SELECTION, dashboardUrl);

        GameRoom gameRoom = (GameRoom) progressDialog.getResults().get(0);
        iscoredScoresEnabled.setSelected(gameRoom.getSettings().isPublicScoresEnabled());

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


              CompetitionRepresentation sub = new CompetitionRepresentation();
              GameRepresentation gameRep = null;
              if (vpsTable != null) {
                gameRep = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
                sub.setVpsTableId(vpsTable.getId());
              }
              if (vpsVersion != null) {
                sub.setVpsTableVersionId(vpsVersion.getId());
              }
              if (gameRep != null) {
                sub.setGameId(gameRep.getId());
              }
              selection.add(sub);
              result.add(sub);
            } catch (Exception e) {
              LOG.error("Failed to parse table list: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to parse table list: " + e.getMessage());
            }
          }
        }
        saveBtn.setDisable(selection.isEmpty());
        this.tableView.setItems(FXCollections.observableList(this.result));
        this.tableView.refresh();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validationContainer.setVisible(false);
    tableView.setPlaceholder(new Label("                     No tables selected!\nEnter an iScored Game Room URL to add tables."));

    saveBtn.setDisable(true);

    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    badgeCombo.setItems(imageList);
    badgeCombo.setCellFactory(c -> new CompetitionOfflineDialogController.CompetitionImageListCell(client));
    badgeCombo.setButtonCell(new CompetitionOfflineDialogController.CompetitionImageListCell(client));


    statusColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
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
      CompetitionRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(new IScoredGameCellContainer(value, getLabelCss(cellData.getValue())));
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      VpsTable vpsTable = value.getVpsTable();
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable, getLabelCss(cellData.getValue())));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion, getLabelCss(cellData.getValue()), true));
    });

    selectionColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation c = cellData.getValue();
      CheckBox checkBox = new CheckBox();
      checkBox.selectedProperty().setValue(selection.contains(c));
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean newVal) {
          if(newVal) {
            if(!selection.contains(c)) {
              selection.add(c);
            }
          }
          else {
            selection.remove(c);
          }
          tableView.refresh();
          saveBtn.setDisable(selection.isEmpty());
        }
      });
      return new SimpleObjectProperty<CheckBox>(checkBox);
    });

    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<CompetitionRepresentation>() {
      @Override
      public void onChanged(Change<? extends CompetitionRepresentation> c) {
        saveBtn.setDisable(selection.isEmpty());
      }
    });

    dashboardUrlField.textProperty().addListener((observable, oldValue, newValue) -> loadIScoredTables());
    String latestUrl = LocalUISettings.getProperties(LocalUISettings.LAST_ISCORED_SELECTION);
    if(latestUrl != null) {
      dashboardUrlField.setText(latestUrl);
    }
  }

  private String getLabelCss(CompetitionRepresentation c) {
    String status = "";
    if (!selection.contains(c)) {
      status = "-fx-font-color: #B0ABAB;-fx-text-fill:#B0ABAB;";
    }
    return status;
  }

  public List<CompetitionRepresentation> getResult() {
    return result;
  }
}

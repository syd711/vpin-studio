package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredSubscriptionDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSubscriptionDialogController.class);

  @FXML
  private ComboBox<IScoredGameRoom> gameRoomsCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private CheckBox selectAllCheckbox;

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, CheckBox> selectionColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, Label> statusColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, Label> visibilityColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> tableColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> vpsTableColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> vpsTableVersionColumn;

  private Stage stage;

  private final List<CompetitionRepresentation> tableList = new ArrayList<>();
  private final List<CompetitionRepresentation> selection = new ArrayList<>();

  private List<CompetitionRepresentation> existingCompetitions = new ArrayList<>();

  @FXML
  private void onCancelClick(ActionEvent e) {
    tableList.clear();
    selection.clear();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onIScoredInfo(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

  }

  @Override
  public void onDialogCancel() {
    selection.clear();
    this.tableList.clear();
  }

  private void loadIScoredGameRoomTables() {
//    this.tableView.setItems(FXCollections.emptyObservableList());
//    this.tableView.refresh();
//    this.tableList.clear();
//
//    this.selectAllCheckbox.setSelected(true);
//
//    IScoredGameRoom iScoredGameRoom = gameRoomsCombo.getValue();
//    if (iScoredGameRoom != null && IScored.getGameRoom(iScoredGameRoom.getUrl()) != null) {
//      GameRoom gameRoom = IScored.getGameRoom(iScoredGameRoom.getUrl());
//      List<IScoredGame> games = gameRoom.getGames();
//      List<IScoredGame> errornousGames = new ArrayList<>();
//      for (IScoredGame game : games) {
//        List<String> tags = game.getTags();
//        Optional<String> first = tags.stream().filter(t -> VPS.isVpsTableUrl(t)).findFirst();
//        if (first.isPresent()) {
//          try {
//            String vpsUrl = first.get();
//            URL url = new URL(vpsUrl);
//            String idSegment = url.getQuery();
//
//            String tableId = idSegment.substring(idSegment.indexOf("=") + 1);
//            if (tableId.contains("&")) {
//              tableId = tableId.substring(0, tableId.indexOf("&"));
//            }
//            else if (tableId.contains("#")) {
//              tableId = tableId.substring(0, tableId.indexOf("#"));
//            }
//
//            VpsTable vpsTable = client.getVpsService().getTableById(tableId);
//            if (vpsTable == null) {
//              errornousGames.add(game);
//              continue;
//            }
//
//            String[] split = vpsUrl.split("#");
//            VpsTableVersion vpsVersion = null;
//            if (split.length > 1) {
//              vpsVersion = vpsTable.getTableVersionById(split[1]);
//            }
//
//            if (vpsVersion == null) {
//              errornousGames.add(game);
//              continue;
//            }
//
//            CompetitionRepresentation sub = new CompetitionRepresentation();
//            sub.setType(CompetitionType.ISCORED.name());
//            sub.setUrl(iScoredGameRoom.getUrl());
//            sub.setName("iScored Subscription for '" + vpsTable.getName() + "'");
//            sub.setBadge(iScoredGameRoom.getBadge());
//            GameRepresentation gameRep = null;
//            gameRep = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
//            sub.setVpsTableId(vpsTable.getId());
//            sub.setVpsTableVersionId(vpsVersion.getId());
//            if (gameRep != null) {
//              sub.setRom(gameRep.getRom() != null ? gameRep.getRom() : gameRep.getTableName());
//              sub.setGameId(gameRep.getId());
//            }
//
//            if (!containsExisting(sub)) {
//              selection.add(sub);
//            }
//
//            tableList.add(sub);
//          }
//          catch (Exception e) {
//            LOG.error("Failed to parse table list: " + e.getMessage(), e);
//            WidgetFactory.showAlert(stage, "Error", "Failed to parse table list: " + e.getMessage());
//          }
//        }
//      }
//
//      if (!errornousGames.isEmpty()) {
//        Platform.runLater(() -> {
//          WidgetFactory.showAlert(stage, "One or more VPS tagged tables could be resolved:",
//              "Revisit VPS tag of game \"" + errornousGames.get(0).getName() + "\".");
//        });
//      }
//
//      saveBtn.setDisable(selection.isEmpty());
//      this.tableView.setItems(FXCollections.observableList(this.tableList));
//      this.tableView.refresh();
//    }
//    else {
//      selection.clear();
//      saveBtn.setDisable(true);
//    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
//    tableView.setPlaceholder(new Label("                     No tables selected!\nSelect an iScored Game Room URL to add tables."));
//
//    IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
//    gameRoomsCombo.setItems(FXCollections.observableList(iScoredSettings.getGameRooms()));
//
//    saveBtn.setDisable(true);
//
//    statusColumn.setCellValueFactory(cellData -> {
//      CompetitionRepresentation value = cellData.getValue();
//      Label label = new Label();
//      String iconColor = getIconColor(value);
//
//      if (value.getGameId() == 0) {
//        FontIcon exclamationIcon = WidgetFactory.createExclamationIcon();
//        if (iconColor != null) {
//          exclamationIcon.setIconColor(Paint.valueOf(iconColor));
//        }
//        label.setTooltip(new Tooltip("No matching table set."));
//        label.setGraphic(exclamationIcon);
//      }
//      else {
//        FontIcon checkIcon = WidgetFactory.createCheckIcon(null);
//        if (iconColor != null) {
//          checkIcon.setIconColor(Paint.valueOf(iconColor));
//        }
//        label.setGraphic(checkIcon);
//      }
//      return new SimpleObjectProperty<>(label);
//    });
//
//    visibilityColumn.setCellValueFactory(cellData -> {
//      CompetitionRepresentation value = cellData.getValue();
//      GameRoom gameRoom = IScored.getGameRoomCached(value.getUrl());
//      IScoredGame gameByVps = gameRoom.getGameByVps(value.getVpsTableId(), value.getVpsTableVersionId());
//      Label label = new Label();
//      if (gameByVps != null) {
//        FontIcon icon = WidgetFactory.createIcon("mdi2e-eye-outline");
//        label.setTooltip(new Tooltip("Game is visible"));
//        if (gameByVps.isGameHidden()) {
//          icon = WidgetFactory.createIcon("mdi2e-eye-off-outline");
//          label.setTooltip(new Tooltip("Game is hidden"));
//        }
//        label.setGraphic(icon);
//      }
//      return new SimpleObjectProperty<>(label);
//    });
//
//    tableColumn.setCellValueFactory(cellData -> {
//      CompetitionRepresentation value = cellData.getValue();
//      VpsTable table = client.getVpsService().getTableById(value.getVpsTableId());
//      if (table == null) {
//        return new SimpleStringProperty("No matching VPS table found.");
//      }
//      GameRoom gameRoom = IScored.getGameRoomCached(value.getUrl());
//      return new SimpleObjectProperty(new IScoredGameCellContainer(value, table, gameRoom, getLabelCss(cellData.getValue())));
//    });
//
//    vpsTableColumn.setCellValueFactory(cellData -> {
//      CompetitionRepresentation value = cellData.getValue();
//      VpsTable table = client.getVpsService().getTableById(value.getVpsTableId());
//      if (table == null) {
//        return new SimpleStringProperty("No matching VPS table found.");
//      }
//      return new SimpleObjectProperty(new VpsTableContainer(table, getLabelCss(cellData.getValue())));
//    });
//
//    vpsTableVersionColumn.setCellValueFactory(cellData -> {
//      CompetitionRepresentation value = cellData.getValue();
//      VpsTable table = client.getVpsService().getTableById(value.getVpsTableId());
//      if (table == null) {
//        return new SimpleStringProperty("No matching VPS table found.");
//      }
//      VpsTableVersion vpsTableVersion = table.getTableVersionById(value.getVpsTableVersionId());
//      if (vpsTableVersion == null) {
//        return new SimpleObjectProperty<>("All versions allowed.");
//      }
//      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(cellData.getValue().getVpsTableId(), cellData.getValue().getVpsTableVersionId());
//      return new SimpleObjectProperty(new VpsVersionContainer(table, vpsTableVersion, getLabelCss(cellData.getValue()), gameByVpsTable == null));
//    });
//
//    selectionColumn.setCellValueFactory(cellData -> {
//      CompetitionRepresentation c = cellData.getValue();
//      CheckBox checkBox = new CheckBox();
//      checkBox.setDisable(containsExisting(c));
//      checkBox.selectedProperty().setValue(selection.contains(c) && !containsExisting(c));
//      checkBox.selectedProperty().addListener((ov, old_val, newVal) -> {
//        if (newVal) {
//          if (!selection.contains(c)) {
//            selection.add(c);
//          }
//        }
//        else {
//          selection.remove(c);
//        }
//        tableView.refresh();
//        saveBtn.setDisable(selection.isEmpty());
//      });
//      return new SimpleObjectProperty<>(checkBox);
//    });
//
//    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<CompetitionRepresentation>() {
//      @Override
//      public void onChanged(Change<? extends CompetitionRepresentation> c) {
//        saveBtn.setDisable(selection.isEmpty());
//      }
//    });
//
//    selectAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
//      @Override
//      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//        if (newValue) {
//          selection.clear();
//          for (CompetitionRepresentation competitionRepresentation : tableList) {
//            if (containsExisting(competitionRepresentation)) {
//              continue;
//            }
//
//            selection.add(competitionRepresentation);
//          }
//        }
//        else {
//          selection.clear();
//        }
//
//        tableView.refresh();
//        saveBtn.setDisable(selection.isEmpty());
//      }
//    });
//
//    gameRoomsCombo.valueProperty().addListener(new ChangeListener<IScoredGameRoom>() {
//      @Override
//      public void changed(ObservableValue<? extends IScoredGameRoom> observable, IScoredGameRoom oldValue, IScoredGameRoom newValue) {
//
//      }
//    });
  }

  private boolean containsExisting(CompetitionRepresentation c) {
    for (CompetitionRepresentation existing : this.existingCompetitions) {
      if (existing.getUrl() != null && existing.getUrl().equals(c.getUrl())
          && existing.getVpsTableId() != null && existing.getVpsTableId().equals(c.getVpsTableId())
          && existing.getVpsTableVersionId() != null && existing.getVpsTableVersionId().equals(c.getVpsTableVersionId())) {
        return true;
      }
    }
    return false;
  }

  private String getLabelCss(CompetitionRepresentation c) {
    String status = "";
    if (!selection.contains(c) || containsExisting(c)) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
    return status;
  }

  public String getIconColor(CompetitionRepresentation c) {
    if (!selection.contains(c) || containsExisting(c)) {
      return "#B0ABAB";
    }
    return null;
  }

  public List<CompetitionRepresentation> getTableList() {
    this.tableList.clear();
    IScoredGameRoom iScoredGameRoom = gameRoomsCombo.getValue();
    for (CompetitionRepresentation competitionRepresentation : this.selection) {
      competitionRepresentation.setBadge(iScoredGameRoom.getBadge());
      competitionRepresentation.setHighscoreReset(iScoredGameRoom.isScoreReset());
      competitionRepresentation.setUuid(UUID.randomUUID().toString());
      tableList.add(competitionRepresentation);
    }
    return tableList;
  }

  public void setData(@NonNull IScoredGameRoom gameRoom, List<CompetitionRepresentation> existingCompetitions) {
    this.existingCompetitions = existingCompetitions;
    this.gameRoomsCombo.setValue(gameRoom);
  }
}

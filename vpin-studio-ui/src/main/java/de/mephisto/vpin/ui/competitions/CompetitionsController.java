package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController {

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnName;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnTable;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStatus;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnScore;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnScoreCount;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnWinner;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStartDate;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnEndDate;

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button duplicateBtn;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @FXML
  private void onCompetitionCreate() {
    CompetitionRepresentation c = Dialogs.openCompetitionDialog(null);
    if (c != null) {
      CompetitionRepresentation newCmp = null;
      try {
        newCmp = client.saveCompetition(c);
      } catch (Exception e) {
        WidgetFactory.showAlert(e.getMessage());
      }
      onReload();
      tableView.getSelectionModel().select(newCmp);
    }
  }

  @FXML
  private void onDuplicate() {
    CompetitionRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    if (selection != null) {
      CompetitionRepresentation clone = selection.cloneCompetition();
      CompetitionRepresentation c = Dialogs.openCompetitionDialog(clone);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.saveCompetition(c);
          onReload();
          tableView.getSelectionModel().select(newCmp);
        } catch (Exception e) {
          WidgetFactory.showAlert(e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onEdit() {
    CompetitionRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    if (selection != null) {
      CompetitionRepresentation c = Dialogs.openCompetitionDialog(selection);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.saveCompetition(c);
          onReload();
          tableView.getSelectionModel().select(newCmp);
        } catch (Exception e) {
          WidgetFactory.showAlert(e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onDelete() {
    CompetitionRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    if (selection != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation("Delete Competition", "Delete Competition '" + selection.getName() + "'?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.deleteCompetition(selection);
        onReload();
      }
    }
  }

  @FXML
  private void onReload() {
    CompetitionRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    List<CompetitionRepresentation> competitions = client.getCompetitions();
    ObservableList<CompetitionRepresentation> data = FXCollections.observableList(competitions);
    tableView.setItems(data);
    tableView.refresh();
    tableView.getSelectionModel().select(selection);

    if (selection == null && !data.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
    tableView.setPlaceholder(new Label("            No competitions found.\nClick the '+' button to create a new one."));

    columnName.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(value.getName());

      if (value.isActive()) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
      }
      return new SimpleObjectProperty(label);
    });


    columnTable.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRepresentation game = client.getGame(value.getGameId());
      Label label = new Label("- not available anymore -");
      if (game != null) {
        label = new Label(game.getGameDisplayName());
      }
      return new SimpleObjectProperty(label);
    });

    columnStatus.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      String status = "FINISHED";
      if (value.isActive()) {
        status = "ACTIVE";
      }
      Label label = new Label(status);
      return new SimpleObjectProperty(label);
    });

    columnStartDate.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(DateFormat.getDateInstance().format(value.getStartDate()));
      return new SimpleObjectProperty(label);
    });

    columnEndDate.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(DateFormat.getDateInstance().format(value.getEndDate()));
      return new SimpleObjectProperty(label);
    });

    columnScoreCount.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(String.valueOf(value.getHighscores().size()));
    });

    columnWinner.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      String winner = "-";
      if (value.getWinner() != null) {
        winner = value.getWinner().getName();
      }
      else if(!StringUtils.isEmpty(value.getWinnerInitials())) {
        winner = value.getWinnerInitials();
      }
      return new SimpleObjectProperty(winner);
    });


    tableView.setPlaceholder(new Label("            Mmmh, not up for a challange yet?\n" +
        "Create a new competition by pressing the '+' button."));
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      editBtn.setDisable(disable || !newSelection.isActive());
      deleteBtn.setDisable(disable);
      duplicateBtn.setDisable(disable);
      refreshView(Optional.ofNullable(newSelection));
    });

    onReload();
  }

  private void refreshView(Optional<CompetitionRepresentation> competition) {

  }
}
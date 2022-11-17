package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
  private TableColumn<CompetitionRepresentation, String> columnStartDate;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnEndDate;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @FXML
  private void onCompetitionCreate() {
    CompetitionRepresentation c = WidgetFactory.openCompetitionDialog();
    if (c != null) {
      CompetitionRepresentation newCmp = Studio.client.saveCompetition(c);
      onReload();
      tableView.getSelectionModel().select(newCmp);
    }
  }

  @FXML
  private void onEdit() {

  }

  @FXML
  private void onReload() {
    List<CompetitionRepresentation> competitions = Studio.client.getCompetitions();
    ObservableList<CompetitionRepresentation> data = FXCollections.observableList(competitions);
    tableView.setItems(data);

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      refreshView(Optional.ofNullable(newSelection));
    });

    if (!data.isEmpty()) {
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
      return new SimpleObjectProperty(label);
    });


    columnTable.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRepresentation game = Studio.client.getGame(value.getGameId());
      Label label = new Label("- not available anymore -");
      if(game != null) {
        label = new Label(game.getGameDisplayName());
      }
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

    onReload();
  }

  private void refreshView(Optional<CompetitionRepresentation> competition) {

  }
}
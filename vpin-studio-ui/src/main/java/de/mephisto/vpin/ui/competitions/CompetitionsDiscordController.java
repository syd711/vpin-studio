package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.Dialogs;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsDiscordController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsDiscordController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnName;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnTable;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStatus;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnCompetitionOwner;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnServer;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStartDate;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnEndDate;

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button finishBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private BorderPane competitionWidget;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;
  private WidgetCompetitionSummaryController competitionWidgetController;
  private BorderPane competitionWidgetRoot;


  private ObservableList<CompetitionRepresentation> data;
  private List<CompetitionRepresentation> competitions;
  private CompetitionsController competitionsController;

  // Add a public no-args constructor
  public CompetitionsDiscordController() {
  }

  @FXML
  private void onCompetitionCreate() {
    CompetitionRepresentation c = Dialogs.openDiscordCompetitionDialog(null);
    if (c != null) {
      CompetitionRepresentation newCmp = null;
      try {
        newCmp = client.saveCompetition(c);
        WidgetFactory.showInformation(Studio.stage, "Competition created",
            "The competition has been created and the Discord channel update requested.",
            "Due to the rate limit (2x updates in 10 minutes) the topic update may take a while.");
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      onReload();
      tableView.getSelectionModel().select(newCmp);
    }
  }

  @FXML
  private void onDuplicate() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      CompetitionRepresentation clone = selection.cloneCompetition();
      CompetitionRepresentation c = Dialogs.openDiscordCompetitionDialog(clone);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.saveCompetition(c);
          onReload();
          tableView.getSelectionModel().select(newCmp);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onJoin() {
    CompetitionRepresentation c = Dialogs.openDiscordJoinCompetitionDialog();
    if (c != null) {
      try {
        CompetitionRepresentation newCmp = client.saveCompetition(c);
        onReload();
        tableView.getSelectionModel().select(newCmp);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
    else {
      onReload();
    }
  }

  @FXML
  private void onEdit() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      CompetitionRepresentation c = Dialogs.openDiscordCompetitionDialog(selection);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.saveCompetition(c);
          onReload();
          tableView.getSelectionModel().select(newCmp);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
      else {
        onReload();
      }
    }
  }

  @FXML
  private void onDelete() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      String help = null;
      String help2 = null;
      if (!StringUtils.isEmpty(selection.getWinnerInitials())) {
        help = "The player '" + selection.getWinnerInitials() + "' will have one less won competition.";
      }
      else if(selection.isActive()) {
        help  = "The competition is still active for another " + selection.remainingDays() + " days.";
        help2 = "This will cancel the competition, no winner will be announced.";
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Competition '" + selection.getName() + "'?",
          help, help2);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        client.deleteCompetition(selection);
        onReload();
      }
    }
  }

  @FXML
  private void onFinish() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null && selection.isActive()) {
      String helpText1 = "The competition is active for another " + selection.remainingDays() + " days.";
      String helpText2 = "Finishing the competition will set the current leader as winner.";

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Finish Competition '" + selection.getName() + "'?", helpText1, helpText2);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.finishCompetition(selection);
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    new Thread(() -> {
      competitions = client.getDiscordCompetitions();
      filterCompetitions(competitions);
      data = FXCollections.observableList(competitions);

      Platform.runLater(() -> {
        if (competitions.isEmpty()) {
          competitionWidget.setTop(null);
        }
        else {
          if (competitionWidget.getTop() == null) {
            competitionWidget.setTop(competitionWidgetRoot);
          }
        }

        tableView.setItems(data);
        tableView.refresh();
        if (selection != null) {
          tableView.getSelectionModel().select(selection);
        }
        else if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
        else {
          refreshView(Optional.empty());
        }

        tableView.setVisible(true);
        tableStack.getChildren().remove(loadingOverlay);
      });
    }).start();

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Competitions"));
    tableView.setPlaceholder(new Label("            No competitions found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Competitions...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

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

    columnServer.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();

      DiscordServer discordServer = client.getDiscordServer(value.getDiscordServerId());
      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);
      Image image = new Image(discordServer.getAvatarUrl());
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);

      Label label = new Label(discordServer.getName());
      hBox.getChildren().addAll(view, label);

      return new SimpleObjectProperty(hBox);
    });

    columnCompetitionOwner.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();

      PlayerRepresentation discordPlayer = client.getDiscordPlayer(value.getDiscordServerId(), Long.valueOf(value.getOwner()));
      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);
      Image image = new Image(discordPlayer.getAvatarUrl());
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);

      Label label = new Label(discordPlayer.getName());
      hBox.getChildren().addAll(view, label);

      return new SimpleObjectProperty(hBox);
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

    tableView.setPlaceholder(new Label("            Mmmh, not up for a challange yet?\n" +
        "Create a new competition by pressing the '+' button."));
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      editBtn.setDisable(disable || !newSelection.isActive());
      finishBtn.setDisable(disable || !newSelection.isActive());
      deleteBtn.setDisable(disable);
      duplicateBtn.setDisable(disable);
      refreshView(Optional.ofNullable(newSelection));
    });

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);
    } catch (IOException e) {
      LOG.error("Failed to load c-widget: " + e.getMessage(), e);
    }

    tableView.setRowFactory(tv -> {
      TableRow<CompetitionRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });

    bindSearchField();
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());

      filterCompetitions(this.competitions);
      tableView.setItems(data);
    });
  }

  private void filterCompetitions(List<CompetitionRepresentation> competitions) {
    List<CompetitionRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();
    for (CompetitionRepresentation c : competitions) {
      if (!c.getName().toLowerCase().contains(filterValue.toLowerCase())) {
        continue;
      }

      filtered.add(c);
    }
    data = FXCollections.observableList(filtered);
  }

  private void refreshView(Optional<CompetitionRepresentation> competition) {
    if (competition.isPresent()) {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(competition.get());
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(null);
    }
    competitionsController.setCompetition(competition.orElse(null));
  }

  @Override
  public void onViewActivated() {

  }

  public void setCompetitionsController(CompetitionsController competitionsController) {
    this.competitionsController = competitionsController;
  }

  public Optional<CompetitionRepresentation> getSelection() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }
}
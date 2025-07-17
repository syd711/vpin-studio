package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.validation.CompetitionValidationTexts;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsOfflineController extends BaseCompetitionController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsOfflineController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnName;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnTable;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStatus;

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
  private Button finishBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private BorderPane competitionWidget;

  @FXML
  private StackPane tableStack;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  private Node validationError;

  private Parent loadingOverlay;
  private WidgetCompetitionSummaryController competitionWidgetController;
  private BorderPane competitionWidgetRoot;


  private ObservableList<CompetitionRepresentation> data;
  private List<CompetitionRepresentation> competitions;
  private CompetitionsController competitionsController;
  private WaitOverlayController loaderController;

  // Add a public no-args constructor
  public CompetitionsOfflineController() {
  }

  @FXML
  private void onCompetitionCreate() {
    CompetitionRepresentation c = CompetitionDialogs.openOfflineCompetitionDialog(this.competitions, null);
    if (c != null) {
      CompetitionRepresentation newCmp = null;
      try {
        newCmp = client.getCompetitionService().saveCompetition(c);
      }
      catch (Exception e) {
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
      CompetitionRepresentation c = CompetitionDialogs.openOfflineCompetitionDialog(this.competitions, clone);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(c);
          onReload();
          GameRepresentation game = client.getGameService().getGame(c.getGameId());
          if (game != null) {
            EventManager.getInstance().notifyTableChange(game.getId(), null);
          }
          tableView.getSelectionModel().select(newCmp);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onEdit() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      CompetitionRepresentation c = CompetitionDialogs.openOfflineCompetitionDialog(this.competitions, selection);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(c);
          onReload();
          GameRepresentation game = client.getGameService().getGame(c.getGameId());
          if (game != null) {
            EventManager.getInstance().notifyTableChange(game.getId(), null);
          }
          tableView.getSelectionModel().select(newCmp);
        }
        catch (Exception e) {
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
      else if (selection.isActive()) {
        help = "The competition is still active for another " + selection.remainingDays() + " days.";
        help2 = "This will cancel the competition, no winner will be announced.";
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Competition '" + selection.getName() + "'?",
          help, help2);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitProgressModel<>("Delete Competition", 
          "Deleting Competition " + selection.getName(), 
          () -> client.getCompetitionService().deleteCompetition(selection)));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
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
        client.getCompetitionService().finishCompetition(selection);
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    new Thread(() -> {
      competitions = client.getCompetitionService().getOfflineCompetitions();
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
    super.initialize();
    NavigationController.setBreadCrumb(Arrays.asList("Competitions"));
    tableView.setPlaceholder(new Label("            No competitions found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnName.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(value.getName());

      if (value.isActive()) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;");
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
      if (value.isActive()) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;");
      }

      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);

      InputStream gameMediaItem = ServerFX.client.getGameMediaItem(value.getGameId(), VPinScreen.Wheel);
      if (gameMediaItem == null) {
        gameMediaItem = Studio.class.getResourceAsStream("avatar-blank.png");
      }
      Image image = new Image(gameMediaItem);
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setSmooth(true);
      view.setFitWidth(60);
      view.setFitHeight(60);
      hBox.getChildren().addAll(view, label);

      return new SimpleObjectProperty(hBox);
    });

    columnStatus.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      String status = "FINISHED";
      if (value.isActive()) {
        status = "ACTIVE";
      }
      else if (value.isPlanned()) {
        status = "PLANNED";
      }
      Label label = new Label(status);
      if (value.isActive()) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;");
      }
      return new SimpleObjectProperty(label);
    });

    columnStartDate.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(DateFormat.getDateTimeInstance().format(value.getStartDate()));
      if (value.isActive()) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;");
      }
      return new SimpleObjectProperty(label);
    });

    columnEndDate.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(DateFormat.getDateTimeInstance().format(value.getEndDate()));
      if (value.isActive()) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;");
      }
      return new SimpleObjectProperty(label);
    });

    columnWinner.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      String winner = "-";

      if (!StringUtils.isEmpty(value.getWinnerInitials())) {
        winner = value.getWinnerInitials();
        PlayerRepresentation player = client.getPlayerService().getPlayer(value.getDiscordServerId(), value.getWinnerInitials());
        if (player != null) {
          winner = player.getName();
        }
      }
      return new SimpleObjectProperty(winner);
    });

    tableView.setPlaceholder(new Label("            Mmmh, not up for a challenge yet?\n" +
        "Create a new competition by pressing the '+' button."));
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

      refreshView(Optional.ofNullable(newSelection));
    });

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);
    }
    catch (IOException e) {
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

    validationError.setVisible(false);
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
    validationError.setVisible(false);
    CompetitionRepresentation newSelection = null;
    if (competition.isPresent()) {
      newSelection = competition.get();
    }
    boolean disable = newSelection == null;
    editBtn.setDisable(disable || !newSelection.isActive());
    finishBtn.setDisable(disable || !newSelection.isActive());
    deleteBtn.setDisable(disable);
    duplicateBtn.setDisable(disable);

    if (competition.isPresent()) {
      validationError.setVisible(newSelection.getValidationState().getCode() > 0);
      if (newSelection.getValidationState().getCode() > 0) {
        LocalizedValidation validationResult = CompetitionValidationTexts.getValidationResult(newSelection);
        validationErrorLabel.setText(validationResult.getLabel());
        validationErrorText.setText(validationResult.getText());
      }

      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(CompetitionType.OFFLINE, competition.get());
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(CompetitionType.OFFLINE, null);
    }
    competitionsController.setCompetition(competition.orElse(null));
  }

  @Override
  public void onViewActivated(NavigationOptions options) {

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
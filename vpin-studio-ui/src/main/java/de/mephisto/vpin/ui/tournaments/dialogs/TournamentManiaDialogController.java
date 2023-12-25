package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.tournaments.TournamentDialogs;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentManiaDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentManiaDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> competitionIconCombo;

  @FXML
  private ComboBox<String> startTime;

  @FXML
  private ComboBox<String> endTime;

  @FXML
  private BorderPane avatarPane;

  @FXML
  private Button saveBtn;

  @FXML
  private Button addTableBtn;

  @FXML
  private Button deleteTableBtn;

  @FXML
  private CheckBox visibilityCheckbox;

  @FXML
  private TextField nameField;

  @FXML
  private TextField iscoredUrlField;

  @FXML
  private Label durationLabel;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  @FXML
  private TableView<GameRepresentation> tableView;

  private ManiaTournamentRepresentation tournament;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.tournament = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onTableAdd(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    List<VpsTableVersion> tableSelection = TournamentDialogs.openTableSelectionDialog(stage, "Virtual Pinball Spreadsheet - Table Selection", tournament);
  }

  @FXML
  private void onTableRemove(ActionEvent e) {

  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  private void validate() {
    if (this.tournament.getCabinetId() != null) {
      return;
    }

    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

    Date startDate = tournament.getStartDate();
    Date endDate = tournament.getEndDate();
    this.durationLabel.setText(DateUtil.formatDuration(startDate, endDate));

    if (StringUtils.isEmpty(tournament.getDisplayName())) {
      validationTitle.setText("No tournament name set.");
      validationDescription.setText("Define a meaningful tournament name.");
      return;
    }

    if (startDate == null || endDate == null || startDate.getTime() >= endDate.getTime()) {
      validationTitle.setText("Invalid start/end date set.");
      validationDescription.setText("Define a valid start and end date.");
      return;
    }

    ObservableList<GameRepresentation> items = this.tableView.getItems();
    if (items.isEmpty()) {
      validationTitle.setText("Not tables selected.");
      validationDescription.setText("A tournament must have at least one table to be played.");
      return;
    }

    validationContainer.setVisible(false);
    this.saveBtn.setDisable(false);
  }

  @Override
  public void onDialogCancel() {
    this.tournament = null;
  }

  public void setTournament(ManiaTournamentRepresentation selectedTournament) {
    if (selectedTournament != null) {
//      this.saveBtn.setDisable(selectedTournament.ge);

      boolean isOwner = true; //TODO selectedTournament.getOwner().equals(botId);
      boolean editable = isOwner && !selectedTournament.isActive();

      competitionIconCombo.setDisable(!editable);
      this.nameField.setText(selectedTournament.getDisplayName());
      this.nameField.setDisable(!editable);

      this.startDatePicker.setValue(selectedTournament.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.startDatePicker.setDisable(!editable);
      this.startTime.setValue(DateUtil.formatTimeString(selectedTournament.getStartDate()));
      this.startTime.setDisable(!editable);

      this.endDatePicker.setValue(selectedTournament.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.endDatePicker.setDisable(!editable);
      this.endTime.setValue(DateUtil.formatTimeString(selectedTournament.getEndDate()));
      this.endTime.setDisable(!editable);

//      this.competitionIconCombo.setValue(selectedTournament.getBadge());

      this.tournament = selectedTournament;

      this.validationContainer.setVisible(editable);
    }
    validate();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                     No tables selected!\nUse the '+' button to add tables to this tournament."));


    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (!StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
    }

    Tile avatar = TileBuilder.create()
      .skinType(Tile.SkinType.IMAGE)
      .prefSize(300, 300)
      .backgroundColor(Color.TRANSPARENT)
      .image(image)
      .imageMask(Tile.ImageMask.ROUND)
      .text("")
      .textSize(Tile.TextSize.BIGGER)
      .textAlignment(TextAlignment.CENTER)
      .build();

    avatarPane.setCenter(avatar);

    ManiaAccountRepresentation account = maniaClient.getAccountClient().getAccount();

    tournament = new ManiaTournamentRepresentation();
//    tournament.setType(CompetitionType.MANIA.name());
//    tournament.setName("My Tournament (Season 1)");
//    tournament.setUuid(UUID.randomUUID().toString());
//    tournament.setOwner(account.getUuid());
//    tournament.setHighscoreReset(true);

    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    tournament.setStartDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    tournament.setEndDate(end);

    saveBtn.setDisable(true);

    nameField.setText(tournament.getDisplayName());
    nameField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("nameField", () -> {
      Platform.runLater(() -> {
        if (nameField.getText().length() > 40) {
          String sub = nameField.getText().substring(0, 40);
          nameField.setText(sub);
        }
        tournament.setDisplayName(nameField.getText());
        validate();
      });
    }, 500));


    startDatePicker.setValue(LocalDate.now());
    startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      tournament.setStartDate(date);
      validate();
    });
    startTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    startTime.setValue("00:00");
    startTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      tournament.setStartDate(date);
      validate();
    });

    endDatePicker.setValue(LocalDate.now().plus(7, ChronoUnit.DAYS));
    endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      tournament.setEndDate(date);
      validate();
    });
    endTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    endTime.setValue("00:00");
    endTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      tournament.setEndDate(date);
      validate();
    });

    List<GameRepresentation> games = client.getGameService().getGamesCached();
    List<GameRepresentation> filtered = new ArrayList<>();
    for (GameRepresentation game : games) {
      if (StringUtils.isEmpty(game.getRom())) {
        continue;
      }

      filtered.add(game);
    }

    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(filtered);
//    tableCombo.getItems().addAll(gameRepresentations);
//    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
//      competition.setGameId(t1.getId());
//      refreshPreview(t1, competitionIconCombo.getValue());
//      refreshVPS(t1);
//      validate();
//    });

    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    competitionIconCombo.setItems(imageList);
    competitionIconCombo.setCellFactory(c -> new CompetitionImageListCell(client));
    competitionIconCombo.valueProperty().addListener((observableValue, s, t1) -> {
//      tournament.setBadge(t1);
      validate();
    });

    this.visibilityCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

    validate();
  }

  public ManiaTournamentRepresentation getTournament() {
    return tournament;
  }

  static class CompetitionImageListCell extends ListCell<String> {

    private final VPinStudioClient client;

    public CompetitionImageListCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getCompetitionService().getCompetitionBadge(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }
}

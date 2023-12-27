package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentVisibility;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.tournaments.TournamentDialogs;
import de.mephisto.vpin.ui.tournaments.view.GameCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.vps.VpsSelection;
import de.mephisto.vpin.ui.vps.VpsTableContainer;
import de.mephisto.vpin.ui.vps.VpsVersionContainer;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
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

public class TournamentManiaDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentManiaDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> tournamentBadgeCombo;

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
  private TextField dashboardUrlField;

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
  private TableView<TournamentTreeModel> tableView;

  @FXML
  private TableColumn<TournamentTreeModel, String> statusColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> tableColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> vpsTableColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> vpsTableVersionColumn;

  private ManiaTournamentRepresentation tournament;

  private List<TournamentTreeModel> tableSelection = new ArrayList<>();

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.tournament = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onTableAdd(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    VpsSelection vpsSelection = TournamentDialogs.openTableSelectionDialog(stage);
    if (vpsSelection.getTable() != null) {
      GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(vpsSelection.getTable(), vpsSelection.getVersion());
      tableSelection.add(new TournamentTreeModel(null, gameByVpsTable, vpsSelection.getTable(), vpsSelection.getVersion()));
      reloadTables();
    }
  }

  @FXML
  private void onTableRemove(ActionEvent e) {
    TournamentTreeModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      tableSelection.remove(selectedItem);
    }
    reloadTables();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    ObservableList<TournamentTreeModel> items = this.tableView.getItems();
    List<String> tableEntries = new ArrayList<>();
    for (TournamentTreeModel item : items) {
      String entry = item.getVpsTable().getId() + "#";
      if (item.getVpsTableVersion() != null) {
        entry += item.getVpsTableVersion().getId();
      }
      tableEntries.add(entry);
    }
    tournament.setTableIds(String.join(",", tableEntries));

    stage.close();
  }

  private void reloadTables() {
    tableView.setItems(FXCollections.observableList(tableSelection));
    tableView.refresh();
    this.validate();
  }

  private void validate() {
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
      validationDescription.setText("The end date must be after the start date.");
      return;
    }

    ObservableList<TournamentTreeModel> items = this.tableView.getItems();
    if (items.isEmpty()) {
      validationTitle.setText("Not tables selected.");
      validationDescription.setText("A tournament must have at least one table to be played.");
      return;
    }

    for (TournamentTreeModel item : items) {
      if (!item.isValid()) {
        validationTitle.setText("Table not valid.");
        validationDescription.setText("One or more tables are invalid.");
        return;
      }
    }

    this.saveBtn.setDisable(false);
    validationContainer.setVisible(false);
  }

  @Override
  public void onDialogCancel() {
    this.tournament = null;
  }

  public void setTournament(ManiaTournamentRepresentation selectedTournament) {
    this.tournament = selectedTournament;
    this.visibilityCheckbox.setSelected(this.tournament.getVisibility().equals(ManiaTournamentVisibility.privateTournament));

    boolean isOwner = selectedTournament.getCabinetId() == null || selectedTournament.getCabinetId().equals(client.getTournamentsService().getConfig().getCabinetId());
    boolean editable = isOwner && !selectedTournament.isActive();
    if (tournament.getUuid() == null) {
      editable = true;
    }

    tournamentBadgeCombo.setDisable(tournament.getUuid() != null);
    this.nameField.setText(selectedTournament.getDisplayName());

    this.startDatePicker.setValue(selectedTournament.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    this.startDatePicker.setDisable(!editable);
    this.startTime.setValue(DateUtil.formatTimeString(selectedTournament.getStartDate()));
    this.startTime.setDisable(tournament.getUuid() != null && tournament.isActive());

    this.endDatePicker.setValue(selectedTournament.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    this.endDatePicker.setDisable(!editable);
    this.endTime.setValue(DateUtil.formatTimeString(selectedTournament.getEndDate()));
    this.endDatePicker.setDisable(tournament.getUuid() != null && tournament.isFinished());

    this.validationContainer.setVisible(editable);

    this.tournament = selectedTournament;

    List<String> tableIdList = this.tournament.getTableIdList();
    for (String s : tableIdList) {
      String[] split = s.split("#");
      VpsTable vpsTable = VPS.getInstance().getTableById(split[0]);
      VpsTableVersion vpsVersion = vpsTable.getVersion(split[1]);
      GameRepresentation game = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
      this.tableSelection.add(new TournamentTreeModel(tournament, game, vpsTable, vpsVersion));
    }

    reloadTables();
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

    deleteTableBtn.setDisable(true);
    saveBtn.setDisable(true);

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

    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    tournamentBadgeCombo.setItems(imageList);
    tournamentBadgeCombo.setCellFactory(c -> new TournamentImageCell(client));
    tournamentBadgeCombo.setButtonCell(new TournamentImageCell(client));
    this.visibilityCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

    dashboardUrlField.textProperty().addListener((observable, oldValue, newValue) -> tournament.setDashboardUrl(newValue));


    statusColumn.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue();
      if (value.getGame() == null) {
        return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
      }

      return new SimpleObjectProperty(WidgetFactory.createCheckIcon(null));
    });

    tableColumn.setCellValueFactory(cellData -> {
      GameRepresentation game = cellData.getValue().getGame();
      if (game != null) {
        return new SimpleObjectProperty(new GameCellContainer(game));
      }

      Label label = new Label("Table not installed");
      label.setStyle("-fx-padding: 3 6 3 6;");
      label.getStyleClass().add("error-title");
      return new SimpleObjectProperty(label);
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      VpsTable vpsTable = cellData.getValue().getVpsTable();
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion));
    });

    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TournamentTreeModel>() {
      @Override
      public void onChanged(Change<? extends TournamentTreeModel> c) {
        deleteTableBtn.setDisable(c == null || c.getList().isEmpty());
      }
    });
  }

  public ManiaTournamentRepresentation getTournament() {
    return tournament;
  }

  static class TournamentImageCell extends ListCell<String> {

    private final VPinStudioClient client;

    public TournamentImageCell(VPinStudioClient client) {
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

package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentVisibility;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.PreferenceChangeListener;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentsManiaController implements Initializable, StudioFXController, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsManiaController.class);

  @FXML
  private TreeTableView<TournamentTreeModel> treeTableView;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnName;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnTable;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnStatus;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnOwner;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnStartDate;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnEndDate;

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button finishBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private Button addBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private SplitMenuButton validateBtn;

  @FXML
  private MenuItem validateAllBtn;

  @FXML
  private Button joinBtn;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private StackPane tableStack;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  private Node validationError;

  private Parent loadingOverlay;
  private ObservableList<TournamentTreeModel> data;
  private TournamentsController tournamentsController;
  private WaitOverlayController loaderController;
  private ManiaAccountRepresentation maniaAccount;
  private File tournamentBadgeFile;


  // Add a public no-args constructor
  public TournamentsManiaController() {
  }

  @FXML
  private void onValidate() {
    if (treeTableView.getSelectionModel().getSelectedItem() != null) {
      TournamentTreeModel selectedItem = this.treeTableView.getSelectionModel().getSelectedItem().getValue();
      if (selectedItem != null) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Synchronize Tournament", "This will re-check your local highscores against the Tournaments server data.");
        if (result.get().equals(ButtonType.OK)) {
//          client.getDiscordService().clearCache();
//          client.getDiscordService().checkCompetition(selectedItem);
//          this.onReload();
        }//TODO
      }
    }
  }

  @FXML
  private void onValidateAll() {
//    List<CompetitionRepresentation> competitionRepresentations = client.getCompetitionService().getDiscordCompetitions().stream().filter(d -> !d.isFinished()).collect(Collectors.toList());
//    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Synchronize " + competitionRepresentations.size() + " Competitions?", "This will re-check your local highscores against the Discord server data.");
//    if (result.get().equals(ButtonType.OK)) {
//      Dialogs.createProgressDialog(new CompetitionSyncProgressModel("Synchronizing Competition", competitionRepresentations));
//      this.onReload();
//    }
  }

  @FXML
  private void onCreate() {
    ManiaTournamentRepresentation t = new ManiaTournamentRepresentation();
    t.setDisplayName("New Tournament (Season 1)");
    t.setVisibility(ManiaTournamentVisibility.publicTournament);
    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    t.setEndDate(end);

    t = TournamentDialogs.openTournamentDialog("Create Tournament", t);
    try {
      if (t != null) {
        ProgressDialog.createProgressDialog(new TournamentCreationProgressModel(t, this.getTournamentBadge()));
        onReload();
        treeTableView.getSelectionModel().select(new TreeItem<>(new TournamentTreeModel(t, null, null, null)));
      }
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
  }

  @FXML
  private void onDuplicate() {
    Optional<TournamentTreeModel> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel model = selection.get();
      ManiaTournamentRepresentation t = model.getTournament().cloneTournament();
      t = TournamentDialogs.openTournamentDialog("Create Tournament", t);
      if (t != null) {
        try {
          t = maniaClient.getTournamentClient().create(t, this.getTournamentBadge());
          onReload();
          treeTableView.getSelectionModel().select(new TreeItem<>(new TournamentTreeModel(t, null, null, null)));
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onJoin() {
//    client.clearDiscordCache();
//    CompetitionRepresentation c = Dialogs.openDiscordJoinCompetitionDialog();
//    if (c != null) {
//      try {
//        ProgressResultModel resultModel = Dialogs.createProgressDialog(new CompetitionSavingProgressModel("Joining Competition", c));
//        onReload();
//        //TODO
////        treeTableView.getSelectionModel().select((CompetitionRepresentation) resultModel.results.get(0));
//      } catch (Exception e) {
//        WidgetFactory.showAlert(Studio.stage, e.getMessage());
//      }
//    }
//    else {
//      onReload();
//    }
  }

  @FXML
  private void onEdit() {
    Optional<TournamentTreeModel> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel tournamentTreeModel = selection.get();
      ManiaTournamentRepresentation t = TournamentDialogs.openTournamentDialog(tournamentTreeModel.getTournament().getDisplayName(), tournamentTreeModel.getTournament());
      if (t != null) {
        try {
          t = maniaClient.getTournamentClient().update(t);
          onReload();
          treeTableView.getSelectionModel().select(new TreeItem<>(new TournamentTreeModel(t, null, null, null)));
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onDelete() {
    Optional<TournamentTreeModel> selection = this.getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel tournamentTreeModel = selection.get();
      ManiaTournamentRepresentation tournament = tournamentTreeModel.getTournament();
      boolean isOwner = true;//TODO
      if (isOwner) {
        deleteTournament(tournament);
      }
      else {
        unsubscribeTournament(tournament);
      }
    }
  }

  private void unsubscribeTournament(ManiaTournamentRepresentation tournament) {
    String remainingDayMsg = tournament.remainingDays() == 1 ? "The tournament is active for another day." :
      "The tournament is still active for another " + tournament.remainingDays() + " days.";
    String help = "You are a member of this tournament. The tournament information will not be shown anymore.";
    String help2 = remainingDayMsg;

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Unsubscribe from Tournament '" + tournament.getDisplayName() + "'?",
      help, help2);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//      treeTableView.getSelectionModel().clearSelection();
//      maniaClient.getTournamentClient().deleteTournament(tournament.getUuid());
      onReload();
    }
  }

  private void deleteTournament(ManiaTournamentRepresentation tournament) {
    String remainingDayMsg = tournament.remainingDays() == 1 ? "The tournament is active for another day." :
      "The tournament is still active for another " + tournament.remainingDays() + " days.";
    String help = remainingDayMsg;
    String help2 = "This will cancel the tournament, no winner will be announced.";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Tournament '" + tournament.getDisplayName() + "'?",
      help, help2);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      treeTableView.getSelectionModel().clearSelection();
      maniaClient.getTournamentClient().deleteTournament(tournament.getUuid());
      onReload();
    }
  }

  @FXML
  private void onFinish() {
//    CompetitionRepresentation selection = this.treeTableView.getSelectionModel().getSelectedItem().getValue();
//    if (selection != null && selection.isActive()) {
//      String helpText1 = "The competition is active for another " + selection.remainingDays() + " days.";
//      String helpText2 = "Finishing the competition will set the current leader as winner.";
//
//      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Finish Competition '" + selection.getName() + "'?", helpText1, helpText2);
//      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//        client.getCompetitionService().finishCompetition(selection);
//        onReload();
//      }
//    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();

    tableStack.getChildren().add(loadingOverlay);

    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    DiscordBotStatus discordStatus = client.getDiscordService().getDiscordStatus(guildId);
    if (!discordStatus.isValid()) {
      textfieldSearch.setDisable(true);
      addBtn.setDisable(true);
      editBtn.setDisable(true);
      validateBtn.setDisable(true);
      deleteBtn.setDisable(true);
      duplicateBtn.setDisable(true);
      finishBtn.setDisable(true);
      reloadBtn.setDisable(true);
      joinBtn.setDisable(true);

      treeTableView.setPlaceholder(new Label("                                         No Discord bot found.\nCreate a \"VPin Mania\" account and join tournaments."));
      treeTableView.setVisible(true);
      tableStack.getChildren().remove(loadingOverlay);

      treeTableView.setRoot(null);
      treeTableView.refresh();
      return;
    }

    new Thread(() -> {

      Platform.runLater(() -> {
        TreeItem<TournamentTreeModel> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
        TreeItem<TournamentTreeModel> root = loadTreeModel();
        treeTableView.setRoot(root);

        treeTableView.refresh();
        treeTableView.getSelectionModel().select(selectedItem);
        tableStack.getChildren().remove(loadingOverlay);
        treeTableView.setVisible(true);
      });
    }).start();

  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      treeTableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());
    });
  }

  private String getLabelCss(TournamentTreeModel model) {
    String status = "";
//    if (value.getValidationState().getCode() > 0) {
//      status = "-fx-font-color: #FF3333;-fx-text-fill:#FF3333;";
//    }
//    else if (value.isActive()) {
//      status = "-fx-font-color: #33CC00;-fx-text-fill:#33CC00;";
//    }
//    else if (value.isPlanned()) {
//      status = "";
//    }
    return status;
  }

  private void refreshView(Optional<TournamentTreeModel> model) {
    validationError.setVisible(false);
    TournamentTreeModel newSelection = null;
    if (model.isPresent()) {
      newSelection = model.get();
    }

    boolean disable = newSelection == null;
    boolean isOwner = newSelection != null && newSelection.getTournament() != null && newSelection.getTournament().getCabinetId().equals(maniaClient.getCabinetId());
    editBtn.setDisable(disable || !isOwner || newSelection.getTournament().isFinished());
    validateBtn.setDisable(model.isEmpty() || model.get().getTournament().isFinished());
    finishBtn.setDisable(disable || !isOwner || model.isEmpty() || !model.get().getTournament().isActive());
    deleteBtn.setDisable(disable);
    duplicateBtn.setDisable(disable || !isOwner);
    reloadBtn.setDisable(this.maniaAccount != null);
    addBtn.setDisable(this.maniaAccount != null);
    joinBtn.setDisable(this.maniaAccount != null);

    if (model.isPresent()) {
//      if (newSelection.getValidationState().getCode() > 0) {
//        LocalizedValidation validationResult = CompetitionValidationTexts.getValidationResult(newSelection);
//        validationErrorLabel.setText(validationResult.getLabel());
//        validationErrorText.setText(validationResult.getText());
//      }
    }
    else {
//      competitionsController.setCompetition(tournament.orElse(null)); //TODO
    }


    NavigationController.setBreadCrumb(Arrays.asList("Tournaments"));
    if(model.isPresent()) {
      NavigationController.setBreadCrumb(Arrays.asList("Tournaments", model.get().getTournament().getDisplayName()));
    }
  }

  @Override
  public void onViewActivated() {
    if (this.tournamentsController != null) {
      refreshView(Optional.empty());
    }
    NavigationController.setBreadCrumb(Arrays.asList("Tournaments"));
  }

  public void setTournamentsController(TournamentsController tournamentsController) {
    this.tournamentsController = tournamentsController;
  }

  public Optional<TournamentTreeModel> getSelection() {
    TreeItem<TournamentTreeModel> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      return Optional.of(selectedItem.getValue());
    }
    return Optional.empty();
  }

  private TreeItem<TournamentTreeModel> loadTreeModel() {
    List<ManiaTournamentRepresentation> tournaments = maniaClient.getTournamentClient().getTournaments();
    TreeItem<TournamentTreeModel> root = new TreeItem<>(new TournamentTreeModel(null, null, null, null));
    for (ManiaTournamentRepresentation tournament : tournaments) {
      if (!tournament.getDisplayName().toLowerCase().contains(textfieldSearch.getText().toLowerCase())) {
        continue;
      }
      TreeItem<TournamentTreeModel> treeModel = toTreeModel(tournament);

      root.getChildren().add(treeModel);
    }
    return root;
  }

  private static TreeItem<TournamentTreeModel> toTreeModel(ManiaTournamentRepresentation tournament) {
    TreeItem<TournamentTreeModel> tournamentNode = new TreeItem<>(new TournamentTreeModel(tournament, null, null, null));
    List<String> tableIdList = tournament.getTableIdList();
    for (String s : tableIdList) {
      VpsTableVersion version = null;
      VpsTable table = null;
      GameRepresentation game = null;
      String[] split = s.split("#");
      if (split.length > 0) {
        String tableId = split[0];
        table = VPS.getInstance().getTableById(tableId);
        if (table != null && split.length == 2) {
          version = table.getVersion(split[1]);
          game = client.getGameService().getGameByVpsTable(table, version);
        }
      }

      TournamentTreeModel model = new TournamentTreeModel(tournament, game, table, version);
      tournamentNode.getChildren().add(new TreeItem<>(model));
    }
    return tournamentNode;
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.AVATAR)) {
      if (this.tournamentBadgeFile != null && this.tournamentBadgeFile.exists()) {
        tournamentBadgeFile.delete();
      }
    }
  }

  private File getTournamentBadge() {
    if (this.tournamentBadgeFile == null || !this.tournamentBadgeFile.exists()) {
      try {
        PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
        Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
        if (!StringUtils.isEmpty(avatarEntry.getValue())) {
          image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
        }

        tournamentBadgeFile = File.createTempFile("default-tournament-badge", ".png");
        tournamentBadgeFile.deleteOnExit();
        BufferedImage toWrite = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(toWrite, "png", tournamentBadgeFile);
      } catch (Exception e) {
        LOG.error("Error writing tournament badge file: " + e.getMessage(), e);
      }
    }

    return this.tournamentBadgeFile;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Tournaments"));
    treeTableView.setPlaceholder(new Label("            No tournaments found.\nClick the '+' button to create a new one."));
    treeTableView.setShowRoot(false);

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Tournaments...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnName.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      Label label = new Label(value.getTournament().getDisplayName());
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });


    columnTable.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      VpsTable vpsTable = value.getVpsTable();
      if (vpsTable != null) {
        GameRepresentation game = client.getGameService().getGameByVpsTable(value.getVpsTable(), value.getVpsTableVersion());
        Label label = new Label("- not available anymore -");
        if (game != null) {
          label = new Label(game.getGameDisplayName());
        }
        label.setStyle(getLabelCss(value));

        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);

        ByteArrayInputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
        Image image = new Image(gameMediaItem);
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(60);
        view.setFitHeight(60);
        hBox.getChildren().addAll(view, label);

        return new SimpleObjectProperty(hBox);
      }

      return new SimpleObjectProperty("todo");
    });

    columnOwner.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);
//      PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(value.getDiscordServerId(), Long.valueOf(value.getOwner()));
//      if (discordPlayer != null) {
//        Image image = new Image(client.getCachedUrlImage(discordPlayer.getAvatarUrl()));
//        ImageView view = new ImageView(image);
//        view.setPreserveRatio(true);
//        view.setFitWidth(50);
//        view.setFitHeight(50);
//        CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
//
//        Label label = new Label(discordPlayer.getName());
//        label.setStyle(getLabelCss(value));
//        hBox.getChildren().addAll(view, label);
//      }

      return new SimpleObjectProperty(hBox);
    });

    columnStatus.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      String status = "FINISHED";
      if (value.getTournament().isActive()) {
        status = "ACTIVE";
      }
      else if (value.getTournament().isPlanned()) {
        status = "PLANNED";
      }

      Label label = new Label(status);
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnStartDate.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      Label label = new Label(DateFormat.getDateTimeInstance().format(value.getTournament().getStartDate()));
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnEndDate.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      Label label = new Label(DateFormat.getDateTimeInstance().format(value.getTournament().getEndDate()));
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    treeTableView.setPlaceholder(new Label("            Mmmh, not up for a challange yet?\n" +
      "Create a new tournament by pressing the '+' button."));
    treeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        refreshView(Optional.ofNullable(newSelection.getValue()));
      }
      else {
        refreshView(Optional.empty());
      }
    });

    treeTableView.setRowFactory(tv -> {
      TreeTableRow<TournamentTreeModel> row = new TreeTableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty()) && !editBtn.isDisabled()) {
          onEdit();
        }
      });
      return row;
    });

    validationError.setVisible(false);
    bindSearchField();
    onViewActivated();


//    treeTableView.setVisible(false);

    EventManager.getInstance().addListener(this);
    onReload();
  }
}
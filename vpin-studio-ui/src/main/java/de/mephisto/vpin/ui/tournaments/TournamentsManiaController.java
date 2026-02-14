package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tournaments.view.DateCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTableGameCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentsManiaController implements Initializable, StudioFXController, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  @FXML
  private TreeTableView<TournamentTreeModel> treeTableView;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnName;

  @FXML
  private TreeTableColumn<TournamentTreeModel, Pane> columnTable;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnStatus;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnDate;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnVPSVersion;

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
  private Button createBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button browseBtn;

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
  private Account maniaAccount;
  private File tournamentBadgeFile;
  private Cabinet cabinet;

  private Map<Long, List<TournamentTable>> tournamentTableCache = new HashMap<>();

  // Add a public no-args constructor
  public TournamentsManiaController() {
  }

  @FXML
  private void onJoin() {
    String tournamentToken = WidgetFactory.showInputDialog(Studio.stage, "Join Tournament", "Enter the token of the private tournament you want to join.",
        "If the tournament is public, you can also use the tournament browser.", "The unique token retrieved from the tournament owner.", null);
    if (tournamentToken != null) {
      Tournament tournament = maniaClient.getTournamentClient().lookupTournament(tournamentToken);
      if (tournament == null) {
        WidgetFactory.showAlert(Studio.stage, "No tournament was found for this token.");
      }
      else if (TournamentHelper.isOwner(tournament, cabinet)) {
        WidgetFactory.showAlert(Studio.stage, "You are the owner of tournament \"" + tournament.getDisplayName() + "\".");
      }
      else {
        TournamentDialogs.openTournamentDialog(tournament.getDisplayName(), tournament, true);
        onReload();
      }
    }
  }

  @FXML
  private void onDownload() {
    Optional<TreeItem<TournamentTreeModel>> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel tournamentTreeModel = selection.get().getValue();
      String url = null;
      if (tournamentTreeModel.getVpsTableVersion() != null) {
        url = tournamentTreeModel.getVpsTableVersion().getUrls().get(0).getUrl();
      }
      else if (tournamentTreeModel.getVpsTable() != null) {
        url = VPS.getVpsTableUrl(tournamentTreeModel.getVpsTable().getId());
      }

      if (url == null) {
        return;
      }

      Studio.browse(url);
    }
  }

  @FXML
  private void onCreate() {
    Tournament newTournament = new Tournament();
    newTournament.setDisplayName("New Tournament (Season 1)");
    newTournament.setVisibility(TournamentVisibility.publicTournament);
    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    newTournament.setEndDate(end);
    newTournament.setStartDate(DateUtil.today());

    ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
    newTournament.setDashboardUrl(settings.getDefaultDashboardUrl());
    newTournament.setDiscordLink(settings.getDefaultDiscordLink());
    newTournament.setDescription(settings.getDefaultDescription());
    newTournament.setWebsite(settings.getDefaultWebsite());

    TournamentCreationModel newTournamentModel = TournamentDialogs.openTournamentDialog("Create Tournament", newTournament, false);
    try {
      if (newTournamentModel != null) {
        PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
        Account maniaAccount = null;
        if (defaultPlayer != null && !StringUtils.isEmpty(defaultPlayer.getTournamentUserUuid())) {
          maniaAccount = maniaClient.getAccountClient().getAccountByUuid(defaultPlayer.getTournamentUserUuid());
        }
        if (maniaAccount != null) {
          PreferenceEntryRepresentation avatarEntry = client.getPreferenceService().getPreference(PreferenceNames.AVATAR);
          Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
          if (!StringUtils.isEmpty(avatarEntry.getValue())) {
            image = new Image(client.getAssetService().getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
          }
          BufferedImage badge = SwingFXUtils.fromFXImage(image, null);

          Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(defaultPlayer.getTournamentUserUuid());
          ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TournamentCreationProgressModel(newTournamentModel, accountByUuid, badge));

          if (!progressDialog.getResults().isEmpty()) {
            Object o = progressDialog.getResults().get(0);
            if (o instanceof Tournament) {
              Tournament newT = (Tournament) o;
              Platform.runLater(() -> {
                onReload(Optional.of(new TreeItem<>(new TournamentTreeModel(newT, null, null, null, null))));
              });
            }
            else {
              WidgetFactory.showAlert(Studio.stage, "Error", "Failed to create tournament: " + o);
            }
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error creating tournament: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
  }

  @FXML
  private void onDuplicate() {
    Optional<TreeItem<TournamentTreeModel>> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel model = selection.get().getValue();
      Tournament t = model.getTournament().cloneTournament();
      TournamentCreationModel newTournament = TournamentDialogs.openTournamentDialog("Create Tournament", t, false);
      if (newTournament != null) {
        try {
          PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
          Account maniaAccount = null;
          if (defaultPlayer != null && !StringUtils.isEmpty(defaultPlayer.getTournamentUserUuid())) {
            maniaAccount = maniaClient.getAccountClient().getAccountByUuid(defaultPlayer.getTournamentUserUuid());
          }
          if (maniaAccount != null) {
            ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TournamentCreationProgressModel(newTournament, defaultPlayer.toManiaAccount(), this.getTournamentBadge()));
            if (!progressDialog.getResults().isEmpty()) {
              Object o = progressDialog.getResults().get(0);
              if (o instanceof Tournament) {
                Tournament newT = (Tournament) o;
                Platform.runLater(() -> {
                  onReload(Optional.of(new TreeItem<>(new TournamentTreeModel(newT, null, null, null, null))));
                });
              }
              else {
                WidgetFactory.showAlert(Studio.stage, "Error", "Failed to create tournament: " + o);
              }
            }
          }
        }
        catch (Exception e) {
          LOG.error("Error duplicating tournament: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onBrowse() {
    Tournament t = TournamentDialogs.openTournamentBrowserDialog();
    if (t != null) {
      try {
        TournamentDialogs.openTournamentDialog(t.getDisplayName(), t, true);
        onReload();
      }
      catch (Exception e) {
        LOG.error("Error browsing tournament: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
  }

  @FXML
  private void onEdit() {
    Optional<TreeItem<TournamentTreeModel>> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel tournamentTreeModel = selection.get().getValue();
      TournamentCreationModel updatedTournament = TournamentDialogs.openTournamentDialog(tournamentTreeModel.getTournament().getDisplayName(), tournamentTreeModel.getTournament(), false);
      if (updatedTournament != null) {
        try {
          ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TournamentUpdateProgressModel(updatedTournament.getNewTournamentModel()));
          if (!progressDialog.getResults().isEmpty()) {
            Tournament update = (Tournament) progressDialog.getResults().get(0);
            onReload(Optional.of(new TreeItem<>(new TournamentTreeModel(update, null, null, null, null))));
          }
        }
        catch (Exception e) {
          LOG.error("Error editing tournament: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onDelete() {
    Optional<TreeItem<TournamentTreeModel>> selection = this.getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel tournamentTreeModel = selection.get().getValue();
      Tournament tournament = tournamentTreeModel.getTournament();
      boolean isOwner = TournamentHelper.isOwner(tournament, cabinet);
      if (isOwner) {
        deleteTournament(tournament);
      }
      else {
        unsubscribeTournament(tournament);
      }
    }
  }

  private void unsubscribeTournament(Tournament tournament) {
    String remainingDayMsg = tournament.remainingDays() == 1 ? "The tournament is active for another day." :
        "The tournament is still active for another " + tournament.remainingDays() + " days.";
    String help = "You are a member of this tournament. The tournament information will not be shown anymore.";
    String help2 = remainingDayMsg;

    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Unsubscribe from Tournament '" + tournament.getDisplayName() + "'?",
        help, help2);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
      if (defaultPlayer != null) {
        String tournamentUserUuid = defaultPlayer.getTournamentUserUuid();
        Account account = maniaClient.getAccountClient().getAccountByUuid(tournamentUserUuid);
        maniaClient.getTournamentClient().removeMember(tournament, account);
        onReload(Optional.empty());
      }
      else {
        LOG.error("Can't unsubscribe, default player is not set.");
      }
    }
  }

  private void deleteTournament(Tournament tournament) {
    String remainingDayMsg = tournament.remainingDays() == 1 ? "The tournament is active for another day." :
        "The tournament is still active for another " + tournament.remainingDays() + " days.";
    String help = remainingDayMsg;
    String help2 = "This will cancel the tournament, no winner will be announced.";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Tournament '" + tournament.getDisplayName() + "'?",
        help, help2);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      treeTableView.getSelectionModel().clearSelection();
      maniaClient.getTournamentClient().deleteTournament(tournament.getId());
      onReload(Optional.empty());
    }
  }

  @FXML
  private void onFinish() {
    TournamentTreeModel value = this.treeTableView.getSelectionModel().getSelectedItem().getValue();
    if (value != null && value.getTournament().isActive()) {
      String helpText1 = "The tournament is active for another " + value.getTournament().remainingDays() + " days.";

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Finish Tournament '" + value.getTournament().getDisplayName() + "'?", helpText1, null, "Finish Tournament");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        maniaClient.getTournamentClient().finishTournament(value.getTournament().getId());
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    onReload(Optional.empty());
  }

  private void onReload(Optional<TreeItem<TournamentTreeModel>> selection) {
    try {
      tournamentTableCache.clear();
      client.clearWheelCache();

      treeTableView.setVisible(false);

      if (!tableStack.getChildren().contains(loadingOverlay)) {
        tableStack.getChildren().add(loadingOverlay);
      }

      textfieldSearch.setDisable(true);
      addBtn.setDisable(true);
      createBtn.setDisable(true);
      editBtn.setDisable(true);
      createBtn.setDisable(true);
      deleteBtn.setDisable(true);
      duplicateBtn.setDisable(true);
      finishBtn.setDisable(true);
      reloadBtn.setDisable(true);
      browseBtn.setDisable(true);

      boolean validConfig = isValidTournamentSetupAvailable();

      if (validConfig) {
        addBtn.setDisable(false);
        createBtn.setDisable(false);
        browseBtn.setDisable(false);
        textfieldSearch.setDisable(false);
        reloadBtn.setDisable(false);

        treeTableView.setPlaceholder(new Label("            Mmmh, not up for a challenge yet?\n" +
            "Create a new tournament by pressing the '+' button."));

        treeTableView.setRoot(null);
        treeTableView.refresh();
      }
      else {
        treeTableView.setPlaceholder(new Label("                                        No VPin Mania default player set!\n" +
            "Go to the players section and select the default player for VPin Mania tournaments!"));

        tableStack.getChildren().remove(loadingOverlay);
        treeTableView.setRoot(null);
        treeTableView.setVisible(true);
        treeTableView.refresh();
        return;
      }

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TournamentsSynchronizeProgressModel());
      if (progressDialog.getResults().isEmpty()) {
        return;
      }
      TreeItem<TournamentTreeModel> root = (TreeItem<TournamentTreeModel>) progressDialog.getResults().get(0);
      tableStack.getChildren().remove(loadingOverlay);
      treeTableView.setVisible(true);

      treeTableView.setRoot(root);
      treeTableView.refresh();
      TournamentTreeModel.expandTreeView(root);

      if (selection.isPresent()) {
        treeTableView.getSelectionModel().select(selection.get());
      }
      else if (!root.getChildren().isEmpty()) {
        treeTableView.getSelectionModel().select(0);
      }
    }
    catch (
        Exception e) {
      LOG.error("Tournament reload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Tournament refresh failed: " + e.getMessage());
      });
    }
  }

  private static boolean isValidTournamentSetupAvailable() {
    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    if (defaultPlayer == null) {
      return false;
    }

    if (!StringUtils.isEmpty(defaultPlayer.getTournamentUserUuid())) {
      Account maniaAccount = maniaClient.getAccountClient().getAccountByUuid(defaultPlayer.getTournamentUserUuid());
      if (maniaAccount == null) {
        WidgetFactory.showAlert(Studio.stage, "Error", "The default player's online account does not exist anymore.", "Select the player from the build-in players list and save again.");
        return false;
      }
      return true;
    }
    return false;
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      treeTableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());
    });
  }

  private String getLabelCss(TournamentTreeModel model) {
    String status = "";
    Tournament tournament = model.getTournament();
    if (tournament.isActive()) {
      status = "-fx-font-color: #33CC00;-fx-text-fill:#33CC00;-fx-font-weight: bold;";
    }
    else if (tournament.isPlanned()) {
      status = "-fx-font-color: #FF9933;-fx-text-fill:#FF9933;-fx-font-weight: bold;";
    }
    else if (tournament.isFinished()) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
    return status;
  }

  private void refreshView(Optional<TreeItem<TournamentTreeModel>> model) {
    tournamentsController.setTournament(model);

    validationError.setVisible(false);
    TournamentTreeModel newSelection = null;
    if (model.isPresent()) {
      newSelection = model.get().getValue();
    }

    addBtn.setDisable(true);
    createBtn.setDisable(true);
    editBtn.setDisable(true);
    finishBtn.setDisable(true);
    deleteBtn.setDisable(true);
    duplicateBtn.setDisable(true);
    reloadBtn.setDisable(true);
    browseBtn.setDisable(true);

    boolean validTournamentSetupAvailable = isValidTournamentSetupAvailable();
    if (validTournamentSetupAvailable) {
      boolean disable = newSelection == null;
      boolean isOwner = newSelection != null && TournamentHelper.isOwner(newSelection.getTournament(), cabinet);

      createBtn.setDisable(disable);
      editBtn.setDisable(disable || !isOwner || newSelection.getTournament().isFinished());
      finishBtn.setDisable(disable || !isOwner || !model.get().getValue().getTournament().isActive());

      deleteBtn.setDisable(disable);
      duplicateBtn.setDisable(disable || !isOwner);
      reloadBtn.setDisable(this.maniaAccount != null);
      addBtn.setDisable(this.maniaAccount != null);
      browseBtn.setDisable(this.maniaAccount != null);
    }

    NavigationController.setBreadCrumb(Arrays.asList("Tournaments"));
    if (model.isPresent()) {
      NavigationController.setBreadCrumb(Arrays.asList("Tournaments", model.get().getValue().getTournament().getDisplayName()));
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    cabinet = maniaClient.getCabinetClient().getCabinet();

    Platform.runLater(() -> {
      this.addBtn.setDisable(!isValidTournamentSetupAvailable());
      this.onReload(Optional.empty());
    });
  }

  public void setTournamentsController(TournamentsController tournamentsController) {
    this.tournamentsController = tournamentsController;
  }

  public Optional<TreeItem<TournamentTreeModel>> getSelection() {
    TreeItem<TournamentTreeModel> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      return Optional.of(selectedItem);
    }
    return Optional.empty();
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.AVATAR)) {
      if (this.tournamentBadgeFile != null && this.tournamentBadgeFile.exists()) {
        tournamentBadgeFile.delete();
      }
    }
  }

  private BufferedImage getTournamentBadge() {
    if (this.tournamentBadgeFile == null || !this.tournamentBadgeFile.exists()) {
      try {
        PreferenceEntryRepresentation avatarEntry = client.getPreferenceService().getPreference(PreferenceNames.AVATAR);
        Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
        if (!StringUtils.isEmpty(avatarEntry.getValue())) {
          image = new Image(client.getAssetService().getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
        }

        tournamentBadgeFile = File.createTempFile("default-tournament-badge", ".png");
        tournamentBadgeFile.deleteOnExit();
        return SwingFXUtils.fromFXImage(image, null);
      }
      catch (Exception e) {
        LOG.error("Error writing tournament badge file: " + e.getMessage(), e);
      }
    }

    return null;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Tournaments"));
    treeTableView.setShowRoot(false);

    cabinet = maniaClient.getCabinetClient().getCabinet();

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Tournaments...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnName.setCellValueFactory(cellData -> {
      if (cellData.getValue().getValue().isTournamentNode()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        List<TournamentTable> tables = getCachedTournamentTables(value.getTournament().getId());
        return new SimpleObjectProperty(new TournamentCellContainer(value.getTournament(), tables));
      }
      return null;
    });


    columnTable.setCellValueFactory(cellData -> {
      if (!cellData.getValue().getValue().isTournamentNode()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        VpsTable vpsTable = value.getVpsTable();
        if (vpsTable != null) {
          GameRepresentation game = client.getGameService().getGameByVpsTable(value.getVpsTable(), value.getVpsTableVersion());
          return new SimpleObjectProperty<>(new TournamentTableGameCellContainer(game, value.getTournament(), value.getTournamentTable()));
        }
      }

      return null;
    });

    columnStatus.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      Tournament tournament = value.getTournament();
      if (!cellData.getValue().getValue().isTournamentNode()) {
        GameRepresentation game = client.getGameService().getGameByVpsTable(value.getVpsTable(), value.getVpsTableVersion());
        if (game != null) {
          Label label = new Label("INSTALLED");
          label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;-fx-font-weight: bold;");

          if (tournament.isFinished()) {
            label.setStyle(WidgetFactory.DISABLED_COLOR);
          }
          return new SimpleObjectProperty(label);
        }
        Label label = new Label("NOT\nINSTALLED");
        label.setStyle(ERROR_STYLE + "-fx-font-weight: bold;");

        if (tournament.isFinished()) {
          label.setStyle(WidgetFactory.DISABLED_COLOR);
        }

        return new SimpleObjectProperty(label);
      }
      else {
        String status = "FINISHED";
        if (value.getTournament().isActive()) {
          status = "ACTIVE";
        }
        else if (value.getTournament().isPlanned()) {
          status = "PLANNED";
        }

        String visibility = tournament.getVisibility().equals(TournamentVisibility.publicTournament) ? "(public)" : "(private)";
        status += "\n" + visibility;

        Label label = new Label(status);
        label.setStyle(getLabelCss(value));
        return new SimpleObjectProperty(label);
      }
    });

    columnDate.setCellValueFactory(cellData -> {
      if (cellData.getValue().getValue().isTournamentNode()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        return new SimpleObjectProperty(new DateCellContainer(value.getTournament()));
      }
      return null;
    });

    columnVPSVersion.setCellValueFactory(cellData -> {
      if (!cellData.getValue().getValue().isTournamentNode()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        VpsTable vpsTable = value.getVpsTable();
        VpsTableVersion vpsTableVersion = value.getVpsTableVersion();
        if (vpsTableVersion == null) {
          return new SimpleObjectProperty<>("All versions allowed.");
        }

        GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(value.getVpsTable(), value.getVpsTableVersion());
        return new SimpleObjectProperty(new VpsVersionContainer(vpsTable, vpsTableVersion, TournamentHelper.getLabelCss(value.getTournament(), value.getTournamentTable()), gameByVpsTable == null));
      }
      return null;
    });

    treeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        Platform.runLater(() -> {
          if (!tableStack.getChildren().contains(loadingOverlay)) {
            tableStack.getChildren().add(loadingOverlay);
          }
          new Thread(() -> {
            Platform.runLater(() -> {
              refreshView(Optional.ofNullable(newSelection));
              tableStack.getChildren().remove(loadingOverlay);
            });
          }).start();
        });
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

    EventManager.getInstance().addListener(this);
  }

  private List<TournamentTable> getCachedTournamentTables(long id) {
    if (!tournamentTableCache.containsKey(id)) {
      tournamentTableCache.put(id, maniaClient.getTournamentClient().getTournamentTables(id));
    }
    return tournamentTableCache.get(id);
  }
}
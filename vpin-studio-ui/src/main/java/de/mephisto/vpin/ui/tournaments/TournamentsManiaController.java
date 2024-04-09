package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentVisibility;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tournaments.view.DateCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.vps.containers.VpsTableContainer;
import de.mephisto.vpin.ui.vps.containers.VpsVersionContainer;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
  private TreeTableColumn<TournamentTreeModel, String> columnDate;

  @FXML
  private TreeTableColumn<TournamentTreeModel, String> columnVPSTable;

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
  private SplitMenuButton validateBtn;

  @FXML
  private MenuItem validateAllBtn;

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
        TournamentDialogs.openTournamentDialog(tournament.getDisplayName(), tournament);
      }
    }
  }

  @FXML
  private void onCreate() {
    Tournament t = new Tournament();
    t.setDisplayName("New Tournament (Season 1)");
    t.setVisibility(TournamentVisibility.publicTournament);
    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    t.setEndDate(end);
    t.setStartDate(DateUtil.today());

    TournamentSettings settings = client.getTournamentsService().getSettings();
    t.setDashboardUrl(settings.getDefaultDashboardUrl());
    t.setDiscordLink(settings.getDefaultDiscordLink());
    t.setDescription(settings.getDefaultDescription());

    t = TournamentDialogs.openTournamentDialog("Create Tournament", t);
    try {
      if (t != null) {
        PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
        if (defaultPlayer != null && defaultPlayer.isRegistered()) {
          AssetRepresentation avatar = defaultPlayer.getAvatar();
          ByteArrayInputStream asset = client.getAsset(AssetType.AVATAR, avatar.getUuid());
          BufferedImage badge = ImageIO.read(asset);

          ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TournamentCreationProgressModel(t, defaultPlayer.toManiaAccount(), badge));

          if (!progressDialog.getResults().isEmpty()) {
            Object o = progressDialog.getResults().get(0);
            if (o instanceof Tournament) {
              Tournament newT = (Tournament) o;
              Platform.runLater(() -> {
                onReload(Optional.of(new TreeItem<>(new TournamentTreeModel(newT, null, null, null))));
              });
            }
            else {
              WidgetFactory.showAlert(Studio.stage, "Error", "Failed to create tournament: " + o);
            }
          }
        }
      }
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
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

      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(url));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage(), e);
        }
      }
    }
  }

  @FXML
  private void onDuplicate() {
    Optional<TreeItem<TournamentTreeModel>> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel model = selection.get().getValue();
      Tournament t = model.getTournament().cloneTournament();
      t = TournamentDialogs.openTournamentDialog("Create Tournament", t);
      if (t != null) {
        try {
          PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
          if (defaultPlayer != null && defaultPlayer.isRegistered()) {
            t = maniaClient.getTournamentClient().create(t, defaultPlayer.toManiaAccount(), this.getTournamentBadge());
            TreeItem<TournamentTreeModel> newModel = new TreeItem<>(new TournamentTreeModel(t, null, null, null));
            onReload(Optional.of(newModel));
          }

        } catch (Exception e) {
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
        Tournament selectedTournament = TournamentDialogs.openTournamentDialog(t.getDisplayName(), t);
        if (selectedTournament != null) {
          PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
          String tournamentUserUuid = defaultPlayer.getTournamentUserUuid();
          List<Account> accounts = maniaClient.getAccountClient().getAccounts();
          Optional<Account> first = accounts.stream().filter(a -> a.getUuid().equals(tournamentUserUuid)).findFirst();
          if (first.isPresent()) {
            maniaClient.getTournamentClient().addMember(selectedTournament, first.get());
          }

          onReload();
        }
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
  }

  @FXML
  private void onEdit() {
    Optional<TreeItem<TournamentTreeModel>> selection = getSelection();
    if (selection.isPresent()) {
      TournamentTreeModel tournamentTreeModel = selection.get().getValue();
      Tournament t = TournamentDialogs.openTournamentDialog(tournamentTreeModel.getTournament().getDisplayName(), tournamentTreeModel.getTournament());
      if (t != null) {
        try {
          t = maniaClient.getTournamentClient().update(t);
          onReload(Optional.of(new TreeItem<>(new TournamentTreeModel(t, null, null, null))));
        } catch (Exception e) {
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
//      treeTableView.getSelectionModel().clearSelection();
//      maniaClient.getTournamentClient().deleteTournament(tournament.getUuid());
      onReload(Optional.empty());
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
    onReload(Optional.empty());
  }

  private void onReload(Optional<TreeItem<TournamentTreeModel>> selection) {
    client.clearWheelCache();

    treeTableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    textfieldSearch.setDisable(true);
    addBtn.setDisable(true);
    createBtn.setDisable(true);
    editBtn.setDisable(true);
    createBtn.setDisable(true);
    validateBtn.setDisable(true);
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

      treeTableView.setPlaceholder(new Label("            Mmmh, not up for a challange yet?\n" +
        "Create a new tournament by pressing the '+' button."));

      treeTableView.setRoot(null);
      treeTableView.refresh();
    }
    else {
      treeTableView.setPlaceholder(new Label("                             No default player set!\n" +
        "Go to the players section and select the default player of your VPin!"));

      tableStack.getChildren().remove(loadingOverlay);
      treeTableView.setVisible(true);
      return;
    }

    new Thread(() -> {
      Platform.runLater(() -> {
        TreeItem<TournamentTreeModel> root = loadTreeModel();
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
      });
    }).start();

  }

  private static boolean isValidTournamentSetupAvailable() {
    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();

    boolean validConfig = defaultPlayer != null && defaultPlayer.isRegistered();
    if (validConfig) {
      List<Account> accounts = maniaClient.getAccountClient().getAccounts();
      Optional<Account> first = accounts.stream().filter(a -> a.getUuid().equals(defaultPlayer.getTournamentUserUuid())).findFirst();
      if (first.isEmpty()) {
        WidgetFactory.showAlert(Studio.stage, "Error", "The default player's online account does not exist anymore.", "Select the player from the build-in players list and save again.");
      }

      validConfig = first.isPresent();
    }
    return validConfig;
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
    validateBtn.setDisable(true);
    finishBtn.setDisable(true);
    deleteBtn.setDisable(true);
    duplicateBtn.setDisable(true);
    reloadBtn.setDisable(true);
    browseBtn.setDisable(true);

    boolean validTournamentSetupAvailable = isValidTournamentSetupAvailable();
    if (validTournamentSetupAvailable) {
      boolean disable = newSelection == null;
      boolean isOwner = TournamentHelper.isOwner(newSelection.getTournament(), cabinet);

      createBtn.setDisable(disable);
      editBtn.setDisable(disable || !isOwner || newSelection.getTournament().isFinished());
      validateBtn.setDisable(model.isEmpty() || model.get().getValue().getTournament().isFinished());
      finishBtn.setDisable(disable || !isOwner || !model.get().getValue().getTournament().isActive());

      deleteBtn.setDisable(disable);
      duplicateBtn.setDisable(disable || !isOwner);
      reloadBtn.setDisable(this.maniaAccount != null);
      addBtn.setDisable(this.maniaAccount != null);
      browseBtn.setDisable(this.maniaAccount != null);
    }

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
    if (model.isPresent()) {
      NavigationController.setBreadCrumb(Arrays.asList("Tournaments", model.get().getValue().getTournament().getDisplayName()));
    }
  }

  @Override
  public void onViewActivated() {
    cabinet = maniaClient.getCabinetClient().getCabinet();

    if (this.addBtn.isDisabled()) {
      this.onReload(Optional.empty());
    }
    else if (this.tournamentsController != null) {
      refreshView(this.getSelection());
    }
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

  private TreeItem<TournamentTreeModel> loadTreeModel() {
    List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
    LOG.info("Loaded " + tournaments.size() + " tournaments.");
    TreeItem<TournamentTreeModel> root = new TreeItem<>(new TournamentTreeModel(null, null, null, null));
    for (Tournament tournament : tournaments) {
      if (!tournament.getDisplayName().toLowerCase().contains(textfieldSearch.getText().toLowerCase())) {
        continue;
      }
      TreeItem<TournamentTreeModel> treeModel = TournamentTreeModel.create(tournament);

      root.getChildren().add(treeModel);
    }
    root.setExpanded(true);
    return root;
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
        PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
        Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
        if (!StringUtils.isEmpty(avatarEntry.getValue())) {
          image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
        }

        tournamentBadgeFile = File.createTempFile("default-tournament-badge", ".png");
        tournamentBadgeFile.deleteOnExit();
        return SwingFXUtils.fromFXImage(image, null);
      } catch (Exception e) {
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
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnName.setCellValueFactory(cellData -> {
      if (!cellData.getValue().getChildren().isEmpty()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        return new SimpleObjectProperty(new TournamentCellContainer(value.getTournament()));
      }
      return null;
    });


    columnTable.setCellValueFactory(cellData -> {
      if (cellData.getValue().getChildren().isEmpty()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        VpsTable vpsTable = value.getVpsTable();
        if (vpsTable != null) {
          GameRepresentation game = client.getGameService().getGameByVpsTable(value.getVpsTable(), value.getVpsTableVersion());
          Label label = new Label("- NOT INSTALLED -");
          label.getStyleClass().add("default-headline");
          Image image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
          if (game != null) {
            label = new Label(game.getGameDisplayName());
            label.getStyleClass().add("default-headline");
            ByteArrayInputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
            if (gameMediaItem != null) {
              image = new Image(gameMediaItem);
            }
          }
          HBox hBox = new HBox(3);
          hBox.setAlignment(Pos.CENTER_LEFT);

          ImageView view = new ImageView(image);
          view.setPreserveRatio(true);
          view.setSmooth(true);
          view.setFitWidth(80);
          hBox.getChildren().addAll(view, label);

          return new SimpleObjectProperty(hBox);
        }
      }

      return null;
    });

    columnStatus.setCellValueFactory(cellData -> {
      TournamentTreeModel value = cellData.getValue().getValue();
      Tournament tournament = value.getTournament();
      if (cellData.getValue().getChildren().isEmpty()) {
        GameRepresentation game = client.getGameService().getGameByVpsTable(value.getVpsTable(), value.getVpsTableVersion());
        if (game != null) {
          Label label = new Label("INSTALLED");
          label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00;-fx-font-weight: bold;");
          return new SimpleObjectProperty(label);
        }
        Label label = new Label("NOT\nINSTALLED");
        label.setStyle("-fx-font-color: #FF3333;-fx-text-fill:#FF3333;-fx-font-weight: bold;");
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
      if (!cellData.getValue().getChildren().isEmpty()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        return new SimpleObjectProperty(new DateCellContainer(value.getTournament()));
      }
      return null;
    });

    columnVPSTable.setCellValueFactory(cellData -> {
      if (cellData.getValue().getChildren().isEmpty()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        VpsTable vpsTable = value.getVpsTable();
        return new SimpleObjectProperty(new VpsTableContainer(vpsTable));
      }
      return null;
    });

    columnVPSVersion.setCellValueFactory(cellData -> {
      if (cellData.getValue().getChildren().isEmpty()) {
        TournamentTreeModel value = cellData.getValue().getValue();
        VpsTableVersion vpsTableVersion = value.getVpsTableVersion();
        if (vpsTableVersion == null) {
          return new SimpleObjectProperty<>("All versions allowed.");
        }

        return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion));
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
    onViewActivated();

    EventManager.getInstance().addListener(this);
    onReload(Optional.empty());
  }
}
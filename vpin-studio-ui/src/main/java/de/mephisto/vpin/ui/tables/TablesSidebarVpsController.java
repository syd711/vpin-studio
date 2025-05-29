package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vps.VpsEntry;
import de.mephisto.vpin.ui.tables.vps.VpsEntryComment;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarVpsController implements Initializable, AutoCompleteTextFieldChangeListener, ChangeListener<VpsTableVersion>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarVpsController.class);

  @FXML
  private VBox detailsBox;

  @FXML
  private VBox dataRoot;

  @FXML
  private TextField nameField;

  @FXML
  private Label yearLabel;

  @FXML
  private Label manufacturerLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label updatedLabel;

  @FXML
  private ComboBox<VpsTableVersion> tableVersionsCombo;

  @FXML
  private CheckBox filterCheckbox;

  @FXML
  private Button copyTableVersionBtn;

  @FXML
  private Button openTableVersionBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label versionAuthorsLabel;

  @FXML
  private Label updateDateLabel;

  @FXML
  private VBox errorBox;

  @FXML
  private VBox multiSelectionPane;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private Button vpsResetUpdatesBtn;

  @FXML
  private Button vpsLinkResetBtn;

  @FXML
  private Hyperlink ipdbLink;


  private AutoCompleteTextField autoCompleteNameField;

  private ValidationState validationState;

  private UISettings uiSettings;

  private List<GameRepresentation> games = new ArrayList<>();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarVpsController() {
  }

  @FXML
  private void onVpsLinkReset() {
    if (!games.isEmpty()) {
      try {
        GameRepresentation gameRepresentation = this.games.get(0);
        client.getFrontendService().vpsLink(gameRepresentation.getId(), null, null);
        EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
      }
      catch (Exception e) {
        LOG.error("Failed to save updated VPS data: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onCopyTableVersion() {
    if (!games.isEmpty()) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(VPS.getVpsTableUrl(this.games.get(0).getExtTableId(), this.games.get(0).getExtTableVersionId()));
      clipboard.setContent(content);
    }
  }


  @FXML
  private void onAutoMatch() {
    if (!this.games.isEmpty()) {
      TableDialogs.openAutoMatch(this.games);
    }
  }

  @FXML
  private void onAutoMatchAll() {
    List<GameRepresentation> games = tablesSidebarController.getTableOverviewController().getTableView().getItems().stream().map(g -> g.getGame()).collect(Collectors.toList());
    TableDialogs.openAutoMatchAll(new ArrayList<>(games));
  }

  @FXML
  private void onDismiss() {
    if (validationState != null && !this.games.isEmpty()) {
      GameRepresentation g = games.get(0);
      DismissalUtil.dismissValidation(g, this.validationState);
    }
  }

  @FXML
  private void onOpen() {
    if (!games.isEmpty()) {
      Studio.browse(VPS.getVpsTableUrl(games.get(0).getExtTableId()));
    }
  }

  @FXML
  private void onTableOpen() {
    VpsTableVersion value = this.tableVersionsCombo.getValue();
    VpsUrl vpsUrl = value.getUrls().get(0);
    Studio.browse(vpsUrl.getUrl());
  }

  @FXML
  private void onVpsReset() {
    if (!games.isEmpty()) {
      TableOverviewController.onVpsResetUpdates(games);
    }
  }

  @FXML
  private void onIpdbLink() {
    Studio.browse(ipdbLink.getText());
  }

  @FXML
  private void onUpdate() {
    ProgressDialog.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(new File("<vpsdb.json>"))));
    List<VpsTable> tables = client.getVpsService().getTables();
    refreshSheetData(tables);
  }

  public void setGames(List<GameRepresentation> games) {
    this.tableVersionsCombo.valueProperty().removeListener(this);
    this.games = games;
    this.refreshView(games);
    this.tableVersionsCombo.valueProperty().addListener(this);
  }

  /**
   * Table name change
   *
   * @param value
   */
  @Override
  public void onChange(String value) {
    try {
      if (this.games.isEmpty()) {
        return;
      }

      this.tableVersionsCombo.valueProperty().removeListener(this);
      List<VpsTable> tables = client.getVpsService().getTables();
      Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
      if (selectedEntry.isPresent()) {
        GameRepresentation gameRepresentation = this.games.get(0);
        VpsTable vpsTable = selectedEntry.get();
        client.getFrontendService().vpsLink(gameRepresentation.getId(), vpsTable.getId(), null);
      }
      this.tableVersionsCombo.valueProperty().addListener(this);
      EventManager.getInstance().notifyTableChange(this.games.get(0).getId(), null);
    }
    catch (Exception e) {
      LOG.error("Failed to save updated VPS data: " + e.getMessage(), e);
    }
  }


  public void refreshView(List<GameRepresentation> games) {
    this.multiSelectionPane.setVisible(games.size() > 1);
    this.dataRoot.setVisible(games.size() == 1);
    this.detailsBox.setVisible(games.size() == 1);
    this.filterCheckbox.setDisable(games.size() != 1);
    this.vpsLinkResetBtn.setDisable(games.size() != 1);

    errorBox.setVisible(false);

    dataRoot.getChildren().removeAll(dataRoot.getChildren());

    autoCompleteNameField.reset();
    autoCompleteNameField.setDisable(games.isEmpty());
    tableVersionsCombo.setDisable(games.isEmpty());
    tableVersionsCombo.setValue(null);


    versionAuthorsLabel.setText("-");
    versionAuthorsLabel.setTooltip(null);
    yearLabel.setText("-");
    manufacturerLabel.setText("-");
    playersLabel.setText("-");
    updatedLabel.setText("-");
    ipdbLink.setText("");
    openTableVersionBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);
    autoFillBtn.setDisable(games.isEmpty());
    vpsResetUpdatesBtn.setDisable(true);

    if (!games.isEmpty()) {
      GameRepresentation game = games.get(0);
      vpsResetUpdatesBtn.setDisable(game.getVpsUpdates().isEmpty() && this.games.size() == 1);

      String vpsTableId = game.getExtTableId();
      String vpsTableVersionId = game.getExtTableVersionId();

      if (StringUtils.isEmpty(vpsTableId) || StringUtils.isEmpty(vpsTableVersionId)) {
        PreferenceEntryRepresentation entry = Studio.client.getPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS);
        List<String> ignoredCsvValue = entry.getCSVValue();
        if (!game.getIgnoredValidations().contains(GameValidationCode.CODE_VPS_MAPPING_MISSING) && !ignoredCsvValue.contains(String.valueOf(GameValidationCode.CODE_VPS_MAPPING_MISSING))) {
          errorBox.setVisible(games.size() == 1);
          validationState = new ValidationState();
          validationState.setCode(GameValidationCode.CODE_VPS_MAPPING_MISSING);
          LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);
          errorTitle.setText(validationResult.getLabel());
          errorText.setText(validationResult.getText());
        }
      }

      openTableVersionBtn.setDisable(StringUtils.isEmpty(vpsTableId));
      openTableVersionBtn.setDisable(StringUtils.isEmpty(vpsTableVersionId));
      copyTableVersionBtn.setDisable(StringUtils.isEmpty(vpsTableVersionId));

      if (StringUtils.isNotEmpty(vpsTableId)) {
        // parallel and async get
        JFXFuture
            .supplyAllAsync(
                () -> client.getVpsService().getTableById(vpsTableId),
                () -> client.getFrontendService().getFrontendMedia(game.getId()),
                () -> client.getEmulatorService().getGameEmulator(game.getEmulatorId()))
            .thenAcceptLater(objs -> {
              VpsTable tableById = (VpsTable) objs[0];
              FrontendMediaRepresentation frontendMedia = (FrontendMediaRepresentation) objs[1];
              GameEmulatorRepresentation emulatorRepresentation = (GameEmulatorRepresentation) objs[2];

              if (tableById != null) {
                String[] tableFormats = emulatorRepresentation.getVpsEmulatorFeatures();
                refreshTableView(tableById, frontendMedia, tableFormats);
                if (!StringUtils.isEmpty(vpsTableVersionId)) {
                  VpsTableVersion version = tableById.getTableVersionById(vpsTableVersionId);
                  tableVersionsCombo.setValue(version);
                }
              }
            });
      }
    }
  }

  private void refreshTableView(VpsTable vpsTable, FrontendMediaRepresentation frontendMedia, String[] tableFormats) {
    versionAuthorsLabel.setText("-");
    versionAuthorsLabel.setTooltip(new Tooltip(null));

    List<VpsTableVersion> tableFiles = new ArrayList<>(vpsTable.getTableFilesForFormat(tableFormats));
    if (!tableFiles.isEmpty() && !this.games.isEmpty()) {
      tableVersionsCombo.setItems(FXCollections.emptyObservableList());
      tableFiles.add(0, null);
      tableVersionsCombo.setItems(FXCollections.observableList(tableFiles));
      String extTableVersionId = games.get(0).getExtTableVersionId();

      if (!StringUtils.isEmpty(extTableVersionId)) {
        for (VpsTableVersion tableVersion : tableFiles) {
          if (tableVersion != null && tableVersion.getId().equals(extTableVersionId)) {
            tableVersionsCombo.setValue(tableVersion);
            if (tableVersion.getAuthors() != null) {
              versionAuthorsLabel.setText(String.join(", ", tableVersion.getAuthors()));
              versionAuthorsLabel.setTooltip(new Tooltip(String.join(", ", tableVersion.getAuthors())));
            }
            break;
          }
        }
      }

      Platform.runLater(() -> {
        tableVersionsCombo.show();
        tableVersionsCombo.hide();
      });
    }
    else {
      tableVersionsCombo.setItems(FXCollections.emptyObservableList());
    }

    autoCompleteNameField.setText(vpsTable.getDisplayName());
    yearLabel.setText(String.valueOf(vpsTable.getYear()));
    manufacturerLabel.setText(vpsTable.getManufacturer());
    playersLabel.setText(String.valueOf(vpsTable.getPlayers()));
    ipdbLink.setText(vpsTable.getIpdbUrl());
    ipdbLink.setDisable(StringUtils.isEmpty(vpsTable.getIpdbUrl()) || !vpsTable.getIpdbUrl().startsWith("http"));
    updatedLabel.setText(DateFormat.getDateInstance().format(new Date(vpsTable.getUpdatedAt())));

    boolean doFilter = filterCheckbox.isSelected();

    if (this.games.isEmpty()) {
      return;
    }
    GameRepresentation game = games.get(0);
    TablesSidebarVpsController.addTablesSection(dataRoot, "Table Version", game, VpsDiffTypes.tableNewVersionVPX, vpsTable, false, null);

    if (!doFilter || game.getPupPackName() == null) {
      addSection(dataRoot, "PUP Pack", game, VpsDiffTypes.pupPack, vpsTable.getPupPackFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsPUPPack(), null);
    }

    if (!doFilter || game.getDirectB2SPath() == null) {
      addSection(dataRoot, "Backglasses", game, VpsDiffTypes.b2s, vpsTable.getB2sFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsBackglass(), null);
    }

    if (!doFilter || !game.isAltSoundAvailable()) {
      addSection(dataRoot, "ALT Sound", game, VpsDiffTypes.altSound, vpsTable.getAltSoundFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsAltSound(), null);
    }

    addSection(dataRoot, "ALT Color", game, VpsDiffTypes.altColor, vpsTable.getAltColorFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsAltColor(), null);

    if (!doFilter || !game.isRomExists()) {
      addSection(dataRoot, "ROM", game, VpsDiffTypes.rom, vpsTable.getRomFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsRom(), null);
    }

    addSection(dataRoot, "Sound", game, VpsDiffTypes.sound, vpsTable.getSoundFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsSound(), null);

    List<FrontendMediaItemRepresentation> items = frontendMedia.getMediaItems(VPinScreen.Topper);
    if (!doFilter || items.isEmpty()) {
      addSection(dataRoot, "Topper", game, VpsDiffTypes.topper, vpsTable.getTopperFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsToppper(), null);
    }

    items = frontendMedia.getMediaItems(VPinScreen.Wheel);
    if (!doFilter || items.isEmpty()) {
      addSection(dataRoot, "Wheel Art", game, VpsDiffTypes.wheel, vpsTable.getWheelArtFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsWheel(), null);
    }

    if (!doFilter || game.getPovPath() == null) {
      addSection(dataRoot, "POV", game, VpsDiffTypes.pov, vpsTable.getPovFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsPOV(), null);
    }

    addSection(dataRoot, "Tutorials", game, VpsDiffTypes.tutorial, vpsTable.getTutorialFiles(), !uiSettings.isHideVPSUpdates() && uiSettings.isVpsTutorial(), null);
  }

  public static void addSection(VBox dataRoot, String title, GameRepresentation game, VpsDiffTypes diffTypes, List<? extends VpsAuthoredUrls> urls, boolean showUpdates, @Nullable Predicate<VpsAuthoredUrls> filterPredicate) {
    if (urls == null || urls.isEmpty()) {
      return;
    }

    List<Node> entries = new ArrayList<>();
    for (VpsAuthoredUrls authoredUrl : urls) {
      List<VpsUrl> authoredUrlUrls = authoredUrl.getUrls();
      if (authoredUrlUrls != null && !authoredUrlUrls.isEmpty()) {
        String version = authoredUrl.getVersion();
        long updatedAt = authoredUrl.getUpdatedAt();
        List<String> authors = authoredUrl.getAuthors();

        String updateText = null;
        if (game != null && showUpdates) {
          List<VPSChange> changes = game.getVpsUpdates().getChanges();
          for (VPSChange change : changes) {
            if (change.getId() != null && authoredUrl.getId() != null && change.getId().equals(authoredUrl.getId())) {
              VpsTable gameTable = client.getVpsService().getTableById(game.getExtTableId());
              updateText = change.toString(gameTable);
              break;
            }
          }
        }

        boolean isFiltered = filterPredicate != null ? filterPredicate.test(authoredUrl) : true;

        for (VpsUrl vpsUrl : authoredUrlUrls) {
          String url = vpsUrl.getUrl();
          VpsEntry vpsEntry = new VpsEntry(game, diffTypes, null, null, version, authors, url, updatedAt, updateText, false, isFiltered);
          if (!entries.contains(vpsEntry)) {
            entries.add(vpsEntry);
          }
        }

        if (authoredUrl instanceof VpsBackglassFile) {
          VpsBackglassFile backglassFile = (VpsBackglassFile) authoredUrl;
          if (!StringUtils.isEmpty(backglassFile.getComment())) {
            entries.add(new VpsEntryComment(backglassFile.getComment()));
          }
        }
      }
    }

    if (!entries.isEmpty()) {
      VBox rows = addSectionHeader(dataRoot, title);
      rows.getChildren().clear();
      rows.getChildren().addAll(entries);
    }
  }

  public static void addTablesSection(VBox dataRoot, String title, GameRepresentation game, VpsDiffTypes diffTypes, VpsTable vpsTable, boolean showUpdates, Predicate<VpsTableVersion> filterPredicate) {
    List<VpsTableVersion> tableVersions;
    if (game != null) {
      GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      String[] tableFormats = emulatorRepresentation.getVpsEmulatorFeatures();
      tableVersions = vpsTable.getTableFilesForFormat(tableFormats);
    }
    else {
      tableVersions = vpsTable.getTableFiles();
    }

    if (tableVersions == null || tableVersions.isEmpty()) {
      return;
    }

    VBox rows = addSectionHeader(dataRoot, title);

    // load in Thread as getGameByVpsTable() uses getGamesCached(-1) and that may take a while...
    new Thread(() -> {
      List<Node> entries = new ArrayList<>();
      for (VpsTableVersion vpsTableVersion : tableVersions) {
        List<VpsUrl> authoredUrlUrls = vpsTableVersion.getUrls();
        String version = vpsTableVersion.getVersion();
        long updatedAt = vpsTableVersion.getUpdatedAt();
        List<String> authors = vpsTableVersion.getAuthors();

        String updateText = null;
        if (game != null && showUpdates) {
          List<VPSChange> changes = game.getVpsUpdates().getChanges();
          for (VPSChange change : changes) {
            if (change.getId() != null && vpsTableVersion.getId() != null && change.getId().equals(vpsTableVersion.getId())) {
              VpsTable gameTable = client.getVpsService().getTableById(game.getExtTableId());
              updateText = change.toString(gameTable);
              break;
            }
          }
        }

        // is it installed ?
        GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(-1, vpsTable, vpsTableVersion);
        boolean installed = (gameByVpsTable != null);

        boolean isFiltered = filterPredicate != null ? filterPredicate.test(vpsTableVersion) : true;

        if (authoredUrlUrls != null && !authoredUrlUrls.isEmpty()) {
          for (VpsUrl vpsUrl : authoredUrlUrls) {
            String url = vpsUrl.getUrl();
            VpsEntry vpsEntry = new VpsEntry(gameByVpsTable, diffTypes, vpsTable, vpsTableVersion,
                version, authors, url, updatedAt, updateText, installed, isFiltered);
            if (!entries.contains(vpsEntry)) {
              entries.add(vpsEntry);
            }
          }
        }
        else {
          VpsEntry vpsEntry = new VpsEntry(gameByVpsTable, diffTypes, vpsTable, vpsTableVersion,
              version, authors, null, updatedAt, updateText, installed, isFiltered);
          if (!entries.contains(vpsEntry)) {
            entries.add(vpsEntry);
          }
        }
      }

      // now refresh UI
      Platform.runLater(() -> {
        rows.getChildren().clear();
        if (!entries.isEmpty()) {
          rows.getChildren().addAll(entries);
        }
      });
    }).start();
  }

  private static VBox addSectionHeader(VBox dataRoot, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarVpsController.class.getResource("section-vps.fxml"));
      Pane section = loader.load();
      Label label = (Label) section.getChildren().get(0);
      label.setText(title);
      dataRoot.getChildren().add(section);
      return (VBox) section.getChildren().get(2);
    }
    catch (IOException e) {
      LOG.error("Failed to load VPS sidebar section: {}", e.getMessage(), e);
    }
    return null;
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  private void refreshSheetData(List<VpsTable> tables) {
    entriesLabel.setText(String.valueOf(tables.size()));
    Date changeDate = client.getVpsService().getChangeDate();
    updateDateLabel.setText(DateFormat.getDateTimeInstance().format(changeDate));
  }

  @Override
  public void changed(ObservableValue<? extends VpsTableVersion> observable, VpsTableVersion oldValue, VpsTableVersion newValue) {
    openTableVersionBtn.setDisable(newValue == null || newValue.getUrls().isEmpty());
    copyTableVersionBtn.setDisable(newValue == null);

    if (!this.games.isEmpty()) {
      try {
        GameRepresentation gameRepresentation = this.games.get(0);
        copyTableVersionBtn.setDisable(newValue == null);

        String updatedId = null;
        if (newValue != null) {
          updatedId = newValue.getId();
        }
        // check value of
        String extTableId = gameRepresentation.getExtTableId();
        String extVersionId = gameRepresentation.getExtTableVersionId();
        if (!StringUtils.equals(updatedId, extVersionId)) {
          client.getFrontendService().vpsLink(gameRepresentation.getId(), extTableId, updatedId);
          EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to save VPS version: " + e.getMessage(), e);
      }
    }

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Image image2 = new Image(Studio.class.getResourceAsStream("vps-checked.png"));
    ImageView iconVpsReset = new ImageView(image2);
    iconVpsReset.setFitWidth(18);
    iconVpsReset.setFitHeight(18);
    vpsResetUpdatesBtn.setGraphic(iconVpsReset);

    preferencesChanged(PreferenceNames.SERVER_SETTINGS, null);

    vpsResetUpdatesBtn.managedProperty().bindBidirectional(vpsResetUpdatesBtn.visibleProperty());
    detailsBox.managedProperty().bindBidirectional(detailsBox.visibleProperty());
    dataRoot.managedProperty().bindBidirectional(dataRoot.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    multiSelectionPane.managedProperty().bindBidirectional(multiSelectionPane.visibleProperty());
    multiSelectionPane.setVisible(false);

    vpsResetUpdatesBtn.setDisable(true);
    openTableVersionBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);

    tableVersionsCombo.setCellFactory(c -> new VpsTableVersionCell());
    tableVersionsCombo.setButtonCell(new VpsTableVersionCell());

    filterCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      this.tableVersionsCombo.valueProperty().removeListener(this);
      refreshView(games);
      this.tableVersionsCombo.valueProperty().addListener(this);
    });

    List<VpsTable> tables = client.getVpsService().getTables();
    refreshSheetData(tables);
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(null, this.nameField, this, collect);

    preferencesChanged(PreferenceNames.UI_SETTINGS, null);
    client.getPreferenceService().addListener(this);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      this.vpsResetUpdatesBtn.setVisible(!uiSettings.isHideVPSUpdates());
    }
  }
}
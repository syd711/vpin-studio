package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import de.mephisto.vpin.ui.tables.panels.UploadsButtonController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.tags.TagButton;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarTableDetailsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarTableDetailsController.class);

  @FXML
  private VBox tableDataBox;

  @FXML
  private ToolBar toolbar;

  @FXML
  private Button tableEditBtn;

  @FXML
  private Button fixVersionBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

  @FXML
  private Label emulatorLabel;

  @FXML
  private Label labelLastPlayed;

  @FXML
  private Label labelTimesPlayed;

  @FXML
  private Label gameName;

  @FXML
  private Label gameFileName;

  @FXML
  private Label gameDisplayName;

  @FXML
  private Label gameTheme;

  @FXML
  private Label gameType;

  @FXML
  private Label gameVersion;

  @FXML
  private Label gameYear;

  @FXML
  private Label dateAdded;

  @FXML
  private Label romName;

  @FXML
  private Label manufacturer;

  @FXML
  private Label numberOfPlayers;

  @FXML
  private BorderPane tags;

  @FXML
  private Label category;

  @FXML
  private Label author;

  @FXML
  private Label launchCustomVar;

  @FXML
  private Label keepDisplays;

  @FXML
  private Label gameRating;

  @FXML
  private Label dof;

  @FXML
  private Label IPDBNum;

  @FXML
  private Label altRunMode;

  @FXML
  private Label url;

  @FXML
  private Label designedBy;

  @FXML
  private TextArea notes;

  @FXML
  private Label status;

  @FXML
  private Label altLaunch;

  @FXML
  private Label custom2;

  @FXML
  private Label custom3;

  @FXML
  private Label volume;

  //extras
  @FXML
  private VBox extrasPanel;

  @FXML
  private Label custom4;

  @FXML
  private Label custom5;

  @FXML
  private Label altRomName;

  @FXML
  private Label webDbId;

  @FXML
  private Label webLink;

  @FXML
  private Label isMod;

  @FXML
  private TextArea gNotes;

  @FXML
  private TextArea gDetails;

  @FXML
  private TextArea gLog;

  @FXML
  private TextArea gPlayLog;

  @FXML
  private VBox popperRuntimeFields;

  @FXML
  private VBox gameMetaDataFields;

  @FXML
  private VBox screenFields;

  @FXML
  private VBox tableFilesBox;

  @FXML
  private Label resFileLabel;
  @FXML
  private Label iniFileLabel;
  @FXML
  private Label povFileLabel;
  @FXML
  private Button resFileUploadBtn;
  @FXML
  private Button iniFileUploadBtn;
  @FXML
  private Button povFileUploadBtn;
  @FXML
  private Button resFileDeleteBtn;
  @FXML
  private Button iniFileDeleteBtn;
  @FXML
  private Button povFileDeleteBtn;
  @FXML
  private Button resFileEditBtn;
  @FXML
  private Button iniFileEditBtn;
  @FXML
  private Button povFileEditBtn;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private UploadsButtonController uploadsButtonController;

  // Add a public no-args constructor
  public TablesSidebarTableDetailsController() {
  }

  @FXML
  private void onIniUpload() {
    if (game.isPresent()) {
      TableDialogs.directUpload(Studio.stage, AssetType.INI, game.get(), null);
    }
  }

  @FXML
  private void onResUpload() {
    if (game.isPresent()) {
      TableDialogs.directUpload(Studio.stage, AssetType.RES, game.get(), null);
    }
  }

  @FXML
  private void onPovUpload() {
    if (game.isPresent()) {
      TableDialogs.directUpload(Studio.stage, AssetType.POV, game.get(), null);
    }
  }

  @FXML
  private void onIniEdit() {
    if (game.isPresent()) {
      try {
        GameRepresentation gameRepresentation = game.get();
        String path = gameRepresentation.getIniPath();
        Studio.editGameFile(gameRepresentation, path);
      }
      catch (Exception e) {
        LOG.error("Failed to open .ini file: {}", e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open .ini file: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onResEdit() {
    if (game.isPresent()) {
      try {
        GameRepresentation gameRepresentation = game.get();
        String path = gameRepresentation.getResPath();
        Studio.editGameFile(gameRepresentation, path);
      }
      catch (Exception e) {
        LOG.error("Failed to open .res file: {}", e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open .res file: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onPovEdit() {
    if (game.isPresent()) {
      try {
        GameRepresentation gameRepresentation = game.get();
        String path = gameRepresentation.getPovPath();
        Studio.editGameFile(gameRepresentation, path);
      }
      catch (Exception e) {
        LOG.error("Failed to open .pov file: {}", e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open .pov file: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onIniDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete .ini for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getIniService().delete(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    }
  }

  @FXML
  private void onResDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete .res for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getResService().delete(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    }
  }

  @FXML
  private void onPovDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete POV file for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getVpxService().deletePOV(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    }
  }

  @FXML
  private void onVersionFix() {
    if (game.isPresent()) {
      Frontend frontend = client.getFrontendService().getFrontendCached();

      GameRepresentation gameRepresentation = game.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Auto-Fix Table Version?",
          FrontendUtil.replaceName("This overwrites the existing [Frontend] table version \""
              + gameRepresentation.getVersion() + "\" with the VPS table version \"" +
              gameRepresentation.getExtVersion() + "\".", frontend),
          "The table update indicator won't be shown afterwards.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getFrontendService().fixVersion(gameRepresentation.getId(), gameRepresentation.getExtVersion());
          EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
        }
        catch (Exception ex) {
          LOG.error("Error fixing version: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error fixing version: " + ex.getMessage());
        }
      }
    }
  }

  @FXML
  private void onTableEdit() {
    if (Studio.client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        TableDialogs.openTableDataDialog(this.tablesSidebarController.getTableOverviewController(), this.game.get());
        this.refreshView(this.game);
      }
      return;
    }

    TableDialogs.openTableDataDialog(this.tablesSidebarController.getTableOverviewController(), this.game.get());
    this.refreshView(this.game);
  }

  @FXML
  private void onAutoFill() {
    if (this.game.isPresent()) {
      TableDialogs.openAutoFillSettingsDialog(Studio.stage, Arrays.asList(this.game.get()), null);
    }
  }

  @FXML
  private void onReload() {
    if (this.game.isPresent()) {
      client.getGameService().reload(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    }
  }

  @FXML
  private void onAutoFillAll() {
    List<GameRepresentation> vpxGamesCached = client.getGameService().getVpxGamesCached();
    TableDialogs.openAutoFillSettingsDialog(Studio.stage, vpxGamesCached, null);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.game = g;

    tableFilesBox.setVisible(false);
    if (g.isEmpty()) {
      uploadsButtonController.setData(Collections.emptyList(), tablesSidebarController.getTableOverviewController().getEmulatorSelection());
    }
    else {
      uploadsButtonController.setData(Arrays.asList(g.get()), tablesSidebarController.getTableOverviewController().getEmulatorSelection());
    }

    if (!Features.FIELDS_STANDARD) {
      tableDataBox.getChildren().remove(gameMetaDataFields);
    }
    if (Features.IS_STANDALONE) {
      tableDataBox.getChildren().remove(screenFields);
    }

    this.tableEditBtn.setDisable(g.isEmpty());
    this.fixVersionBtn.setDisable(g.isEmpty() || !g.get().isUpdateAvailable());
    this.reloadBtn.setDisable(g.isEmpty());
    this.autoFillBtn.setDisable(g.isEmpty());

    GameRepresentation game = g.orElse(null);

    if (game != null) {
      GameEmulatorRepresentation gameEmulator = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      tableFilesBox.setVisible(gameEmulator != null && gameEmulator.isVpxEmulator());

      autoFillBtn.setVisible(client.getEmulatorService().isVpxGame(game) && Features.FIELDS_STANDARD);

      dateAdded.setText(game.getDateAdded() == null ? "-" : DateFormat.getDateTimeInstance().format(game.getDateAdded()));
      emulatorLabel.setText(client.getEmulatorService().getGameEmulator(game.getEmulatorId()).getName());
      gameVersion.setText(StringUtils.defaultIfEmpty(game.getVersion(), "-"));
      gameName.setText(StringUtils.defaultIfEmpty(game.getGameName(), "-"));
      gameFileName.setText(StringUtils.defaultIfEmpty(game.getGameFileName(), "-"));
      gameDisplayName.setText(StringUtils.defaultIfEmpty(game.getGameDisplayName(), "-"));
      // will be overriden by tableDetails status
      status.setText(game.isDisabled() ? "Disabled" : "Enabled");
      notes.setText(StringUtils.defaultIfEmpty(game.getComment(), ""));
      romName.setText(StringUtils.defaultIfEmpty(game.getRom(), "-"));

      this.resFileLabel.setText(StringUtils.defaultIfEmpty(game.getResPath(), "-"));
      this.resFileDeleteBtn.setDisable(game.getResPath() == null);
      this.resFileEditBtn.setDisable(game.getResPath() == null);
      this.iniFileLabel.setText(StringUtils.defaultIfEmpty(game.getIniPath(), "-"));
      this.iniFileDeleteBtn.setDisable(game.getIniPath() == null);
      this.iniFileEditBtn.setDisable(game.getIniPath() == null);
      this.povFileLabel.setText(StringUtils.defaultIfEmpty(game.getPovPath(), "-"));
      this.povFileDeleteBtn.setDisable(game.getPovPath() == null);
      this.povFileEditBtn.setDisable(game.getPovPath() == null);
    }
    else {
      autoFillBtn.setVisible(false);

      dateAdded.setText("-");
      emulatorLabel.setText("-");
      gameName.setText("-");
      gameFileName.setText("-");
      gameDisplayName.setText("-");
      status.setText("-");
      notes.setText("");
      romName.setText("-");

      this.povFileDeleteBtn.setDisable(true);
      this.povFileUploadBtn.setDisable(true);
      this.povFileEditBtn.setDisable(true);

      this.resFileDeleteBtn.setDisable(true);
      this.resFileUploadBtn.setDisable(true);
      this.resFileEditBtn.setDisable(true);

      this.iniFileDeleteBtn.setDisable(true);
      this.iniFileUploadBtn.setDisable(true);
      this.iniFileEditBtn.setDisable(true);

      this.resFileLabel.setText("-");
      this.iniFileLabel.setText("-");
      this.povFileLabel.setText("-");
    }


    TableDetails tableDetails = game != null ? Studio.client.getFrontendService().getTableDetails(game.getId()) : null;
    if (tableDetails != null) {

      extrasPanel.setVisible(tableDetails.isPopper15());

      gameType.setText(tableDetails.getGameType() != null ? tableDetails.getGameType() : "-");
      gameTheme.setText(StringUtils.isEmpty(tableDetails.getGameTheme()) ? "-" : tableDetails.getGameTheme());
      gameYear.setText(tableDetails.getGameYear() == null ? "-" : String.valueOf(tableDetails.getGameYear()));
      manufacturer.setText(StringUtils.isEmpty(tableDetails.getManufacturer()) ? "-" : tableDetails.getManufacturer());
      numberOfPlayers.setText(tableDetails.getNumberOfPlayers() == null ? "-" : String.valueOf(tableDetails.getNumberOfPlayers()));
      altLaunch.setText(tableDetails.getAltLaunchExe() == null ? "-" : tableDetails.getAltLaunchExe());

      FlowPane tagsRoot = new FlowPane(3, 3);
      tags.setCenter(tagsRoot);

      for (String tagsValue : game.getTags()) {
        TagButton tagButton = new TagButton(game.getId(), tableDetails, client.getTaggingService().getTags(), tagsValue);
        tagButton.setButtonListener(new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            BaseFilterController<GameRepresentation, GameRepresentationModel> filterController = tablesSidebarController.getTableOverviewController().getFilterController();
            TableFilterController tableFilterController = (TableFilterController) filterController;
            tableFilterController.getTagField().toggleTag(tagsValue);
          }
        });
        tagsRoot.getChildren().add(tagButton);
      }

      category.setText(StringUtils.isEmpty(tableDetails.getCategory()) ? "-" : tableDetails.getCategory());
      author.setText(StringUtils.isEmpty(tableDetails.getAuthor()) ? "-" : tableDetails.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(tableDetails.getLaunchCustomVar()) ? "-" : tableDetails.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(tableDetails.getKeepDisplays()) ? "-" :
          VPinScreen.toString(VPinScreen.keepDisplaysToScreens(tableDetails.getKeepDisplays())));
      gameRating.setText(tableDetails.getGameRating() == null ? "-" : String.valueOf(tableDetails.getGameRating()));
      dof.setText(StringUtils.isEmpty(tableDetails.getDof()) ? "-" : tableDetails.getDof());
      IPDBNum.setText(StringUtils.isEmpty(tableDetails.getIPDBNum()) ? "-" : tableDetails.getIPDBNum());
      altRunMode.setText(StringUtils.isEmpty(tableDetails.getAltRunMode()) ? "-" : tableDetails.getAltRunMode());
      url.setText(StringUtils.isEmpty(tableDetails.getUrl()) ? "-" : tableDetails.getUrl());
      designedBy.setText(StringUtils.isEmpty(tableDetails.getDesignedBy()) ? "-" : tableDetails.getDesignedBy());
      notes.setText(StringUtils.isEmpty(tableDetails.getNotes()) ? "" : tableDetails.getNotes());
      custom2.setText(StringUtils.isEmpty(tableDetails.getCustom2()) ? "-" : tableDetails.getCustom2());
      custom3.setText(StringUtils.isEmpty(tableDetails.getCustom3()) ? "-" : tableDetails.getCustom3());
      volume.setText(StringUtils.isEmpty(tableDetails.getVolume()) ? "-" : tableDetails.getVolume());

      labelLastPlayed.setText(tableDetails.getLastPlayed() != null ? DateFormat.getDateInstance().format(tableDetails.getLastPlayed()) : "-");
      if (tableDetails.getNumberPlays() != null) {
        labelTimesPlayed.setText(String.valueOf(tableDetails.getNumberPlays()));
      }
      else {
        labelTimesPlayed.setText("0");
      }

      if (tableDetails.getStatus() >= 0) {
        List<TableStatus> statuses = TableDataController.supportedStatuses();
        Optional<TableStatus> first = statuses.stream().filter(status -> status.value == tableDetails.getStatus()).findFirst();
        if (first.isPresent()) {
          status.setText(first.get().label);
        }
      }

      //extras
      if (tableDetails.isPopper15()) {
        custom4.setText(StringUtils.isEmpty(tableDetails.getCustom4()) ? "-" : tableDetails.getCustom4());
        custom5.setText(StringUtils.isEmpty(tableDetails.getCustom5()) ? "-" : tableDetails.getCustom5());
        altRomName.setText(StringUtils.isEmpty(tableDetails.getRomAlt()) ? "-" : tableDetails.getRomAlt());
        webDbId.setText(StringUtils.isEmpty(tableDetails.getWebGameId()) ? "-" : tableDetails.getWebGameId());
        webLink.setText(StringUtils.isEmpty(tableDetails.getWebLink2Url()) ? "-" : tableDetails.getWebLink2Url());
        isMod.setText(String.valueOf(tableDetails.isMod()));
        gDetails.setText(StringUtils.isEmpty(tableDetails.getgDetails()) ? "" : tableDetails.getgDetails());
        gNotes.setText(StringUtils.isEmpty(tableDetails.getgNotes()) ? "" : tableDetails.getgNotes());
        gLog.setText(StringUtils.isEmpty(tableDetails.getgLog()) ? "" : tableDetails.getgLog());
        gPlayLog.setText(StringUtils.isEmpty(tableDetails.getgPlayLog()) ? "" : tableDetails.getgPlayLog());
      }
    }
    else {

      extrasPanel.setVisible(false);

      labelLastPlayed.setText("-");
      labelTimesPlayed.setText("-");

      isMod.setText("-");
      gameYear.setText("-");
      gameType.setText("-");
      gameTheme.setText("-");
      manufacturer.setText("-");
      numberOfPlayers.setText("-");
      tags.setCenter(new Label("-"));
      category.setText("-");
      author.setText("-");
      launchCustomVar.setText("-");
      keepDisplays.setText("-");
      gameRating.setText("-");
      dof.setText("-");
      IPDBNum.setText("-");
      altRunMode.setText("-");
      url.setText("-");
      designedBy.setText("-");
      gDetails.setText("");
      gLog.setText("");
      gPlayLog.setText("");
      gNotes.setText("");
      webDbId.setText("-");
      custom2.setText("-");
      custom3.setText("-");
      custom4.setText("-");
      custom5.setText("-");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableFilesBox.managedProperty().bindBidirectional(tableFilesBox.visibleProperty());

    autoFillBtn.managedProperty().bindBidirectional(autoFillBtn.visibleProperty());
    extrasPanel.managedProperty().bindBidirectional(extrasPanel.visibleProperty());
    popperRuntimeFields.managedProperty().bindBidirectional(popperRuntimeFields.visibleProperty());
    gameMetaDataFields.managedProperty().bindBidirectional(gameMetaDataFields.visibleProperty());

    popperRuntimeFields.setVisible(Features.FIELDS_EXTENDED);


    tags.setCenter(new Label("-"));

    try {
      FXMLLoader loader = new FXMLLoader(UploadsButtonController.class.getResource("uploads-btn.fxml"));
      Parent uploadsButton = loader.load();
      uploadsButtonController = loader.getController();
      uploadsButtonController.setCompact(false);
      toolbar.getItems().add(0, uploadsButton);
    }
    catch (IOException e) {
      LOG.error("failed to load uploads button: " + e.getMessage(), e);
    }
  }
}
package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.UploadsButtonController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

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
  private Label tags;

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

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private UploadsButtonController uploadsButtonController;

  // Add a public no-args constructor
  public TablesSidebarTableDetailsController() {
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
  private void onAutoFillAll() {
    List<GameRepresentation> vpxGamesCached = client.getGameService().getVpxGamesCached();
    TableDialogs.openAutoFillSettingsDialog(Studio.stage, vpxGamesCached, null);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.game = g;

    if (g.isEmpty()) {
      uploadsButtonController.setData(Collections.emptyList(), tablesSidebarController.getTableOverviewController().getEmulatorSelection());
    }
    else {
      uploadsButtonController.setData(Arrays.asList(g.get()), tablesSidebarController.getTableOverviewController().getEmulatorSelection());
    }


    FrontendType frontendType = client.getFrontendService().getFrontendType();

    if (!frontendType.supportStandardFields()) {
      tableDataBox.getChildren().remove(gameMetaDataFields);
    }
    if (!frontendType.isNotStandalone()) {
      tableDataBox.getChildren().remove(screenFields);
    }

    this.tableEditBtn.setDisable(g.isEmpty());
    this.fixVersionBtn.setDisable(g.isEmpty() || !g.get().isUpdateAvailable());
    this.autoFillBtn.setDisable(g.isEmpty());

    GameRepresentation game = g.orElse(null);

    if (game != null) {
      autoFillBtn.setVisible(game.isVpxGame() && frontendType.supportStandardFields());

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
      tags.setText(StringUtils.isEmpty(tableDetails.getTags()) ? "-" : tableDetails.getTags());
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
        List<TableStatus> statuses = TableDataController.supportedStatuses(frontendType);
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
      tags.setText("-");
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
    autoFillBtn.managedProperty().bindBidirectional(autoFillBtn.visibleProperty());
    extrasPanel.managedProperty().bindBidirectional(extrasPanel.visibleProperty());
    popperRuntimeFields.managedProperty().bindBidirectional(popperRuntimeFields.visibleProperty());
    gameMetaDataFields.managedProperty().bindBidirectional(gameMetaDataFields.visibleProperty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    popperRuntimeFields.setVisible(frontendType.supportExtendedFields());

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
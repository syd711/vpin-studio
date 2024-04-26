package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarPopperController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPopperController.class);

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

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private TableDetails tableDetails;

  // Add a public no-args constructor
  public TablesSidebarPopperController() {
  }

  @FXML
  private void onVersionFix() {
    if (game.isPresent()) {
      GameRepresentation gameRepresentation = game.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Auto-Fix Table Version?", "This overwrites the existing PinUP Popper table version \""
        + gameRepresentation.getVersion() + "\" with the VPS table version \"" +
        gameRepresentation.getExtVersion() + "\".", "The table update indicator won't be shown afterwards.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        TableDetails td = client.getPinUPPopperService().getTableDetails(gameRepresentation.getId());
        td.setGameVersion(gameRepresentation.getExtVersion());
        try {
          client.getPinUPPopperService().saveTableDetails(td, gameRepresentation.getId());
          EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
        } catch (Exception ex) {
          LOG.error("Error saving table manifest: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error saving table manifest: " + ex.getMessage());
        }
      }
    }
  }

  @FXML
  private void onTableEdit() {
    if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        TableDialogs.openTableDataDialog(this.tablesSidebarController.getTablesController(), this.game.get());
        this.refreshView(this.game);
      }
      return;
    }

    TableDialogs.openTableDataDialog(this.tablesSidebarController.getTablesController(), this.game.get());
    this.refreshView(this.game);
  }

  @FXML
  private void onAutoFill() {
    if (this.game.isPresent()) {
      TableDetails td = TableDialogs.openAutoFill(this.game.get());
      if (td != null) {
        EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
      }
    }
  }

  @FXML
  private void onAutoFillAll() {
    TableDialogs.openAutoFillAll();
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.tableDetails = null;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.tableEditBtn.setDisable(g.isEmpty());
    this.fixVersionBtn.setDisable(g.isEmpty() || !g.get().isUpdateAvailable());
    autoFillBtn.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      autoFillBtn.setVisible(game.isVpxGame());

      tableDetails = Studio.client.getPinUPPopperService().getTableDetails(game.getId());

      extrasPanel.setVisible(tableDetails.isPopper15());

      labelLastPlayed.setText(tableDetails.getLastPlayed() != null ? DateFormat.getDateInstance().format(tableDetails.getLastPlayed()) : "-");
      if (tableDetails.getNumberPlays() != null) {
        labelTimesPlayed.setText(String.valueOf(tableDetails.getNumberPlays()));
      }
      else {
        labelTimesPlayed.setText("0");
      }

      emulatorLabel.setText(client.getPinUPPopperService().getGameEmulator(tableDetails.getEmulatorId()).getName());
      gameType.setText(tableDetails.getGameType() != null ? tableDetails.getGameType().name() : "-");
      gameName.setText(StringUtils.isEmpty(tableDetails.getGameName()) ? "-" : tableDetails.getGameName());
      gameFileName.setText(StringUtils.isEmpty(tableDetails.getGameFileName()) ? "-" : tableDetails.getGameFileName());
      gameVersion.setText(StringUtils.isEmpty(tableDetails.getGameVersion()) ? "-" : tableDetails.getGameVersion());
      gameDisplayName.setText(StringUtils.isEmpty(tableDetails.getGameDisplayName()) ? "-" : tableDetails.getGameDisplayName());
      gameTheme.setText(StringUtils.isEmpty(tableDetails.getGameTheme()) ? "-" : tableDetails.getGameTheme());
      dateAdded.setText(tableDetails.getDateAdded() == null ? "-" : DateFormat.getDateTimeInstance().format(tableDetails.getDateAdded()));
      gameYear.setText(tableDetails.getGameYear() == null ? "-" : String.valueOf(tableDetails.getGameYear()));
      romName.setText(StringUtils.isEmpty(tableDetails.getRomName()) ? "-" : tableDetails.getRomName());
      manufacturer.setText(StringUtils.isEmpty(tableDetails.getManufacturer()) ? "-" : tableDetails.getManufacturer());
      numberOfPlayers.setText(tableDetails.getNumberOfPlayers() == null ? "-" : String.valueOf(tableDetails.getNumberOfPlayers()));
      altLaunch.setText(tableDetails.getAltLaunchExe() == null ? "-" : tableDetails.getAltLaunchExe());
      tags.setText(StringUtils.isEmpty(tableDetails.getTags()) ? "-" : tableDetails.getTags());
      category.setText(StringUtils.isEmpty(tableDetails.getCategory()) ? "-" : tableDetails.getCategory());
      author.setText(StringUtils.isEmpty(tableDetails.getAuthor()) ? "-" : tableDetails.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(tableDetails.getLaunchCustomVar()) ? "-" : tableDetails.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(tableDetails.getKeepDisplays()) ? "-" : tableDetails.getKeepDisplays());
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

      if (tableDetails.getStatus() > 0) {
        Optional<TableStatus> first = TableDataController.TABLE_STATUSES_15.stream().filter(status -> status.value == tableDetails.getStatus()).findFirst();
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
      labelLastPlayed.setText("-");
      labelTimesPlayed.setText("-");

      isMod.setText("-");
      status.setText("-");
      emulatorLabel.setText("-");
      dateAdded.setText("-");
      gameName.setText("-");
      gameFileName.setText("-");
      gameDisplayName.setText("-");
      gameYear.setText("-");
      gameType.setText("-");
      gameTheme.setText("-");
      romName.setText("-");
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
      notes.setText("");
      gDetails.setText("");
      gLog.setText("");
      gPlayLog.setText("");
      gNotes.setText("");
      webDbId.setText("-");
      custom2.setText("-");
      custom3.setText("-");
      custom4.setText("-");
      custom5.setText("-");
      gameVersion.setText("-");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    autoFillBtn.managedProperty().bindBidirectional(autoFillBtn.visibleProperty());
    extrasPanel.managedProperty().bindBidirectional(extrasPanel.visibleProperty());

    Image image4 = new Image(Studio.class.getResourceAsStream("popper-edit.png"));
    ImageView view4 = new ImageView(image4);
    view4.setFitWidth(18);
    view4.setFitHeight(18);
    tableEditBtn.setGraphic(view4);
  }
}
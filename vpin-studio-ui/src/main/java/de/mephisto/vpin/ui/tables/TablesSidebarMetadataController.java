package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarMetadataController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarMetadataController.class);

  @FXML
  private Label labelId;

  @FXML
  private Label labelRom;

  @FXML
  private Label labelRomAlias;

  @FXML
  private Label labelNVOffset;

  @FXML
  private Label labelFilename;

  @FXML
  private Label labelFilesize;

  @FXML
  private Label labelLastPlayed;

  @FXML
  private Label labelTimesPlayed;

  @FXML
  private Label labelHSFilename;

  @FXML
  private Label labelTableName;

  @FXML
  private Label labelLastModified;

  @FXML
  private Button editHsFileNameBtn;

  @FXML
  private Button editRomNameBtn;

  @FXML
  private Button romUploadBtn;

  @FXML
  private Button editTableNameBtn;

  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarMetadataController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  @FXML
  private void onHsFileNameEdit() {
    GameRepresentation gameRepresentation = game.get();
    String fs = WidgetFactory.showInputDialog(Studio.stage, "EM Highscore Filename", "Enter EM Highscore Filename",
        "Enter the name of the highscore file for this table.", "If available, the file is located in the 'VisualPinball\\User' folder.", gameRepresentation.getHsFileName());
    if (fs != null) {
      gameRepresentation.setHsFileName(fs);

      try {
        client.saveGame(gameRepresentation);
        this.tablesSidebarController.getTablesSidebarHighscoresController().refreshView(game, true);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      tablesSidebarController.getTablesController().onReload();
    }
  }

  @FXML
  public void onRomUpload() {
    boolean uploaded = Dialogs.openRomUploadDialog();
    if (uploaded) {
      tablesSidebarController.getTablesController().onReload();
    }
  }

  @FXML
  private void onTableNameEdit() {
    GameRepresentation gameRepresentation = game.get();
    String tableName = WidgetFactory.showInputDialog(Studio.stage, "Table Name", "Enter Table Name",
        "Enter the value for the 'TableName' property.",
        "The value is configured for some tables and used during highscore extraction.",
        gameRepresentation.getTableName());
    if (tableName != null) {
      gameRepresentation.setTableName(tableName);
      try {
        client.saveGame(gameRepresentation);
        tablesSidebarController.getTablesSidebarHighscoresController().refreshView(game, true);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      tablesSidebarController.getTablesController().onReload();
    }
  }

  @FXML
  private void onRomEdit() {
    GameRepresentation gameRepresentation = game.get();
    String romName = WidgetFactory.showInputDialog(Studio.stage, "ROM Name", "ROM Name", "The ROM name will be used for highscore and PUP pack resolving.", "Open the VPX table script editor to search for the ROM name.", gameRepresentation.getRom());
    if (romName != null) {
      gameRepresentation.setRom(romName);
      try {
        client.saveGame(gameRepresentation);
        this.tablesSidebarController.getTablesSidebarHighscoresController().refreshView(game, true);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      tablesSidebarController.getTablesController().onReload();
    }
  }

  public void refreshView(Optional<GameRepresentation> g) {
    editHsFileNameBtn.setDisable(g.isEmpty());
    editRomNameBtn.setDisable(g.isEmpty());
    editTableNameBtn.setDisable(g.isEmpty());
    romUploadBtn.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      editHsFileNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      editRomNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      editTableNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      romUploadBtn.setDisable(!game.getEmulator().isVisualPinball());

      labelId.setText(String.valueOf(game.getId()));
      labelRom.setText(game.getOriginalRom() != null ? game.getOriginalRom() : game.getRom());
      labelRomAlias.setText(game.getOriginalRom() != null ? game.getRom() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName() != null ? game.getGameFileName() : "-");
      labelFilesize.setText(FileUtils.readableFileSize(game.getGameFileSize()));
      labelTableName.setText(game.getTableName() != null ? game.getTableName() : "-");
      labelLastModified.setText(game.getModified() != null ? DateFormat.getDateTimeInstance().format(game.getModified()) : "-");
      labelLastPlayed.setText(game.getLastPlayed() != null ? DateFormat.getDateTimeInstance().format(game.getLastPlayed()) : "-");
      labelTimesPlayed.setText(String.valueOf(game.getNumberPlays()));
      if (!StringUtils.isEmpty(game.getHsFileName())) {
        labelHSFilename.setText(game.getHsFileName());
      }
      else {
        labelHSFilename.setText("-");
      }
    }
    else {
      labelId.setText("-");
      labelRom.setText("-");
      labelRomAlias.setText("-");
      labelNVOffset.setText("-");
      labelFilename.setText("-");
      labelLastModified.setText("-");
      labelLastPlayed.setText("-");
      labelTableName.setText("-");
      labelTimesPlayed.setText("-");
      labelHSFilename.setText("-");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

}
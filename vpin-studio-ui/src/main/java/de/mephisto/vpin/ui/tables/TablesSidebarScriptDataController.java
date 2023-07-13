package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.ScriptDownloadProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarScriptDataController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarScriptDataController.class);

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
  private Button inspectBtn;

  @FXML
  private Button editBtn;

  @FXML
  private Button editTableNameBtn;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarScriptDataController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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
        Studio.client.getGameService().saveGame(gameRepresentation);
        this.tablesSidebarController.getTablesSidebarHighscoresController().refreshView(game, true);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId());
    }
  }

  @FXML
  public void onEdit() {
    if(this.game.isPresent()) {
      tablesSidebarController.getTablesController().showEditor(this.game.get());
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
        Studio.client.getGameService().saveGame(gameRepresentation);
        tablesSidebarController.getTablesSidebarHighscoresController().refreshView(game, true);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId());
    }
  }

  @FXML
  private void onRomEdit() {
    GameRepresentation gameRepresentation = game.get();
    String romName = WidgetFactory.showInputDialog(Studio.stage, "ROM Name", "ROM Name", "The ROM name will be used for highscore and PUP pack resolving.", "Open the VPX table script editor to search for the ROM name.", gameRepresentation.getRom());
    if (romName != null) {
      gameRepresentation.setRom(romName);
      try {
        Studio.client.getGameService().saveGame(gameRepresentation);
        this.tablesSidebarController.getTablesSidebarHighscoresController().refreshView(game, true);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId());
    }
  }

  @FXML
  private void onInspect() {
    if (game.isPresent()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Inspect script of table\"" + game.get().getGameDisplayName() + "\"?",
          "This will extract the table script into a temporary file.",
          "It will be opened afterwards in a text editor.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {

        ProgressResultModel resultModel = Dialogs.createProgressDialog(new ScriptDownloadProgressModel("Extracting Table Script", game.get()));
        if (!resultModel.getResults().isEmpty()) {
          File file = (File) resultModel.getResults().get(0);
          try {
            Desktop.getDesktop().open(file);
          } catch (IOException e) {
            WidgetFactory.showAlert(Studio.stage, "Failed to open script file " + file.getAbsolutePath() + ": " + e.getMessage());
          }
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "Script extraction failed, check log for details.");
        }
      }
    }
  }

  public void refreshView(Optional<GameRepresentation> g) {
    editHsFileNameBtn.setDisable(g.isEmpty());
    editRomNameBtn.setDisable(g.isEmpty());
    editTableNameBtn.setDisable(g.isEmpty());
    romUploadBtn.setDisable(g.isEmpty());
    inspectBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());
    editBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      editHsFileNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      editRomNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      editTableNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      romUploadBtn.setDisable(!game.getEmulator().isVisualPinball());

      labelRom.setText(game.getOriginalRom() != null ? game.getOriginalRom() : game.getRom());
      labelRomAlias.setText(game.getOriginalRom() != null ? game.getRom() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName() != null ? game.getGameFileName() : "-");
      labelFilesize.setText(game.getGameFileSize() > 0 ? FileUtils.readableFileSize(game.getGameFileSize()) : "-");
      labelTableName.setText(game.getTableName() != null ? game.getTableName() : "-");
      labelLastModified.setText(game.getModified() != null ? DateFormat.getDateTimeInstance().format(game.getModified()) : "-");
      if (!StringUtils.isEmpty(game.getHsFileName())) {
        labelHSFilename.setText(game.getHsFileName());
      }
      else {
        labelHSFilename.setText("-");
      }
    }
    else {
      labelRom.setText("-");
      labelRomAlias.setText("-");
      labelNVOffset.setText("-");
      labelFilename.setText("-");
      labelFilesize.setText("-");
      labelLastModified.setText("-");
      labelTableName.setText("-");
      labelHSFilename.setText("-");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

}
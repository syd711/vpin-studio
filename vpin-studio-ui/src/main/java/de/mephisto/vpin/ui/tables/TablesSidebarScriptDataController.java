package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.ScriptDownloadProgressModel;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

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
  private Button scanBtn;

  @FXML
  private Button openEMHighscoreBtn;

  @FXML
  private Button editAliasBtn;

  @FXML
  private Button deleteAliasBtn;

  @FXML
  private Button editTableNameBtn;

  @FXML
  private Button vpSaveEditBtn;

  @FXML
  private Button openTablesFolderBtn;

  @FXML
  private Button openTableDescriptionBtn;

  @FXML
  private Button openTableRulesBtn;

  @FXML
  private Label tableNameLabel;

  @FXML
  private Label authorWebsiteLabel;

  @FXML
  private Label authorNameLabel;

  @FXML
  private TextArea tableBlurbLabel;

  @FXML
  private TextArea tableRulesLabel;

  @FXML
  private TextArea tableDescriptionLabel;

  @FXML
  private Label tableVersionLabel;

  @FXML
  private Label authorEmailLabel;

  @FXML
  private Label releaseDateLabel;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  private ValidationState validationState;
  private SystemSummary systemSummary;
  private TableInfo tableInfo;

  // Add a public no-args constructor
  public TablesSidebarScriptDataController() {
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  @FXML
  private void onShowTableRules() {
    if(tableInfo == null || game.isEmpty()) {
      return;
    }

    String value = tableInfo.getTableRules();
    if (!StringUtils.isEmpty(value)) {
      try {
        File tmp = File.createTempFile(game.get().getGameDisplayName() + "-rules", ".txt");
        tmp.deleteOnExit();

        Path path = Paths.get(tmp.toURI());
        Files.write(path, value.getBytes());

        Desktop.getDesktop().open(tmp);
      } catch (IOException e) {
        LOG.error("Failed to create temp file for rules: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onShowTableDescription() {
    if(tableInfo == null || game.isEmpty()) {
      return;
    }

    String value = tableInfo.getTableDescription();
    if (!StringUtils.isEmpty(value)) {
      try {
        File tmp = File.createTempFile(game.get().getGameDisplayName() + "-description", ".txt");
        tmp.deleteOnExit();

        Path path = Paths.get(tmp.toURI());
        Files.write(path, value.getBytes());

        Desktop.getDesktop().open(tmp);
      } catch (IOException e) {
        LOG.error("Failed to create temp file for rules: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onTablesFolderOpen() {
    if(this.game.isPresent()) {
      try {
        GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(game.get().getEmulatorId());
        new ProcessBuilder("explorer.exe", new File(emulatorRepresentation.getTablesDirectory()).getAbsolutePath()).start();
      } catch (Exception e) {
        LOG.error("Failed to open Explorer: " + e.getMessage(), e);
      }
    }
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
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
    }
  }

  @FXML
  public void onAliasEdit() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      String rom = g.getRom();
      String alias = g.getRomAlias();
      Dialogs.openAliasMappingDialog(g, alias, rom);
    }
  }


  @FXML
  public void onDeleteAlias() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(g.getEmulatorId());
      String alias = g.getRomAlias();

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Alias", "Delete alias \"" + alias + "\" for ROM \"" + g.getRom() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Studio.client.getRomService().deleteAliasMapping(emulatorRepresentation.getId(), alias);
        EventManager.getInstance().notifyTableChange(g.getId(), g.getRom());
      }
    }
  }


  @FXML
  public void onEdit() {
    this.game.ifPresent(gameRepresentation -> tablesSidebarController.getTablesController().showScriptEditor(gameRepresentation));
  }

  @FXML
  public void onScan() {
    if (this.game.isPresent()) {
      Dialogs.createProgressDialog(new TableScanProgressModel("Scanning Table \"" + this.game.get().getGameDisplayName() + "\"", List.of(this.game.get())));
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    }
  }

  @FXML
  public void onVPSaveEdit() {
    try {
      SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
      ProcessBuilder builder = new ProcessBuilder(new File("resources", "VPSaveEdit.exe").getAbsolutePath());
      builder.directory(new File("resources"));
      builder.start();
    } catch (IOException e) {
      LOG.error("Failed to open VPSaveEdit: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open VPSaveEdit: " + e.getMessage());
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
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
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
        LOG.info("Error saving updated ROM name: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error saving updated ROM name: " + e.getMessage());
      }
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
    }
  }

  @FXML
  private void onEMHighscore() {
    if(this.game.isPresent()) {
      GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(this.game.get().getEmulatorId());

      File folder = new File(emulatorRepresentation.getUserDirectory());
      File file = new File(folder, game.get().getHsFileName());
      if (file.exists()) {
        try {
          Desktop.getDesktop().open(file);
        } catch (IOException e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open EM highscore file \"" + game.get().getHsFileName() + "\": " + e.getMessage());
        }
      }
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

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    tablesSidebarController.getTablesController().dismissValidation(g, this.validationState);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    tableInfo = null;
    errorBox.setVisible(false);

    editHsFileNameBtn.setDisable(g.isEmpty());
    editRomNameBtn.setDisable(g.isEmpty());
    editTableNameBtn.setDisable(g.isEmpty());
    romUploadBtn.setDisable(g.isEmpty());
    openTablesFolderBtn.setVisible(Studio.client.getSystemService().isLocal());
    openEMHighscoreBtn.setVisible(Studio.client.getSystemService().isLocal());
    openEMHighscoreBtn.setDisable(true);

    inspectBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());
    editBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());
    scanBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());
    editAliasBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());
    deleteAliasBtn.setDisable(g.isEmpty() || !g.get().isGameFileAvailable());

    tableNameLabel.setText("-");
    authorWebsiteLabel.setText("-");
    authorNameLabel.setText("-");
    tableBlurbLabel.setText("-");
    tableRulesLabel.setText("-");
    tableVersionLabel.setText("-");
    authorEmailLabel.setText("-");
    releaseDateLabel.setText("-");
    tableDescriptionLabel.setText("-");

    openTableRulesBtn.setDisable(true);
    openTableDescriptionBtn.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      tableInfo = Studio.client.getVpxService().getTableInfo(game);
      if (tableInfo != null) {
        tableNameLabel.setText(StringUtils.isEmpty(tableInfo.getTableName()) ? "-" : tableInfo.getTableName());
        authorWebsiteLabel.setText(StringUtils.isEmpty(tableInfo.getAuthorWebSite()) ? "-" : tableInfo.getAuthorWebSite());
        authorNameLabel.setText(StringUtils.isEmpty(tableInfo.getAuthorName()) ? "-" : tableInfo.getAuthorName());
        tableVersionLabel.setText(StringUtils.isEmpty(tableInfo.getTableVersion()) ? "-" : tableInfo.getTableVersion());
        authorEmailLabel.setText(StringUtils.isEmpty(tableInfo.getAuthorEmail()) ? "-" : tableInfo.getAuthorEmail());
        releaseDateLabel.setText(StringUtils.isEmpty(tableInfo.getReleaseDate()) ? "-" : tableInfo.getReleaseDate());
        tableBlurbLabel.setText(tableInfo.getTableBlurb());
        tableRulesLabel.setText(tableInfo.getTableRules());
        tableDescriptionLabel.setText(tableInfo.getTableDescription());

        openTableRulesBtn.setDisable(StringUtils.isEmpty(tableInfo.getTableRules()));
        openTableDescriptionBtn.setDisable(StringUtils.isEmpty(tableInfo.getTableDescription()));
      }

      deleteAliasBtn.setDisable(StringUtils.isEmpty(game.getRomAlias()));

      if (Studio.client.getSystemService().isLocal()) {
        if (!StringUtils.isEmpty(game.getHsFileName())) {
          GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(game.getEmulatorId());
          File folder = new File(emulatorRepresentation.getUserDirectory());
          File file = new File(folder, game.getHsFileName());
          openEMHighscoreBtn.setDisable(!file.exists());
        }
      }

      labelRom.setText(!StringUtils.isEmpty(game.getRom()) ? game.getRom() : "-");
      labelRomAlias.setText(!StringUtils.isEmpty(game.getRomAlias()) ? game.getRomAlias() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName() != null ? game.getGameFileName() : "-");
      labelFilesize.setText(game.getGameFileSize() > 0 ? FileUtils.readableFileSize(game.getGameFileSize()) : "-");
      labelTableName.setText(!StringUtils.isEmpty(game.getTableName()) ? game.getTableName() : "-");
      labelLastModified.setText(game.getModified() != null ? DateFormat.getDateTimeInstance().format(game.getModified()) : "-");
      if (!StringUtils.isEmpty(game.getHsFileName())) {
        labelHSFilename.setText(game.getHsFileName());
      }
      else {
        labelHSFilename.setText("-");
      }

      List<ValidationState> validationStates = Studio.client.getGameService().getRomValidations(game.getId());
      errorBox.setVisible(!validationStates.isEmpty());
      if (!validationStates.isEmpty()) {
        validationState = validationStates.get(0);
        LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);
        errorTitle.setText(validationResult.getLabel());
        errorText.setText(validationResult.getText());
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    systemSummary = Studio.client.getSystemService().getSystemSummary();
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    vpSaveEditBtn.setDisable(!Studio.client.getSystemService().isLocal());
  }

}
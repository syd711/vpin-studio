package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.ScriptDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vbsedit.VBSManager;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarScriptDataController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarScriptDataController.class);

  @FXML
  private Label labelNVOffset;

  @FXML
  private Label labelFilename;

  @FXML
  private Label labelFilesize;

  @FXML
  private Label labelLastModified;

  @FXML
  private Button inspectBtn;

  @FXML
  private SplitMenuButton editBtn;

  @FXML
  private MenuItem editInternalBtn;

  @FXML
  private SplitMenuButton scanBtn;

  @FXML
  private Button vpSaveEditBtn;

  @FXML
  private Button openTablesFolderBtn;

  @FXML
  private Button openTableDescriptionBtn;

  @FXML
  private Button openTableRulesBtn;

  @FXML
  private Button viewScreenshotBtn;

  @FXML
  private Button screenshotBtn;

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
  private ImageView screenshotView;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  private TableInfo tableInfo;

  // Add a public no-args constructor
  public TablesSidebarScriptDataController() {
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  @FXML
  private void onScreenshotView() {
    if (this.game.isPresent()) {
      TableDialogs.openMediaDialog(Studio.stage, "Table Screenshot", Studio.client.getRestClient().getBaseUrl() + VPinStudioClientService.API + "vpx/screenshot/" + game.get().getId());
    }
  }


  @FXML
  private void onScreenshot() {
    if (this.game.isPresent()) {
      this.loadScreenshot(this.game.get(), true);
    }
  }

  @FXML
  private void onShowTableRules() {
    if (tableInfo == null || game.isEmpty()) {
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
    if (tableInfo == null || game.isEmpty()) {
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
    if (this.game.isPresent()) {
      try {
        GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(game.get().getEmulatorId());
        // support of table sub folders
        File tableFile = new File(emulatorRepresentation.getGamesDirectory(), game.get().getGameFileName());
        SystemUtil.openFolder(tableFile.getParentFile());
      } catch (Exception e) {
        LOG.error("Failed to open Explorer: " + e.getMessage(), e);
      }
    }
  }


  @FXML
  public void onEdit() {
    VBSManager.getInstance().edit(this.game);
  }

  @FXML
  public void onEditInternal() {
    VBSManager.getInstance().edit(this.game, true);
  }

  @FXML
  public void onScanAll() {
    boolean scanned = TableDialogs.openScanAllDialog(client.getGameService().getVpxGamesCached());
    if (scanned) {
      EventManager.getInstance().notifyTablesChanged();
    }
  }

  @FXML
  public void onScan() {
    if (this.game.isPresent()) {
      ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning Table \"" + this.game.get().getGameDisplayName() + "\"", List.of(this.game.get())));
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    }
  }

  @FXML
  public void onVPSaveEdit() {
    try {
      ProcessBuilder builder = new ProcessBuilder(new File("resources", "VPSaveEdit.exe").getAbsolutePath());
      builder.directory(new File("resources"));
      builder.start();
    } catch (IOException e) {
      LOG.error("Failed to open VPSaveEdit: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open VPSaveEdit: " + e.getMessage());
    }
  }


  @FXML
  private void onInspect() {
    if (game.isPresent()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Inspect script of table\"" + game.get().getGameDisplayName() + "\"?",
        "This will extract the table script into a temporary file.",
        "It will be opened afterwards in a text editor.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {

        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new ScriptDownloadProgressModel("Extracting Table Script", game.get()));
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
    tableInfo = null;
    openTablesFolderBtn.setVisible(Studio.client.getSystemService().isLocal());

    boolean gameFileAvailable = g.isPresent() && g.get().getGameFilePath() != null;
    inspectBtn.setDisable(g.isEmpty() || !gameFileAvailable);
    editBtn.setDisable(g.isEmpty() || !gameFileAvailable);
    scanBtn.setDisable(g.isEmpty() || !gameFileAvailable);
    viewScreenshotBtn.setDisable(g.isEmpty());
    screenshotBtn.setDisable(g.isEmpty());
    screenshotView.setImage(null);

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

      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName() != null ? game.getGameFileName() : "-");
      labelFilesize.setText(game.getGameFileSize() > 0 ? FileUtils.readableFileSize(game.getGameFileSize()) : "-");
      labelLastModified.setText(game.getModified() != null ? DateFormat.getDateTimeInstance().format(game.getModified()) : "-");

      loadScreenshot(game, false);
    }
    else {
      labelNVOffset.setText("-");
      labelFilename.setText("-");
      labelFilesize.setText("-");
      labelLastModified.setText("-");
    }
  }

  private void loadScreenshot(GameRepresentation game, boolean reload) {
    Platform.runLater(() -> {
      String url = client.getURL("vpx/screenshot/" + game.getId());
      if (reload) {
        client.getImageCache().clear(url);
      }
      InputStream cachedUrlImage = client.getCachedUrlImage(url);
      Image image = new Image(cachedUrlImage);
      screenshotView.setImage(image);
      viewScreenshotBtn.setDisable(false);
    });
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpSaveEditBtn.setDisable(!Studio.client.getSystemService().isLocal());
  }

}
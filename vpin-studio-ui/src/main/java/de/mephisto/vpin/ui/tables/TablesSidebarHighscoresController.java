package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.ScoreGraphUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.*;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.mania.VPinManiaScoreSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import de.mephisto.vpin.ui.tables.dialogs.HighscoreBackupProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import eu.hansolo.tilesfx.Tile;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontText;
import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarHighscoresController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarHighscoresController.class);

  @FXML
  private Label hsTypeLabel;

  @FXML
  private Label hsFileLabel;

  @FXML
  private Label hsLastModifiedLabel;

  @FXML
  private Label hsLastScannedLabel;

  @FXML
  private Label hsRecordLabel;

  @FXML
  private VBox formattedScoreWrapper;

  @FXML
  private VBox rawScoreWrapper;

  @FXML
  private VBox scoreGraphWrapper;

  @FXML
  private Button resetBtn;

  @FXML
  private Button maniaBtn;

  @FXML
  private Button maniaSyncBtn;

  @FXML
  private SplitMenuButton scanHighscoreBtn;

  @FXML
  private Button cardBtn;

  @FXML
  private SplitMenuButton backupBtn;

  @FXML
  private Button restoreBtn;

  @FXML
  private Label rawScoreLabel;

  @FXML
  private Label formattedScoreLabel;

  @FXML
  private Label rawTitleLabel;

  @FXML
  private Label formattedTitleLabel;

  @FXML
  private Label backupCountLabel;

  @FXML
  private BorderPane scoreGraph;

  @FXML
  private Label statusLabel;

  @FXML
  private VBox statusPane;

  @FXML
  private VBox dataPane;

  @FXML
  private Button vpSaveEditBtn;

  @FXML
  private ImageView cardImage;

  @FXML
  private CheckBox cardsEnabledCheckbox;

  @FXML
  private VBox multiSelectionPane;


  private Optional<GameRepresentation> game = Optional.empty();
  private List<GameRepresentation> games = new ArrayList<>();

  private TablesSidebarController tablesSidebarController;
  private List<HighscoreBackup> highscoreBackups;

  // Add a public no-args constructor
  public TablesSidebarHighscoresController() {
  }


  @FXML
  private void onManiaTable() {
    if (this.game.isPresent() && !StringUtils.isEmpty(this.game.get().getExtTableId())) {
      Studio.browse(ManiaUrlFactory.createTableUrl(this.game.get().getExtTableId(), this.game.get().getExtTableVersionId()));
    }
  }

  @FXML
  private void onManiaTableSync() {
    if (!this.games.isEmpty()) {
      List<VpsTable> tables = new ArrayList<>();
      for (GameRepresentation gameRepresentation : this.games) {
        VpsTable vpsTable = client.getVpsService().getTableById(gameRepresentation.getExtTableId());
        if (vpsTable != null) {
          tables.add(vpsTable);
        }
      }
      if (!tables.isEmpty()) {
        ProgressDialog.createProgressDialog(new VPinManiaScoreSynchronizeProgressModel(tables));
      }
    }
  }

  @FXML
  public void onVPSaveEdit() {
    try {
      ProcessBuilder builder = new ProcessBuilder(new File("resources", "VPSaveEdit.exe").getAbsolutePath());
      builder.directory(new File("resources"));
      builder.start();
    }
    catch (IOException e) {
      LOG.error("Failed to open VPSaveEdit: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open VPSaveEdit: " + e.getMessage());
    }
  }

  @FXML
  private void onCard() {
    if (this.game.isPresent()) {
      NavigationController.navigateTo(NavigationItem.HighscoreCards, new NavigationOptions(this.game.get().getId()));
    }
  }

  @FXML
  private void onScan() {
    this.refreshView(game);
    if (game.isPresent()) {
      client.getGameService().reload(this.game.get().getId());
    }
  }

  @FXML
  private void onScanAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Scan for highscores updates of all " + client.getGameService().getVpxGamesCached().size() + " tables?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new TableHighscoresScanProgressModel(client.getGameService().getVpxGamesCached()));
      EventManager.getInstance().notifyTablesChanged();
    }
  }

  @FXML
  private void onBackAll() {
    List<GameRepresentationModel> items = new ArrayList<>(tablesSidebarController.getTableOverviewController().getTableView().getItems());
    List<GameRepresentation> allGames = items.stream().map(g -> g.getGame()).collect(Collectors.toList());
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Create highscore backup for all currently selected " + allGames.size() + " tables?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new HighscoreBackupProgressModel(allGames));
    }
  }

  @FXML
  private void onBackup() {
    if (this.games.size() == 1) {
      GameRepresentation g = this.game.get();
      String last = null;
      if (highscoreBackups != null && !this.highscoreBackups.isEmpty()) {
        last = "The last backup was created at " + this.highscoreBackups.get(0);
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Create highscore backup for table \"" + g.getGameDisplayName() + "\"?", last);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          Studio.client.getHigscoreBackupService().backup(g.getId());
        }
        catch (Exception e) {
          LOG.error("Failed to back highscore: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed create highscore backup: " + e.getMessage());
        }
        EventManager.getInstance().notifyTableChange(g.getId(), g.getRom());
      }
    }
    else if (this.games.size() > 1) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Create highscore backup for " + games.size() + " tables?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        ProgressDialog.createProgressDialog(new HighscoreBackupProgressModel(this.games));
      }
    }
  }

  @FXML
  private void onRestore() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      if (StringUtils.isEmpty(g.getRom()) && StringUtils.isEmpty(g.getTableName())) {
        WidgetFactory.showAlert(Studio.stage, "ROM name is missing.",
            "To backup the the highscore of a table, the ROM name or tablename must have been resolved.",
            "You can enter the values for this manually in the \"Script Details\" section.");
      }
      else {
        TableDialogs.openHighscoresAdminDialog(tablesSidebarController, this.game.get());
      }
    }
  }

  @FXML
  private void onScoreReset() {
    if (!this.games.isEmpty()) {
      TableDialogs.openHighscoresResetDialog(this.games);
    }
  }

  public void setGames(List<GameRepresentation> games) {
    this.highscoreBackups = new ArrayList<>();
    this.game = Optional.empty();
    this.games = games;

    if (!games.isEmpty()) {
      this.game = Optional.of(games.get(0));
    }

    Platform.runLater(() -> {
      this.refreshView(game);
    });
  }

  public void refreshView(Optional<GameRepresentation> g) {
    HighscoreMetadataRepresentation metadata = null;
    if (g.isPresent()) {
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new TableHighscoresScanProgressModel(Arrays.asList(g.get())));
      if (!progressDialog.getResults().isEmpty()) {
        metadata = (HighscoreMetadataRepresentation) progressDialog.getResults().get(0);
      }
    }

    rawScoreLabel.setText("");
    formattedScoreLabel.setText("");

    this.hsFileLabel.setText("-");
    this.hsTypeLabel.setText("-");
    this.hsLastModifiedLabel.setText("-");
    this.hsLastScannedLabel.setText("-");
    this.hsRecordLabel.setText("-");
    this.backupCountLabel.setText("-");

    rawTitleLabel.setVisible(false);
    formattedTitleLabel.setVisible(false);

    rawScoreWrapper.setVisible(false);
    formattedScoreWrapper.setVisible(false);
    scoreGraphWrapper.setVisible(false);

    scanHighscoreBtn.setDisable(true);
    cardBtn.setDisable(true);
    resetBtn.setDisable(games.size() != 1);

    backupBtn.setDisable(games.isEmpty());
    restoreBtn.setDisable(games.size() != 1);
    restoreBtn.setText("Restore");

    cardsEnabledCheckbox.setDisable(true);

    this.multiSelectionPane.setVisible(games.size() > 1);
    this.statusPane.setVisible(games.size() == 1);
    this.dataPane.setVisible(games.size() == 1);

    maniaBtn.setDisable(game.isEmpty() || StringUtils.isEmpty(game.get().getExtTableId()));

    if (g.isPresent() && this.games.size() == 1) {
      GameRepresentation game = g.get();
      scanHighscoreBtn.setDisable(false);
      restoreBtn.setDisable(false);

      cardsEnabledCheckbox.setDisable(false);
      cardsEnabledCheckbox.setSelected(!game.isCardDisabled());
      List<CardTemplate> templates = client.getHighscoreCardTemplatesClient().getTemplates();
      Long templateId = g.get().getTemplateId();
      Optional<CardTemplate> first = templates.stream().filter(t -> t.getId().equals(templateId)).findFirst();
      if (first.isEmpty()) {
        first = templates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst();
      }
      InputStream highscoreCard = client.getHighscoreCardsService().getHighscoreCardPreview(game, first.get());

      if (highscoreCard != null) {
        cardImage.setImage(new Image(highscoreCard));
      }
      else {
        cardImage.setImage(new Image(ResourceLoader.class.getResourceAsStream("empty-preview.png")));
      }

      String rom = game.getRom();
      if (StringUtils.isEmpty(rom)) {
        rom = game.getTableName();
      }

      if (StringUtils.isEmpty(rom)) {
        backupCountLabel.setText("0");
      }
      else {
        highscoreBackups = Studio.client.getHigscoreBackupService().get(rom);
        backupCountLabel.setText(String.valueOf(highscoreBackups.size()));
        if (!highscoreBackups.isEmpty()) {
          restoreBtn.setText("Restore (" + highscoreBackups.size() + ")");
        }
      }

      boolean hasHighscore = metadata != null && metadata.getStatus() == null && !StringUtils.isEmpty(metadata.getRaw());
      dataPane.setVisible(hasHighscore);
      statusPane.setVisible(!hasHighscore);

      statusLabel.setText("Unknown status.");
      if (metadata != null) {
        if (!hasHighscore) {
          if (!StringUtils.isEmpty(metadata.getStatus())) {
            statusLabel.setText(metadata.getStatus());
          }
          else {
            statusLabel.setText("Unknown status.");
          }
        }

        backupBtn.setDisable(metadata.getType() == null);
        restoreBtn.setDisable(metadata.getType() == null && (highscoreBackups == null || highscoreBackups.isEmpty()));

        if (metadata.getFilename() != null) {
          this.hsFileLabel.setText(metadata.getFilename());
        }

        if (metadata.getType() != null) {
          this.hsTypeLabel.setText(metadata.getType());
        }

        if (metadata.getModified() != null) {
          this.hsLastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(metadata.getModified()));
        }

        if (metadata.getScanned() != null) {
          this.hsLastScannedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(metadata.getScanned()));
        }

        if (!StringUtils.isEmpty(metadata.getRaw())) {
          rawTitleLabel.setVisible(true);
          rawScoreWrapper.setVisible(true);
          rawScoreLabel.setFont(getScoreFontText());
          String raw = ScoreFormatUtil.formatRaw(metadata.getRaw());
          rawScoreLabel.setText(raw);
        }

        ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
        if (!summary.getScores().isEmpty()) {
          cardBtn.setDisable(false);
          resetBtn.setDisable(StringUtils.isEmpty(rom));

          scoreGraphWrapper.setVisible(true);

          List<ScoreRepresentation> scores = summary.getScores();
          StringBuilder builder = new StringBuilder();
          for (ScoreRepresentation score : scores) {
            builder.append("#");
            builder.append(score.getPosition());
            builder.append(" ");
            builder.append(score.getPlayerInitials());
            builder.append("   ");
            builder.append(score.getFormattedScore());
            builder.append("\n");
          }

          formattedTitleLabel.setVisible(true);
          formattedScoreWrapper.setVisible(true);

          formattedScoreLabel.setFont(getScoreFontText());
          formattedScoreLabel.setText(builder.toString());
        }
      }

      ScoreListRepresentation scoreHistory = Studio.client.getGameService().getScoreHistory(game.getId());
      hsRecordLabel.setText(String.valueOf(scoreHistory.getScores().size()));
      if (!scoreHistory.getScores().isEmpty()) {
        try {
          Tile highscoresGraphTile = ScoreGraphUtil.createGraph(scoreHistory);
          scoreGraph.setCenter(highscoresGraphTile);
        }
        catch (Exception e) {
          //ignore
        }
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataPane.managedProperty().bindBidirectional(dataPane.visibleProperty());
    statusPane.managedProperty().bindBidirectional(statusPane.visibleProperty());
    vpSaveEditBtn.setVisible(client.getSystemService().isLocal());
    maniaBtn.managedProperty().bindBidirectional(maniaBtn.visibleProperty());
    maniaBtn.setVisible(Features.MANIA_ENABLED);
    maniaSyncBtn.managedProperty().bindBidirectional(maniaSyncBtn.visibleProperty());
    maniaSyncBtn.setVisible(Features.MANIA_ENABLED);

    Image imageMania = new Image(Studio.class.getResourceAsStream("mania.png"));
    ImageView iconMania = new ImageView(imageMania);
    iconMania.setFitWidth(18);
    iconMania.setFitHeight(18);
    maniaBtn.setGraphic(iconMania);

    cardsEnabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (game.isPresent()) {
          try {
            game.get().setCardDisabled(!newValue);
            client.getGameService().saveGame(game.get());
          }
          catch (Exception e) {
            LOG.error("Failed to save game: " + e.getMessage());
          }
        }
      }
    });
  }
}
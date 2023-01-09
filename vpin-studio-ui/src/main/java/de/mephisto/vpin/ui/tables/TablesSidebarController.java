package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.POVExportProgressModel;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class TablesSidebarController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarController.class);

  @FXML
  private BorderPane screenTopper;

  @FXML
  private BorderPane screenBackGlass;

  @FXML
  private BorderPane screenDMD;

  @FXML
  private BorderPane screenPlayField;

  @FXML
  private BorderPane screenMenu;

  @FXML
  private BorderPane screenOther2;

  @FXML
  private BorderPane screenWheel;

  @FXML
  private BorderPane screenGameInfo;

  @FXML
  private BorderPane screenGameHelp;

  @FXML
  private BorderPane screenLoading;

  @FXML
  private BorderPane screenAudio;

  @FXML
  private BorderPane screenAudioLaunch;

  @FXML
  private Button playfieldViewBtn;

  @FXML
  private Accordion accordion;

  @FXML
  private TitledPane titledPaneMedia;

  @FXML
  private Pane mediaRootPane;

  @FXML
  private Pane povSettingsPane;

  @FXML
  private VBox povCreatePane;

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
  private Slider volumeSlider;

  @FXML
  private Label rawScoreLabel;

  @FXML
  private Label formattedScoreLabel;

  @FXML
  private Label formattedTitleLabel;

  @FXML
  private Label rawTitleLabel;

  @FXML
  private ImageView rawDirectB2SImage;

  @FXML
  private Button openDirectB2SImageButton;

  @FXML
  private Button editHsFileNameBtn;

  @FXML
  private Button editRomNameBtn;

  @FXML
  private Button editTableNameBtn;

  @FXML
  private Button directb2sUploadBtn;

  @FXML
  private Button scanBtn;

  @FXML
  private Button romUploadBtn;

  @FXML
  private Label resolutionLabel;

  @FXML
  private CheckBox mediaPreviewCheckbox;

  @FXML
  private Label hsTypeLabel;

  @FXML
  private Label hsFileLabel;

  @FXML
  private Label hsLastModifiedLabel;

  @FXML
  private Label hsStatusLabel;

  @FXML
  private Label hsLastScannedLabel;

  @FXML
  private VBox formattedScoreWrapper;

  @FXML
  private VBox rawScoreWrapper;

  @FXML
  private VBox assetList;

  @FXML
  private ComboBox<POVComboModel> povSSAACombo;

  @FXML
  private ComboBox<POVPostProcComboModel> povPostprocAACombo;

  @FXML
  private ComboBox<POVComboModel> povIngameAOCombo;

  @FXML
  private ComboBox<POVComboModel> povScSpReflectCombo;

  @FXML
  private ComboBox<POVComboModel> povFpsLimiterCombo;

  @FXML
  private CheckBox povOverwriteDetailCheckbox;

  @FXML
  private Slider povDetailsSlider;

  @FXML
  private ComboBox<POVComboModel> povBallReflectionCombobox;

  @FXML
  private ComboBox<POVComboModel> povBallTrailCombobox;

  @FXML
  private Spinner<Integer> povBallTrailStrengthSpinner;

  @FXML
  private CheckBox povOverwriteNightDayCheckbox;

  @FXML
  private Spinner<Integer> povNighDaySpinner;

  @FXML
  private Spinner<Double> povGameDifficultySpinner;

  @FXML
  private Slider povSoundVolumeSlider;

  @FXML
  private Slider povMusicVolumeSlider;

  @FXML
  private Spinner<Integer>  povRotationFullscreenSpinner;

  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesController tablesController;
  private POVRepresentation pov;

  // Add a public no-args constructor
  public TablesSidebarController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
    this.accordion.setExpandedPane(titledPaneMedia);
    povCreatePane.managedProperty().bind(povCreatePane.visibleProperty());

    volumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
      if (game.isPresent()) {
        debouncer.debounce("tableVolume" + game.get().getId(), () -> {
          int value = t1.intValue();
          if (value == 0) {
            value = 1;
          }
          game.get().setVolume(value);

          try {
            client.saveGame(game.get());
          } catch (Exception e) {
            WidgetFactory.showAlert(Studio.stage, e.getMessage());
          }
        }, 1000);
      }
    });

    titledPaneMedia.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
      else {
        resetMedia();
      }
    });

    mediaPreviewCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshView(game));


    povSSAACombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povSSAACombo.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.SSAA, newValue.getValue()));

    povPostprocAACombo.setItems(FXCollections.observableList(POVPostProcComboModel.MODELS));
    povPostprocAACombo.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.POST_PROC_AA, newValue.getValue()));

    povIngameAOCombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povIngameAOCombo.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.INGAME_AO, newValue.getValue()));

    povScSpReflectCombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povScSpReflectCombo.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.SCSP_REFLECT, newValue.getValue()));

    povFpsLimiterCombo.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povFpsLimiterCombo.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.FPS_LIMITER, newValue.getValue()));

    povOverwriteDetailCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      int result = 0;
      if (newValue) {
        result = 1;
      }
      client.setPOVPreference(game.get().getId(), getPOV(), POV.OVERWRITE_DETAILS_LEVEL, result);
      povDetailsSlider.setDisable(!newValue);
    });

    povDetailsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.DETAILS_LEVEL, () -> {
        int value1 = ((Double) newValue).intValue();
        client.setPOVPreference(game.get().getId(), getPOV(), POV.DETAILS_LEVEL, value1);
      }, 500);
    });

    povBallReflectionCombobox.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povBallReflectionCombobox.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.BALL_REFLECTION, newValue.getValue()));

    povBallTrailCombobox.setItems(FXCollections.observableList(POVComboModel.MODELS));
    povBallTrailCombobox.valueProperty().addListener((observable, oldValue, newValue) -> client.setPOVPreference(game.get().getId(), getPOV(), POV.BALL_TRAIL, newValue.getValue()));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    povBallTrailStrengthSpinner.setValueFactory(factory);
    povBallTrailStrengthSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.BALL_TRAIL_STRENGTH, () -> {
        double formattedValue = Double.valueOf(newValue) / 100;
        client.setPOVPreference(game.get().getId(), getPOV(), POV.BALL_TRAIL_STRENGTH, formattedValue);
      }, 500);
    });

    povOverwriteNightDayCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      int result = 0;
      if (newValue) {
        result = 1;
      }
      client.setPOVPreference(game.get().getId(), getPOV(), POV.OVERWRITE_NIGHTDAY, result);
      povNighDaySpinner.setDisable(!newValue);
    });;

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryNightDay = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    povNighDaySpinner.setValueFactory(factoryNightDay);
    factoryNightDay.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.NIGHTDAY_LEVEL, () -> {
        client.setPOVPreference(game.get().getId(), getPOV(), POV.NIGHTDAY_LEVEL, newValue);
      }, 500);
    });

    SpinnerValueFactory.DoubleSpinnerValueFactory factoryDifficulty = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 0);
    povGameDifficultySpinner.setValueFactory(factoryDifficulty);
    povGameDifficultySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.GAMEPLAY_DIFFICULTY, () -> {
        client.setPOVPreference(game.get().getId(), getPOV(), POV.GAMEPLAY_DIFFICULTY, newValue);
      }, 500);
    });

    povSoundVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.SOUND_VOLUME, () -> {
        int v = (int) newValue;
        client.setPOVPreference(game.get().getId(), getPOV(), POV.SOUND_VOLUME, v);
      }, 500);
    });
    povMusicVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.MUSIC_VOLUME, () -> {
        int v = (int) newValue;
        client.setPOVPreference(game.get().getId(), getPOV(), POV.MUSIC_VOLUME, v);
      }, 500);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryRotation = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 360, 0);
    povRotationFullscreenSpinner.setValueFactory(factoryRotation);
    povRotationFullscreenSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(POV.FULLSCREEN_ROTATION, () -> {
        client.setPOVPreference(game.get().getId(), getPOV(), POV.FULLSCREEN_ROTATION, newValue);
      }, 500);
    });
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.pov = null;
    this.game = game;
    this.refreshView(game);
  }

  @FXML
  private void onPOVReload() {
    this.refreshView(this.game);
  }

  @FXML
  private void onPOVReExport() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-Export POV file for table '" + this.game.get().getGameDisplayName() + "'?", "This will overwrite the POV file with the table values.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.deletePOV(this.game.get().getId());
      this.onPOVExport();
    }
  }

  @FXML
  private void onPOVDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete POV file for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.deletePOV(this.game.get().getId());
      tablesController.onReload();
    }
  }


  @FXML
  private void onDirectb2sUpload() {
    if (this.game.isPresent()) {
      boolean uploaded = Dialogs.openDirectB2SUploadDialog(this.game.get());
      if (uploaded) {
        tablesController.onReload();
      }
    }
  }

  @FXML
  private void onRomUpload() {
    boolean uploaded = Dialogs.openRomUploadDialog();
    if (uploaded) {
      tablesController.onReload();
    }
  }

  @FXML
  private void onCard() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      boolean b = client.generateHighscoreCardSample(g);
      if(b) {
        ByteArrayInputStream s = Studio.client.getHighscoreCard(g);
        MediaUtil.openMedia(s);
      }
      else {
        ScoreSummaryRepresentation summary = client.getGameScores(g.getId());
        String status = summary.getMetadata().getStatus();
        WidgetFactory.showAlert(Studio.stage, "Card Generation Failed.", "The card generation failed: " + status);
      }
    }
  }

  @FXML
  private void onPlayClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    MediaView mediaView = (MediaView) borderPane.getCenter();

    FontIcon icon = (FontIcon) source.getChildrenUnmodifiable().get(0);
    String iconLiteral = icon.getIconLiteral();
    if (iconLiteral.equals("bi-play")) {
      mediaView.getMediaPlayer().setMute(false);
      mediaView.getMediaPlayer().setCycleCount(1);
      mediaView.getMediaPlayer().play();
      icon.setIconLiteral("bi-stop");
    }
    else {
      mediaView.getMediaPlayer().stop();
      icon.setIconLiteral("bi-play");
    }
  }

  @FXML
  private void onOpenDirectB2SBackground() {
    if (game.isPresent()) {
      ByteArrayInputStream image = client.getDirectB2SImage(game.get());
      MediaUtil.openMedia(image);
    }
  }

  @FXML
  private void onMediaViewClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    Node center = borderPane.getCenter();
    if (center == null) {
      center = screenPlayField.getCenter();
    }

    GameMediaItemRepresentation mediaItem = (GameMediaItemRepresentation) center.getUserData();
    if (mediaItem != null) {
      GameRepresentation gameRepresentation = game.get();
      Dialogs.openMediaDialog(client, gameRepresentation, mediaItem);
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
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      tablesController.onReload();
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
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      tablesController.onReload();
    }
  }

  @FXML
  private void onPOVExport() {
    if (game.isPresent()) {
      GameRepresentation g = game.get();
      ProgressResultModel resultModel = Dialogs.createProgressDialog(new POVExportProgressModel(client, "Export POV Settings", g));
      if (!resultModel.getResults().isEmpty()) {
        tablesController.onReload();
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "POV export failed, check log for details.");
      }
    }
  }

  private POVRepresentation getPOV() {
    return this.pov;
  }

  @FXML
  private void onScan() {
    refreshHighscore(game, true);
  }

  private void refreshHighscore(Optional<GameRepresentation> gameRepresentation, boolean forceRescan) {
    rawScoreLabel.setText("");
    formattedScoreLabel.setText("");

    this.hsFileLabel.setText("-");
    this.hsStatusLabel.setText("-");
    this.hsTypeLabel.setText("-");
    this.hsLastModifiedLabel.setText("-");
    this.hsLastScannedLabel.setText("-");

    rawTitleLabel.setVisible(false);
    formattedTitleLabel.setVisible(false);

    rawScoreWrapper.setVisible(false);
    formattedScoreWrapper.setVisible(false);

    if (gameRepresentation.isPresent()) {
      GameRepresentation game = gameRepresentation.get();
      if (forceRescan) {
        client.scanGameScore(game.getId());
      }

      ScoreSummaryRepresentation summary = client.getGameScores(game.getId());
      if (summary != null && summary.getMetadata() != null) {
        if (summary.getMetadata().getFilename() != null) {
          this.hsFileLabel.setText(summary.getMetadata().getFilename());
        }

        if (summary.getMetadata().getStatus() != null) {
          this.hsStatusLabel.setText(summary.getMetadata().getStatus());
        }

        if (summary.getMetadata().getType() != null) {
          this.hsTypeLabel.setText(summary.getMetadata().getType());
        }

        if (summary.getMetadata().getModified() != null) {
          this.hsLastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(summary.getMetadata().getModified()));
        }

        if (summary.getMetadata().getScanned() != null) {
          this.hsLastScannedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(summary.getMetadata().getScanned()));
        }

        if (!summary.getScores().isEmpty()) {
          rawTitleLabel.setVisible(true);
          rawScoreWrapper.setVisible(true);

          rawScoreLabel.setFont(WidgetController.getScoreFontText());
          rawScoreLabel.setText(summary.getRaw());

          List<ScoreRepresentation> scores = summary.getScores();
          StringBuilder builder = new StringBuilder();
          for (ScoreRepresentation score : scores) {
            builder.append("#");
            builder.append(score.getPosition());
            builder.append(" ");
            builder.append(score.getPlayerInitials());
            builder.append("   ");
            builder.append(score.getScore());
            builder.append("\n");
          }

          formattedTitleLabel.setVisible(true);
          formattedScoreWrapper.setVisible(true);

          formattedScoreLabel.setFont(WidgetController.getScoreFontText());
          formattedScoreLabel.setText(builder.toString());
        }
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
        client.saveGame(gameRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      tablesController.onReload();
    }
  }

  private void resetMedia() {
    disposeMediaPane(screenAudioLaunch);
    disposeMediaPane(screenAudio);
    disposeMediaPane(screenLoading);
    disposeMediaPane(screenGameHelp);
    disposeMediaPane(screenGameInfo);
    disposeMediaPane(screenDMD);
    disposeMediaPane(screenBackGlass);
    disposeMediaPane(screenTopper);
    disposeMediaPane(screenMenu);
    disposeMediaPane(screenPlayField);
    disposeMediaPane(screenOther2);
    disposeMediaPane(screenWheel);
  }

  private void refreshView(Optional<GameRepresentation> g) {
    povSettingsPane.setVisible(g.isEmpty());
    povCreatePane.setVisible(g.isEmpty());

    editHsFileNameBtn.setDisable(g.isEmpty());
    editRomNameBtn.setDisable(g.isEmpty());
    editTableNameBtn.setDisable(g.isEmpty());
    romUploadBtn.setDisable(g.isEmpty());
    scanBtn.setDisable(g.isEmpty());
    directb2sUploadBtn.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      GameMediaRepresentation gameMedia = game.getGameMedia();

      povSettingsPane.setVisible(game.isPov());
      povCreatePane.setVisible(!game.isPov());

      editHsFileNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      editRomNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      editTableNameBtn.setDisable(!game.getEmulator().isVisualPinball());
      romUploadBtn.setDisable(!game.getEmulator().isVisualPinball());
      scanBtn.setDisable(!game.getEmulator().isVisualPinball());


      volumeSlider.setDisable(false);
      volumeSlider.setValue(game.getVolume());

      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.PlayField);
      playfieldViewBtn.setVisible(item != null);

      labelId.setText(String.valueOf(game.getId()));
      labelRom.setText(game.getOriginalRom() != null ? game.getOriginalRom() : game.getRom());
      labelRomAlias.setText(game.getOriginalRom() != null ? game.getRom() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName() != null ? game.getGameFileName() : "-");
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

      refreshDirectB2SPreview(g);
      refreshPOV(g);

      if (titledPaneMedia.isExpanded()) {
        refreshMedia(gameMedia);
      }
    }
    else {
      resetMedia();

      playfieldViewBtn.setVisible(false);

      volumeSlider.setValue(100);
      volumeSlider.setDisable(true);

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

      refreshDirectB2SPreview(Optional.empty());
    }
    refreshHighscore(g, false);
  }

  private void refreshPOV(Optional<GameRepresentation> g) {
    povSoundVolumeSlider.setDisable(true);
    povMusicVolumeSlider.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      if (game.isPov()) {
        pov = client.getPOV(game.getId());

        povSSAACombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.SSAA)));
        povPostprocAACombo.valueProperty().setValue(POVPostProcComboModel.forValue(pov.getValue(POV.POST_PROC_AA)));
        povIngameAOCombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.INGAME_AO)));
        povScSpReflectCombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.SCSP_REFLECT)));
        povFpsLimiterCombo.valueProperty().setValue(POVComboModel.forValue(pov.getValue(POV.FPS_LIMITER)));

        povOverwriteDetailCheckbox.setSelected(pov.getBooleanValue(POV.OVERWRITE_DETAILS_LEVEL));
        povDetailsSlider.setValue(pov.getIntValue(POV.DETAILS_LEVEL));
        povDetailsSlider.setDisable(!pov.getBooleanValue(POV.OVERWRITE_DETAILS_LEVEL));

        povBallReflectionCombobox.setValue(POVComboModel.forValue(pov.getValue(POV.BALL_REFLECTION)));
        povBallTrailCombobox.setValue(POVComboModel.forValue(pov.getValue(POV.BALL_TRAIL)));
        int ballStrengthValue = (int) (pov.getDoubleValue(POV.BALL_TRAIL_STRENGTH) * 100);
        povBallTrailStrengthSpinner.getValueFactory().setValue(ballStrengthValue);

        povOverwriteNightDayCheckbox.setSelected(pov.getBooleanValue(POV.OVERWRITE_NIGHTDAY));
        povNighDaySpinner.setDisable(!pov.getBooleanValue(POV.OVERWRITE_NIGHTDAY));
        povNighDaySpinner.getValueFactory().setValue(pov.getIntValue(POV.NIGHTDAY_LEVEL));

        povGameDifficultySpinner.getValueFactory().setValue(pov.getDoubleValue(POV.GAMEPLAY_DIFFICULTY));

        povSoundVolumeSlider.setDisable(false);
        povSoundVolumeSlider.setValue(pov.getIntValue(POV.SOUND_VOLUME));
        povMusicVolumeSlider.setDisable(false);
        povMusicVolumeSlider.setValue(pov.getIntValue(POV.MUSIC_VOLUME));

        povRotationFullscreenSpinner.getValueFactory().setValue(pov.getIntValue(POV.FULLSCREEN_ROTATION));
      }
      else {
        povSoundVolumeSlider.setValue(100);
        povMusicVolumeSlider.setValue(100);
      }
    }
  }

  private void refreshDirectB2SPreview(Optional<GameRepresentation> game) {
    try {
      openDirectB2SImageButton.setVisible(false);
      openDirectB2SImageButton.setTooltip(new Tooltip("Open directb2s image"));
      rawDirectB2SImage.setVisible(false);

      if (game.isPresent()) {
        InputStream input = client.getDirectB2SImage(game.get());
        Image image = new Image(input);
        rawDirectB2SImage.setVisible(true);
        rawDirectB2SImage.setImage(image);
        input.close();

        if (image.getWidth() > 300) {
          openDirectB2SImageButton.setVisible(true);
          resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
        }
        else {
          resolutionLabel.setText("");
        }
      }
      else {
        resolutionLabel.setText("");
      }
    } catch (IOException e) {
      LOG.error("Failed to load raw b2s: " + e.getMessage(), e);
    }
  }

  private void refreshMedia(GameMediaRepresentation gameMedia) {
    PreferenceEntryRepresentation entry = client.getPreference(PreferenceNames.IGNORED_MEDIA);
    List<String> ignoreScreenNames = entry.getCSVValue();

    PopperScreen[] values = PopperScreen.values();
    for (PopperScreen value : values) {
      BorderPane screen = this.getScreenBorderPaneFor(value);
      boolean ignored = ignoreScreenNames.contains(value.name());
      GameMediaItemRepresentation item = gameMedia.getItem(value);
      WidgetFactory.createMediaContainer(Studio.client, screen, item, ignored, mediaPreviewCheckbox.isSelected());
    }
  }

  private BorderPane getScreenBorderPaneFor(PopperScreen value) {
    BorderPane lookup = (BorderPane) mediaRootPane.lookup("#screen" + value.name());
    if (lookup == null) {
      throw new UnsupportedOperationException("No screen found for id 'screen" + value.name() + "'");
    }
    return lookup;
  }

  private void disposeMediaPane(BorderPane parent) {
    if (parent != null && parent.getCenter() != null) {
      WidgetFactory.disposeMediaBorderPane(parent);
    }
  }
}
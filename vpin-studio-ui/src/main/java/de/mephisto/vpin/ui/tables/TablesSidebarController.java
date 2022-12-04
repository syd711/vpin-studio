package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
import java.util.*;

public class TablesSidebarController implements Initializable, StudioFXController {
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
  private Slider volumeSlider;

  @FXML
  private TextArea highscoreTextArea;

  @FXML
  private ImageView rawDirectB2SImage;

  @FXML
  private Button openDirectB2SImageButton;

  @FXML
  private Button editHsFileNameBtn;

  @FXML
  private Button editRomNameBtn;

  @FXML
  private Button directb2sUploadBtn;

  @FXML
  private Button romUploadBtn;

  @FXML
  private Label resolutionLabel;


  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesController tablesController;

  // Add a public no-args constructor
  public TablesSidebarController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
    this.accordion.setExpandedPane(titledPaneMedia);

    volumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
      if (game.isPresent()) {
        BindingUtil.debouncer.debounce("tableVolume" + game.get().getId(), () -> {
          int value = t1.intValue();
          if (value == 0) {
            value = 1;
          }
          game.get().setVolume(value);

          try {
            client.saveGame(game.get());
          } catch (Exception e) {
            WidgetFactory.showAlert(e.getMessage());
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
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
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
      Dialogs.openMediaDialog(gameRepresentation, mediaItem);
    }
  }

  @FXML
  private void onRomEdit() {
    GameRepresentation gameRepresentation = game.get();
    String romName = WidgetFactory.showInputDialog("Enter ROM Name", null, gameRepresentation.getRom());
    if (romName != null) {
      gameRepresentation.setRom(romName);
      try {
        client.saveGame(gameRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(e.getMessage());
      }
      tablesController.onReload();
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation gameRepresentation = game.get();
    Optional<ButtonType> result = WidgetFactory.showConfirmation("Ignore this warning for future validations of table '" + gameRepresentation.getGameDisplayName() + "?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      String validationState = String.valueOf(gameRepresentation.getValidationState());
      String ignoredValidations = gameRepresentation.getIgnoredValidations();
      if (ignoredValidations == null) {
        ignoredValidations = "";
      }
      List<String> gameIgnoreList = new ArrayList<>(Arrays.asList(ignoredValidations.split(",")));
      if (!gameIgnoreList.contains(validationState)) {
        gameIgnoreList.add(validationState);
      }

      gameRepresentation.setIgnoredValidations(StringUtils.join(gameIgnoreList, ","));

      try {
        client.saveGame(gameRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(e.getMessage());
      }
      tablesController.onReload();
    }
  }

  @FXML
  private void onHsFileNameEdit() {
    GameRepresentation gameRepresentation = game.get();
    String fs = WidgetFactory.showInputDialog("EM Highscore Filename", "Enter the name of the highscore file for this table.\nThe file is located in the 'User' folder.", gameRepresentation.getHsFileName());
    if (fs != null) {
      gameRepresentation.setHsFileName(fs);

      try {
        client.saveGame(gameRepresentation);
      } catch (Exception e) {
        WidgetFactory.showAlert(e.getMessage());
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
    editHsFileNameBtn.setDisable(g.isEmpty());
    editRomNameBtn.setDisable(g.isEmpty());
    directb2sUploadBtn.setDisable(g.isEmpty());
    romUploadBtn.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());

      volumeSlider.setDisable(false);
      volumeSlider.setValue(game.getVolume());

      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.PlayField);
      playfieldViewBtn.setVisible(item != null);

      labelId.setText(String.valueOf(game.getId()));
      labelRom.setText(game.getOriginalRom() != null ? game.getOriginalRom() : game.getRom());
      labelRomAlias.setText(game.getOriginalRom() != null ? game.getRom() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName());
      labelLastPlayed.setText(game.getLastPlayed() != null ? DateFormat.getTimeInstance().format(game.getLastPlayed()) : "-");
      labelTimesPlayed.setText(String.valueOf(game.getNumberPlays()));
      if (!StringUtils.isEmpty(game.getHsFileName())) {
        labelHSFilename.setText(game.getHsFileName());
      }
      else {
        labelHSFilename.setText("-");
      }

      refreshDirectB2SPreview(g);

      if (titledPaneMedia.isExpanded()) {
        refreshMedia(gameMedia);
      }

      ScoreSummaryRepresentation gameScores = client.getGameScores(game.getId());
      if (!gameScores.getScores().isEmpty()) {
        highscoreTextArea.setText(gameScores.getRaw());
      }
      else {
        highscoreTextArea.setText("");
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
      labelLastPlayed.setText("-");
      labelTimesPlayed.setText("-");
      labelHSFilename.setText("-");

      refreshDirectB2SPreview(Optional.empty());

      highscoreTextArea.setText("");
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
      WidgetFactory.createMediaContainer(screen, item, ignored);
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
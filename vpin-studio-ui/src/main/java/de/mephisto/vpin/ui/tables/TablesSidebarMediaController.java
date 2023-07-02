package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.SystemSummary;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.VisibilityHoverListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarMediaController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarMediaController.class);

  @FXML
  private Pane mediaRootPane;

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
  private Button btn_Audio;

  @FXML
  private Button btn_upload_Audio;

  @FXML
  private Button btn_AudioLaunch;

  @FXML
  private Node top_AudioLaunch;

  @FXML
  private Button btn_upload_AudioLaunch;

  @FXML
  private Button btn_Topper;

  @FXML
  private Button btn_edit_Topper;

  @FXML
  private Button btn_upload_Topper;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarMediaController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    btn_AudioLaunch.setVisible(Studio.client.getSystemService().isLocal());
    btn_Audio.setVisible(Studio.client.getSystemService().isLocal());


    SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
    File folder = new File(systemSummary.getPinupSystemDirectory() + "/POPMedia/Visual Pinball X");
    File audio = new File(folder, "Audio");

    screenAudio.setOnDragOver(new FileDragEventHandler(screenAudio));
    screenAudio.setOnDragDropped(new TableMediaFileDropEventHandler(audio, tablesSidebarController, PopperScreen.Audio, ".mp3"));

    top_AudioLaunch.setVisible(false);
    screenAudioLaunch.hoverProperty().addListener(new VisibilityHoverListener(top_AudioLaunch));
  }

  @FXML
  private void onMediaEdit(ActionEvent e) {
    if (game.isPresent()) {
      Button source = (Button) e.getSource();
      String id = source.getId();
      String screen = id.substring(id.lastIndexOf("_") + 1);
      PopperScreen popperScreen = PopperScreen.valueOf(screen);

      Dialogs.openMediaUploadDialog(tablesSidebarController, game.get(), popperScreen);
    }
  }

  @FXML
  private void onMediaUpload(ActionEvent e) {
    if (game.isPresent()) {
      Button source = (Button) e.getSource();
      String id = source.getId();
      String screen = id.substring(id.lastIndexOf("_") + 1);

      PopperScreen popperScreen = PopperScreen.valueOf(screen);
      Dialogs.openMediaUploadDialog(tablesSidebarController, game.get(), popperScreen);
    }
  }

  @FXML
  private void onMediaFolderOpenClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    String id = source.getId();
    String screen = id.substring(id.indexOf("_") + 1);

    GameRepresentation selection = tablesSidebarController.getTablesController().getSelection();
    if (selection != null) {
      File emulatorFolder = new File(selection.getEmulator().getMediaDir());
      SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
      File file = new File(systemSummary.getPinupSystemDirectory() + "/POPMedia/" + emulatorFolder.getName() + "/" + screen);
      if (!file.exists()) {
        WidgetFactory.showAlert(Studio.stage, "Did not PinUP Popper media folder for screen \"" + screen + "\"");
        return;
      }

      try {
        new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
      } catch (Exception ex) {
        LOG.error("Failed to open Explorer: " + ex.getMessage(), e);
      }
    }
  }

  @FXML
  private void onMediaViewClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent().getParent().getParent();
    Node center = borderPane.getCenter();
    GameMediaItemRepresentation mediaItem = (GameMediaItemRepresentation) center.getUserData();
    if (mediaItem != null) {
      GameRepresentation gameRepresentation = game.get();
      Dialogs.openMediaDialog(Studio.client, gameRepresentation, mediaItem);
    }
  }

  @FXML
  private void onPlayClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent().getParent();
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
  private void onMediaDrop() {

  }

  public void setGame(Optional<GameRepresentation> game, boolean preview) {
    this.game = game;
    this.refreshView(game, preview);
  }

  public void refreshView(Optional<GameRepresentation> g, boolean preview) {
    if (g.isPresent()) {
      GameRepresentation game = g.get();
      GameMediaRepresentation gameMedia = game.getGameMedia();
      refreshMedia(gameMedia, preview);
    }
    else {
      resetMedia();
    }
  }

  public void refreshMedia(GameMediaRepresentation gameMedia, boolean preview) {
    Platform.runLater(() -> {
      PreferenceEntryRepresentation entry = Studio.client.getPreference(PreferenceNames.IGNORED_MEDIA);
      List<String> ignoreScreenNames = entry.getCSVValue();

      PopperScreen[] values = PopperScreen.values();
      for (PopperScreen value : values) {
        BorderPane screen = this.getScreenBorderPaneFor(value);
        boolean ignored = ignoreScreenNames.contains(value.name());
        GameMediaItemRepresentation item = gameMedia.getItem(value);
        WidgetFactory.createMediaContainer(Studio.client, screen, item, ignored, preview);
      }
    });
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public void resetMedia() {
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
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

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
  private BorderPane playfieldRoot;

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
  private Button btn_edit_Audio;

  @FXML
  private Button btn_edit_AudioLaunch;

  @FXML
  private Button btn_edit_Topper;

  @FXML
  private Button btn_edit_Menu;

  @FXML
  private Button btn_edit_BackGlass;

  @FXML
  private Button btn_edit_Loading;

  @FXML
  private Button btn_edit_GameInfo;

  @FXML
  private Button btn_edit_DMD;

  @FXML
  private Button btn_edit_Other2;

  @FXML
  private Button btn_edit_GameHelp;

  @FXML
  private Button btn_edit_PlayField;

  @FXML
  private Button btn_edit_Wheel;


  @FXML
  private Button btn_Audio;

  @FXML
  private Button btn_AudioLaunch;

  @FXML
  private Button btn_Topper;

  @FXML
  private Button btn_Menu;

  @FXML
  private Button btn_BackGlass;

  @FXML
  private Button btn_Loading;

  @FXML
  private Button btn_GameInfo;

  @FXML
  private Button btn_DMD;

  @FXML
  private Button btn_Other2;

  @FXML
  private Button btn_GameHelp;

  @FXML
  private Button btn_PlayField;

  @FXML
  private Button btn_Wheel;


  @FXML
  private Button btn_view_Topper;

  @FXML
  private Button btn_view_Menu;

  @FXML
  private Button btn_view_BackGlass;

  @FXML
  private Button btn_view_Loading;

  @FXML
  private Button btn_view_GameInfo;

  @FXML
  private Button btn_view_DMD;

  @FXML
  private Button btn_view_Other2;

  @FXML
  private Button btn_view_GameHelp;

  @FXML
  private Button btn_view_PlayField;

  @FXML
  private Button btn_view_Wheel;


  @FXML
  private Node top_Audio;

  @FXML
  private Node top_AudioLaunch;

  @FXML
  private Node top_Topper;

  @FXML
  private Node top_Loading;

  @FXML
  private Node top_PlayField;

  @FXML
  private Node top_BackGlass;

  @FXML
  private Node top_GameInfo;

  @FXML
  private Node top_GameHelp;

  @FXML
  private Node top_DMD;

  @FXML
  private Node top_Menu;

  @FXML
  private Node top_Other2;

  @FXML
  private Node top_Wheel;

//  @FXML
//  private Node hbox_Audio;
//
//  @FXML
//  private Node hbox_AudioLaunch;
//
//  @FXML
//  private Node hbox_Topper;
//
//  @FXML
//  private Node hbox_Loading;
//
//  @FXML
//  private Node hbox_PlayField;
//
//  @FXML
//  private Node hbox_BackGlass;
//
//  @FXML
//  private Node hbox_GameInfo;
//
//  @FXML
//  private Node hbox_GameHelp;
//
//  @FXML
//  private Node hbox_DMD;
//
//  @FXML
//  private Node hbox_Menu;
//
//  @FXML
//  private Node hbox_Other2;
//
//  @FXML
//  private Node hbox_Wheel;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarMediaController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    top_Audio.setVisible(false);
    top_AudioLaunch.setVisible(false);
    top_Topper.setVisible(false);
    top_Loading.setVisible(false);
    top_PlayField.setVisible(false);
    top_BackGlass.setVisible(false);
    top_GameInfo.setVisible(false);
    top_GameHelp.setVisible(false);
    top_Menu.setVisible(false);
    top_DMD.setVisible(false);
    top_Other2.setVisible(false);
    top_Wheel.setVisible(false);

    boolean isLocal = Studio.client.getSystemService().isLocal();
    btn_Audio.setVisible(isLocal);
    btn_AudioLaunch.setVisible(isLocal);
    btn_Topper.setVisible(isLocal);
    btn_Loading.setVisible(isLocal);
    btn_PlayField.setVisible(isLocal);
    btn_BackGlass.setVisible(isLocal);
    btn_GameInfo.setVisible(isLocal);
    btn_GameHelp.setVisible(isLocal);
    btn_Menu.setVisible(isLocal);
    btn_DMD.setVisible(isLocal);
    btn_Other2.setVisible(isLocal);
    btn_Wheel.setVisible(isLocal);


    Predicate showPredicate = o -> tablesSidebarController.getTablesController().getSelection() != null;

    screenAudio.hoverProperty().addListener(new VisibilityHoverListener(top_Audio, showPredicate));
    screenAudioLaunch.hoverProperty().addListener(new VisibilityHoverListener(top_AudioLaunch, showPredicate));
    screenDMD.hoverProperty().addListener(new VisibilityHoverListener(top_DMD, showPredicate));
    screenBackGlass.hoverProperty().addListener(new VisibilityHoverListener(top_BackGlass, showPredicate));
    screenMenu.hoverProperty().addListener(new VisibilityHoverListener(top_Menu, showPredicate));
    screenGameInfo.hoverProperty().addListener(new VisibilityHoverListener(top_GameInfo, showPredicate));
    screenGameHelp.hoverProperty().addListener(new VisibilityHoverListener(top_GameHelp, showPredicate));
    screenLoading.hoverProperty().addListener(new VisibilityHoverListener(top_Loading, showPredicate));
    screenBackGlass.hoverProperty().addListener(new VisibilityHoverListener(top_BackGlass, showPredicate));
    screenPlayField.hoverProperty().addListener(new VisibilityHoverListener(top_PlayField, showPredicate));
    top_PlayField.hoverProperty().addListener(new VisibilityHoverListener(top_PlayField, showPredicate));
    playfieldRoot.hoverProperty().addListener(new VisibilityHoverListener(top_PlayField, showPredicate));
    screenTopper.hoverProperty().addListener(new VisibilityHoverListener(top_Topper, showPredicate));
    screenOther2.hoverProperty().addListener(new VisibilityHoverListener(top_Other2, showPredicate));
    screenWheel.hoverProperty().addListener(new VisibilityHoverListener(top_Wheel, showPredicate));
  }

  @FXML
  private void onMediaEdit(ActionEvent e) {
    if (game.isPresent()) {
      Button source = (Button) e.getSource();
      String id = source.getId();
      String screen = id.substring(id.lastIndexOf("_") + 1);
      PopperScreen popperScreen = PopperScreen.valueOf(screen);

      Dialogs.openMediaAdminDialog(tablesSidebarController, game.get(), popperScreen);
    }
  }

  @FXML
  private void onMediaUpload(ActionEvent e) {
    if (game.isPresent()) {
      Button source = (Button) e.getSource();
      String id = source.getId();
      String screen = id.substring(id.lastIndexOf("_") + 1);

      PopperScreen popperScreen = PopperScreen.valueOf(screen);
      Dialogs.openMediaUploadDialog(Studio.stage, tablesSidebarController, game.get(), popperScreen);
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
    String id = source.getId();
    String screen = id.substring(id.lastIndexOf("_") + 1);


    GameRepresentation gameRepresentation = game.get();
    GameMediaItemRepresentation defaultMediaItem = gameRepresentation.getGameMedia().getDefaultMediaItem(PopperScreen.valueOf(screen));
    if (defaultMediaItem != null) {
      Dialogs.openMediaDialog(Studio.client, gameRepresentation, defaultMediaItem);
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
    btn_edit_Audio.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Audio).isEmpty());
    btn_edit_AudioLaunch.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.AudioLaunch).isEmpty());
    btn_edit_Topper.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Topper).isEmpty());
    btn_edit_Menu.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Menu).isEmpty());
    btn_edit_BackGlass.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.BackGlass).isEmpty());
    btn_edit_Loading.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Loading).isEmpty());
    btn_edit_GameInfo.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.GameInfo).isEmpty());
    btn_edit_DMD.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.DMD).isEmpty());
    btn_edit_Other2.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Other2).isEmpty());
    btn_edit_GameHelp.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.GameHelp).isEmpty());
    btn_edit_PlayField.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.PlayField).isEmpty());
    btn_edit_Wheel.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Wheel).isEmpty());

    btn_view_Topper.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Topper).isEmpty());
    btn_view_Menu.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Menu).isEmpty());
    btn_view_BackGlass.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.BackGlass).isEmpty());
    btn_view_Loading.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Loading).isEmpty());
    btn_view_GameInfo.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.GameInfo).isEmpty());
    btn_view_DMD.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.DMD).isEmpty());
    btn_view_Other2.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Other2).isEmpty());
    btn_view_GameHelp.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.GameHelp).isEmpty());
    btn_view_PlayField.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.PlayField).isEmpty());
    btn_view_Wheel.setDisable(g.isEmpty() || g.get().getGameMedia().getMediaItems(PopperScreen.Wheel).isEmpty());

    if (g.isPresent()) {
      btn_edit_Audio.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.Audio).size()));
      btn_edit_AudioLaunch.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.AudioLaunch).size()));
      btn_edit_Topper.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.Topper).size()));
      btn_edit_Menu.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.Menu).size()));
      btn_edit_BackGlass.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.BackGlass).size()));
      btn_edit_Loading.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.Loading).size()));
      btn_edit_GameInfo.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.GameInfo).size()));
      btn_edit_DMD.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.DMD).size()));
      btn_edit_Other2.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.Other2).size()));
      btn_edit_GameHelp.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.GameHelp).size()));
      btn_edit_PlayField.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.PlayField).size()));
      btn_edit_Wheel.setText(String.valueOf(g.get().getGameMedia().getMediaItems(PopperScreen.Wheel).size()));


      GameRepresentation game = g.get();
      GameMediaRepresentation gameMedia = game.getGameMedia();
      refreshMedia(gameMedia, preview);
    }
    else {
      btn_edit_Audio.setText(" ");
      btn_edit_AudioLaunch.setText(" ");
      btn_edit_Topper.setText(" ");
      btn_edit_Menu.setText(" ");
      btn_edit_BackGlass.setText(" ");
      btn_edit_Loading.setText(" ");
      btn_edit_GameInfo.setText(" ");
      btn_edit_DMD.setText(" ");
      btn_edit_Other2.setText(" ");
      btn_edit_GameHelp.setText(" ");
      btn_edit_PlayField.setText(" ");
      btn_edit_Wheel.setText(" ");


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
        GameMediaItemRepresentation item = gameMedia.getDefaultMediaItem(value);
        WidgetFactory.createMediaContainer(Studio.client, screen, item, ignored, preview);
      }
    });
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;

    screenAudio.setOnDragOver(new FileDragEventHandler(screenAudio));
    screenAudio.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.Audio, "mp3"));
    screenAudioLaunch.setOnDragOver(new FileDragEventHandler(screenAudioLaunch));
    screenAudioLaunch.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.AudioLaunch, "mp3"));
    screenTopper.setOnDragOver(new FileDragEventHandler(screenTopper));
    screenTopper.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.Topper, "mp4", "png", "jpg"));
    screenLoading.setOnDragOver(new FileDragEventHandler(screenLoading));
    screenLoading.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.Loading, "mp4"));
    screenPlayField.setOnDragOver(new FileDragEventHandler(screenPlayField));
    screenPlayField.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.PlayField, "mp4"));
    screenBackGlass.setOnDragOver(new FileDragEventHandler(screenBackGlass));
    screenBackGlass.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.BackGlass, "mp4", "png", "jpg"));
    screenGameInfo.setOnDragOver(new FileDragEventHandler(screenGameInfo));
    screenGameInfo.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.GameInfo,  "png", "jpg"));
    screenGameHelp.setOnDragOver(new FileDragEventHandler(screenGameHelp));
    screenGameHelp.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.GameHelp,  "png", "jpg"));
    screenMenu.setOnDragOver(new FileDragEventHandler(screenMenu));
    screenMenu.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.Menu, "mp4", "png", "jpg"));
    screenDMD.setOnDragOver(new FileDragEventHandler(screenDMD));
    screenDMD.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.DMD, "mp4", "png", "jpg"));
    screenOther2.setOnDragOver(new FileDragEventHandler(screenOther2));
    screenOther2.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.Other2, "mp4", "png", "jpg"));
    screenWheel.setOnDragOver(new FileDragEventHandler(screenWheel));
    screenWheel.setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController, PopperScreen.Wheel, "png", "apng"));
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
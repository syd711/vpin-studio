package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.drophandler.TableMediaFileDropEventHandler;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.JFXHelper;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.util.VisibilityHoverListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarMediaController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarMediaController.class);

  @FXML
  private Pane mediaRootPane;

  @FXML
  private BorderPane mediaRoot;

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
  private Button btn_delete_Audio;

  @FXML
  private Button btn_delete_AudioLaunch;

  @FXML
  private Button btn_delete_Topper;

  @FXML
  private Button btn_delete_Menu;

  @FXML
  private Button btn_delete_BackGlass;

  @FXML
  private Button btn_delete_Loading;

  @FXML
  private Button btn_delete_GameInfo;

  @FXML
  private Button btn_delete_DMD;

  @FXML
  private Button btn_delete_Other2;

  @FXML
  private Button btn_delete_GameHelp;

  @FXML
  private Button btn_delete_PlayField;

  @FXML
  private Button btn_delete_Wheel;


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
  private Button btn_dmdPos_DMD;

  @FXML
  private Button btn_dmdPos_Backglass;

  @FXML
  private Button btn_dmdPos_Apron;


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

  private final Tooltip highscoreCardTooltip = new Tooltip("Highscore cards are generated for this screen.");

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarMediaController() {
  }

  @FXML
  private void onDMDPosition() {
    if (game.isPresent()) {
      TableDialogs.openDMDPositionDialog(game.get(), null);
    }
  }

  @FXML
  private void onMediaEdit(ActionEvent e) {
    if (game.isPresent()) {
      Button source = (Button) e.getSource();
      String id = source.getId();
      String screen = id.substring(id.lastIndexOf("_") + 1);
      VPinScreen vPinScreen = VPinScreen.valueOf(screen);

      TableDialogs.openTableAssetsDialog(tablesSidebarController.getTableOverviewController(), game.get(), vPinScreen);
    }
  }

  @FXML
  private void onMediaFolderOpenClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    String id = source.getId();
    String screen = id.substring(id.indexOf("_") + 1);

    GameRepresentation selection = tablesSidebarController.getTableOverviewController().getSelection();
    if (selection != null) {
      File screendir = client.getFrontendService().getMediaDirectory(selection.getId(), screen);
      SystemUtil.openFolder(screendir);
    }
  }

  @FXML
  private void onMediaViewClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    String id = source.getId();
    String screen = id.substring(id.lastIndexOf("_") + 1);

    GameRepresentation gameRepresentation = game.get();

    FrontendMediaItemRepresentation defaultMediaItem = client.getFrontendService().getDefaultFrontendMediaItem(
        gameRepresentation.getId(), VPinScreen.valueOf(screen));
    if (defaultMediaItem != null) {
      TableDialogs.openMediaDialog(Studio.stage, gameRepresentation, defaultMediaItem);
    }
  }

  @FXML
  private void onMediaDeleteClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    String id = source.getId();
    String screen = id.substring(id.lastIndexOf("_") + 1);

    VPinScreen vPinScreen = VPinScreen.valueOf(screen);
    GameRepresentation gameRepresentation = game.get();
    FrontendMediaItemRepresentation defaultMediaItem = client.getFrontendService().getDefaultFrontendMediaItem(gameRepresentation.getId(), vPinScreen);
    if (defaultMediaItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete", "Delete \"" + defaultMediaItem.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getGameMediaService().deleteMedia(gameRepresentation.getId(), vPinScreen, defaultMediaItem.getName());
        EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
      }
    }
  }

  public void setGame(Optional<GameRepresentation> game, boolean preview) {
    this.game = game;
    this.refreshView(game, preview);
  }

  public void refreshView(Optional<GameRepresentation> g, boolean preview) {

    btn_edit_Audio.setDisable(g.isEmpty());
    btn_edit_AudioLaunch.setDisable(g.isEmpty());
    btn_edit_Topper.setDisable(g.isEmpty());
    btn_edit_Menu.setDisable(g.isEmpty());
    btn_edit_BackGlass.setDisable(g.isEmpty());
    btn_edit_Loading.setDisable(g.isEmpty());
    btn_edit_GameInfo.setDisable(g.isEmpty());
    btn_edit_DMD.setDisable(g.isEmpty());
    btn_edit_Other2.setDisable(g.isEmpty());
    btn_edit_GameHelp.setDisable(g.isEmpty());
    btn_edit_PlayField.setDisable(g.isEmpty());
    btn_edit_Wheel.setDisable(g.isEmpty());

    //FrontendMediaRepresentation frontendMedia = new FrontendMediaRepresentation();
    if (g.isPresent()) {
      CompletableFuture<FrontendMediaRepresentation> f1 = 
        CompletableFuture.supplyAsync(() -> client.getFrontendService().getFrontendMedia(g.get().getId()));
      CompletableFuture<DirectB2SData> f2 = 
        CompletableFuture.supplyAsync(() -> client.getBackglassServiceClient().getDirectB2SData(g.get().getId()));
      CompletableFuture<CardSettings> f3 = 
        CompletableFuture.supplyAsync(() -> client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class));

      CompletableFuture.allOf(f1, f2, f3).thenRunAsync(() -> {
        try {
          FrontendMediaRepresentation frontendMedia = f1.get();
          DirectB2SData directB2SData = f2.get();
          CardSettings cardSettings = f3.get();

          VPinScreen cardScreen = null;
          if (!StringUtils.isEmpty(cardSettings.getPopperScreen())) {
            cardScreen = VPinScreen.valueOf(cardSettings.getPopperScreen());
          }

          btn_view_Topper.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Topper).isEmpty());
          btn_view_Menu.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Menu).isEmpty());
          btn_view_BackGlass.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.BackGlass).isEmpty());
          btn_view_Loading.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Loading).isEmpty());
          btn_view_GameInfo.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.GameInfo).isEmpty());
          btn_view_DMD.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.DMD).isEmpty());
          btn_view_Other2.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Other2).isEmpty());
          btn_view_GameHelp.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.GameHelp).isEmpty());
          btn_view_PlayField.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.PlayField).isEmpty());
          btn_view_Wheel.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Wheel).isEmpty());
      
          btn_delete_Topper.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Topper).isEmpty());
          btn_delete_Menu.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Menu).isEmpty());
          btn_delete_BackGlass.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.BackGlass).isEmpty());
          btn_delete_Loading.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Loading).isEmpty());
          btn_delete_GameInfo.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.GameInfo).isEmpty());
          btn_delete_DMD.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.DMD).isEmpty());
          btn_delete_Other2.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Other2).isEmpty());
          btn_delete_GameHelp.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.GameHelp).isEmpty());
          btn_delete_PlayField.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.PlayField).isEmpty());
          btn_delete_Wheel.setDisable(g.isEmpty() || frontendMedia.getMediaItems(VPinScreen.Wheel).isEmpty());

          boolean directb2sAvailable = g.isPresent() && g.get().getDirectB2SPath() != null;
          btn_dmdPos_DMD.setDisable(g.isEmpty() || !directb2sAvailable);
          btn_dmdPos_Apron.setDisable(g.isEmpty() || !directb2sAvailable);
          btn_dmdPos_Backglass.setDisable(g.isEmpty() || !directb2sAvailable);

          btn_edit_Audio.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.Audio).size()));
          btn_edit_AudioLaunch.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.AudioLaunch).size()));
          btn_edit_Topper.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.Topper).size()));
//          btn_edit_Menu.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.Menu).size()));//TODO mpf
          btn_edit_BackGlass.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.BackGlass).size()));
          btn_edit_Loading.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.Loading).size()));
          btn_edit_GameInfo.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.GameInfo).size()));
          btn_edit_DMD.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.DMD).size()));
          btn_edit_Other2.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.Other2).size()));
          btn_edit_GameHelp.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.GameHelp).size()));
          btn_edit_PlayField.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.PlayField).size()));
          btn_edit_Wheel.setText(String.valueOf(frontendMedia.getMediaItems(VPinScreen.Wheel).size()));  

          refreshMedia(frontendMedia, cardScreen, preview, directB2SData);
        }
        catch (Exception e) {
          
        }
      }, Platform::runLater)
      .exceptionally(e -> {
        return null;
      });
    }
    else {
      btn_edit_Audio.setText(" ");
      btn_edit_AudioLaunch.setText(" ");
      btn_edit_Topper.setText(" ");
//      btn_edit_Menu.setText("");//TODO
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

  public void refreshMedia(FrontendMediaRepresentation gameMedia, VPinScreen cardScreen, boolean preview, DirectB2SData directB2SData) {
    Platform.runLater(() -> {
      VPinScreen[] values = VPinScreen.values();
      for (VPinScreen screen : values) {
        BorderPane mediaRoot = this.getScreenBorderPaneFor(screen);
        mediaRoot.getStyleClass().remove("highscore-screen");
        Tooltip.uninstall(mediaRoot, highscoreCardTooltip);
        if (cardScreen != null && cardScreen.equals(screen)) {
          mediaRoot.getStyleClass().add("highscore-screen");
          Tooltip.install(mediaRoot, highscoreCardTooltip);
        }

        FrontendMediaItemRepresentation item = gameMedia.getDefaultMediaItem(screen);
        WidgetFactory.createMediaContainer(client, mediaRoot, item, preview);

        Node center = mediaRoot.getCenter();
        if (screen.equals(VPinScreen.BackGlass) && center instanceof Label && directB2SData != null && directB2SData.isBackgroundAvailable()) {
          VBox box = new VBox(3);
          box.setAlignment(Pos.CENTER);
          box.getChildren().add(center);
          Label l = new Label("Backglass available");
          l.setStyle(WidgetFactory.MEDIA_CONTAINER_LABEL);
          box.getChildren().add(l);
          mediaRoot.setCenter(box);
        }

        if (screen.equals(VPinScreen.Menu) && center instanceof Label && directB2SData != null && directB2SData.isDmdImageAvailable()) {
          VBox box = new VBox(3);
          box.setAlignment(Pos.CENTER);
          box.getChildren().add(center);
          Label l = new Label("FullDMD available");
          l.setStyle(WidgetFactory.MEDIA_CONTAINER_LABEL);
          box.getChildren().add(l);
          mediaRoot.setCenter(box);
        }
      }
    });
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;

    FileDragEventHandler.install(mediaRootPane, screenAudio, false, "mp3")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.Audio, "mp3"));

    FileDragEventHandler.install(mediaRootPane, screenAudioLaunch, false, "mp3")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.AudioLaunch, "mp3"));

    FileDragEventHandler.install(mediaRootPane, screenTopper, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.Topper, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenLoading, false, "mp4")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.Loading, "mp4"));

    FileDragEventHandler.install(mediaRootPane, screenPlayField, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.PlayField, "mp4"));

    FileDragEventHandler.install(mediaRootPane, screenBackGlass, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.BackGlass, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenGameInfo, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.GameInfo, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenGameHelp, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.GameHelp, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenMenu, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.Menu, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenDMD, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.DMD, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenOther2, false, "mp4", "png", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.Other2, "mp4", "png", "jpg"));

    FileDragEventHandler.install(mediaRootPane, screenWheel, false, "png", "apng", "jpg")
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), VPinScreen.Wheel, "png", "apng", "jpg"));
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

  private BorderPane getScreenBorderPaneFor(VPinScreen value) {
    BorderPane lookup = (BorderPane) mediaRootPane.lookup("#screen" + value.name());
    if (lookup == null) {
      throw new UnsupportedOperationException("No screen found for id 'screen" + value.name() + "'");
    }
    return lookup;
  }

  private void disposeMediaPane(BorderPane parent) {
    WidgetFactory.disposeMediaPane(parent);
    WidgetFactory.createNoMediaLabel(parent);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    List<VPinScreen> supportedScreens = frontend.getSupportedScreens();

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

    boolean isOpenFolderSupported = SystemUtil.isFolderActionSupported();
    btn_Audio.setVisible(isOpenFolderSupported);
    btn_AudioLaunch.setVisible(isOpenFolderSupported);
    btn_Topper.setVisible(isOpenFolderSupported);
    btn_Loading.setVisible(isOpenFolderSupported);
    btn_PlayField.setVisible(isOpenFolderSupported);
    btn_BackGlass.setVisible(isOpenFolderSupported);
    btn_GameInfo.setVisible(isOpenFolderSupported);
    btn_GameHelp.setVisible(isOpenFolderSupported);
    btn_Menu.setVisible(isOpenFolderSupported);
    btn_DMD.setVisible(isOpenFolderSupported);
    btn_Other2.setVisible(isOpenFolderSupported);
    btn_Wheel.setVisible(isOpenFolderSupported);


    Predicate showPredicate = o -> tablesSidebarController.getTableOverviewController().getSelection() != null;

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

    screenAudio.setVisible(supportedScreens.contains(VPinScreen.Audio));
    screenAudioLaunch.setVisible(supportedScreens.contains(VPinScreen.AudioLaunch));
    screenDMD.setVisible(supportedScreens.contains(VPinScreen.DMD));
    screenBackGlass.setVisible(supportedScreens.contains(VPinScreen.BackGlass));
    screenMenu.setVisible(supportedScreens.contains(VPinScreen.Menu));
    screenGameInfo.setVisible(supportedScreens.contains(VPinScreen.GameInfo));
    screenGameHelp.setVisible(supportedScreens.contains(VPinScreen.GameHelp));
    screenLoading.setVisible(supportedScreens.contains(VPinScreen.Loading));
    screenBackGlass.setVisible(supportedScreens.contains(VPinScreen.BackGlass));
    screenPlayField.setVisible(supportedScreens.contains(VPinScreen.PlayField));
    screenTopper.setVisible(supportedScreens.contains(VPinScreen.Topper));
    screenOther2.setVisible(supportedScreens.contains(VPinScreen.Other2));
    screenWheel.setVisible(supportedScreens.contains(VPinScreen.Wheel));

    Studio.stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        List<MediaView> mediaViews = JFXHelper.getMediaPlayers(mediaRoot);
        for (MediaView mediaView : mediaViews) {
          MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
          if (newValue) {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
              mediaPlayer.play();
            }
          }
          else {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
              mediaPlayer.pause();
            }
          }
        }
      }
      catch (Exception e) {
        LOG.error("Failed to update focus state of media players: " + e.getMessage());
      }
    });
  }
}
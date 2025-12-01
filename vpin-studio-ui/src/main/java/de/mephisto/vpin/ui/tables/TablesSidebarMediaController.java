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
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerPane;
import de.mephisto.vpin.ui.tables.drophandler.TableMediaFileDropEventHandler;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.JFXHelper;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.util.VisibilityHoverListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarMediaController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private TableAssetManagerPane<TablesSidebarMediaItemPane> mediaRootPane;


  private final Tooltip highscoreCardTooltip = new Tooltip("Highscore cards are generated for this screen.");

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarMediaController() {
  }

  public void onDMDPosition() {
    if (game.isPresent()) {
      TableDialogs.openDMDPositionDialog(game.get(), tablesSidebarController.getTableOverviewController());
    }
  }

  public void onMediaEdit(VPinScreen vPinScreen) {
    if (game.isPresent()) {
      TableDialogs.openTableAssetsDialog(tablesSidebarController.getTableOverviewController(), game.get(), vPinScreen);
    }
  }

  public void onMediaFolderOpenClick(VPinScreen vpinScreen) {
    GameRepresentation selection = tablesSidebarController.getTableOverviewController().getSelection();
    if (selection != null) {
      File screendir = client.getFrontendService().getMediaDirectory(selection.getId(), vpinScreen);
      SystemUtil.openFolder(screendir);
    }
  }

  public void onMediaViewClick(VPinScreen vPinScreen) {
    GameRepresentation gameRepresentation = game.get();

    FrontendMediaItemRepresentation defaultMediaItem = client.getFrontendService().getDefaultFrontendMediaItem(
        gameRepresentation.getId(), vPinScreen);
    if (defaultMediaItem != null) {
      TableDialogs.openMediaDialog(Studio.stage, defaultMediaItem);
    }
  }

  public void onMediaDeleteClick(VPinScreen vPinScreen) {
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

    for (TablesSidebarMediaItemPane pane : mediaRootPane.getMediaPanes()) {
      pane.btn_edit.setDisable(g.isEmpty());
    }

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
          boolean directb2sAvailable = g.isPresent() && g.get().getDirectB2SPath() != null;

          CardSettings cardSettings = f3.get();
          VPinScreen cardScreen = null;
          if (!StringUtils.isEmpty(cardSettings.getPopperScreen())) {
            cardScreen = VPinScreen.valueOf(cardSettings.getPopperScreen());
          }

          for (TablesSidebarMediaItemPane pane : mediaRootPane.getMediaPanes()) {
            pane.btn_view.setDisable(g.isEmpty() || frontendMedia.getMediaItems(pane.getScreen()).isEmpty());
            pane.btn_delete.setDisable(g.isEmpty() || frontendMedia.getMediaItems(pane.getScreen()).isEmpty());

            if (pane.hasDmdPos()) {
              pane.btn_dmdPos.setDisable(g.isEmpty() || !directb2sAvailable);
            }

            pane.btn_edit.setText(String.valueOf(frontendMedia.getMediaItems(pane.getScreen()).size()));
          }

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
      for (TablesSidebarMediaItemPane pane : mediaRootPane.getMediaPanes()) {
        pane.btn_edit.setText(" ");
      }
      resetMedia();
    }
  }

  public void refreshMedia(FrontendMediaRepresentation gameMedia, VPinScreen cardScreen, boolean preview, DirectB2SData directB2SData) {
    Platform.runLater(() -> {
      for (TablesSidebarMediaItemPane mediaRoot : mediaRootPane.getMediaPanes()) {
        VPinScreen screen = mediaRoot.getScreen();
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

    for (TablesSidebarMediaItemPane pane : mediaRootPane.getMediaPanes()) {
      FileDragEventHandler.install(mediaRootPane, pane, false, pane.getSuffixes())
        .setOnDragDropped(new TableMediaFileDropEventHandler(tablesSidebarController.getTableOverviewController(), pane.getScreen(), pane.getSuffixes()));
    }
  }

  public void resetMedia() {
    for (TablesSidebarMediaItemPane pane : mediaRootPane.getMediaPanes()) {
      WidgetFactory.disposeMediaPane(pane);
      WidgetFactory.createNoMediaLabel(pane);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    List<VPinScreen> supportedScreens = frontend.getSupportedScreens();

    mediaRootPane.createPanes((rootPane, text, screen, suffixes) -> new TablesSidebarMediaItemPane(this, rootPane, text, screen, suffixes), false);

    boolean isOpenFolderSupported = SystemUtil.isFolderActionSupported();
    Predicate<Boolean> showPredicate = o -> tablesSidebarController.getTableOverviewController().getSelection() != null;

    for (TablesSidebarMediaItemPane pane : mediaRootPane.getMediaPanes()) {
      pane.top.setVisible(false);
      pane.btn_openfolder.setVisible(isOpenFolderSupported);

      pane.hoverProperty().addListener(new VisibilityHoverListener(pane.top, showPredicate));

      pane.setVisible(supportedScreens.contains(pane.getScreen()));
    }

    Studio.stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        List<MediaView> mediaViews = JFXHelper.getMediaPlayers(mediaRootPane);
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
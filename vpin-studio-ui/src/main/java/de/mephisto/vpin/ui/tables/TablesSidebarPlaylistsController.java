package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.playlistmanager.PlaylistDialogs;
import de.mephisto.vpin.ui.playlistmanager.PlaylistUpdateProgressModel;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TablesSidebarPlaylistsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPlaylistsController.class);
  public static final int FAV_PADDING_LEFT = 118;
  public static final int FAV_WIDTH = 392;

  @FXML
  private VBox parentBox;

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private Node dataRoot;

  @FXML
  private Hyperlink dismissLink;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private Button playlistManagerBtn;

  @FXML
  private ToolBar toolbar;

  @FXML
  private Label dialogTitleLabel;

  @FXML
  private Separator playlistManagerSeparator;

  private List<GameRepresentation> games = new ArrayList<>();

  private TableOverviewController tableOverviewController;

  private boolean dialogMode = false;

  // Add a public no-args constructor
  public TablesSidebarPlaylistsController() {
  }

  @FXML
  private void onDismiss() {
  }

  @FXML
  private void onPlaylistManager() {
    PlaylistDialogs.openPlaylistManager(tableOverviewController, null);
  }

  @FXML
  private void onMediaEdit() {
    if (this.games.size() == 1) {
      List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();
      if (!playlists.isEmpty()) {
        PlaylistRepresentation playlistRepresentation = playlists.get(0);
        TableDialogs.openTableAssetsDialog(tableOverviewController, this.games.get(0), playlistRepresentation, VPinScreen.Wheel);
      }
    }
  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
    this.refreshView(games);
  }

  public void setDialogMode() {
    this.dialogMode = true;
    this.dialogTitleLabel.setVisible(true);
    this.toolbar.setVisible(false);
  }

  public void refreshView() {
    refreshView(games);
  }
  public void refreshView(List<GameRepresentation> games) {
    dataBox.getChildren().removeAll(dataBox.getChildren());

    assetManagerBtn.setDisable(games.size() != 1);

    emptyDataBox.setVisible(true);
    dataRoot.setVisible(true);
    errorBox.setVisible(false);

    List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();

    emptyDataBox.setVisible(games.isEmpty());
    dataBox.setVisible(!games.isEmpty());
    dataRoot.setVisible(!games.isEmpty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    if (!frontendType.supportPlaylists()) {
      parentBox.getChildren().remove(dataRoot);
      return;
    }

    this.errorBox.setVisible(false);
    if (!games.isEmpty()) {
      Frontend frontend = client.getFrontendService().getFrontendCached();

      boolean locked = client.getFrontendService().isFrontendRunning();
      if (locked) {
        emptyDataBox.setVisible(false);
        dataRoot.setVisible(false);
        errorBox.setVisible(true);
        errorTitle.setText("The database is currently locked.");
        errorText.setText("Exit [Frontend] to modify playlists.");
        FrontendUtil.replaceName(errorText, frontend);
        return;
      }

      for (PlaylistRepresentation playlist : playlists) {
        boolean linkedToEmu = playlist.getEmulatorId() == null || (this.games.size() == 1 && playlist.getEmulatorId() == games.get(0).getEmulatorId());

        HBox root = new HBox();
        root.setAlignment(Pos.BASELINE_LEFT);
        root.setSpacing(3);
        CheckBox gameCheckbox = new CheckBox(playlist.getName());
        gameCheckbox.getStyleClass().add("default-text");
        gameCheckbox.setPrefWidth(370);
        gameCheckbox.setUserData(playlist);
        gameCheckbox.setSelected(isFullSelectionInPlaylist(playlist));
        boolean disabled = !linkedToEmu || !isPlaylistSelectable(playlist);
        gameCheckbox.setDisable(disabled);
        gameCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

        HBox favLists = new HBox(12);
        favLists.setPadding(new Insets(0, 0, 0, 49));
        gameCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
            try {
              String title = "Removing " + games.size() + " games from \"" + playlist.getName() + "\"";
              if (games.size() == 1) {
                title = "Removing \"" + games.get(0).getGameDisplayName() + "\" from \"" + playlist.getName() + "\"";
              }

              if (t1) {
                title = "Adding " + games.size() + " games to \"" + playlist.getName() + "\"";
                if (games.size() == 1) {
                  title = "Adding \"" + games.get(0).getGameDisplayName() + "\" to \"" + playlist.getName() + "\"";
                }
              }

              ProgressDialog.createProgressDialog(new PlaylistUpdateProgressModel(title, playlist, games, t1));
              refreshPlaylist(client.getPlaylistsService().getPlaylist(playlist.getId()), false);
            }
            catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });

        Label playlistIcon = WidgetFactory.createPlaylistIcon(playlist, uiSettings);
        Tooltip playlistTooltip = TableOverviewController.createPlaylistTooltip(playlist, playlistIcon);
        playlistIcon.setTooltip(playlistTooltip);
        if (frontendType.supportPlaylistsCrud() && isEditablePlaylist(playlist) && !dialogMode) {
          Button plyButton = new Button();
          plyButton.setGraphic(playlistIcon.getGraphic());
          plyButton.setTooltip(playlistTooltip);
          plyButton.getStyleClass().add("ghost-button-tiny");
          plyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              PlaylistDialogs.openPlaylistManager(tableOverviewController, playlist);
            }
          });
          root.getChildren().add(plyButton);
        }
        else {
          root.getChildren().add(playlistIcon);
        }

        String tooltip = null;
        FontIcon icon = null;
        if (playlist.isSqlPlayList()) {
          tooltip = "SQL Playlist";
          icon = WidgetFactory.createIcon("mdi2d-database-search-outline");
        }
        else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_FAVORITE_ID) {
          tooltip = "Favorite";
          icon = WidgetFactory.createIcon("mdi2d-database-search-outline");
        }
        else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_GLOBALFAV_ID) {
          tooltip = "Global Favorite";
          icon = WidgetFactory.createIcon("mdi2d-database-search-outline");
        }
        else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_JUSTADDED_ID) {
          tooltip = "Just Added";
          icon = WidgetFactory.createIcon("mdi2d-database-search-outline");
        }
        else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_MOSTPLAYED_ID) {
          tooltip = "Most Played";
          icon = WidgetFactory.createIcon("mdi2d-database-search-outline");
        }
        else {
          tooltip = "Curated Playlist";
          icon = WidgetFactory.createIcon("mdi2f-format-list-checkbox");
        }
        Label playListTypeIcon = new Label(null, icon);
        playListTypeIcon.setTooltip(new Tooltip(tooltip));
        root.getChildren().add(playListTypeIcon);

        root.getChildren().add(gameCheckbox);

        if (linkedToEmu) {
          ColorPicker colorPicker = new ColorPicker(Color.web(WidgetFactory.hexColor(playlist.getMenuColor())));
          colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
              try {
                String colorhex = PreferenceBindingUtil.toHexString(t1);
                if (colorhex.startsWith("#")) {
                  colorhex = colorhex.substring(1);
                }
                playlist.setMenuColor((int) Long.parseLong(colorhex, 16));
                PlaylistRepresentation update = client.getPlaylistsService().savePlaylist(playlist);
                if (update == null) {
                  LOG.error("Saving playlist failed, check server logs.");
                }
                //client.getPlaylistsService().clearCache();
                refreshPlaylist(update, true);
              }
              catch (Exception e) {
                LOG.error("Failed to update playlists: " + e.getMessage(), e);
                WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
              }
            }
          });
          root.getChildren().add(colorPicker);
        }

        VBox entry = new VBox(3);
        entry.getChildren().add(root);

        if (!favLists.getChildren().isEmpty()) {
          entry.getChildren().add(favLists);
        }
        dataBox.getChildren().add(entry);
      }
    }
  }

  private boolean isEditablePlaylist(PlaylistRepresentation playlist) {
    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (frontendType.equals(FrontendType.Popper)) {
      if (playlist.getId() == PlaylistRepresentation.PLAYLIST_FAVORITE_ID ||
          playlist.getId() == PlaylistRepresentation.PLAYLIST_GLOBALFAV_ID
      ) {
        return false;
      }
    }
    return true;
  }

  private boolean isPlaylistSelectable(PlaylistRepresentation playlist) {
    if (playlist.isSqlPlayList()) {
      return false;
    }
    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (frontendType.equals(FrontendType.Popper)) {
      if (playlist.getId() < 0) {
        return false;
      }
    }
    else if (frontendType.equals(FrontendType.PinballX)) {
      if (playlist.getId() < 0) {
        return false;
      }
    }
    else if (frontendType.equals(FrontendType.PinballY)) {
      if (playlist.getId() == PlaylistRepresentation.PLAYLIST_FAVORITE_ID) {
        return true;
      }

      if (playlist.getId() < 0) {
        return false;
      }
    }

    return true;
  }

  private boolean isFullSelectionInPlaylist(PlaylistRepresentation playlist) {
    List<PlaylistGame> playlistGames = playlist.getGames();

    for (GameRepresentation game : games) {
      int id = game.getId();
      if (!playlistGames.stream().anyMatch(g -> g.getId() == id)) {
        return false;
      }
    }
    return true;
  }

  public void refreshPlaylist(PlaylistRepresentation playlist, boolean updateAll) {
    client.getPreferenceService().clearCache(PreferenceNames.UI_SETTINGS);
    tableOverviewController.updatePlaylist(playlist);

    if (updateAll) {
      List<PlaylistGame> games = playlist.getGames();
      if (games.size() > UIDefaults.DEFAULT_MAX_REFRESH_COUNT) {
        EventManager.getInstance().notifyTablesChanged();
      }
      else {
        for (PlaylistGame playlistGame : games) {
          EventManager.getInstance().notifyTableChange(playlistGame.getId(), null);
        }
      }
    }
    // also update the game if it has not been updated previously
    if (!this.games.isEmpty()) {
      if (games.size() > UIDefaults.DEFAULT_MAX_REFRESH_COUNT) {
        EventManager.getInstance().notifyTablesChanged();
      }
      else {
        for (GameRepresentation game : this.games) {
          int gameId = game.getId();
          EventManager.getInstance().notifyTableChange(gameId, null);
        }
      }
    }
  }

  public void setTableOverviewController(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistManagerBtn.managedProperty().bindBidirectional(playlistManagerBtn.visibleProperty());
    playlistManagerSeparator.managedProperty().bindBidirectional(playlistManagerSeparator.visibleProperty());
    toolbar.managedProperty().bindBidirectional(toolbar.visibleProperty());
    dialogTitleLabel.managedProperty().bindBidirectional(dialogTitleLabel.visibleProperty());
    dialogTitleLabel.setVisible(false);

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    playlistManagerBtn.setVisible(frontendType.supportPlaylistsCrud() && Features.PLAYLIST_MANAGER);
    playlistManagerSeparator.setVisible(frontendType.supportPlaylistsCrud() && Features.PLAYLIST_MANAGER);

    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);

    dismissLink.setVisible(false);
  }
}
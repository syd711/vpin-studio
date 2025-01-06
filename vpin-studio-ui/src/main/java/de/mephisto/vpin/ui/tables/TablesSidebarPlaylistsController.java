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
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
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
  private Separator playlistManagerSeparator;

  private List<GameRepresentation> games = new ArrayList<>();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarPlaylistsController() {
  }

  @FXML
  private void onDismiss() {
  }

  @FXML
  private void onPlaylistManager() {
    PlaylistDialogs.openPlaylistManager(tablesSidebarController.getTableOverviewController(), null);
  }

  @FXML
  private void onMediaEdit() {
    if (this.games.size() == 1) {
      List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();
      if (!playlists.isEmpty()) {
        PlaylistRepresentation playlistRepresentation = playlists.get(0);
        TableDialogs.openTableAssetsDialog(this.tablesSidebarController.getTableOverviewController(), this.games.get(0), playlistRepresentation, VPinScreen.Wheel);
      }
    }
  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
    this.refreshView(games);
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

    if (frontendType.supportPlaylists()) {
      HBox localFavsRoot = new HBox();
      localFavsRoot.setAlignment(Pos.BASELINE_LEFT);
      localFavsRoot.setSpacing(3);
      ColorPicker colorPicker = new ColorPicker(Color.web(uiSettings.getLocalFavsColor()));
      colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
        try {
          String hexValue = t1 != null ? PreferenceBindingUtil.toHexString(t1) : WidgetFactory.LOCAL_FAVS_COLOR;
          uiSettings.setLocalFavsColor(hexValue);
          client.getPreferenceService().setJsonPreference(uiSettings);
          tablesSidebarController.getTablesController().refreshPlaylists();
          EventManager.getInstance().notifyTablesChanged();
        }
        catch (Exception e) {
          LOG.error("Failed to update playlists: " + e.getMessage(), e);
          WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
        }
      });
      Label name = new Label("Local Favorites");
      name.setPadding(new Insets(0, 0, 3, FAV_PADDING_LEFT));
      name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
      name.setPrefWidth(FAV_WIDTH);

      Label playlistIcon = WidgetFactory.createLocalFavoritePlaylistIcon(PreferenceBindingUtil.toHexString(colorPicker.getValue()));
      localFavsRoot.getChildren().add(playlistIcon);
      localFavsRoot.getChildren().add(name);
      localFavsRoot.getChildren().add(colorPicker);
      dataBox.getChildren().add(localFavsRoot);


      HBox globalFavsRoot = new HBox();
      globalFavsRoot.setAlignment(Pos.BASELINE_LEFT);
      globalFavsRoot.setSpacing(3);
      colorPicker = new ColorPicker(Color.web(uiSettings.getGlobalFavsColor()));
      colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
        try {
          String hexValue = t1 != null ? PreferenceBindingUtil.toHexString(t1) : WidgetFactory.GLOBAL_FAVS_COLOR;
          uiSettings.setGlobalFavsColor(hexValue);
          client.getPreferenceService().setJsonPreference(uiSettings);
          tablesSidebarController.getTablesController().refreshPlaylists();
          EventManager.getInstance().notifyTablesChanged();
        }
        catch (Exception e) {
          LOG.error("Failed to update playlists: " + e.getMessage(), e);
          WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
        }
      });
      name = new Label("Global Favorites");
      name.setPadding(new Insets(0, 0, 24, FAV_PADDING_LEFT));
      name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
      name.setPrefWidth(FAV_WIDTH);

      playlistIcon = WidgetFactory.createGlobalFavoritePlaylistIcon(PreferenceBindingUtil.toHexString(colorPicker.getValue()));
      globalFavsRoot.getChildren().add(playlistIcon);
      globalFavsRoot.getChildren().add(name);
      globalFavsRoot.getChildren().add(colorPicker);
      dataBox.getChildren().add(globalFavsRoot);

      dataBox.getChildren().add(new Label(" "));
    }
    else {
      parentBox.getChildren().remove(dataRoot);
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
        if (playlist.getId() < 0) {
          continue;
        }

        boolean linkedToEmu = playlist.getEmulatorId() == null || (this.games.size() == 1 && playlist.getEmulatorId() == games.get(0).getEmulatorId());

        HBox root = new HBox();
        root.setAlignment(Pos.BASELINE_LEFT);
        root.setSpacing(3);
        CheckBox gameCheckbox = new CheckBox(playlist.getName());
        gameCheckbox.getStyleClass().add("default-text");
        gameCheckbox.setPrefWidth(370);
        gameCheckbox.setUserData(playlist);
        gameCheckbox.setSelected(isFullSelectionInPlaylist(playlist));
        boolean disabled = playlist.isSqlPlayList() || !linkedToEmu;
        gameCheckbox.setDisable(disabled);
        gameCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

        boolean wasPlayed = games.size() == 1 && playlist.wasPlayed(games.get(0).getId());

        HBox favLists = new HBox(12);
        favLists.setPadding(new Insets(0, 0, 0, 49));
        boolean addFavCheckboxes = this.games.size() == 1 && playlist.getId() != 0 && playlist.containsGame(games.get(0).getId()) && playlist.isAddFavCheckboxes();
        boolean fav = this.games.size() == 1 && playlist.isFavGame(games.get(0).getId());
        boolean globalFav = this.games.size() == 1 && playlist.isGlobalFavGame(games.get(0).getId());

        if (addFavCheckboxes && frontendType.supportExtendedPlaylists() && linkedToEmu) {
          GameRepresentation game = games.get(0);

          CheckBox favCheckbox = new CheckBox();
          favCheckbox.setText("Local Favorite");
          favCheckbox.setUserData(playlist);
          favCheckbox.setDisable(!playlist.containsGame(game.getId()) || !wasPlayed);
          if (favCheckbox.isDisabled()) {
            favCheckbox.setTooltip(new Tooltip("Playlist does not contain this game of wasn't played yet."));
          }
          favCheckbox.setSelected(playlist.isFavGame(game.getId()));
          favCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");
          favCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
              try {
                playlist.getGame(game.getId()).setFav(t1);

                if (t1) {
                  client.getPlaylistsService().updatePlaylistGame(playlist, game, t1, false);
                }
                else {
                  client.getPlaylistsService().updatePlaylistGame(playlist, game, t1, playlist.isGlobalFavGame(game.getId()));
                }
                refreshPlaylist(playlist, false);
              }
              catch (Exception e) {
                LOG.error("Failed to update playlists: " + e.getMessage(), e);
                WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
              }
            }
          });
          if (!wasPlayed) {
            favCheckbox.setTooltip(new Tooltip("The game needs to be played once."));
          }

          favLists.getChildren().add(favCheckbox);

          CheckBox globalFavCheckbox = new CheckBox();
          globalFavCheckbox.setText("Global Favorite");
          globalFavCheckbox.setUserData(playlist);
          globalFavCheckbox.setDisable(!playlist.containsGame(game.getId()) || !wasPlayed);
          globalFavCheckbox.setSelected(playlist.isGlobalFavGame(game.getId()));
          globalFavCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");
          globalFavCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
              try {
                playlist.getGame(game.getId()).setGlobalFav(t1);
                if (t1) {
                  client.getPlaylistsService().updatePlaylistGame(playlist, game, false, t1);
                }
                else {
                  client.getPlaylistsService().updatePlaylistGame(playlist, game, playlist.isFavGame(game.getId()), t1);
                }
                refreshPlaylist(playlist, false);
              }
              catch (Exception e) {
                LOG.error("Failed to update playlists: " + e.getMessage(), e);
                WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
              }
            }
          });

          if (!wasPlayed) {
            globalFavCheckbox.setTooltip(new Tooltip("The game needs to be played once."));
          }
          favLists.getChildren().add(globalFavCheckbox);
        }

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
        Button plyButton = new Button();
        plyButton.setGraphic(playlistIcon.getGraphic());
        plyButton.getStyleClass().add("ghost-button-tiny");
        plyButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            PlaylistDialogs.openPlaylistManager(tablesSidebarController.getTableOverviewController(), playlist);
          }
        });
        root.getChildren().add(plyButton);

        String tooltip = null;
        FontIcon icon = null;
        if (playlist.isSqlPlayList()) {
          tooltip = "SQL Playlist";
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

        if (playlist.getId() >= 0 && linkedToEmu) {
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
                client.getPlaylistsService().clearCache();
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

  private void refreshPlaylist(PlaylistRepresentation playlist, boolean updateAll) {
    tablesSidebarController.getTableOverviewController().updatePlaylist(playlist);

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

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistManagerBtn.managedProperty().bindBidirectional(playlistManagerBtn.visibleProperty());
    playlistManagerSeparator.managedProperty().bindBidirectional(playlistManagerSeparator.visibleProperty());

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
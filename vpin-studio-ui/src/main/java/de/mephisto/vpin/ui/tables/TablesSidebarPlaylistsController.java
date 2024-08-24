package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TablesSidebarPlaylistsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPlaylistsController.class);

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

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarPlaylistsController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);

    dismissLink.setVisible(false);
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
//    DismissalUtil..dismissValidation(g, options.getValidationStates().get(0));
  }

  @FXML
  private void onMediaEdit() {
    if (this.game.isPresent()) {
      List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();
      if (!playlists.isEmpty()) {
        PlaylistRepresentation playlistRepresentation = playlists.get(0);
        TableDialogs.openTableAssetsDialog(this.tablesSidebarController.getTableOverviewController(), this.game.get(), playlistRepresentation, VPinScreen.Wheel);
      }
    }
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    dataBox.getChildren().removeAll(dataBox.getChildren());

    emptyDataBox.setVisible(true);
    dataRoot.setVisible(true);
    errorBox.setVisible(false);

    List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();

    emptyDataBox.setVisible(g.isEmpty());
    dataBox.setVisible(g.isPresent());
    dataRoot.setVisible(g.isPresent());

    assetManagerBtn.setDisable(g.isEmpty());

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    HBox localFavsRoot = new HBox();
    localFavsRoot.setAlignment(Pos.BASELINE_LEFT);
    localFavsRoot.setSpacing(3);
    ColorPicker colorPicker = new ColorPicker(Color.web(uiSettings.getLocalFavsColor()));
    colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
      try {
        String hexValue = t1 != null ? PreferenceBindingUtil.toHexString(t1) : WidgetFactory.LOCAL_FAVS_COLOR;
        uiSettings.setLocalFavsColor(hexValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
        tablesSidebarController.getTableOverviewController().refreshPlaylists();
        EventManager.getInstance().notifyTablesChanged();
      }
      catch (Exception e) {
        LOG.error("Failed to update playlists: " + e.getMessage(), e);
        WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
      }
    });
    Label name = new Label("Local Favorites");
    name.setPadding(new Insets(0, 0, 3, 120));
    name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
    name.setPrefWidth(392);

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
        client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
        tablesSidebarController.getTableOverviewController().refreshPlaylists();
        EventManager.getInstance().notifyTablesChanged();
      }
      catch (Exception e) {
        LOG.error("Failed to update playlists: " + e.getMessage(), e);
        WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
      }
    });
    name = new Label("Global Favorites");
    name.setPadding(new Insets(0, 0, 24, 120));
    name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
    name.setPrefWidth(392);

    playlistIcon = WidgetFactory.createGlobalFavoritePlaylistIcon(PreferenceBindingUtil.toHexString(colorPicker.getValue()));
    globalFavsRoot.getChildren().add(playlistIcon);
    globalFavsRoot.getChildren().add(name);
    globalFavsRoot.getChildren().add(colorPicker);
    dataBox.getChildren().add(globalFavsRoot);

    dataBox.getChildren().add(new Label(" "));


    this.errorBox.setVisible(false);
    if (g.isPresent()) {
      GameRepresentation game = g.get();

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
        HBox root = new HBox();
        root.setAlignment(Pos.BASELINE_LEFT);
        root.setSpacing(3);
        CheckBox gameCheckbox = new CheckBox();
        gameCheckbox.setUserData(playlist);
        gameCheckbox.setSelected(playlist.containsGame(game.getId()));
        gameCheckbox.setDisable(playlist.isSqlPlayList());
        gameCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

        boolean wasPlayed = playlist.wasPlayed(game.getId());

        HBox favLists = new HBox(12);
        favLists.setPadding(new Insets(0, 0, 3, 27));
        boolean addFavCheckboxes = playlist.getId() != 0 && playlist.containsGame(game.getId()) && playlist.isSqlPlayList() && playlist.getPlayListSQL() != null && playlist.getPlayListSQL().contains("EMUID");
        boolean fav = playlist.isFavGame(game.getId());
        boolean globalFav = playlist.isGlobalFavGame(game.getId());

        if (addFavCheckboxes) {
          CheckBox favCheckbox = new CheckBox();
          favCheckbox.setText("Local Favorite");
          favCheckbox.setUserData(playlist);
          favCheckbox.setDisable(!playlist.containsGame(game.getId()) || !wasPlayed);
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
              if (t1) {
                PlaylistRepresentation update = client.getPlaylistsService().addToPlaylist(playlist, game, fav, globalFav);
                refreshPlaylist(update, false);
              }
              else {
                PlaylistRepresentation update = client.getPlaylistsService().removeFromPlaylist(playlist, game);
                refreshPlaylist(update, false);
              }
            }
            catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });

        colorPicker = new ColorPicker(Color.web(WidgetFactory.hexColor(playlist.getMenuColor())));
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
          @Override
          public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
            try {
              PlaylistRepresentation update = client.getPlaylistsService().setPlaylistColor(playlist, PreferenceBindingUtil.toHexString(t1));
              refreshPlaylist(update, true);
            }
            catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });
        name = new Label(playlist.getName());
        name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
        name.setPrefWidth(370);

        playlistIcon = WidgetFactory.createPlaylistIcon(playlist, uiSettings);
        root.getChildren().add(playlistIcon);
        root.getChildren().add(gameCheckbox);
        root.getChildren().add(name);
        root.getChildren().add(colorPicker);

        VBox entry = new VBox(3);
        entry.getChildren().add(root);

        if (!favLists.getChildren().isEmpty()) {
          entry.getChildren().add(favLists);
        }

        if (playlist.isSqlPlayList()) {
          Label label = new Label("(SQL Playlist)");

          label.getStyleClass().add("default-text");
          label.setStyle("-fx-font-size: 12px;");
          label.setPadding(new Insets(0, 0, 12, 27));
          entry.getChildren().add(label);
        }
        dataBox.getChildren().add(entry);
      }
    }
  }

  private void refreshPlaylist(PlaylistRepresentation playlist, boolean updateAll) {
    tablesSidebarController.getTableOverviewController().updatePlaylist();

    if (updateAll) {
      List<PlaylistGame> games = playlist.getGames();
      if (games.size() > 5) {
        EventManager.getInstance().notifyTablesChanged();
      }
      else {
        for (PlaylistGame playlistGame : games) {
          EventManager.getInstance().notifyTableChange(playlistGame.getId(), null);
        }
      }
    }
    else {
      if (this.game.isPresent()) {
        EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
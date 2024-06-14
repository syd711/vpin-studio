package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.Playlist;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    dataBox.getChildren().removeAll(dataBox.getChildren());

    emptyDataBox.setVisible(true);
    dataRoot.setVisible(true);
    errorBox.setVisible(false);

    List<Playlist> playlists = client.getPlaylistsService().getPlaylists();

    emptyDataBox.setVisible(g.isEmpty());
    dataBox.setVisible(g.isPresent());

    this.errorBox.setVisible(false);
    if (g.isPresent()) {
      GameRepresentation game = g.get();

      boolean locked = client.getFrontendService().isPinUPPopperRunning();
      if (locked) {
        emptyDataBox.setVisible(false);
        dataRoot.setVisible(false);
        errorBox.setVisible(true);
        errorTitle.setText("The database is currently locked.");
        errorText.setText("Exit PinUP Popper to modify playlists.");
        return;
      }

      for (Playlist playlist : playlists) {
        HBox root = new HBox();
        root.setAlignment(Pos.BASELINE_LEFT);
        root.setSpacing(3);
        CheckBox gameCheckbox = new CheckBox();
        gameCheckbox.setUserData(playlist);
        gameCheckbox.setSelected(playlist.containsGame(game.getId()));
        gameCheckbox.setDisable(playlist.isSqlPlayList());
        gameCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

        CheckBox favCheckbox = new CheckBox();
        favCheckbox.setText("Favorite");
        favCheckbox.setUserData(playlist);
        favCheckbox.setDisable(!playlist.containsGame(game.getId()));
        favCheckbox.setSelected(playlist.isFavGame(game.getId()));
        favCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

        CheckBox globalFavCheckbox = new CheckBox();
        globalFavCheckbox.setText("Global Favorite");
        globalFavCheckbox.setUserData(playlist);
        globalFavCheckbox.setDisable(!playlist.containsGame(game.getId()));
        globalFavCheckbox.setSelected(playlist.isGlobalFavGame(game.getId()));
        globalFavCheckbox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

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

              refreshPlaylist(playlist);
            } catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });

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
              refreshPlaylist(playlist);
            } catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });


        gameCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
            try {
              if (t1) {
                Playlist update = client.getPlaylistsService().addToPlaylist(playlist, game, favCheckbox.isSelected(), globalFavCheckbox.isSelected());
                refreshPlaylist(update);
              }
              else {
                Playlist update = client.getPlaylistsService().removeFromPlaylist(playlist, game);
                refreshPlaylist(update);
              }
            } catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });

        ColorPicker colorPicker = new ColorPicker(Color.web(WidgetFactory.hexColor(playlist.getMenuColor())));
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
          @Override
          public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
            try {
              Playlist update = client.getPlaylistsService().setPlaylistColor(playlist, PreferenceBindingUtil.toHexString(t1));
              refreshPlaylist(update);
            } catch (Exception e) {
              LOG.error("Failed to update playlists: " + e.getMessage(), e);
              WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
            }
          }
        });
        Label name = new Label(playlist.getName());
        name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
        name.setPrefWidth(370);

        Label playlistIcon = WidgetFactory.createPlaylistIcon(playlist);
        root.getChildren().add(playlistIcon);
        root.getChildren().add(gameCheckbox);
        root.getChildren().add(name);
        root.getChildren().add(colorPicker);

        BorderPane entry = new BorderPane();
        entry.setCenter(root);

        HBox favLists = new HBox(6);
        if (playlist.isSqlPlayList()) {
          favLists.setPadding(new Insets(0, 0, 12, 55));
          Label label = new Label("(SQL Playlist)");
          label.getStyleClass().add("default-text");
          label.setStyle("-fx-font-size: 12px;");
          favLists.getChildren().add(label);
        }
        else {
          favLists.setPadding(new Insets(0, 0, 12, 27));
          favLists.getChildren().add(favCheckbox);
          favLists.getChildren().add(globalFavCheckbox);
        }
        entry.setBottom(favLists);
        dataBox.getChildren().add(entry);
      }

    }
  }

  private void refreshPlaylist(Playlist update) {
    tablesSidebarController.getTablesController().updatePlaylist(update);
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
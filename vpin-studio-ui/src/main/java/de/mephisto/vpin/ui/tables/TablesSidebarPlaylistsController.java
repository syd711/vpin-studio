package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
//    tablesSidebarController.getTablesController().dismissValidation(g, options.getValidationStates().get(0));
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    dataBox.getChildren().removeAll(dataBox.getChildren());

    List<PlaylistRepresentation> playlists = this.tablesSidebarController.getTablesController().getPlaylists();

    emptyDataBox.setVisible(g.isEmpty());
    dataBox.setVisible(g.isPresent());

    this.errorBox.setVisible(false);
    if (g.isPresent()) {
      GameRepresentation game = g.get();

      for (PlaylistRepresentation playlist : playlists) {
        HBox root = new HBox();
        root.setStyle("-fx-padding: 3 0 3 0;");
        root.setAlignment(Pos.BASELINE_LEFT);
        root.setSpacing(3);
        CheckBox checkBox = new CheckBox();
        checkBox.setUserData(playlist);
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
            if(t1) {
              Studio.client.getPlaylistsService().addToPlaylist(playlist, game);
            }
            else {
              Studio.client.getPlaylistsService().removeFromPlaylist(playlist, game);
            }
          }
        });
        checkBox.setSelected(playlist.getGameIds().contains(game.getId()));
        checkBox.setDisable(playlist.isSqlPlayList());
        checkBox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

        String hex = "#FFFFFF";
        if (playlist.getMenuColor() != null) {
          if(playlist.getMenuColor() == 0) {
            hex = "#000000";
          }
          else {
            hex = "#" + Integer.toHexString(playlist.getMenuColor());
          }
        }
        ColorPicker colorPicker = new ColorPicker(Color.web(hex));
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
          @Override
          public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
            Studio.client.getPlaylistsService().setPlaylistColor(playlist, BindingUtil.toHexString(t1));
          }
        });

        Label name = new Label(playlist.getName());
        name.setStyle("-fx-font-size: 14px;-fx-text-fill: white;-fx-padding: 0 0 0 6;");
        name.setPrefWidth(370);

        Label playlistIcon = WidgetFactory.createPlaylistIcon(playlist);
        root.getChildren().add(playlistIcon);
        root.getChildren().add(checkBox);
        root.getChildren().add(name);
        root.getChildren().add(colorPicker);

        dataBox.getChildren().add(root);
      }

    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
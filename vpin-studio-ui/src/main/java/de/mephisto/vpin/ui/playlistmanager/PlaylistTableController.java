package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class PlaylistTableController extends BaseTableController<GameRepresentation, GameRepresentationModel> implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistTableController.class);

  @FXML
  private Node root;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnEmulator;

  private Stage stage;

  public void setData(Stage dialogStage, Optional<PlaylistRepresentation> value) {
    if (value.isPresent()) {
      setItems(value.get().getGames().stream().map(this::toGameModel).collect(Collectors.toList()));
    }
  }

  private GameRepresentation toGameModel(PlaylistGame playlistGame) {
    return client.getGameService().getGameCached(playlistGame.getId());
  }

  @FXML
  private void onTableMouseClicked(MouseEvent e) {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("media", "media", new PlaylistTableColumnSorter(this));

    BaseLoadingColumn.configureColumn(columnName, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setTooltip(new Tooltip(model.getName()));
      label.setText(model.getName());
      return label;
    }, true);

    BaseLoadingColumn.configureColumn(columnEmulator, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setText(model.getGameEmulator().getName());
      return label;
    }, true);
  }

  @Override
  protected GameRepresentationModel toModel(GameRepresentation game) {
    GameRepresentationModel gameRepresentationModel = new GameRepresentationModel(game);
    gameRepresentationModel.load();//TODO this should not be the case
    return gameRepresentationModel;
  }
}

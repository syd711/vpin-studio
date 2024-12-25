package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private Button removeBtn;

  private Stage dialogStage;
  private Optional<PlaylistRepresentation> playlist;
  private Stage stage;


  @FXML
  private void onRemove() {
    if (playlist.isPresent()) {
      List<GameRepresentationModel> gameModels = this.tableView.getSelectionModel().getSelectedItems();
      List<GameRepresentation> games = gameModels.stream().map(model -> model.getBean()).collect(Collectors.toList());
      String title = "Removing " + games.size() + " games from \"" + playlist.get().getName() + "\"";
      if (games.size() == 1) {
        title = "Removing \"" + games.get(0).getGameDisplayName() + "\" from \"" + playlist.get().getName() + "\"";
      }
      ProgressDialog.createProgressDialog(new PlaylistUpdateProgressModel(title, playlist.get(), games, false));

      setData(playlist);
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent e) {

  }

  public void setStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void setData(Optional<PlaylistRepresentation> value) {
    if (value.isPresent()) {
      this.playlist = Optional.of(client.getPlaylistsService().getPlaylist(value.get().getId()));
      setItems(playlist.get().getGames().stream().map(this::toGameModel).collect(Collectors.toList()));
    }
  }

  private GameRepresentation toGameModel(PlaylistGame playlistGame) {
    return client.getGameService().getGameCached(playlistGame.getId());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("media", "media", new PlaylistTableColumnSorter(this));

    removeBtn.setDisable(true);

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<GameRepresentationModel>() {
      @Override
      public void onChanged(Change<? extends GameRepresentationModel> c) {
        removeBtn.setDisable(c.getList().isEmpty());
      }
    });

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

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    List<GameEmulatorRepresentation> filtered = new ArrayList<>(client.getFrontendService().getFilteredEmulatorsWithAllVpx(uiSettings));
    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
  }

  @Override
  protected GameRepresentationModel toModel(GameRepresentation game) {
    GameRepresentationModel gameRepresentationModel = new GameRepresentationModel(game);
    gameRepresentationModel.load();//TODO this should not be the case
    return gameRepresentationModel;
  }
}

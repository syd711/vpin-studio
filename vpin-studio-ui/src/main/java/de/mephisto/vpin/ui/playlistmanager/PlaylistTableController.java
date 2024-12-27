package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
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
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDateAdded;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDateModified;

  @FXML
  private ComboBox<GameEmulatorRepresentation> allEmulatorsCombo;


  @FXML
  private Button removeBtn;

  private Stage dialogStage;
  private Optional<PlaylistRepresentation> playlist;
  private Stage stage;

  @Override
  protected void onDelete(Event e) {
    onRemove();
  }

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
      refresh();
    }
  }

  private void refresh() {
    Platform.runLater(() -> {
      ProgressDialog.createProgressDialog(new PlaylistLoadingProgressModel(playlist.get()));
      List<PlaylistGame> games = playlist.get().getGames();
      List<GameRepresentation> collect = games.stream().map(this::toGameModel).filter(Objects::nonNull).collect(Collectors.toList());
      setItems(filterItems(collect));
      labelCount.setText(collect.size() + " tables");
    });
  }

  private List<GameRepresentation> filterItems(List<GameRepresentation> collect) {
    List<GameRepresentation> result = new ArrayList<>(collect);
    String term = searchTextField.getText();
    if (!StringUtils.isEmpty(term)) {
      result = result.stream().filter(g -> g.getGameDisplayName().toLowerCase().contains(term.toLowerCase())).collect(Collectors.toList());
    }

    GameEmulatorRepresentation emulator = this.allEmulatorsCombo.getValue();
    if (emulator != null) {
      result = result.stream().filter(g -> g.getEmulatorId() == emulator.getId()).collect(Collectors.toList());
    }

    return result;
  }

  private GameRepresentation toGameModel(PlaylistGame playlistGame) {
    return client.getGameService().getGameCached(playlistGame.getId());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("media", "media", new PlaylistTableColumnSorter(this));

    removeBtn.setDisable(true);

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      clearSelection();
      applyFilter();
      refresh();
      clearBtn.setVisible(filterValue != null && !filterValue.isEmpty());
    });
    searchTextField.setOnKeyPressed(event -> {
      if (event.getCode().toString().equalsIgnoreCase("ESCAPE")) {
        searchTextField.setText("");
        tableView.requestFocus();
        event.consume();
      }
    });


    tableView.setOnKeyPressed(event -> {
      super.onKeyEvent(event);
    });

    tableView.setPlaceholder(new Label("Empty Playlist"));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<GameRepresentationModel>() {
      @Override
      public void onChanged(Change<? extends GameRepresentationModel> c) {
        removeBtn.setDisable(c.getList().isEmpty() || !playlist.isPresent() || playlist.get().isSqlPlayList());
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

    BaseLoadingColumn.configureColumn(columnDateAdded, (value, model) -> {
      Label label = null;
      if (value.getDateAdded() != null) {
        label = new Label(TableOverviewController.dateFormat.format(value.getDateAdded()));
      }
      else {
        label = new Label("-");
      }
      label.getStyleClass().add("default-text");
      return label;
    }, true);

    BaseLoadingColumn.configureColumn(columnDateModified, (value, model) -> {
      Label label = null;
      if (value.getDateAdded() != null) {
        label = new Label(TableOverviewController.dateFormat.format(value.getDateUpdated()));
      }
      else {
        label = new Label("-");
      }
      label.getStyleClass().add("default-text");
      return label;
    }, true);

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    List<GameEmulatorRepresentation> filtered = new ArrayList<>(client.getFrontendService().getFilteredEmulatorsWithEmptyOption(uiSettings));
    this.allEmulatorsCombo.setItems(FXCollections.observableList(filtered));
    this.allEmulatorsCombo.valueProperty().addListener(new ChangeListener<GameEmulatorRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
        refresh();
      }
    });
  }

  @Override
  protected GameRepresentationModel toModel(GameRepresentation game) {
    GameRepresentationModel gameRepresentationModel = new GameRepresentationModel(game);
    gameRepresentationModel.load();//TODO this should not be the case
    return gameRepresentationModel;
  }
}

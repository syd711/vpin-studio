package de.mephisto.vpin.ui.components.doftester;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.client;

public class DOFTesterController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDOFMapping;

  @FXML
  private Button tableEditBtn;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private BorderPane toysEditorPane;

  private DOFTesterController.GameEmulatorChangeListener gameEmulatorChangeListener;

  private DOFToysController dofToysController;

  public DOFTesterController() {
  }

  @FXML
  private void onTableEdit() {
    GameRepresentation selection = getSelection();
    if (selection != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(selection.getId()));
    }
  }

  //------------

  @FXML
  private void onReload() {
    doReload(true);
  }


  public void doReload(boolean force) {
    startReload("Loading Tables...");

    refreshEmulators();
    client.getDofTesterService().clearCache();

    // load in parallel games and templates, it will ensure templates are cached before the columns access them
    JFXFuture.supplyAllAsync(
            () -> client.getHighscoreCardTemplatesClient().getTemplates(),
            () -> {
              if (force) {
                client.getGameService().clearCache();
              }

              List<GameRepresentation> games = new ArrayList<>();
              if (emulatorCombo.getValue() != null) {
                GameEmulatorRepresentation emu = emulatorCombo.getValue();
                games.addAll(client.getGameService().getGamesByEmulator(emu.getId()));
              }
              else {
                games.addAll(client.getGameService().getVpxGamesCached());
                games.addAll(client.getGameService().getFPGamesCached());
              }

              return games;
            }
        )
        .onErrorSupply(e -> {
          Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Loading tables failed: " + e.getMessage()));
          return new Object[]{Collections.emptyList(), Collections.emptyList()};
        })
        .thenAcceptLater(objs -> {
          @SuppressWarnings({"unchecked", "unused"})
          List<CardTemplate> _templates = (List<CardTemplate>) objs[0];
          @SuppressWarnings("unchecked")
          List<GameRepresentation> games = (List<GameRepresentation>) objs[1];

          // keep current selection
          GameRepresentationModel selectedItem = getSelectedModel();

          setItems(games);

          if (games.isEmpty()) {
            tableView.setPlaceholder(new Label("No tables found"));
          }
          toysEditorPane.setVisible(!games.isEmpty());

          tableView.refresh();
          tableView.requestFocus();

          // select the game, it will refresh the view and select associated template
          setSelectionOrFirst(selectedItem);
          endReload();
        });
  }

  @Override
  public void refreshView(GameRepresentationModel model) {
    //Only refresh this if we're actually on that screen
    if (NavigationController.getActiveNavigation().equals(NavigationItem.SystemManager)) {
      GameRepresentation game = model != null ? model.getBean() : null;
      refreshView(game);
    }
  }

  public void refreshView(GameRepresentation game) {
    toysEditorPane.setVisible(game != null);

    List<String> breadcrumb = new ArrayList<>(Arrays.asList("System Manager", "DOF Tester"));
    if (game != null) {
      breadcrumb.add(game.getGameDisplayName());
    }
    NavigationController.setBreadCrumb(breadcrumb);

    dofToysController.selectTable(Optional.ofNullable(game));
    tableView.refresh();
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("System Manager", "DOF Tester"));

    if (options != null && options.getGameId() > 0) {
      GameRepresentationModel selectedItem = tableView.getItems().stream().filter(g -> g.getGameId() == options.getGameId()).findFirst().orElse(null);
      if (selectedItem != null) {
        setSelection(selectedItem, true);
      }
    }

    refreshEmulators();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("DOF Tester ", "DOF Tester", new DOFTesterColumnSorter(this));
    super.loadFilterPanel("scene-dof-tester-filter.fxml");

    NavigationController.setBreadCrumb(Arrays.asList("Designer", "Highscore Cards"));

    try {
      FXMLLoader loader = new FXMLLoader(DOFToysController.class.getResource("dof-toys.fxml"));
      Parent editorRoot = loader.load();
      dofToysController = loader.getController();
      dofToysController.setParentController(this);
      toysEditorPane.setCenter(editorRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load template editor: " + e.getMessage(), e);
    }

    BaseLoadingColumn.configureColumn(columnDisplayName, (value, model) -> {
      Label label = new Label(value.getGameDisplayName());
      label.setTooltip(new Tooltip(value.getGameDisplayName()));
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnDOFMapping, (value, model) -> {
      Label label = new Label();
      label.getStyleClass().add("default-text");
      label.setGraphic(WidgetFactory.createCheckIcon());
      return label;
    }, this, true);

    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      tableEditBtn.setDisable(tableView.getSelectionModel().getSelectedItems().size() != 1);
      refreshView(newSelection);
    });

    EventManager.getInstance().addListener(this);

    this.gameEmulatorChangeListener = new DOFTesterController.GameEmulatorChangeListener();
    doReload(false);
  }


  private void refreshEmulators() {
    this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
    final GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();

    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getEmulatorService().getVpxGameEmulators());
    emulators.addAll(client.getEmulatorService().getFpGameEmulators());

    this.emulatorCombo.setDisable(true);
    this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
    this.emulatorCombo.setItems(FXCollections.observableList(emulators));
    this.emulatorCombo.setDisable(false);

    if (selectedEmu != null) {
      this.emulatorCombo.getSelectionModel().select(selectedEmu);
    }
    GameEmulatorRepresentation newSelection = this.emulatorCombo.getSelectionModel().getSelectedItem();
    if (newSelection == null) {
      this.emulatorCombo.getSelectionModel().selectFirst();
    }
    this.emulatorCombo.valueProperty().addListener(gameEmulatorChangeListener);
  }

  @Override
  public void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {
    JFXFuture.supplyAsync(() -> client.getGameService().getGame(id))
        .thenAcceptLater(refreshedGame -> {
          if (refreshedGame != null) {
            reloadItem(refreshedGame);
          }
          else {
            // detection of deletion for a table
            Optional<GameRepresentationModel> model = models.stream().filter(g -> g.getGameId() == id).findFirst();
            if (model.isPresent()) {
              models.remove(model.get());
            }
          }
        });
  }

  @Override
  public void tablesChanged() {
    doReload(false);
  }

  @Override
  protected GameRepresentationModel toModel(GameRepresentation game) {
    return new GameRepresentationModel(game);
  }


  class GameEmulatorChangeListener implements ChangeListener<GameEmulatorRepresentation> {
    @Override
    public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
      // callback to filter tables, once the data has been reloaded
      Platform.runLater(() -> {
        doReload(false);
      });
    }
  }

}

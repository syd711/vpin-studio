package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.ui.util.ThemeColors;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.cards.panels.TemplateEditorController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.TableDialogs;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class HighscoreCardsController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnBaseTemplate;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private BorderPane templateEditorPane;

  @FXML
  private BorderPane rootPane;

  @FXML
  private BorderPane designerTile;

  private final List<String> ignoreList = new ArrayList<>();


  private TemplateEditorController templateEditorController;
  private GameEmulatorChangeListener gameEmulatorChangeListener;

  public HighscoreCardsController() {
  }

  public static void onAssetManager(@NonNull DesignMode designMode, @Nullable GameRepresentation game) {
    if (game == null) {
      return;
    }
    VPinScreen screen = switch (designMode) {
        case wheel -> VPinScreen.Wheel;
        case highscoreCard -> VPinScreen.Other2;
        case instructionCard -> VPinScreen.GameHelp;
    };
      TableDialogs.openTableAssetsDialog(null, game, screen);
  }

  private void onHighscoreSettings() {
    PreferencesController.open("highscore_cards");
  }

  //------------

  @FXML
  private void onReload() {
    doReload(true);
  }

  public void doReload(boolean force) {
    startReload(Messages.get("cards.highscore_cards.loading_tables"));

    refreshEmulators();

    // load in parallel games and templates, it will ensure templates are cached before the columns access them
    JFXFuture.supplyAllAsync(
            () -> client.getHighscoreCardTemplatesClient().getTemplates(),
            () -> {
              if (force) {
                client.getGameService().clearCache();
              }

              List<GameRepresentation> games = new ArrayList<>();
              if(emulatorCombo.getValue() != null) {
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
          Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.loading_tables_failed", e.getMessage())));
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
            tableView.setPlaceholder(new Label(Messages.get("cards.highscore_cards.no_tables_found")));
          }
          templateEditorPane.setVisible(!games.isEmpty());

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
     if (NavigationController.getActiveNavigation().equals(NavigationItem.HighscoreCards)) {
         GameRepresentation game = model != null ? model.getBean() : null;
         refreshView(game);
     }
  }

  public void refreshView(GameRepresentation game) {
    templateEditorPane.setVisible(game != null);

    List<String> breadcrumb = new ArrayList<>(Arrays.asList(Messages.get("navigation.designer"), Messages.get("cards.template_editor.highscore_cards")));
    if (game != null) {
      breadcrumb.add(game.getGameDisplayName());
    }
    NavigationController.setBreadCrumb(breadcrumb);

    templateEditorController.selectTable(Optional.ofNullable(game));
    tableView.refresh();
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.designer"), Messages.get("cards.template_editor.highscore_cards")));

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
    super.initialize(Messages.get("dialog.noun_table"), Messages.get("dialog.noun_tables"), new HighscoreCardsColumnSorter(this));
    super.loadFilterPanel("scene-highscore-cards-filter.fxml");

    NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.designer"), Messages.get("cards.template_editor.highscore_cards")));

    try {
      FXMLLoader loader = new FXMLLoader(TemplateEditorController.class.getResource("template-editor.fxml"));
      loader.setResources(Messages.getBundle());
      Parent editorRoot = loader.load();
      templateEditorController = loader.getController();
      templateEditorController.setCardsController(this);
      templateEditorPane.setCenter(editorRoot);

      // the Highscore cards / Wheels tabs and the designer toolbar span the whole page, above the three tiles
      TabPane designerTabs = templateEditorController.getTabPane();
      ToolBar designerToolbar = templateEditorController.getToolbar();
      ((Pane) designerTabs.getParent()).getChildren().removeAll(designerTabs, designerToolbar);
      // the preferences button sits in the tab header, like in the Tables tab bar
      Button settingsBtn = new Button();
      settingsBtn.getStyleClass().addAll("sm-button", "sm-button-icon");
      FontIcon settingsIcon = new FontIcon("mdi2c-cog");
      settingsIcon.setIconSize(18);
      settingsIcon.getStyleClass().add("c-icon-white");
      settingsBtn.setGraphic(settingsIcon);
      settingsBtn.setTooltip(new Tooltip(Messages.get("cards.highscore_cards.open_highscore_card_settings")));
      settingsBtn.setOnAction(e -> onHighscoreSettings());
      StackPane tabHeader = new StackPane(designerTabs, settingsBtn);
      StackPane.setAlignment(settingsBtn, Pos.TOP_RIGHT);
      StackPane.setMargin(settingsBtn, new Insets(10, 14, 0, 0));

      VBox pageHeader = new VBox(14, tabHeader, designerToolbar);
      VBox.setMargin(designerToolbar, new Insets(0, 16, 0, 16));
      designerTile.setTop(pageHeader);
    }
    catch (IOException e) {
      LOG.error("failed to load template editor: " + e.getMessage(), e);
    }

    try {
      ignoreList.add("popperScreen");
    }
    catch (Exception e) {
      LOG.error("Failed to init card editor: " + e.getMessage(), e);
    }

    BaseLoadingColumn.configureColumn(columnDisplayName, (value, model) -> {
      Label label = new Label(value.getGameDisplayName());
      label.setTooltip(new Tooltip(value.getGameDisplayName()));
      label.getStyleClass().add("default-text");
      if (value.isCardDisabled()) {
        label.setStyle("-fx-text-fill: " + ThemeColors.ICON_DISABLED);
        label.setTooltip(new Tooltip(Messages.get("dialog.card_generation_disabled_for_game")));
      }
      return label;
    }, this, true);

    // template column: the "Custom" pill for tables with their own design, the base template name otherwise
    BaseLoadingColumn.configureColumn(columnBaseTemplate, (value, model) -> {
      CardTemplate template = getCardTemplateForGame(value);
      if (template != null && !template.isTemplate()) {
        Label pill = new Label(Messages.get("cards.designer.custom_pill"));
        pill.getStyleClass().addAll("sm-pill", "sm-pill-info");
        pill.setTooltip(new Tooltip(Messages.get("dialog.customized_highscore_card")));
        return pill;
      }
      CardTemplate baseTemplate = getBaseCardTemplateForGame(value);
      String templateName = baseTemplate == null ? CardTemplate.DEFAULT : baseTemplate.getName();
      Label label = new Label(templateName);
      label.getStyleClass().add("sm-hint");
      label.setTooltip(new Tooltip(templateName));
      return label;
    }, this, true);

    emulatorCombo.setButtonCell(new EmulatorButtonCell());

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(newSelection);
    });

    EventManager.getInstance().addListener(this);

    this.gameEmulatorChangeListener = new GameEmulatorChangeListener();
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

  public CardTemplate getCardTemplateForGame(GameRepresentation game) {
    CardTemplateType templateType = templateEditorController.getSelectedTemplateType();
    return client.getHighscoreCardTemplatesClient().getCardTemplateForGame(game, templateType);
  }

  public CardTemplate getBaseCardTemplateForGame(GameRepresentation game) {
    CardTemplateType templateType = templateEditorController.getSelectedTemplateType();
    return client.getHighscoreCardTemplatesClient().getBaseCardTemplateForGame(game, templateType);
  }

  @Override
  protected GameRepresentationModel toModel(GameRepresentation game) {
    return new GameRepresentationModel(game);
  }


  /**
   * Shows the selected emulator as "Emulator <name>" with a muted prefix
   */
  static class EmulatorButtonCell extends ListCell<GameEmulatorRepresentation> {
    private final Label prefix = new Label(Messages.get("cards.designer.emulator_prefix"));
    private final Label name = new Label();
    private final HBox box = new HBox(6, prefix, name);

    EmulatorButtonCell() {
      prefix.getStyleClass().add("sm-hint");
      name.getStyleClass().add("sm-text");
      box.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(GameEmulatorRepresentation item, boolean empty) {
      super.updateItem(item, empty);
      setText(null);
      if (empty || item == null) {
        setGraphic(null);
      }
      else {
        name.setText(item.toString());
        setGraphic(box);
      }
    }
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
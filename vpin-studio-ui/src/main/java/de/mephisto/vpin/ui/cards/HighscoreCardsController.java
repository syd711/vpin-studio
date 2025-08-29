package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.cards.panels.TemplateEditorController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreCardsController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);


  @FXML
  private Button maniaBtn;


  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnTemplate;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnBaseTemplate;

  @FXML
  private Button tableEditBtn;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private BorderPane templateEditorPane;

  private final List<String> ignoreList = new ArrayList<>();


  private TemplateEditorController templateEditorController;

  public HighscoreCardsController() {
  }

  @FXML
  private void onMediaEdit() {
    GameRepresentation selectedItems = getSelection();
    if (selectedItems != null) {
      TableDialogs.openTableAssetsDialog(null, selectedItems, VPinScreen.BackGlass);
    }
  }

  @FXML
  private void onTableEdit() {
    GameRepresentation selection = getSelection();
    if (selection != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(selection.getId()));
    }
  }

  @FXML
  private void onManiaTable() {
    GameRepresentation selection = getSelection();
    if (selection != null && !StringUtils.isEmpty(selection.getExtTableId())) {
      Studio.browse(ManiaUrlFactory.createTableUrl(selection.getExtTableId(), selection.getExtTableVersionId()));
    }
  }

  @FXML
  private void onHighscoreSettings() {
    PreferencesController.open("highscore_cards");
  }

  //------------

  @FXML
  private void onReload() {
    doReload(true);
  }

  private void doReload(boolean force) {
    startReload("Loading Tables...");

    // load in parallel games and templates
    JFXFuture.supplyAllAsync(
            () -> client.getHighscoreCardTemplatesClient().getTemplates(),
            () -> {
              if (force) {
                client.getGameService().clearCache();
              }
              return client.getGameService().getVpxGamesCached();
            }
        )
        .onErrorSupply(e -> {
          Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Loading tables failed: " + e.getMessage()));
          return new Object[]{Collections.emptyList(), Collections.emptyList()};
        })
        .thenAcceptLater(objs -> {
          @SuppressWarnings("unchecked")
          List<CardTemplate> _templates = (List<CardTemplate>) objs[0];
          @SuppressWarnings("unchecked")
          List<GameRepresentation> games = (List<GameRepresentation>) objs[1];

          // keep current selection
          GameRepresentationModel selectedItem = getSelectedModel();

          setItems(games);

          if (games.isEmpty()) {
            tableView.setPlaceholder(new Label("No tables found"));
          }
          templateEditorPane.setVisible(!games.isEmpty());

          tableView.refresh();
          tableView.requestFocus();

          GameRepresentation selection = this.getSelection();
          CardTemplate template = null;
          if (selection != null) {
            CardTemplate cardTemplate = client.getCardTemplate(selection);
            if (cardTemplate != null) {
              if (cardTemplate.isTemplate()) {
                template = cardTemplate;
              }
              else {
                template = client.getHighscoreCardTemplatesClient().getTemplateById(selection.getTemplateId());
              }
            }
          }

          if (template == null) {
            template = client.getHighscoreCardTemplatesClient().getDefaultTemplate();
          }

          templateEditorController.loadTemplates(client.getHighscoreCardTemplatesClient().getTemplates(), template);

          setSelectionOrFirst(selectedItem);
          endReload();
        });
  }

  public void refreshTemplates(CardTemplate selectedTemplate) {
    JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().getTemplates())
        .onErrorSupply(e -> {
          Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Loading templates failed: " + e.getMessage()));
          return Collections.emptyList();
        })
        .thenAcceptLater(templates -> {
          templateEditorController.loadTemplates(templates, selectedTemplate);
        });
  }

  @Override
  public void refreshView(GameRepresentationModel model) {
    GameRepresentation game = model != null ? model.getBean() : null;
    refreshView(game);
  }

  public void refreshView(GameRepresentation game) {
    templateEditorPane.setVisible(game != null);
    tableEditBtn.setDisable(game == null);
    maniaBtn.setDisable(game == null || StringUtils.isEmpty(game.getExtTableId()));

    List<String> breadcrumb = new ArrayList<>(Arrays.asList("Highscore Cards"));
    if (game != null) {
      breadcrumb.add(game.getGameDisplayName());
    }
    NavigationController.setBreadCrumb(breadcrumb);

    templateEditorController.selectTable(Optional.ofNullable(game));
    tableView.refresh();
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));

    if (options != null && options.getGameId() > 0) {
      GameRepresentationModel selectedItem = tableView.getItems().stream().filter(g -> g.getGameId() == options.getGameId()).findFirst().orElse(null);
      if (selectedItem != null) {
        setSelection(selectedItem, true);
      }
    }
    //templateEditorController.selectTable(selectedItem, false);
    //refreshRawPreview(selectedItem);
    //refreshPreview(getSelection());
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("Highscore ", "backglasses", new HighscoreCardsColumnSorter(this));
    super.loadFilterPanel("scene-highscore-cards-filter.fxml");

    maniaBtn.managedProperty().bindBidirectional(maniaBtn.visibleProperty());
    maniaBtn.setVisible(Features.MANIA_ENABLED);

    Image imageMania = new Image(Studio.class.getResourceAsStream("mania.png"));
    ImageView iconMania = new ImageView(imageMania);
    iconMania.setFitWidth(18);
    iconMania.setFitHeight(18);
    maniaBtn.setGraphic(iconMania);

    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));

    try {
      FXMLLoader loader = new FXMLLoader(TemplateEditorController.class.getResource("template-editor.fxml"));
      Parent editorRoot = loader.load();
      templateEditorController = loader.getController();
      templateEditorController.setCardsController(this);
      templateEditorPane.setCenter(editorRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load template editor: " + e.getMessage(), e);
    }

    try {
      ignoreList.addAll(Arrays.asList("popperScreen"));
    }
    catch (Exception e) {
      LOG.error("Failed to init card editor: " + e.getMessage(), e);
    }

    BaseLoadingColumn.configureColumn(columnDisplayName, (value, model) -> {
      Label label = new Label(value.getGameDisplayName());
      label.setTooltip(new Tooltip(value.getGameDisplayName()));
      label.getStyleClass().add("default-text");
      if (value.isCardDisabled()) {
        label.setStyle("-fx-text-fill: " + DISABLED_COLOR);
        label.setTooltip(new Tooltip("The card generation is disabled for this game."));
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnTemplate, (value, model) -> {
      CardTemplate template = client.getCardTemplate(value);
      Label label = new Label("");
      if(template != null && !template.isTemplate()) {
        label.setGraphic(WidgetFactory.createCheckboxIcon());
      }
      label.getStyleClass().add("default-text");
      if (template != null) {
        label.setTooltip(new Tooltip(template.getName()));
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnBaseTemplate, (value, model) -> {
      CardTemplate template = client.getCardTemplate(value);
      String templateName = "-";
      if(template == null) {
        templateName = CardTemplate.DEFAULT;
      }
      else if (template.isTemplate()) {
        templateName = template.getName();
      }
      else {
        CardTemplate templateById = client.getHighscoreCardTemplatesClient().getTemplateById(template.getParentId());
        if (templateById == null) {
          templateName = CardTemplate.DEFAULT;
        }
        else {
          templateName = templateById.getName();
        }
      }

      Label label = new Label(templateName);
      label.getStyleClass().add("default-text");
      label.setTooltip(new Tooltip(templateName));
      return label;
    }, this, true);


    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(newSelection);
    });

    EventManager.getInstance().addListener(this);

    doReload(false);
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

}
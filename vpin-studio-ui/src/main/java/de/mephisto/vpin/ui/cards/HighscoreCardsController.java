package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreCardsController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private Button maniaBtn;

  @FXML
  private ImageView rawDirectB2SImage;


  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnTemplate;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnStatus;

  @FXML
  private Button tableEditBtn;

  @FXML
  private TitledPane defaultBackgroundTitlePane;

  @FXML
  private BorderPane templateEditorPane;


  private final List<String> ignoreList = new ArrayList<>();


  private TemplateEditorController templateEditorController;

  public HighscoreCardsController() {
  }

  @FXML
  private void onBackgroundReset() {
    GameRepresentation game = getSelection();
    if (game != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-generate default background for \"" + game.getGameDisplayName() + "\"?",
          "This will re-generate the existing default background.", null, "Generate Background");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Studio.client.getAssetService().deleteGameAssets(game.getId());
        refreshRawPreview(game);
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
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

    GameRepresentationModel selectedItem = getSelectedModel();

    JFXFuture.supplyAsync(() -> {
      if (force) {
        client.getGameService().clearCache();
      }
      return client.getGameService().getVpxGamesCached();
    })
    .onErrorSupply(e -> {
      Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Loading tables failed: " + e.getMessage()));
      return Collections.emptyList();
    })
    .thenAcceptLater(games -> {
        setItems(games);

        if (games.isEmpty()) {
          tableView.setPlaceholder(new Label("No tables found"));
        }
        templateEditorPane.setVisible(!games.isEmpty());

        tableView.refresh();
        tableView.requestFocus();

        setSelectionOrFirst(selectedItem);

        endReload();
    });
  }


  @FXML
  private void onDefaultPictureUpload() {
    GameRepresentation game = getSelection();
    if (game != null) {
      boolean uploaded = TableDialogs.openDefaultBackgroundUploadDialog(game);
      if (uploaded) {
        reloadItem(game);
      }
    }
  }

  @FXML
  private void onOpenDefaultPicture() {
    GameRepresentation game = getSelection();
    if (game != null) {
      TableDialogs.openMediaDialog(Studio.stage, "Default Picture", client.getBackglassServiceClient().getDefaultPictureUrl(game));
    }
  }

  private void refreshRawPreview(GameRepresentation game) {
    if (!defaultBackgroundTitlePane.isExpanded()) {
      return;
    }

    try {
      resolutionLabel.setText("");
      openDefaultPictureBtn.setVisible(false);
      rawDirectB2SImage.setImage(null);

      if (game != null) {
        openDefaultPictureBtn.setTooltip(new Tooltip("Open directb2s image"));
        InputStream input = client.getBackglassServiceClient().getDefaultPicture(game);
        Image image = new Image(input);
        rawDirectB2SImage.setImage(image);
        input.close();

        if (image.getWidth() > 300) {
          openDefaultPictureBtn.setVisible(true);
          resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
        }
      }
    }
    catch (IOException e) {
      LOG.error("Failed to load raw b2s: " + e.getMessage(), e);
    }
  }

  @Override
  public void refreshView(GameRepresentationModel model) {
    GameRepresentation game = model != null ? model.getBean(): null;

    templateEditorPane.setVisible(model != null);
    tableEditBtn.setDisable(model == null);
    maniaBtn.setDisable(game == null || StringUtils.isEmpty(game.getExtTableId()));

    List<String> breadcrumb = new ArrayList<>(Arrays.asList("Highscore Cards"));
    if (game != null) {
      breadcrumb.add(game.getGameDisplayName());
    }
    NavigationController.setBreadCrumb(breadcrumb);

    templateEditorController.selectTable(Optional.ofNullable(game));
    refreshRawPreview(game);
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

    BaseLoadingColumn.configureColumn(columnStatus, (value, model) -> {
      FontIcon checkIcon = WidgetFactory.createCheckIcon();
      if (value.isCardDisabled()) {
        checkIcon.setIconColor(Paint.valueOf(DISABLED_COLOR));
      }
      return checkIcon;
    }, this, true);

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
      CardTemplate template = templateEditorController.getCardTemplateForGame(value);
      String templateName = template != null ? template.getName() : "-";
      Label label = new Label(templateName);
      label.getStyleClass().add("default-text");
      if (template != null) {
        label.setTooltip(new Tooltip(templateName));
      }
      return label;
    }, this, true);


    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(newSelection);
    });

    defaultBackgroundTitlePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (getSelection() != null) {
          refreshRawPreview(getSelection());
        }
      }
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
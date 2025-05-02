package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.cards.panels.TemplateEditorController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.Keys;
import de.mephisto.vpin.ui.util.MediaUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class HighscoreCardsController implements Initializable, StudioFXController, ListChangeListener<GameRepresentation>, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private Button maniaBtn;

  @FXML
  private ImageView rawDirectB2SImage;

  //table components
  @FXML
  private TableView<GameRepresentation> tableView;

  @FXML
  private TableColumn<GameRepresentation, Label> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, Label> columnTemplate;

  @FXML
  private TableColumn<GameRepresentation, Node> columnStatus;

  @FXML
  private MenuButton filterButton;

  @FXML
  private Button tableEditBtn;

  @FXML
  private TextField searchField;

  @FXML
  private StackPane loaderStack;

  @FXML
  private TitledPane defaultBackgroundTitlePane;

  @FXML
  private BorderPane templateEditorPane;

  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  private final List<String> ignoreList = new ArrayList<>();

  private Parent tablesLoadingOverlay;

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";
  private List<CardTemplate> cardTemplates;
  private TemplateEditorController templateEditorController;

  public HighscoreCardsController() {
  }

  @FXML
  private void onBackgroundReset() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-generate default background for \"" + game.getGameDisplayName() + "\"?",
          "This will re-generate the existing default background.", null, "Generate Background");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Studio.client.getAssetService().deleteGameAssets(game.getId());
        refreshRawPreview(Optional.of(game));
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
    }
  }

  @FXML
  private void onTableEdit() {
    GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(selection.getId()));
    }
  }

  @FXML
  private void onReloadPressed() {
    onReload(true);
  }

  @FXML
  private void onManiaTable() {
    GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null && !StringUtils.isEmpty(selection.getExtTableId())) {
      Studio.browse(ManiaUrlFactory.createTableUrl(selection.getExtTableId(), selection.getExtTableVersionId()));
    }
  }

  @FXML
  private void onHighscoreSettings() {
    PreferencesController.open("highscore_cards");
  }

  @FXML
  private void onReload(boolean force) {
    setBusy(true);
    GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();

    new Thread(() -> {
      if (force) {
        client.getGameService().clearCache();
      }
      games = client.getGameService().getVpxGamesCached();

      Platform.runLater(() -> {
        filterGames(games);
        tableView.setItems(data);
        templateEditorPane.setVisible(!data.isEmpty());

        tableView.refresh();
        if (selection != null) {
          final Optional<GameRepresentation> updatedGame = this.games.stream().filter(g -> g.getId() == selection.getId()).findFirst();
          if (updatedGame.isPresent()) {
            GameRepresentation gameRepresentation = updatedGame.get();
            tableView.getSelectionModel().select(gameRepresentation);
          }
        }
        else if (!games.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
        setBusy(false);
        Platform.runLater(() -> {
          tableView.requestFocus();
        });
      });
    }).start();
  }

  private void setBusy(boolean b) {
    Platform.runLater(() -> {
      if (b) {
        tableView.setVisible(false);
        if (!loaderStack.getChildren().contains(tablesLoadingOverlay)) {
          loaderStack.getChildren().add(tablesLoadingOverlay);
        }
      }
      else {
        tableView.setVisible(true);
        loaderStack.getChildren().remove(tablesLoadingOverlay);
      }
    });
  }

  @FXML
  private void onDefaultPictureUpload() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      boolean uploaded = TableDialogs.openDefaultBackgroundUploadDialog(game);
      if (uploaded) {
        refreshRawPreview(Optional.of(game));
        onReloadPressed();
        templateEditorController.selectTable(Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()), true);
      }
    }
  }

  @FXML
  private void onOpenDefaultPicture() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      ByteArrayInputStream s = client.getBackglassServiceClient().getDefaultPicture(game);
      MediaUtil.openMedia(s);
    }
  }

  private void refreshRawPreview(Optional<GameRepresentation> game) {
    if (!defaultBackgroundTitlePane.isExpanded()) {
      return;
    }

    try {
      resolutionLabel.setText("");
      openDefaultPictureBtn.setVisible(false);
      rawDirectB2SImage.setImage(null);

      if (game.isPresent()) {
        openDefaultPictureBtn.setTooltip(new Tooltip("Open directb2s image"));
        InputStream input = client.getBackglassServiceClient().getDefaultPicture(game.get());
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

  private void refreshPreview(Optional<GameRepresentation> game, boolean regenerate) {
    List<String> breadcrumb = new ArrayList<>(Arrays.asList("Highscore Cards"));
    templateEditorPane.setVisible(game.isPresent());
    tableEditBtn.setDisable(game.isEmpty());
    maniaBtn.setDisable(game.isEmpty() || StringUtils.isEmpty(game.get().getExtTableId()));

    if (game.isPresent()) {
      breadcrumb.add(game.get().getGameDisplayName());
    }
    else {
      templateEditorPane.setVisible(false);
    }
    NavigationController.setBreadCrumb(breadcrumb);
    templateEditorController.selectTable(game, regenerate);
    refreshRawPreview(game);
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));
    Optional<GameRepresentation> selectedItem = Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    if (options != null && options.getGameId() > 0) {
      selectedItem = tableView.getItems().stream().filter(g -> g.getId() == options.getGameId()).findFirst();
      if (selectedItem.isPresent()) {
        GameRepresentation game = selectedItem.get();
        tableView.getSelectionModel().clearSelection();
        tableView.getSelectionModel().select(game);
        tableView.scrollTo(game);
      }
    }
    templateEditorController.selectTable(selectedItem, false);
    refreshRawPreview(selectedItem);

    templateEditorController.refreshPreviewSize();
    refreshPreview(Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()), false);
  }

  private void filterGames(List<GameRepresentation> games) {
    List<GameRepresentation> filtered = new ArrayList<>();
    String filterValue = searchField.textProperty().getValue();

    List<Integer> emuIds = new ArrayList<>();
    ObservableList<MenuItem> items = this.filterButton.getItems();
    for (MenuItem item : items) {
      CheckBox checkBox = (CheckBox) ((CustomMenuItem) item).getContent();
      GameEmulatorRepresentation emulatorRepresentation = (GameEmulatorRepresentation) checkBox.getUserData();
      if (checkBox.isSelected()) {
        emuIds.add(emulatorRepresentation.getId());
      }
    }

    filterButton.getStyleClass().remove("filter-button-selected");
    if (emuIds.size() != client.getEmulatorService().getVpxGameEmulators().size()) {
      filterButton.getStyleClass().add("filter-button-selected");
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu"));
    }
    else {
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu-outline"));
    }


    for (GameRepresentation game : games) {
      if (game.getHighscoreType() == null) {
        continue;
      }

      if (!emuIds.contains(game.getEmulatorId())) {
        continue;
      }

      if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(game);
      }
    }

    data = FXCollections.observableList(filtered);
  }

  private GameRepresentation getSelectedTable() {
    return this.tableView.getSelectionModel().getSelectedItem();
  }

  @Override
  public void onChanged(Change<? extends GameRepresentation> c) {
    if (c.getList().isEmpty()) {
      refreshPreview(Optional.empty(), false);
    }
    else {
      refreshPreview(Optional.ofNullable(c.getList().get(0)), false);
    }
  }

  public void onDragDone() {
    if (!NavigationItem.HighscoreCards.equals(NavigationController.getActiveNavigation())) {
      return;
    }

    debouncer.debounce("position", () -> {
      Platform.runLater(() -> {
        templateEditorController.refreshPreviewSize();
        refreshPreview(Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()), false);
      });
    }, 500);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    maniaBtn.managedProperty().bindBidirectional(maniaBtn.visibleProperty());
    maniaBtn.setVisible(Features.MANIA_ENABLED);

    Image imageMania = new Image(Studio.class.getResourceAsStream("mania.png"));
    ImageView iconMania = new ImageView(imageMania);
    iconMania.setFitWidth(18);
    iconMania.setFitHeight(18);
    maniaBtn.setGraphic(iconMania);

    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));
    games = client.getGameService().getVpxGamesCached();
    cardTemplates = client.getHighscoreCardTemplatesClient().getTemplates();

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

    stage.widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());

    try {
      ignoreList.addAll(Arrays.asList("popperScreen"));
    }
    catch (Exception e) {
      LOG.error("Failed to init card editor: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      tablesLoadingOverlay.setTranslateY(-100);
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      boolean defaultBackgroundAvailable = value.isDefaultBackgroundAvailable();
      if (!defaultBackgroundAvailable) {
        Label label = new Label();
        if (value.isCardDisabled()) {
          label.setStyle("-fx-text-fill: " + DISABLED_COLOR);
        }
        label.setGraphic(WidgetFactory.createAlertIcon("mdi2i-image-off-outline"));
        Tooltip tt = new Tooltip("The table does not have a default background.");
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
        return new SimpleObjectProperty<>(label);
      }

      FontIcon checkIcon = WidgetFactory.createCheckIcon();
      if (value.isCardDisabled()) {
        checkIcon.setIconColor(Paint.valueOf(DISABLED_COLOR));
      }
      return new SimpleObjectProperty<>(checkIcon);
    });

    columnDisplayName.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      Label label = new Label(value.getGameDisplayName());
      label.setTooltip(new Tooltip(value.getGameDisplayName()));
      label.getStyleClass().add("default-text");
      if (value.isCardDisabled()) {
        label.setStyle("-fx-text-fill: " + DISABLED_COLOR);
        label.setTooltip(new Tooltip("The card generation is disabled for this game."));
      }

      return new SimpleObjectProperty<>(label);
    });

    columnTemplate.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();

      CardTemplate template = cardTemplates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst().get();
      if (value.getTemplateId() != null) {
        Optional<CardTemplate> first = cardTemplates.stream().filter(g -> g.getId().equals(value.getTemplateId())).findFirst();
        if (first.isPresent()) {
          template = first.get();
        }
      }

      String templateName = template.getName();
      Label label = new Label(templateName);
      label.getStyleClass().add("default-text");
      label.setTooltip(new Tooltip(templateName));
      return new SimpleObjectProperty<>(label);
    });


    tableView.setItems(data);
    tableView.setEditable(true);
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().getSelectedItems().addListener(this);
    tableView.setSortPolicy(new Callback<TableView<GameRepresentation>, Boolean>() {
      @Override
      public Boolean call(TableView<GameRepresentation> gameRepresentationTableView) {
        //GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<GameRepresentation, ?> column = gameRepresentationTableView.getSortOrder().get(0);
          if (column.equals(columnDisplayName)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getGameDisplayName()));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnTemplate)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getTemplateId())));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
        }
        return true;
      }
    });

    tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (Keys.isSpecial(event)) {
          return;
        }

        String text = event.getText();

        long timeDiff = System.currentTimeMillis() - lastKeyInputTime;
        if (timeDiff > 800) {
          lastKeyInputTime = System.currentTimeMillis();
          lastKeyInput = text;
        }
        else {
          lastKeyInputTime = System.currentTimeMillis();
          lastKeyInput = lastKeyInput + text;
          text = lastKeyInput;
        }

        for (GameRepresentation game : data) {
          if (game.getGameDisplayName().toLowerCase().startsWith(text.toLowerCase())) {
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().select(game);
            tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
            break;
          }
        }
      }
    });

    searchField.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      filterGames(games);
      tableView.setItems(data);
    });

    List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getVpxGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      CustomMenuItem item = new CustomMenuItem();
      CheckBox checkBox = new CheckBox(gameEmulator.getName());
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(true);
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          tableView.getSelectionModel().clearSelection();
          filterGames(games);
          tableView.setItems(data);
        }
      });
      checkBox.setUserData(gameEmulator);
      item.setContent(checkBox);
      filterButton.getItems().add(item);
    }

    defaultBackgroundTitlePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (getSelectedTable() != null) {
          refreshRawPreview(Optional.of(getSelectedTable()));
        }
      }
    });

    EventManager.getInstance().addListener(this);
    onReload(false);
    templateEditorController.refreshPreviewSize();
  }

  public void refresh(Optional<GameRepresentation> gameRepresentation, List<CardTemplate> templates, boolean refreshAll) {
    this.cardTemplates = templates;
    if (refreshAll) {
      onReload(false);
      return;
    }

    if (gameRepresentation.isPresent()) {
      GameRepresentation refreshedGame = client.getGameService().getVpxGameCached(gameRepresentation.get().getId());
      Platform.runLater(() -> {
        tableView.getSelectionModel().getSelectedItems().removeListener(this);
        GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
        tableView.getSelectionModel().clearSelection();

        int index = data.indexOf(refreshedGame);
        if (index != -1) {
          data.remove(index);
          data.add(index, refreshedGame);
        }
        tableView.getSelectionModel().getSelectedItems().addListener(this);

        if (selection != null && data.contains(refreshedGame)) {
          tableView.getSelectionModel().select(refreshedGame);
        }

        tableView.refresh();
      });
    }
  }

  public List<GameRepresentation> getSelection() {
    return tableView.getSelectionModel().getSelectedItems();
  }

  @Override
  public void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {
    this.onReloadPressed();
  }

  @Override
  public void tablesChanged() {
    this.onReloadPressed();
  }
}

package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.Keys;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreCardsController implements Initializable, StudioFXController, PreferenceChangeListener, BindingChangedListener, ListChangeListener<GameRepresentation>, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<CardTemplate> templateCombo;

  @FXML
  private ComboBox<PopperScreen> screensComboBox;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private BorderPane popperPreviewPanel;

  @FXML
  private ImageView cardPreview;

  @FXML
  private ImageView rawDirectB2SImage;

  @FXML
  private Button openImageBtn;

  @FXML
  private Button generateBtn;

  @FXML
  private Button generateAllBtn;

  @FXML
  private StackPane previewStack;

  @FXML
  private Pane previewPanel;

  //table components
  @FXML
  private TableView<GameRepresentation> tableView;

  @FXML
  private TableColumn<GameRepresentation, Label> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, Label> columnTemplate;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private MenuButton filterButton;

  @FXML
  private TextField searchField;

  @FXML
  private StackPane loaderStack;

  @FXML
  private Button folderBtn;

  // template editing
  @FXML
  private Button editTemplateBtn;

  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  private List<String> ignoreList = new ArrayList<>();
  private Parent waitOverlay;

  private CardSettings cardSettings;

  private Parent tablesLoadingOverlay;

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";
  private List<CardTemplate> cardTemplates;
  private TemplateComboChangeListener templateComboChangeListener;

  public HighscoreCardsController() {
  }

  @FXML
  private void onTemplateEdit() {
    CardsDialogs.openTemplateManager(this);

    this.templateCombo.valueProperty().removeListener(templateComboChangeListener);
    CardTemplate value = this.templateCombo.getValue();
    cardTemplates = client.getHighscoreCardTemplatesClient().getTemplates();
    Optional<CardTemplate> selectionExists = cardTemplates.stream().filter(t -> t.getId().equals(value.getId())).findFirst();
    this.templateCombo.setItems(FXCollections.observableList(cardTemplates));

    if (selectionExists.isPresent()) {
      this.templateCombo.setValue(selectionExists.get());
      this.templateCombo.valueProperty().addListener(templateComboChangeListener);
    }
    else {
      this.templateCombo.valueProperty().addListener(templateComboChangeListener);
      this.templateCombo.getSelectionModel().select(0);
    }

    onReload();
  }

  @FXML
  private void onResize() {
    System.out.println("resize");
  }

  @FXML
  private void onResizeEnd() {
    System.out.println("resize end");
  }

  @FXML
  private void onRename(ActionEvent e) {
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
  }

  @FXML
  private void onDelete(ActionEvent e) {
  }

  @FXML
  private void onReload() {
    this.generateBtn.setDisable(true);
    this.generateAllBtn.setDisable(true);
    this.openImageBtn.setDisable(true);

    setBusy(true);

    new Thread(() -> {
      Platform.runLater(() -> {
        tableView.getSelectionModel().getSelectedItems().removeListener(this);

        GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
        games = client.getGameService().getVpxGamesCached();
        cardTemplates = client.getHighscoreCardTemplatesClient().getTemplates();

        filterGames(games);
        tableView.setItems(data);

        tableView.refresh();
        tableView.getSelectionModel().getSelectedItems().addListener(this);

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

        this.generateBtn.setDisable(games.isEmpty());
        this.generateAllBtn.setDisable(games.isEmpty());
        this.openImageBtn.setDisable(games.isEmpty());

        setBusy(false);
        Platform.runLater(() -> {
          tableView.requestFocus();
        });
      });
    }).start();
  }

  private void setBusy(boolean b) {
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
  }

  @FXML
  private void onOpenImage() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      ByteArrayInputStream s = client.getHighscoreCardsService().getHighscoreCard(game);
      MediaUtil.openMedia(s);
    }
  }

  @FXML
  private void onDefaultPictureUpload() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    boolean uploaded = TableDialogs.openDefaultBackgroundUploadDialog(game);
    if (uploaded) {
      refreshRawPreview(Optional.of(game));
      onGenerateClick();
    }
  }

  @FXML
  private void onGenerateAll() {
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    String targetScreen = cardSettings.getPopperScreen();
    if (StringUtils.isEmpty(targetScreen)) {
      WidgetFactory.showAlert(Studio.stage, "Not target screen selected.", "Select a target screen in the preferences.");
    }
    else {
      ProgressDialog.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Cards"));
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

  @FXML
  private void onGenerateClick() {
    Platform.runLater(() -> {
      GameRepresentation value = tableView.getSelectionModel().getSelectedItem();
      refreshPreview(Optional.ofNullable(value), true);
    });
  }

  private void updateTransparencySettings(Boolean newValue) {
    Platform.runLater(() -> {
      if (newValue) {
        Image backgroundImage = new Image(Studio.class.getResourceAsStream("transparent.png"));
        BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
        previewPanel.setBackground(new Background(myBI));
      }
      else {
        previewPanel.setBackground(new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null)));
      }
    });
  }

  private void refreshRawPreview(Optional<GameRepresentation> game) {
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
    } catch (IOException e) {
      LOG.error("Failed to load raw b2s: " + e.getMessage(), e);
    }
  }

  private void refreshPreview(Optional<GameRepresentation> game, boolean regenerate) {
    int offset = 36;
    Platform.runLater(() -> {
      this.generateBtn.setDisable(game.isEmpty());
      this.openImageBtn.setDisable(game.isEmpty());

      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);

      if (game.isEmpty()) {
        return;
      }

      try {
        new Thread(() -> {
          if (regenerate) {
            client.getHighscoreCardsService().generateHighscoreCardSample(game.get());
          }

          InputStream input = client.getHighscoreCardsService().getHighscoreCard(game.get());
          Image image = new Image(input);
          cardPreview.setImage(image);
          cardPreview.setVisible(true);

          Platform.runLater(() -> {
            refreshPopperPreview();
            refreshRawPreview(game);
            previewStack.getChildren().remove(waitOverlay);
            updateTransparencySettings(this.templateCombo.getSelectionModel().getSelectedItem().isTransparentBackground());
          });

        }).start();
        cardPreview.setFitHeight(previewPanel.getHeight() - offset);
        cardPreview.setFitWidth(previewPanel.getWidth() - offset);

      } catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void onViewActivated() {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));
    onReload();
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.HIGHSCORE_CARD_SETTINGS)) {
      this.cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    }
  }

  @Override
  public void beanPropertyChanged(Object bean, String key, Object value) {
    if (bean instanceof CardTemplate) {
      onGenerateClick();
    }
    else if (bean instanceof CardSettings) {
      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, this.cardSettings);
      onGenerateClick();
    }
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
    if (emuIds.size() != client.getPinUPPopperService().getVpxGameEmulators().size()) {
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

  public GameRepresentation getSelectedTable() {
    return this.tableView.getSelectionModel().getSelectedItem();
  }

  public CardTemplate getSelectedTemplate() {
    return templateCombo.getSelectionModel().getSelectedItem();
  }

  @Override
  public void onChanged(Change<? extends GameRepresentation> c) {
    boolean disable = c.getList().isEmpty() || c.getList().size() > 1;
    this.generateBtn.setDisable(disable);
    this.generateAllBtn.setDisable(disable);
    this.openImageBtn.setDisable(disable);
    this.templateCombo.setDisable(c.getList().isEmpty());

    previewPanel.setVisible(!c.getList().isEmpty());

    this.templateCombo.valueProperty().removeListener(templateComboChangeListener);
    if (c.getList().isEmpty()) {
      refreshPreview(Optional.empty(), false);
    }
    else {
      GameRepresentation gameRepresentation = c.getList().get(0);
      if (gameRepresentation.getTemplateId() == null) {
        Optional<CardTemplate> first = cardTemplates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst();
        templateCombo.setValue(first.get());
      }
      else {
        Optional<CardTemplate> first = cardTemplates.stream().filter(t -> t.getId().equals(gameRepresentation.getTemplateId())).findFirst();
        //not present if deleted
        if (first.isPresent()) {
          templateCombo.setValue(first.get());
        }
        else {
          templateCombo.getSelectionModel().select(0);
        }
      }

      Platform.runLater(() -> {
        refreshPreview(Optional.of(c.getList().get(0)), true);
      });
    }
    this.templateCombo.valueProperty().addListener(templateComboChangeListener);
  }

  @FXML
  private void onFolderBtn() {
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    String popperScreen = cardSettings.getPopperScreen();
    if (!StringUtils.isEmpty(popperScreen)) {
      PopperScreen screen = PopperScreen.valueOfScreen(popperScreen);
      GameEmulatorRepresentation gameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
      String mediaDir = gameEmulator.getMediaDirectory();
      File screenDir = new File(mediaDir, screen.name());
      SystemUtil.openFolder(screenDir);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    folderBtn.setVisible(SystemUtil.isFolderActionSupported());

    try {
      cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
      ignoreList.addAll(Arrays.asList("popperScreen"));

      cardTemplates = client.getHighscoreCardTemplatesClient().getTemplates();
      templateCombo.setItems(FXCollections.observableList(cardTemplates));
      templateCombo.getSelectionModel().select(0);

      this.templateComboChangeListener = new TemplateComboChangeListener();
      templateCombo.valueProperty().addListener(this.templateComboChangeListener);
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Card...");

      cardPreview.setPreserveRatio(true);
      previewPanel.widthProperty().addListener((obs, oldVal, newVal) -> {
        debouncer.debounce("refresh", () -> {
          Platform.runLater(() -> {
            cardPreview.setFitWidth(newVal.intValue() / 2);
            refreshPreview(Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()), false);
          });
        }, 300);
      });
    } catch (Exception e) {
      LOG.error("Failed to init card editor: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      tablesLoadingOverlay.setTranslateY(-100);
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      boolean defaultBackgroundAvailable = value.isDefaultBackgroundAvailable();
      if (!defaultBackgroundAvailable) {
        Label label = new Label();
        label.setGraphic(WidgetFactory.createAlertIcon("mdi2i-image-off-outline"));
        Tooltip tt = new Tooltip("The table does not have a default background.");
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
        return new SimpleObjectProperty(label);
      }
      return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
    });

    columnDisplayName.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      Label label = new Label(value.getGameDisplayName());
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty(label);
    });

    columnTemplate.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.getTemplateId() != null) {
        Optional<CardTemplate> first = cardTemplates.stream().filter(g -> g.getId().equals(value.getTemplateId())).findFirst();
        if (first.isPresent()) {
          CardTemplate template = first.get();
          Label label = new Label(template.getName());
          label.getStyleClass().add("default-text");
          return new SimpleObjectProperty(label);
        }
      }

      Label label = new Label(CardTemplate.DEFAULT);
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty(label);
    });


    tableView.setItems(data);
    tableView.setEditable(true);
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().getSelectedItems().addListener(this);
    tableView.setSortPolicy(new Callback<TableView<GameRepresentation>, Boolean>() {
      @Override
      public Boolean call(TableView<GameRepresentation> gameRepresentationTableView) {
        GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
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

    client.getPreferenceService().addListener(this);

    List<GameEmulatorRepresentation> gameEmulators = client.getPinUPPopperService().getVpxGameEmulators();
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

    List<PopperScreen> popperScreens = new ArrayList<>(Arrays.asList(PopperScreen.values()));
    popperScreens.remove(PopperScreen.Audio);
    popperScreens.remove(PopperScreen.AudioLaunch);
    popperScreens.remove(PopperScreen.PlayField);
    popperScreens.remove(PopperScreen.Loading);

    screensComboBox.setItems(FXCollections.observableList(popperScreens));
    PopperScreen previewPopperScreen = cardSettings.getPreviewPopperScreen();
    if (previewPopperScreen != null) {
      screensComboBox.setValue(previewPopperScreen);
    }
    screensComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      cardSettings.setPreviewPopperScreen(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);
      refreshPopperPreview();
    });

    EventManager.getInstance().addListener(this);
    onReload();
  }

  private void refreshPopperPreview() {
    Node center = popperPreviewPanel.getCenter();
    if (center instanceof AssetMediaPlayer) {
      ((AssetMediaPlayer) center).disposeMedia();
    }

    GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      GameMediaItemRepresentation defaultMediaItem = selectedItem.getGameMedia().getDefaultMediaItem(screensComboBox.getValue());
      if (defaultMediaItem != null) {
        WidgetFactory.addMediaItemToBorderPane(client, defaultMediaItem, popperPreviewPanel);
      }
      else {
        InputStream in = Studio.class.getResourceAsStream("empty-preview.png");
        ImageView noPreview = new ImageView();
        noPreview.setPreserveRatio(true);
        noPreview.setFitHeight(250);
        noPreview.setImage(new Image(in));
        popperPreviewPanel.setCenter(noPreview);
      }
    }
  }

  @Override
  public void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {
    if (id > 0) {
      GameRepresentation refreshedGame = client.getGameService().getGameCached(id);
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

  class TemplateComboChangeListener implements ChangeListener<CardTemplate> {
    @Override
    public void changed(ObservableValue<? extends CardTemplate> observable, CardTemplate oldValue, CardTemplate newValue) {
      List<GameRepresentation> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());

      if (!selectedItems.isEmpty()) {
        ProgressDialog.createProgressDialog(new TemplateAssigmentProgressModel(selectedItems, newValue.getId()));
        Platform.runLater(() -> {
          refreshPreview(Optional.of(selectedItems.get(0)), true);
        });
      }
    }
  }
}
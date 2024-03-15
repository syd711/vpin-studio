
package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.Keys;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;
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
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class HighscoreCardsController implements Initializable, StudioFXController, PreferenceChangeListener, BindingChangedListener, ListChangeListener<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<CardTemplate> templateCombo;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private ImageView cardPreview;

  @FXML
  private Label titleFontLabel;

  @FXML
  private Label scoreFontLabel;

  @FXML
  private Label tableFontLabel;

  @FXML
  private ImageView rawDirectB2SImage;

  @FXML
  private CheckBox useDirectB2SCheckbox;

  @FXML
  private CheckBox grayScaleCheckbox;

  @FXML
  private CheckBox transparentBackgroundCheckbox;

  @FXML
  private ColorPicker fontColorSelector;

  @FXML
  private ComboBox<String> backgroundImageCombo;

  @FXML
  private TextField titleText;

  @FXML
  private Slider brightenSlider;

  @FXML
  private Slider darkenSlider;

  @FXML
  private Slider blurSlider;

  @FXML
  private Slider borderSlider;

  @FXML
  private Spinner<Integer> marginTopSpinner;

  @FXML
  private Spinner<Integer> wheelImageSpinner;

  @FXML
  private Spinner<Integer> rowSeparatorSpinner;

  @FXML
  private Slider alphaPercentageSpinner;

  @FXML
  private CheckBox renderRawHighscore;

  @FXML
  private FontIcon rawHighscoreHelp;

  @FXML
  private Button falbackUploadBtn;

  @FXML
  private Button openImageBtn;

  @FXML
  private Button generateBtn;

  @FXML
  private Button generateAllBtn;

  @FXML
  private StackPane previewStack;

  @FXML
  private TitledPane backgroundSettingsPane;

  @FXML
  private Accordion accordion;

  @FXML
  private CheckBox renderTitleCheckbox;

  @FXML
  private CheckBox renderTableNameCheckbox;

  @FXML
  private CheckBox renderWheelIconCheckbox;

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
  private Button renameBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button createBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private MenuButton filterButton;

  @FXML
  private TextField searchField;

  @FXML
  private StackPane loaderStack;

  // template editing
  @FXML
  private Button editTemplateBtn;

  @FXML
  private ToolBar editorFooterToolbar;

  @FXML
  private ToolBar editorHeaderToolbar;

  @FXML
  private Label templateLabel;

  @FXML
  private TabPane tabPane;

  @FXML
  private Pane editorFrame;

  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  private List<String> ignoreList = new ArrayList<>();
  private ObservableList<String> imageList;
  private Parent waitOverlay;

  private CardSettings cardSettings;
  private BeanBinder templateBeanBinder;
  private BeanBinder cardSettingsBinder;

  private Parent tablesLoadingOverlay;

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";

  @FXML
  private void onTemplateCreate() {

  }

  @FXML
  private void onTemplateEdit() {
    CardTemplate template = templateCombo.getValue();
    editorFooterToolbar.setVisible(true);
    editorHeaderToolbar.setVisible(true);

    duplicateBtn.setDisable(true);
    deleteBtn.setDisable(true);
    editTemplateBtn.setDisable(true);
    createBtn.setDisable(true);
    templateCombo.setDisable(true);

    editorFrame.getStyleClass().add("edit-mode-border");
    templateLabel.setText(template.getName());
  }

  @FXML
  private void onTemplateSave() {
    CardTemplate template = templateCombo.getValue();
    editorFooterToolbar.setVisible(false);
    editorHeaderToolbar.setVisible(false);

    duplicateBtn.setDisable(false);
    deleteBtn.setDisable(false);
    editTemplateBtn.setDisable(false);
    createBtn.setDisable(false);
    templateCombo.setDisable(false);

    editorFrame.getStyleClass().clear();
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
//    DirectB2S selectedItem = directb2sList.getSelectionModel().getSelectedItem();
//    if (selectedItem != null) {
//      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
//      String newName = WidgetFactory.showInputDialog(stage, "Rename Backglass", "Enter new name for backglass file \"" + selectedItem.getName() + ".directb2s\"", null, null, selectedItem.getName());
//      if (newName != null) {
//        if (!FileUtils.isValidFilename(newName)) {
//          WidgetFactory.showAlert(stage, "Invalid Filename", "The specified file name contains invalid characters.");
//          return;
//        }
//
//        try {
//          if (!newName.endsWith(".directb2s")) {
//            newName = newName + ".directb2s";
//          }
//          client.getBackglassServiceClient().renameBackglass(selectedItem, newName);
//        } catch (Exception ex) {
//          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
//        }
//        onReload();
//      }
//    }
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
//    DirectB2S selectedItem = directb2sList.getSelectionModel().getSelectedItem();
//    if (selectedItem != null) {
//      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
//      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Duplicate Backglass", "Duplicate backglass file \"" + selectedItem.getName() + ".directb2s\"?", null, "Duplicate");
//      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//        try {
//          client.getBackglassServiceClient().duplicateBackglass(selectedItem);
//        } catch (Exception ex) {
//          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
//        }
//        onReload();
//      }
//    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
//    try {
//      DirectB2S selectedItem = directb2sList.getSelectionModel().getSelectedItem();
//      if (selectedItem != null) {
//        Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
//        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Backglass", "Delete backglass file \"" + selectedItem.getName() + ".directb2s\"?", null, "Delete");
//        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//          client.getBackglassServiceClient().deleteBackglass(selectedItem);
//          onReload();
//        }
//      }
//    } catch (Exception ex) {
//      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
//    }
  }

  @FXML
  private void onReload() {
    this.duplicateBtn.setDisable(true);
    this.generateBtn.setDisable(true);
    this.generateAllBtn.setDisable(true);
    this.openImageBtn.setDisable(true);

    setBusy(true);

    new Thread(() -> {
      Platform.runLater(() -> {
        GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
        games = client.getGameService().getKnownGames();

        filterGames(games);
        tableView.setItems(data);

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

        this.duplicateBtn.setDisable(games.isEmpty());
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
  private void onUploadButton() {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.jpeg"),
      new FileChooser.ExtensionFilter("JPG", "*.jpg"),
      new FileChooser.ExtensionFilter("PNG", "*.png"));
    File file = fileChooser.showOpenDialog(stage);
    if (file != null && file.exists()) {
      try {
        boolean result = client.getHighscoreCardsService().uploadHighscoreBackgroundImage(file, null);
        if (result) {
          String baseName = FilenameUtils.getBaseName(file.getName());
          if (!imageList.contains(baseName)) {
            imageList.add(baseName);
          }
        }
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Uploading image failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      }
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
  private void onFontTitleSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "title", titleFontLabel);
  }

  @FXML
  private void onFontTableSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "table", tableFontLabel);
  }

  @FXML
  private void onFontScoreSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "score", scoreFontLabel);
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

  private CardTemplate getCardTemplate() {
    return this.templateCombo.getValue();
  }

  public HighscoreCardsController() {
  }

  private void initFields() {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));

    try {
      templateBeanBinder.bindFontLabel(titleFontLabel, getCardTemplate(), "title");
      templateBeanBinder.bindFontLabel(tableFontLabel, getCardTemplate(), "table");
      templateBeanBinder.bindFontLabel(scoreFontLabel, getCardTemplate(), "score");

      templateBeanBinder.bindColorPicker(fontColorSelector, getCardTemplate(), "fontColor");

      templateBeanBinder.bindCheckbox(useDirectB2SCheckbox, getCardTemplate(), "useDirectB2S");
      useDirectB2SCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          backgroundImageCombo.setDisable(newValue);
          falbackUploadBtn.setDisable(newValue);
        }
      });
      backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
      falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

      templateBeanBinder.bindCheckbox(grayScaleCheckbox, getCardTemplate(), "grayScale");
      templateBeanBinder.bindCheckbox(transparentBackgroundCheckbox, getCardTemplate(), "transparentBackground");
      templateBeanBinder.bindCheckbox(renderTableNameCheckbox, getCardTemplate(), "renderTableName");
      templateBeanBinder.bindCheckbox(renderWheelIconCheckbox, getCardTemplate(), "renderWheelIcon");
      templateBeanBinder.bindCheckbox(renderTitleCheckbox, getCardTemplate(), "renderTitle");

      imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
      backgroundImageCombo.setItems(imageList);
      backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
      backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

      templateBeanBinder.bindComboBox(backgroundImageCombo, getCardTemplate(), "background");
      String backgroundName = getCardTemplate().getBackground();
      if (StringUtils.isEmpty(backgroundName)) {
        backgroundImageCombo.setValue(imageList.get(0));
      }

      templateBeanBinder.bindTextField(titleText, getCardTemplate(), "title", "Highscores");
      templateBeanBinder.bindSlider(brightenSlider, getCardTemplate(), "alphaWhite");
      templateBeanBinder.bindSlider(darkenSlider, getCardTemplate(), "alphaBlack");
      templateBeanBinder.bindSlider(blurSlider, getCardTemplate(), "blur");
      templateBeanBinder.bindSlider(borderSlider, getCardTemplate(), "borderWidth");
      templateBeanBinder.bindSlider(alphaPercentageSpinner, getCardTemplate(), "transparentPercentage");
      templateBeanBinder.bindSpinner(marginTopSpinner, getCardTemplate(), "padding");
      templateBeanBinder.bindSpinner(wheelImageSpinner, getCardTemplate(), "wheelPadding");
      templateBeanBinder.bindSpinner(rowSeparatorSpinner, getCardTemplate(), "rowMargin");

      transparentBackgroundCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          updateTransparencySettings(newValue);
        }
      });
      updateTransparencySettings(transparentBackgroundCheckbox.isSelected());

      templateBeanBinder.bindCheckbox(renderRawHighscore, getCardTemplate(), "rawScore");
      renderRawHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
        wheelImageSpinner.setDisable(t1);
        rowSeparatorSpinner.setDisable(t1);
      });

      wheelImageSpinner.setDisable(renderRawHighscore.isSelected());
      rowSeparatorSpinner.setDisable(renderRawHighscore.isSelected());
    } catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
    }

    rawHighscoreHelp.setCursor(javafx.scene.Cursor.HAND);

    Tooltip tooltip = new Tooltip();
    tooltip.setGraphic(rawHighscoreHelp);
    Tooltip.install(rawHighscoreHelp, new Tooltip("The font size of the highscore text will be adapted according to the number of lines."));

    GameRepresentation value = tableView.getSelectionModel().getSelectedItem();
    refreshRawPreview(Optional.ofNullable(value));
    refreshPreview(Optional.ofNullable(value), false);

    accordion.setExpandedPane(backgroundSettingsPane);
  }

  private void updateTransparencySettings(Boolean newValue) {
    Platform.runLater(() -> {
      grayScaleCheckbox.setDisable(newValue);
      useDirectB2SCheckbox.setDisable(newValue);
      blurSlider.setDisable(newValue);
      brightenSlider.setDisable(newValue);
      darkenSlider.setDisable(newValue);
      backgroundImageCombo.setDisable(newValue || getCardTemplate().isUseDirectB2S());
      alphaPercentageSpinner.setDisable(!newValue);

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
    refreshRawPreview(game);
    if (!game.isPresent()) {
      return;
    }

    int offset = 36;
    Platform.runLater(() -> {
      this.generateBtn.setDisable(!game.isPresent());
      this.openImageBtn.setDisable(!game.isPresent());

      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);

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
            previewStack.getChildren().remove(waitOverlay);
            updateTransparencySettings(this.transparentBackgroundCheckbox.isSelected());
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
    if (emuIds.size() != client.getPinUPPopperService().getGameEmulators().size()) {
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

  @Override
  public void onChanged(Change<? extends GameRepresentation> c) {
    boolean disable = c.getList().isEmpty() || c.getList().size() > 1;
    this.duplicateBtn.setDisable(disable);
    this.generateBtn.setDisable(disable);
    this.generateAllBtn.setDisable(disable);
    this.openImageBtn.setDisable(disable);

    if (c.getList().isEmpty()) {
      refreshPreview(Optional.empty(), false);
    }
    else {
      GameRepresentation gameRepresentation = c.getList().get(0);
      refreshPreview(Optional.ofNullable(gameRepresentation), true);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      editorFooterToolbar.setVisible(false);
      editorFooterToolbar.getStyleClass().add("media-container");

      editorHeaderToolbar.setVisible(false);
      editorHeaderToolbar.getStyleClass().add("media-container");

      editorFrame.getStyleClass().remove("edit-mode-border");


      templateBeanBinder = new BeanBinder(this);
      cardSettingsBinder = new BeanBinder(this);

      cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
      ignoreList.addAll(Arrays.asList("popperScreen"));

      List<CardTemplate> items = new ArrayList<>(client.getHighscoreCardTemplatesClient().getTemplates());
      if (items.isEmpty()) {
        items.add(new CardTemplate());
      }
      templateCombo.setItems(FXCollections.observableList(items));
      templateCombo.getSelectionModel().select(0);

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Card...");

      initFields();

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
      LOG.error("Failed to init highscores: " + e.getMessage(), e);
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

    columnDisplayName.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      Label label = new Label(value.getGameDisplayName());
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty(label);
    });

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      boolean defaultBackgroundAvailable = value.isDefaultBackgroundAvailable();
      if (!defaultBackgroundAvailable) {
        Label label = new Label();
        label.setGraphic(WidgetFactory.createExclamationIcon(null));
        Tooltip tt = new Tooltip("The table does not have a default background.");
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
        return new SimpleObjectProperty(label);
      }
      return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
    });

    tableView.setItems(data);
    tableView.setEditable(true);
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

    columnTemplate.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      Label label = new Label(value.getGameDisplayName());
      return new SimpleObjectProperty("");
    });

    searchField.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      filterGames(games);
      tableView.setItems(data);
    });

    client.getPreferenceService().addListener(this);

    List<GameEmulatorRepresentation> gameEmulators = client.getPinUPPopperService().getGameEmulators();
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

    onReload();
  }
}
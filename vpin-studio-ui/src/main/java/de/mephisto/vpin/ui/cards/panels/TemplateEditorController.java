package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.cards.CardGraphicsHighscore;
import de.mephisto.vpin.commons.fx.cards.CardLayer;
import de.mephisto.vpin.commons.fx.cards.CardLayerCanvas;
import de.mephisto.vpin.commons.fx.cards.CardLayerWheel;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.ImageViewer;
import de.mephisto.vpin.commons.utils.media.MediaPlayerListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreCardResolution;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.cards.HighscoreGeneratorProgressModel;
import de.mephisto.vpin.ui.cards.TemplateAssigmentProgressModel;
import de.mephisto.vpin.ui.util.*;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TemplateEditorController implements Initializable, BindingChangedListener, MediaPlayerListener {
  private final static Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);

  @FXML
  private ComboBox<CardTemplate> templateCombo;

  @FXML
  private StackPane previewStack;

  @FXML
  private Button renameBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Label titleFontLabel;

  @FXML
  private Label scoreFontLabel;

  @FXML
  private Label tableFontLabel;

  @FXML
  private CheckBox useDirectB2SCheckbox;

  @FXML
  private CheckBox grayScaleCheckbox;

  @FXML
  private CheckBox transparentBackgroundCheckbox;

  @FXML
  private CheckBox overlayModeCheckbox;

  @FXML
  private ColorPicker fontColorSelector;

  @FXML
  private ColorPicker friendsFontColorSelector;

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
  private Spinner<Integer> paddingSpinner;

  @FXML
  private Spinner<Integer> wheelSizeSpinner;

  @FXML
  private Spinner<Integer> wheelImageSpinner;

  @FXML
  private Spinner<Integer> rowSeparatorSpinner;

  @FXML
  private Spinner<Integer> maxScoresSpinner;

  @FXML
  private Spinner<Integer> marginTopSpinner;
  @FXML
  private Spinner<Integer> marginRightSpinner;
  @FXML
  private Spinner<Integer> marginBottomSpinner;
  @FXML
  private Spinner<Integer> marginLeftSpinner;

  @FXML
  private Slider alphaPercentageSpinner;

  @FXML
  private CheckBox renderRawHighscore;

  @FXML
  private Button falbackUploadBtn;

  @FXML
  private TitledPane backgroundSettingsPane;
  @FXML
  private TitledPane canvasSettingsPane;
  @FXML
  private TitledPane titleSettingsPane;
  @FXML
  private TitledPane tableNameSettingsPane;
  @FXML
  private TitledPane wheelSettingsPane;
  @FXML
  private TitledPane ScoreSettingsPane;

  @FXML
  private Accordion accordion;

  @FXML
  private CheckBox renderFriendsHighscore;

  @FXML
  private CheckBox renderPositionsCheckbox;

  @FXML
  private Slider canvasAlphaPercentageSlider;

  @FXML
  private ColorPicker canvasColorSelector;

  @FXML
  private Spinner<Integer> canvasXSpinner;

  @FXML
  private Spinner<Integer> canvasYSpinner;

  @FXML
  private Spinner<Integer> canvasWidthSpinner;

  @FXML
  private Spinner<Integer> canvasHeightSpinner;

  @FXML
  private Spinner<Integer> canvasBorderRadiusSpinner;

  @FXML
  private Pane previewPanel;

  @FXML
  private BorderPane previewOverlayPanel;

  @FXML
  private ComboBox<String> screensComboBox;

  //@FXML
  //private Pane imagepane;

  //@FXML
  //private ImageView cardPreview;
  private CardGraphicsHighscore cardPreview = new CardGraphicsHighscore(false);

  @FXML
  private Pane mediaPlayerControl;

  @FXML
  private Button generateAllBtn;

  @FXML
  private Button generateBtn;

  @FXML
  private Button openImageBtn;

  @FXML
  private Button folderBtn;

  @FXML
  private Label resolutionLabel;

  /** the different dragboxes */
  private List<PositionResizer> dragBoxes = new ArrayList<>();

  public Debouncer cardTemplateSaveDebouncer = new Debouncer();

  private BeanBinder templateBeanBinder;
  private ObservableList<String> imageList;

  private Parent waitOverlay;
  private HighscoreCardsController highscoreCardsController;
  private AssetMediaPlayer assetMediaPlayer;
  private Optional<GameRepresentation> gameRepresentation;
  private List<CardTemplate> templates;


  @FXML
  private void onOpenImage() {
    if (gameRepresentation.isPresent()) {
      ByteArrayInputStream s = client.getHighscoreCardsService().getHighscoreCard(gameRepresentation.get());
      MediaUtil.openMedia(s);
    }
  }

  @FXML
  private void onGenerateAll() {
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    String targetScreen = cardSettings.getPopperScreen();
    if (StringUtils.isEmpty(targetScreen)) {
      WidgetFactory.showAlert(stage, "No target screen selected.", "Select a target screen in the preferences.");
    }
    else {
      ProgressDialog.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Cards"));
    }
  }


  @FXML
  private void onFolderBtn() {
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    String popperScreen = cardSettings.getPopperScreen();
    if (!StringUtils.isEmpty(popperScreen)) {
      VPinScreen screen = VPinScreen.valueOfScreen(popperScreen);
      File screenDir = client.getFrontendService().getMediaDirectory(-1, screen.name());
      SystemUtil.openFolder(screenDir);
    }
  }

  @FXML
  private void onStart() {
    if (assetMediaPlayer != null) {
      assetMediaPlayer.getMediaPlayer().play();
    }
  }

  @FXML
  private void onStop() {
    if (assetMediaPlayer != null) {
      assetMediaPlayer.getMediaPlayer().pause();
    }
  }

  @FXML
  private void onCreate(ActionEvent e) {
    CardTemplate selection = this.templateCombo.getValue();
    String gameName = gameRepresentation.get().getGameName();

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String s = WidgetFactory.showInputDialog(stage, "New Template", "Enter Template Name", "Enter a meaningful name that identifies the card design.", "The values of the selected template \"" + selection.getName() + "\" will be used as default.", gameName);
    if (!StringUtils.isEmpty(s)) {
      ObservableList<CardTemplate> items = this.templateCombo.getItems();

      Optional<CardTemplate> duplicate = items.stream().filter(t -> t.getName().equals(s)).findFirst();
      if (duplicate.isPresent()) {
        WidgetFactory.showAlert(stage, "Error", "A template with the name \"" + s + "\" already exist.");
        return;
      }

      selection.setName(s);
      selection.setId(null);
      JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().save(selection))
      .thenAcceptLater(newTemplate -> {
          loadTemplates();
          this.templateCombo.setValue(newTemplate);

          highscoreCardsController.refresh(gameRepresentation, templates, false);
        })
      .onErrorLater(ex -> {
          LOG.error("Failed to create new template: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Creating Template Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
        });
    }
  }

  @FXML
  private void onRename(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    CardTemplate cardTemplate = getCardTemplate();
    String s = WidgetFactory.showInputDialog(stage, "Rename Template", "Enter Template Name", "Enter the new template name.", null, cardTemplate.getName());
    if (!StringUtils.isEmpty(s) && !cardTemplate.getName().equals(s)) {
      cardTemplate.setName(s);

      JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().save(cardTemplate))
      .thenAcceptLater(updatedTemplate -> {
          loadTemplates();  
          this.templateCombo.setValue(updatedTemplate);

          assignTemplate(updatedTemplate);
          highscoreCardsController.refresh(gameRepresentation, templates, true);
        })
      .onErrorLater(ex -> {
        LOG.error("Failed to rename template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Renaming Template Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
      });
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    CardTemplate cardTemplate = getCardTemplate();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Template", "Delete Template \"" + cardTemplate.getName() + "\"?", "Assigned tables will use the default template again.", "Delete");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getHighscoreCardTemplatesClient().deleteTemplate(cardTemplate.getId());
        Platform.runLater(() -> {
          CardTemplate defaultTemplate = templates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst().get();

          loadTemplates();
          this.templateCombo.setValue(defaultTemplate);

          assignTemplate(defaultTemplate);
          highscoreCardsController.refresh(gameRepresentation, templates, true);
        });
      }
      catch (Exception ex) {
        LOG.error("Failed to delete template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Template Deletion Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
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
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Uploading image failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFontTitleSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "title", titleFontLabel);
  }

  @FXML
  private void onFontTitleApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ObservableList<CardTemplate> items = templateCombo.getItems();
      CardTemplate selection = getCardTemplate();
      for (CardTemplate item : items) {
        item.setTitleFontName(selection.getTitleFontName());
        item.setTitleFontSize(selection.getTitleFontSize());
        item.setTitleFontStyle(selection.getTitleFontStyle());
      }
      saveAllTemplates(items);
    }
  }

  @FXML
  private void onFontTableSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "table", tableFontLabel);
  }

  @FXML
  private void onFontTableApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ObservableList<CardTemplate> items = templateCombo.getItems();
      CardTemplate selection = getCardTemplate();
      for (CardTemplate item : items) {
        item.setTableFontName(selection.getTableFontName());
        item.setTableFontSize(selection.getTableFontSize());
        item.setTableFontStyle(selection.getTableFontStyle());
      }
      saveAllTemplates(items);
    }
  }

  @FXML
  private void onFontScoreSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "score", scoreFontLabel);
  }

  @FXML
  private void onFontScoreApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ObservableList<CardTemplate> items = templateCombo.getItems();
      CardTemplate selection = getCardTemplate();
      for (CardTemplate item : items) {
        item.setScoreFontName(selection.getScoreFontName());
        item.setScoreFontSize(selection.getScoreFontSize());
        item.setScoreFontStyle(selection.getScoreFontStyle());
      }
      saveAllTemplates(items);
    }
  }

  private void saveAllTemplates(List<CardTemplate> items) {
    ProgressDialog.createProgressDialog(new WaitNProgressModel<>("Save Templates", items,
    item -> "Saving Highscore Card Templates " + item.getName() + "...", 
    item -> {
      client.getHighscoreCardTemplatesClient().save(item);
    }));
    WidgetFactory.showConfirmation(stage, "Update Finished", "Updated " + items.size() + " templates.");
  }

  public CardTemplate getCardTemplate() {
    return this.templateCombo.getValue();
  }

  private void setTemplate(CardTemplate cardTemplate) {
    if (templateBeanBinder == null) {
      initBindings();
    }

    deleteBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));
    renameBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));

    templateBeanBinder.setBean(cardTemplate);
    templateBeanBinder.setPaused(true);

    templateBeanBinder.setIconVisibility(canvasSettingsPane, cardTemplate.isRenderCanvas());
    templateBeanBinder.setIconVisibility(titleSettingsPane, cardTemplate.isRenderTitle());
    templateBeanBinder.setIconVisibility(tableNameSettingsPane, cardTemplate.isRenderTableName());
    templateBeanBinder.setIconVisibility(wheelSettingsPane, cardTemplate.isRenderWheelIcon());

    // Canvas
    templateBeanBinder.setColorPickerValue(canvasColorSelector, getCardTemplate(), "canvasBackground");
    canvasXSpinner.getValueFactory().setValue(cardTemplate.getCanvasX());
    canvasYSpinner.getValueFactory().setValue(cardTemplate.getCanvasY());
    canvasWidthSpinner.getValueFactory().setValue(cardTemplate.getCanvasWidth());
    canvasHeightSpinner.getValueFactory().setValue(cardTemplate.getCanvasHeight());
    canvasBorderRadiusSpinner.getValueFactory().setValue(cardTemplate.getCanvasBorderRadius());
    canvasAlphaPercentageSlider.setValue(cardTemplate.getCanvasAlphaPercentage());

    // Title
    titleText.setText(cardTemplate.getTitle());
    templateBeanBinder.setFontLabel(titleFontLabel, cardTemplate, "title");

    // TableName
    templateBeanBinder.setFontLabel(tableFontLabel, cardTemplate, "table");

    // Wheel
    wheelImageSpinner.getValueFactory().setValue(cardTemplate.getWheelPadding());
    wheelSizeSpinner.getValueFactory().setValue(cardTemplate.getWheelSize());

    // Scores
    templateBeanBinder.setFontLabel(scoreFontLabel, cardTemplate, "score");
    templateBeanBinder.setColorPickerValue(fontColorSelector, cardTemplate, "fontColor");
    templateBeanBinder.setColorPickerValue(friendsFontColorSelector, cardTemplate, "friendsFontColor");


    useDirectB2SCheckbox.setSelected(cardTemplate.isUseDirectB2S());
    backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
    falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

    grayScaleCheckbox.setSelected(cardTemplate.isGrayScale());
    transparentBackgroundCheckbox.setSelected(cardTemplate.isTransparentBackground());
    overlayModeCheckbox.setSelected(cardTemplate.isOverlayMode());
    
    brightenSlider.setValue(cardTemplate.getAlphaWhite());
    darkenSlider.setValue(cardTemplate.getAlphaBlack());
    blurSlider.setValue(cardTemplate.getBlur());
    borderSlider.setValue(cardTemplate.getBorderWidth());
    alphaPercentageSpinner.setValue(cardTemplate.getTransparentPercentage());
    paddingSpinner.getValueFactory().setValue(cardTemplate.getPadding());
    marginTopSpinner.getValueFactory().setValue(cardTemplate.getMarginTop());
    marginRightSpinner.getValueFactory().setValue(cardTemplate.getMarginRight());
    marginBottomSpinner.getValueFactory().setValue(cardTemplate.getMarginBottom());
    marginLeftSpinner.getValueFactory().setValue(cardTemplate.getMarginLeft());
    maxScoresSpinner.getValueFactory().setValue(cardTemplate.getMaxScores());
    rowSeparatorSpinner.getValueFactory().setValue(cardTemplate.getRowMargin());

    renderFriendsHighscore.setSelected(cardTemplate.isRenderFriends());
    renderRawHighscore.setSelected(cardTemplate.isRawScore());
    maxScoresSpinner.setDisable(renderRawHighscore.isSelected());
    renderPositionsCheckbox.setDisable(renderRawHighscore.isSelected());


    templateBeanBinder.setPaused(false);
    cardPreview.setTemplate(cardTemplate);
    refreshPreview(this.gameRepresentation, true);
  }

  private void initBindings() {
    try {
      templateBeanBinder = new BeanBinder(this);
      templateBeanBinder.setBean(this.getCardTemplate());

      templateBeanBinder.bindColorPicker(fontColorSelector, "fontColor");
      templateBeanBinder.bindColorPicker(friendsFontColorSelector, "friendsFontColor");

      templateBeanBinder.bindCheckbox(useDirectB2SCheckbox, "useDirectB2S");
      useDirectB2SCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          backgroundImageCombo.setDisable(newValue);
          falbackUploadBtn.setDisable(newValue);
        }
      });
      backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
      falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

      List<VPinScreen> VPinScreens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
      VPinScreens.remove(VPinScreen.Audio);
      VPinScreens.remove(VPinScreen.AudioLaunch);
      VPinScreens.remove(VPinScreen.GameInfo);
      VPinScreens.remove(VPinScreen.GameHelp);
      VPinScreens.remove(VPinScreen.DMD);
      VPinScreens.remove(VPinScreen.Wheel);
      VPinScreens.remove(VPinScreen.Other2);
      VPinScreens.remove(VPinScreen.PlayField);
      VPinScreens.remove(VPinScreen.Loading);
      screensComboBox.setItems(FXCollections.observableList(VPinScreens.stream().map(p -> p.name()).collect(Collectors.toList())));
      screensComboBox.setDisable(!getCardTemplate().isOverlayMode());

      templateBeanBinder.bindCheckbox(grayScaleCheckbox, "grayScale");
      templateBeanBinder.bindCheckbox(transparentBackgroundCheckbox, "transparentBackground");

      templateBeanBinder.bindVisibilityIcon(canvasSettingsPane, "renderCanvas");
      templateBeanBinder.bindVisibilityIcon(titleSettingsPane, "renderTitle");
      templateBeanBinder.bindVisibilityIcon(tableNameSettingsPane, "renderTableName");
      templateBeanBinder.bindVisibilityIcon(wheelSettingsPane, "renderWheelIcon");

      // Canvas
      templateBeanBinder.bindColorPicker(canvasColorSelector, "canvasBackground");
      templateBeanBinder.bindSlider(canvasAlphaPercentageSlider, "canvasAlphaPercentage");
      templateBeanBinder.bindSpinner(canvasXSpinner, "canvasX", 0, 1920);
      templateBeanBinder.bindSpinner(canvasYSpinner, "canvasY", 0, 1920);
      templateBeanBinder.bindSpinner(canvasWidthSpinner, "canvasWidth", 0, 1920);
      templateBeanBinder.bindSpinner(canvasHeightSpinner, "canvasHeight", 0, 1080);
      templateBeanBinder.bindSpinner(canvasBorderRadiusSpinner, "canvasBorderRadius", 0, 100);

      // Wheel
      templateBeanBinder.bindSpinner(wheelSizeSpinner, "wheelSize");
      templateBeanBinder.bindSpinner(wheelImageSpinner, "wheelPadding");

      // Title
      templateBeanBinder.bindTextField(titleText, "title");

      templateBeanBinder.bindCheckbox(renderPositionsCheckbox, "renderPositions");
      templateBeanBinder.bindCheckbox(overlayModeCheckbox, "overlayMode");

      overlayModeCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          screensComboBox.setDisable(!newValue);
        }
      });

      imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
      backgroundImageCombo.setItems(imageList);
      backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
      backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

      templateBeanBinder.bindComboBox(backgroundImageCombo, getCardTemplate(), "background");
      String backgroundName = getCardTemplate().getBackground();
      if (StringUtils.isEmpty(backgroundName)) {
        backgroundImageCombo.setValue(imageList.get(0));
      }

      templateBeanBinder.bindComboBox(screensComboBox, getCardTemplate(), "overlayScreen");

      templateBeanBinder.bindSlider(brightenSlider, "alphaWhite");
      templateBeanBinder.bindSlider(brightenSlider, getCardTemplate().getAlphaWhite(), alpha -> getCardTemplate().setAlphaWhite(alpha));
      templateBeanBinder.bindSlider(darkenSlider, "alphaBlack");
      templateBeanBinder.bindSlider(blurSlider, "blur");
      templateBeanBinder.bindSlider(borderSlider, "borderWidth");
      templateBeanBinder.bindSlider(alphaPercentageSpinner, "transparentPercentage");

      templateBeanBinder.bindSpinner(paddingSpinner, "padding");
      templateBeanBinder.bindSpinner(marginTopSpinner, "marginTop");
      templateBeanBinder.bindSpinner(marginRightSpinner, "marginRight");
      templateBeanBinder.bindSpinner(marginBottomSpinner, "marginBottom");
      templateBeanBinder.bindSpinner(marginLeftSpinner, "marginLeft");
      templateBeanBinder.bindSpinner(maxScoresSpinner, "maxScores", 0, 100);
      templateBeanBinder.bindSpinner(rowSeparatorSpinner, "rowMargin", 0, 300);

      transparentBackgroundCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          overlayModeCheckbox.setDisable(!getCardTemplate().isTransparentBackground());
          overlayModeCheckbox.setSelected(getCardTemplate().isOverlayMode());
          screensComboBox.setDisable(!getCardTemplate().isOverlayMode() || !getCardTemplate().isTransparentBackground());

          if (newValue && alphaPercentageSpinner.getValue() <= 0) {
            alphaPercentageSpinner.setValue(50);
          }
        }
      });

      templateBeanBinder.bindCheckbox(renderRawHighscore, "rawScore");
      renderRawHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
        maxScoresSpinner.setDisable(t1);
        renderPositionsCheckbox.setDisable(t1);
        renderFriendsHighscore.setDisable(t1);
      });

      maxScoresSpinner.setDisable(renderRawHighscore.isSelected());
    }
    catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
    }
  }

  private void refreshTransparency() {
    boolean enabled = getCardTemplate().isTransparentBackground();
    grayScaleCheckbox.setDisable(enabled);
    useDirectB2SCheckbox.setDisable(enabled);
    blurSlider.setDisable(enabled);
    brightenSlider.setDisable(enabled);
    darkenSlider.setDisable(enabled);
    backgroundImageCombo.setDisable(enabled || getCardTemplate().isUseDirectB2S());
    alphaPercentageSpinner.setDisable(!enabled);

    if (enabled) {
      if (!getCardTemplate().isOverlayMode()) {
        Image backgroundImage = new Image(Studio.class.getResourceAsStream("transparent.png"));
        BackgroundImage myBI = new BackgroundImage(backgroundImage,
            BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
            BackgroundSize.DEFAULT);
        previewPanel.setBackground(new Background(myBI));
      }
      else {
        //the existing CSS class will hide the video else
        previewPanel.setBackground(Background.EMPTY);
      }
    }
    else {
      previewPanel.setBackground(new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null)));
    }
  }


  @FXML
  private void onGenerate() {
    if (this.gameRepresentation.isPresent()) {
      CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
      String targetScreen = cardSettings.getPopperScreen();
      if (StringUtils.isEmpty(targetScreen)) {
        WidgetFactory.showAlert(stage, "Not target screen selected.", "Select a target screen in the preferences.");
      }
      else {
        ProgressDialog.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Card", this.gameRepresentation.get()));
      }
      refreshPreview(this.gameRepresentation, true);
    }
  }

  @FXML
  private void onGenerateClick() {
    JFXFuture.runAsync(() -> client.getHighscoreCardTemplatesClient().save((CardTemplate) this.templateBeanBinder.getBean()))
      .thenLater(() -> refreshPreview(this.gameRepresentation, true))
      .onErrorLater(e -> {
        LOG.error("Failed to save template: " + e.getMessage());
        WidgetFactory.showAlert(stage, "Error", "Failed to save template: " + e.getMessage());
      });
  }

  private void refreshPreview(Optional<GameRepresentation> game, boolean regenerate) {
    this.openImageBtn.setDisable(true);
    this.generateBtn.setDisable(true);
    this.generateAllBtn.setDisable(true);
    mediaPlayerControl.setVisible(false);

    if (!game.isPresent()) {
      // empty game information
      cardPreview.setData(null);
      return;
    }
    previewStack.getChildren().remove(waitOverlay);
    previewStack.getChildren().add(waitOverlay);
    refreshTransparency();
    refreshOverlayBackgroundPreview();

    JFXFuture.supplyAsync(() -> client.getHighscoreCardsService().getHighscoreCardData(game.get(), templateCombo.getValue()))
      .thenAcceptLater(cardData -> {
        cardPreview.setData(cardData);
      });

    previewStack.getChildren().remove(waitOverlay);
    this.openImageBtn.setDisable(false);
    this.generateBtn.setDisable(false);
    this.generateAllBtn.setDisable(false);
  }


  private void refreshOverlayBackgroundPreview() {
    if (assetMediaPlayer != null) {
      assetMediaPlayer.disposeMedia();
      assetMediaPlayer.setMediaViewSize(0, 0);
    }
    mediaPlayerControl.setVisible(false);
    previewOverlayPanel.setVisible(false);

    if (this.gameRepresentation.isPresent() && getCardTemplate().getOverlayScreen() != null) {
      VPinScreen overlayScreen = VPinScreen.valueOf(getCardTemplate().getOverlayScreen());

      JFXFuture.supplyAsync(() -> client.getFrontendService().getFrontendMedia(this.gameRepresentation.get().getId()))
      .thenAcceptLater(frontendMedia -> {
        FrontendMediaItemRepresentation defaultMediaItem = frontendMedia.getDefaultMediaItem(overlayScreen);
        if (defaultMediaItem != null) {
          assetMediaPlayer = WidgetFactory.addMediaItemToBorderPane(client, defaultMediaItem, previewOverlayPanel, this);
          //images do not have a media player
          if (assetMediaPlayer != null) {
            double fitwith = stage.getWidth() - 900; // was cardPreview.getFitWidth()
            double fitheight = stage.getHeight() - 200; // was cardPreview.getFitHeight()
            assetMediaPlayer.setSize(fitwith, fitheight);
            mediaPlayerControl.setVisible(true);
          }

          if (previewOverlayPanel.getCenter() instanceof ImageViewer) {
            ImageViewer imageViewer = (ImageViewer) previewOverlayPanel.getCenter();
            // FIXME OLE imageViewer.scaleForTemplate(cardPreview);
          }

          previewOverlayPanel.setVisible(true);
        }
      });
    }
  }


  @Override
  public void beanPropertyChanged(Object bean, String key, Object value) {
    if (bean instanceof CardTemplate) {
      saveCardTemplate((CardTemplate) bean);
    }
  }

  private void saveCardTemplate(CardTemplate cardTemplate) {
    cardTemplateSaveDebouncer.debounce("cardTemplate", () -> {
      JFXFuture.runAsync(() -> client.getHighscoreCardTemplatesClient().save(cardTemplate))
        .thenLater(() -> refreshPreview(this.gameRepresentation, true))
        .onErrorLater(e -> {
          LOG.error("Failed to save template: " + e.getMessage());
          WidgetFactory.showAlert(stage, "Error", "Failed to save template: " + e.getMessage());
        });

      }, 1000);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    friendsFontColorSelector.managedProperty().bindBidirectional(friendsFontColorSelector.visibleProperty());
    renderFriendsHighscore.managedProperty().bindBidirectional(renderFriendsHighscore.visibleProperty());

    friendsFontColorSelector.setVisible(Features.MANIA_ENABLED && Features.MANIA_SOCIAL_ENABLED);
    renderFriendsHighscore.setVisible(Features.MANIA_ENABLED && Features.MANIA_SOCIAL_ENABLED);

    folderBtn.setVisible(SystemUtil.isFolderActionSupported());
    resolutionLabel.setText("");

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendUtil.replaceName(folderBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    try {
      this.deleteBtn.setDisable(true);
      this.renameBtn.setDisable(true);

      loadTemplates();

      templateCombo.valueProperty().addListener(new ChangeListener<CardTemplate>() {
        @Override
        public void changed(ObservableValue<? extends CardTemplate> observable, CardTemplate oldValue, CardTemplate newValue) {
          if (newValue != null) {
            setTemplate(newValue);
            if (gameRepresentation.isPresent()) {
              assignTemplate(newValue);
              highscoreCardsController.refresh(gameRepresentation, templates, false);
            }
          }
        }
      });

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Getting Card Data...");

      accordion.setExpandedPane(backgroundSettingsPane);

      previewStack.setBackground(Background.fill(Color.AQUA));
      previewPanel.setBackground(Background.fill(Color.ALICEBLUE));

      previewStack.widthProperty().addListener((obs, o, n) -> resizeCardPreview(n.doubleValue(), previewStack.getHeight(), true));
      previewStack.heightProperty().addListener((obs, o, n) -> resizeCardPreview(previewStack.getWidth(), n.doubleValue(), false));
      previewPanel.getChildren().add(cardPreview);

      cardPreview.setOnMousePressed(e -> onDragboxEnter(e));

    }
    catch (Exception e) {
      LOG.error("Failed to initialize template editor: " + e.getMessage(), e);
    }
  }

  private void loadTemplates() {
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS);
    HighscoreCardResolution res = cardSettings.getCardResolution();
    this.templates = new ArrayList<>(client.getHighscoreCardTemplatesClient().getTemplates());
    if (res != null) {
      for (CardTemplate template : templates) {
        if (template.getReferenceWidth() < 0 || template.getReferenceHeight() < 0) {
          template.setReferenceWidth(res.toWidth());
          template.setReferenceHeight(res.toHeight());
        }
      }
    }
    templateCombo.setItems(FXCollections.observableList(templates));
  }

  private void resizeCardPreview(double width, double height, boolean forceWidth) {
    // make sure the panel is full size always
    previewPanel.resizeRelocate(0, 0, width, height);

    if (width > 0 && height > 0) {
      double aspectRatio = 16.0 / 9.0;
      double newWidth = width;
      double newHeight = height;
      double offSetX = 0;
      double offSetY = 0;
      if (forceWidth) {
        newHeight = newWidth / aspectRatio;
        if (newHeight > height) {
          newHeight = height;
          newWidth = height * aspectRatio;
          // constraint width and center horizontally
          offSetX = (width - newWidth) / 2;
        }
        else {
          offSetY = (height - newHeight) / 2;
        }
      }
      else {
        newWidth = newHeight * aspectRatio;
        if (newWidth > width) {
          newWidth = width;
          newHeight = width / aspectRatio;
          // constraint width and center horizontally
          offSetY = (height - newHeight) / 2;
        }
        else {
          offSetX = (width - newWidth) / 2;
        }
      }
      cardPreview.resizeRelocate(offSetX, offSetY, newWidth, newHeight);
    }
  }

  //-------------------------------------------

  // imagepane 

  public void onDragboxEnter(MouseEvent e) {
      CardLayer layer = cardPreview.selectCardLayer(e.getX(), e.getY());
      loadDragBoxes(layer);
      e.consume();
  }

  private void configureSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                                ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {
    IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
  }

  private void loadDragBoxes(CardLayer layer) {

    // first delete previous boxes
    for (PositionResizer dragBox : dragBoxes) {
      dragBox.removeFromPane(cardPreview);
    }
    dragBoxes.clear();

    if (layer != null) {

      // The canvas box
      PositionResizer dragBox = new PositionResizer();

      CardTemplate cardtemplate = getCardTemplate();
      dragBox.setBounds(0, 0, cardtemplate.getReferenceWidth(), cardtemplate.getReferenceHeight());

      // keep the order of setters !
      double zoomX = cardPreview.getZoomX();
      double zoomY = cardPreview.getZoomY();
      dragBox.setZoomX(zoomX);
      dragBox.setZoomY(zoomY);

      dragBox.setWidth((int) (layer.getWidth() / zoomX));
      dragBox.setHeight((int) (layer.getHeight() / zoomY));
      dragBox.setX((int) (layer.getLocX() / zoomX));
      dragBox.setY((int) (layer.getLocY() / zoomY));

      if (layer instanceof CardLayerCanvas) {
        configureSpinner(canvasXSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
        configureSpinner(canvasYSpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
        configureSpinner(canvasWidthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
        configureSpinner(canvasHeightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());
      }
      else if (layer instanceof CardLayerWheel) {
        // force aspect ratio of 1 for 
        dragBox.setAspectRatio(1.0);
        configureSpinner(wheelSizeSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
      }

      // setup linkages between spinner and our dragbox
      dragBox.selectProperty().addListener((obs, oldV, newV) -> {
        if (newV) {
          templateBeanBinder.setPaused(true);
        }
        else {
          //CardTemplate cardtemplate = getCardTemplate();
          //cardtemplate.setCanvasX((int) dragBox.getX());
          //cardtemplate.setCanvasY((int) dragBox.getY());
          //cardtemplate.setCanvasWidth((int) dragBox.getWidth());
          //cardtemplate.setCanvasHeight((int) dragBox.getHeight());
          //dragBox.removeFromPane(cardPreview);
          //dragBoxes.remove(dragBox);
          templateBeanBinder.setPaused(false);
        }
      });

      dragBox.select();
      dragBox.addToPane(cardPreview);
      dragBoxes.add(dragBox);
    }   
  }

  //-----------------------------------------

  private void assignTemplate(CardTemplate newValue) {
    List<GameRepresentation> selection = highscoreCardsController.getSelection();
    ProgressDialog.createProgressDialog(new TemplateAssigmentProgressModel(selection, newValue.getId()));
  }

  public void setCardsController(HighscoreCardsController highscoreCardsController) {
    this.highscoreCardsController = highscoreCardsController;
  }

  public void selectTable(Optional<GameRepresentation> gameRepresentation, boolean refresh) {
    this.gameRepresentation = gameRepresentation;
    if (this.gameRepresentation.isPresent()) {
      GameRepresentation game = gameRepresentation.get();
      CardTemplate template = templates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst().get();
      if (game.getTemplateId() != null) {
        Optional<CardTemplate> first = templates.stream().filter(g -> g.getId().equals(game.getTemplateId())).findFirst();
        if (first.isPresent()) {
          template = first.get();
        }
      }
      if (template.equals(templateCombo.getValue())) {
        setTemplate(template);
      }
      else {
        templateCombo.setValue(template);
      }
    }
  }

  //-------------- MediaPlayerListener
  @Override
  public void onReady(Media media) {
    if (media != null && media.getWidth() > 0) {
      resolutionLabel.setText("Resolution: " + media.getWidth() + " x " + media.getHeight());
    }
  }

  @Override
  public void onDispose() {
    this.resolutionLabel.setText("");
  }
}

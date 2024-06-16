package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.cards.HighscoreGeneratorProgressModel;
import de.mephisto.vpin.ui.cards.TemplateAssigmentProgressModel;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;
import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TemplateEditorController implements Initializable, BindingChangedListener {
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
  private Accordion accordion;

  @FXML
  private CheckBox renderTitleCheckbox;

  @FXML
  private CheckBox renderTableNameCheckbox;

  @FXML
  private CheckBox renderWheelIconCheckbox;

  @FXML
  private CheckBox renderPositionsCheckbox;

  @FXML
  private CheckBox renderCanvasCheckbox;

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

  @FXML
  private ImageView cardPreview;

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
      WidgetFactory.showAlert(stage, "Not target screen selected.", "Select a target screen in the preferences.");
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
      GameEmulatorRepresentation gameEmulator = client.getFrontendService().getDefaultGameEmulator();
      String mediaDir = gameEmulator.getMediaDirectory();
      File screenDir = new File(mediaDir, screen.name());
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
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String s = WidgetFactory.showInputDialog(stage, "New Template", "Enter Template Name", "Enter a meaningful name that identifies the card design.", "The values of the selected template will be used as default.", null);
    if (!StringUtils.isEmpty(s)) {
      ObservableList<CardTemplate> items = this.templateCombo.getItems();

      Optional<CardTemplate> duplicate = items.stream().filter(t -> t.getName().equals(s)).findFirst();
      if (duplicate.isPresent()) {
        WidgetFactory.showAlert(stage, "Error", "A template with the name \"" + s + "\" already exist.");
        return;
      }

      CardTemplate selection = this.templateCombo.getValue();
      Platform.runLater(() -> {
        selection.setName(s);
        selection.setId(null);
        try {
          CardTemplate newTemplate = client.getHighscoreCardTemplatesClient().save(selection);
          templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
          this.templateCombo.setValue(newTemplate);

          highscoreCardsController.refresh(gameRepresentation, templates, false);
        }
        catch (Exception ex) {
          LOG.error("Failed to create new template: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Creating Template Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
        }
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

      try {
        CardTemplate updatedTemplate = client.getHighscoreCardTemplatesClient().save(cardTemplate);
        Platform.runLater(() -> {
          this.templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
          this.templateCombo.setValue(updatedTemplate);

          assignTemplate(updatedTemplate);
          highscoreCardsController.refresh(gameRepresentation, templates, true);
        });
      }
      catch (Exception ex) {
        LOG.error("Failed to rename template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Renaming Template Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
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

          this.templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
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
  private void onFontTableSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "table", tableFontLabel);
  }

  @FXML
  private void onFontScoreSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "score", scoreFontLabel);
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

    titleFontLabel.setText(cardTemplate.getTitleFontName());
    tableFontLabel.setText(cardTemplate.getTableFontName());
    scoreFontLabel.setText(cardTemplate.getScoreFontName());

    templateBeanBinder.setColorPickerValue(fontColorSelector, getCardTemplate(), "fontColor");

    useDirectB2SCheckbox.setSelected(cardTemplate.isUseDirectB2S());
    backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
    falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

    grayScaleCheckbox.setSelected(cardTemplate.isGrayScale());
    transparentBackgroundCheckbox.setSelected(cardTemplate.isTransparentBackground());
    overlayModeCheckbox.setSelected(cardTemplate.isOverlayMode());
    renderTableNameCheckbox.setSelected(cardTemplate.isRenderTableName());
    renderWheelIconCheckbox.setSelected(cardTemplate.isRenderWheelIcon());
    renderTitleCheckbox.setSelected(cardTemplate.isRenderTitle());

    titleText.setText(cardTemplate.getTitle());
    brightenSlider.setValue(cardTemplate.getAlphaWhite());
    darkenSlider.setValue(cardTemplate.getAlphaBlack());
    blurSlider.setValue(cardTemplate.getBlur());
    borderSlider.setValue(cardTemplate.getBorderWidth());
    alphaPercentageSpinner.setValue(cardTemplate.getTransparentPercentage());
    paddingSpinner.getValueFactory().setValue(cardTemplate.getPadding());
    wheelSizeSpinner.getValueFactory().setValue(cardTemplate.getWheelSize());
    marginTopSpinner.getValueFactory().setValue(cardTemplate.getMarginTop());
    marginRightSpinner.getValueFactory().setValue(cardTemplate.getMarginRight());
    marginBottomSpinner.getValueFactory().setValue(cardTemplate.getMarginBottom());
    marginLeftSpinner.getValueFactory().setValue(cardTemplate.getMarginLeft());
    wheelImageSpinner.getValueFactory().setValue(cardTemplate.getWheelPadding());
    maxScoresSpinner.getValueFactory().setValue(cardTemplate.getMaxScores());
    rowSeparatorSpinner.getValueFactory().setValue(cardTemplate.getRowMargin());

    renderRawHighscore.setSelected(cardTemplate.isRawScore());
    wheelImageSpinner.setDisable(renderRawHighscore.isSelected());
    maxScoresSpinner.setDisable(renderRawHighscore.isSelected());
    rowSeparatorSpinner.setDisable(renderRawHighscore.isSelected());
    renderPositionsCheckbox.setDisable(renderRawHighscore.isSelected());

    templateBeanBinder.setColorPickerValue(canvasColorSelector, getCardTemplate(), "canvasBackground");

    renderCanvasCheckbox.setSelected(cardTemplate.isRenderCanvas());
    canvasXSpinner.getValueFactory().setValue(cardTemplate.getCanvasX());
    canvasYSpinner.getValueFactory().setValue(cardTemplate.getCanvasY());
    canvasWidthSpinner.getValueFactory().setValue(cardTemplate.getCanvasWidth());
    canvasHeightSpinner.getValueFactory().setValue(cardTemplate.getCanvasHeight());
    canvasBorderRadiusSpinner.getValueFactory().setValue(cardTemplate.getCanvasBorderRadius());
    canvasAlphaPercentageSlider.setValue(cardTemplate.getCanvasAlphaPercentage());

    canvasAlphaPercentageSlider.setDisable(!renderCanvasCheckbox.isSelected());
    canvasColorSelector.setDisable(!renderCanvasCheckbox.isSelected());
    canvasXSpinner.setDisable(!renderCanvasCheckbox.isSelected());
    canvasYSpinner.setDisable(!renderCanvasCheckbox.isSelected());
    canvasWidthSpinner.setDisable(!renderCanvasCheckbox.isSelected());
    canvasHeightSpinner.setDisable(!renderCanvasCheckbox.isSelected());
    canvasBorderRadiusSpinner.setDisable(!renderCanvasCheckbox.isSelected());

    templateBeanBinder.setPaused(false);

    refreshPreview(this.gameRepresentation, true);
  }


  private void initBindings() {
    try {
      templateBeanBinder = new BeanBinder(this);
      templateBeanBinder.setBean(this.getCardTemplate());

      templateBeanBinder.bindFontLabel(titleFontLabel, getCardTemplate(), "title");
      templateBeanBinder.bindFontLabel(tableFontLabel, getCardTemplate(), "table");
      templateBeanBinder.bindFontLabel(scoreFontLabel, getCardTemplate(), "score");

      templateBeanBinder.bindColorPicker(fontColorSelector, getCardTemplate(), "fontColor");
      templateBeanBinder.bindColorPicker(canvasColorSelector, getCardTemplate(), "canvasBackground");

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

      templateBeanBinder.bindCheckbox(grayScaleCheckbox, getCardTemplate(), "grayScale");
      templateBeanBinder.bindCheckbox(transparentBackgroundCheckbox, getCardTemplate(), "transparentBackground");
      templateBeanBinder.bindCheckbox(renderTableNameCheckbox, getCardTemplate(), "renderTableName");
      templateBeanBinder.bindCheckbox(renderWheelIconCheckbox, getCardTemplate(), "renderWheelIcon");
      templateBeanBinder.bindCheckbox(renderTitleCheckbox, getCardTemplate(), "renderTitle");
      templateBeanBinder.bindCheckbox(renderPositionsCheckbox, getCardTemplate(), "renderPositions");
      templateBeanBinder.bindCheckbox(renderCanvasCheckbox, getCardTemplate(), "renderCanvas");
      templateBeanBinder.bindCheckbox(overlayModeCheckbox, getCardTemplate(), "overlayMode");

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

      templateBeanBinder.bindTextField(titleText, getCardTemplate(), "title", "Highscores");
      templateBeanBinder.bindSlider(brightenSlider, getCardTemplate(), "alphaWhite");
      templateBeanBinder.bindSlider(darkenSlider, getCardTemplate(), "alphaBlack");
      templateBeanBinder.bindSlider(blurSlider, getCardTemplate(), "blur");
      templateBeanBinder.bindSlider(borderSlider, getCardTemplate(), "borderWidth");
      templateBeanBinder.bindSlider(alphaPercentageSpinner, getCardTemplate(), "transparentPercentage");
      templateBeanBinder.bindSlider(canvasAlphaPercentageSlider, getCardTemplate(), "canvasAlphaPercentage");
      templateBeanBinder.bindSpinner(wheelSizeSpinner, getCardTemplate(), "wheelSize");
      templateBeanBinder.bindSpinner(paddingSpinner, getCardTemplate(), "padding");
      templateBeanBinder.bindSpinner(marginTopSpinner, getCardTemplate(), "marginTop");
      templateBeanBinder.bindSpinner(marginRightSpinner, getCardTemplate(), "marginRight");
      templateBeanBinder.bindSpinner(marginBottomSpinner, getCardTemplate(), "marginBottom");
      templateBeanBinder.bindSpinner(marginLeftSpinner, getCardTemplate(), "marginLeft");
      templateBeanBinder.bindSpinner(wheelImageSpinner, getCardTemplate(), "wheelPadding");
      templateBeanBinder.bindSpinner(maxScoresSpinner, getCardTemplate(), "maxScores", 0, 100);
      templateBeanBinder.bindSpinner(rowSeparatorSpinner, getCardTemplate(), "rowMargin", 0, 300);

      templateBeanBinder.bindSpinner(canvasXSpinner, getCardTemplate(), "canvasX", 0, 1920);
      templateBeanBinder.bindSpinner(canvasYSpinner, getCardTemplate(), "canvasY", 0, 1920);
      templateBeanBinder.bindSpinner(canvasWidthSpinner, getCardTemplate(), "canvasWidth", 0, 1920);
      templateBeanBinder.bindSpinner(canvasHeightSpinner, getCardTemplate(), "canvasHeight", 0, 1080);
      templateBeanBinder.bindSpinner(canvasBorderRadiusSpinner, getCardTemplate(), "canvasBorderRadius", 0, 100);

      renderWheelIconCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          wheelSizeSpinner.setDisable(!newValue);
        }
      });

      renderTitleCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          titleText.setDisable(!newValue);
        }
      });

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

      templateBeanBinder.bindCheckbox(renderRawHighscore, getCardTemplate(), "rawScore");
      renderRawHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
        maxScoresSpinner.setDisable(t1);
        wheelImageSpinner.setDisable(t1);
        rowSeparatorSpinner.setDisable(t1);
        renderPositionsCheckbox.setDisable(t1);
      });

      renderCanvasCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
        canvasAlphaPercentageSlider.setDisable(!t1);
        canvasColorSelector.setDisable(!t1);
        canvasXSpinner.setDisable(!t1);
        canvasYSpinner.setDisable(!t1);
        canvasWidthSpinner.setDisable(!t1);
        canvasHeightSpinner.setDisable(!t1);
        canvasBorderRadiusSpinner.setDisable(!t1);
      });

      titleText.setDisable(!renderTitleCheckbox.isSelected());
      wheelSizeSpinner.setDisable(!renderWheelIconCheckbox.isSelected());
      wheelImageSpinner.setDisable(renderRawHighscore.isSelected());
      maxScoresSpinner.setDisable(renderRawHighscore.isSelected());
      rowSeparatorSpinner.setDisable(renderRawHighscore.isSelected());
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
  private void onGenerateClick() {
    Platform.runLater(() -> {
      try {
        client.getHighscoreCardTemplatesClient().save((CardTemplate) this.templateBeanBinder.getBean());
        refreshPreview(this.gameRepresentation, true);
      }
      catch (Exception e) {
        LOG.error("Failed to save template: " + e.getMessage());
        WidgetFactory.showAlert(stage, "Error", "Failed to save template: " + e.getMessage());
      }
    });
  }

  private void refreshPreview(Optional<GameRepresentation> game, boolean regenerate) {
    this.openImageBtn.setDisable(true);
    this.generateBtn.setDisable(true);
    this.generateAllBtn.setDisable(true);
    mediaPlayerControl.setVisible(false);

    if (!game.isPresent()) {
      return;
    }

    Platform.runLater(() -> {
      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);
      refreshTransparency();
      refreshOverlayBackgroundPreview();

      try {
        new Thread(() -> {
          if (regenerate) {
            InputStream input = client.getHighscoreCardsService().getHighscoreCardPreview(game.get(), templateCombo.getValue());
            Image image = new Image(input);
            cardPreview.setImage(image);
            cardPreview.setVisible(true);
          }

          Platform.runLater(() -> {
            previewStack.getChildren().remove(waitOverlay);
            this.openImageBtn.setDisable(false);
            this.generateBtn.setDisable(false);
            this.generateAllBtn.setDisable(false);
          });

        }).start();
      }
      catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
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
      GameMediaItemRepresentation defaultMediaItem = this.gameRepresentation.get().getGameMedia().getDefaultMediaItem(overlayScreen);
      if (defaultMediaItem != null) {
        assetMediaPlayer = WidgetFactory.addMediaItemToBorderPane(client, defaultMediaItem, previewOverlayPanel);
        if(assetMediaPlayer != null) {
          assetMediaPlayer.setSize(cardPreview.getFitWidth(), cardPreview.getFitHeight());
          mediaPlayerControl.setVisible(true);
          previewOverlayPanel.setVisible(true);
        }
      }
    }
  }

  public void refreshPreviewSize() {
    cardPreview.setFitWidth(stage.getWidth() - 900);
    cardPreview.setFitHeight(stage.getHeight() - 200);
  }

  @Override
  public void beanPropertyChanged(Object bean, String key, Object value) {
    if (bean instanceof CardTemplate) {
      onGenerateClick();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    folderBtn.setVisible(SystemUtil.isFolderActionSupported());
    try {
      this.deleteBtn.setDisable(true);
      this.renameBtn.setDisable(true);

      templates = new ArrayList<>(client.getHighscoreCardTemplatesClient().getTemplates());
      templateCombo.setItems(FXCollections.observableList(templates));

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
      ctrl.setLoadingMessage("Generating Card...");

      accordion.setExpandedPane(backgroundSettingsPane);

      cardPreview.setPreserveRatio(true);
    }
    catch (Exception e) {
      LOG.error("Failed to initialize template editor: " + e.getMessage(), e);
    }
  }

  private void assignTemplate(CardTemplate newValue) {
    GameRepresentation game = gameRepresentation.get();
    if (!newValue.getId().equals(gameRepresentation.get().getTemplateId())) {
      ProgressDialog.createProgressDialog(new TemplateAssigmentProgressModel(Arrays.asList(game), newValue.getId()));
    }
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
}

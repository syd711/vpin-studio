package de.mephisto.vpin.ui.cards.panels;

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
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.cards.HighscoreGeneratorProgressModel;
import de.mephisto.vpin.ui.cards.TemplateAssigmentProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.DMDPositionResizer;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
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
  private Accordion accordion;

  @FXML
  private CheckBox renderTitleCheckbox;

  @FXML
  private CheckBox renderTableNameCheckbox;

  @FXML
  private CheckBox renderWheelIconCheckbox;

  @FXML
  private CheckBox renderFriendsHighscore;

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
  private Pane imagepane;

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

  @FXML
  private Label resolutionLabel;

  /** the different dragboxes */
  private List<DMDPositionResizer> dragBoxes = new ArrayList<>();


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
          templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
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
          this.templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
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
      //imagepane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> onDragboxEnter(e));
    }

    deleteBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));
    renameBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));

    templateBeanBinder.setBean(cardTemplate);
    templateBeanBinder.setPaused(true);

    titleFontLabel.setText(cardTemplate.getTitleFontName() + ", " + cardTemplate.getTitleFontStyle() + ", " + cardTemplate.getTitleFontSize() + "px");
    tableFontLabel.setText(cardTemplate.getTableFontName() + ", " + cardTemplate.getTableFontStyle() + ", " + cardTemplate.getTableFontName() + "px");
    scoreFontLabel.setText(cardTemplate.getScoreFontName() + ", " + cardTemplate.getScoreFontStyle() + ", " + cardTemplate.getScoreFontSize() + "px");

    templateBeanBinder.setColorPickerValue(fontColorSelector, getCardTemplate(), "fontColor");
    templateBeanBinder.setColorPickerValue(friendsFontColorSelector, getCardTemplate(), "friendsFontColor");

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

    renderFriendsHighscore.setSelected(cardTemplate.isRenderFriends());
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
    renderFriendsHighscore.setDisable(renderRawHighscore.isSelected());

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
      templateBeanBinder.bindColorPicker(friendsFontColorSelector, getCardTemplate(), "friendsFontColor");
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
        renderFriendsHighscore.setDisable(t1);
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
    }
    mediaPlayerControl.setVisible(false);
    previewOverlayPanel.setVisible(false);

    if (this.gameRepresentation.isPresent() && getCardTemplate().getOverlayScreen() != null) {
      VPinScreen overlayScreen = VPinScreen.valueOf(getCardTemplate().getOverlayScreen());

      JFXFuture.supplyAsync(() -> client.getFrontendService().getFrontendMedia(this.gameRepresentation.get().getId()))
      .thenAcceptLater(frontendMedia -> {
        FrontendMediaItemRepresentation defaultMediaItem = frontendMedia.getDefaultMediaItem(overlayScreen);
        if (defaultMediaItem != null) {
          assetMediaPlayer = WidgetFactory.addMediaItemToBorderPane(client, defaultMediaItem, previewOverlayPanel, this, null);
          //images do not have a media player
          if (assetMediaPlayer != null) {
            mediaPlayerControl.setVisible(true);
          }

          if (previewOverlayPanel.getCenter() instanceof ImageViewer) {
            ImageViewer imageViewer = (ImageViewer) previewOverlayPanel.getCenter();
            imageViewer.scaleForTemplate(cardPreview);
          }

          previewOverlayPanel.setVisible(true);
        }
      });
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

  //-------------------------------------------

  // imagepane 

  public void onDragboxEnter(MouseEvent e) {
    if (dragBoxes.size() == 0) {
      loadDragBoxes(getCardTemplate());
      e.consume();
    }
  }

  private void configureSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                                ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {
    SpinnerValueFactory<Integer> factory = spinner.getValueFactory();
    factory.valueProperty().bindBidirectional(property);
  }

  private void loadDragBoxes(CardTemplate cardtemplate) {

    // first delete previous boxes
    for (DMDPositionResizer dragBox : dragBoxes) {
      dragBox.removeFromPane(imagepane);
    }
    dragBoxes.clear();

    if (renderCanvasCheckbox.isSelected()) {

      // The canvas box
      DMDPositionResizer dragBox = new DMDPositionResizer();

      dragBox.setBounds(0, 0, 1920, 1080);

      dragBox.setWidth(cardtemplate.getCanvasWidth());
      dragBox.setHeight(cardtemplate.getCanvasHeight());
      dragBox.setX(cardtemplate.getCanvasX());
      dragBox.setY(cardtemplate.getCanvasY());
  
      double cardw = cardPreview.getFitWidth();
      double zoom = cardw / 1920.0;
      dragBox.setZoom(zoom);

      // setup linkages between spinner and our dragbox
      dragBox.selectProperty().addListener((obs, oldV, newV) -> {
        if (newV) {
          templateBeanBinder.setPaused(true);
          configureSpinner(canvasXSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
          configureSpinner(canvasYSpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
          configureSpinner(canvasWidthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
          configureSpinner(canvasHeightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());
        }
        else {
          cardtemplate.setCanvasX((int) dragBox.getX());
          cardtemplate.setCanvasWidth((int) dragBox.getWidth());
          cardtemplate.setCanvasHeight((int) dragBox.getHeight());
          dragBox.removeFromPane(imagepane);
          dragBoxes.remove(dragBox);
          templateBeanBinder.setPaused(false);
        }
      });
      dragBox.select();

      dragBox.addToPane(imagepane);
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

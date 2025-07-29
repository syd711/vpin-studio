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
import de.mephisto.vpin.restclient.client.VPinStudioClient;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TemplateEditorController implements Initializable, BindingChangedListener, MediaPlayerListener {
  private final static Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);

  @FXML
  private ComboBox<CardTemplate> templateCombo;

  @FXML
  private Button renameBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Accordion accordion;

  @FXML
  private LayerEditorBackgroundController layerEditorBackgroundController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorLayoutController layerEditorLayoutController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorCanvasController layerEditorCanvasController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorTitleController layerEditorTitleController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorTableNameController layerEditorTableNameController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorWheelController layerEditorWheelController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorScoresController layerEditorScoresController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private StackPane previewStack;
  @FXML
  private Pane previewPanel;
  @FXML
  private BorderPane previewOverlayPanel;

  private Parent waitOverlay;

  /** The ive preview component */
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

  public void applyFontOnAllTemplates(Consumer<CardTemplate> font) {
    ObservableList<CardTemplate> items = templateCombo.getItems();
    for (CardTemplate item : items) {
      font.accept(item);
    }
    saveAllTemplates(items);
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

  public BeanBinder getBeanBinder() {
    return templateBeanBinder;
  }

  private void setTemplate(CardTemplate cardTemplate) {
    deleteBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));
    renameBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));

    templateBeanBinder.setBean(cardTemplate);

    templateBeanBinder.setPaused(true);
    layerEditorBackgroundController.setTemplate(cardTemplate);
    layerEditorLayoutController.setTemplate(cardTemplate);
    layerEditorCanvasController.setTemplate(cardTemplate);
    layerEditorTitleController.setTemplate(cardTemplate);
    layerEditorTableNameController.setTemplate(cardTemplate);
    layerEditorWheelController.setTemplate(cardTemplate);
    layerEditorScoresController.setTemplate(cardTemplate);
    templateBeanBinder.setPaused(false);

    cardPreview.setTemplate(cardTemplate);
    refreshPreview(this.gameRepresentation, true);
  }

  private void refreshTransparency() {
    boolean enabled = getCardTemplate().isTransparentBackground();
    if (enabled) {
      if (!getCardTemplate().isOverlayMode()) {
        Image backgroundImage = new Image(Studio.class.getResourceAsStream("transparent.png"));
        BackgroundImage myBI = new BackgroundImage(backgroundImage,
            BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
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

    cardPreview.setData(null);

    if (game.isPresent()) {
      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);
      refreshTransparency();
      refreshOverlayBackgroundPreview();

      JFXFuture.supplyAsync(() -> client.getHighscoreCardsService().getHighscoreCardData(game.get(), templateCombo.getValue()))
        .thenAcceptLater(cardData -> {
          String baseurl = client.getRestClient().getBaseUrl() + VPinStudioClient.API;
          cardData.addBaseUrl(baseurl);
          cardPreview.setData(cardData);

          previewStack.getChildren().remove(waitOverlay);
          this.openImageBtn.setDisable(false);
          this.generateBtn.setDisable(false);
          this.generateAllBtn.setDisable(false);
        });
    }
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
      // refresh the preview immediately
      cardPreview.setTemplate((CardTemplate) bean);
      // and background save with debounce
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

      //accordion.setExpandedPane(backgroundSettingsPane);

      // Initialize bindings between CardTemplate and sidebar
      initBindings();

      // Resize handlers
      previewStack.widthProperty().addListener((obs, o, n) -> resizeCardPreview(n.doubleValue(), previewStack.getHeight(), true));
      previewStack.heightProperty().addListener((obs, o, n) -> resizeCardPreview(previewStack.getWidth(), n.doubleValue(), false));
      previewPanel.getChildren().add(cardPreview);

      // selector
      cardPreview.setOnMousePressed(e -> onDragboxEnter(e));
    }
    catch (Exception e) {
      LOG.error("Failed to initialize template editor: " + e.getMessage(), e);
    }
  }

  private void initBindings() {
    try {
      templateBeanBinder = new BeanBinder(this);
      templateBeanBinder.setBean(this.getCardTemplate());

      layerEditorBackgroundController.initialize(this);
      layerEditorLayoutController.initialize(this);
      layerEditorCanvasController.initialize(this);
      layerEditorTitleController.initialize(this);
      layerEditorTableNameController.initialize(this);
      layerEditorWheelController.initialize(this);
      layerEditorScoresController.initialize(this);
    }
    catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
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

  //------------------------------------------- SELECTION ---

  public void onDragboxEnter(MouseEvent e) {
      CardLayer layer = cardPreview.selectCardLayer(e.getX(), e.getY());
      loadDragBoxes(layer);
      e.consume();
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
        layerEditorCanvasController.bindDragBox(dragBox);
      }
      else if (layer instanceof CardLayerWheel) {
        layerEditorWheelController.bindDragBox(dragBox);
      }

      dragBox.selectProperty().addListener((obs, oldV, newV) -> {
        //templateBeanBinder.setPaused(newV);
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

package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.cards.*;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.ImageViewer;
import de.mephisto.vpin.commons.utils.media.MediaPlayerListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardResolution;
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
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TemplateEditorController implements Initializable, MediaPlayerListener {
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
  private Label resolutionLabel;

  @FXML
  private LayerEditorOverlayController layerEditorOverlayController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorBackgroundController layerEditorBackgroundController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorFrameController layerEditorFrameController; //fxml magic! Not unused -> id + "Controller"
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

  /**
   * The live preview component
   * Disable autosizing as this is imposed by container
   */
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

  /**
   * the dragbox of the selected Layer
   */
  private PositionResizer dragBox;

  public Debouncer cardTemplateSaveDebouncer = new Debouncer();

  private CardTemplateBinder templateBeanBinder;

  private HighscoreCardsController highscoreCardsController;
  private AssetMediaPlayer assetMediaPlayer;

  private Optional<GameRepresentation> gameRepresentation;
  private List<CardTemplate> templates;



  @FXML
  private void onOpenImage() {
    if (gameRepresentation.isPresent()) {
      TableDialogs.openMediaDialog(Studio.stage, "Highscore Card", client.getHighscoreCardsService().getHighscoreCardUrl(gameRepresentation.get()));
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
    CardTemplate template = this.templateCombo.getValue();
    String gameName = gameRepresentation.get().getGameName();

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String s = WidgetFactory.showInputDialog(stage, "New Template", "Enter Template Name", "Enter a meaningful name that identifies the card design.", "The values of the selected template \"" + template.getName() + "\" will be used as default.", gameName);
    if (!StringUtils.isEmpty(s)) {
      ObservableList<CardTemplate> items = this.templateCombo.getItems();

      Optional<CardTemplate> duplicate = items.stream().filter(t -> t.getName().equals(s)).findFirst();
      if (duplicate.isPresent()) {
        WidgetFactory.showAlert(stage, "Error", "A template with the name \"" + s + "\" already exist.");
        return;
      }

      template.setName(s);
      template.setId(null);
      JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().save(template))
          .thenAcceptLater(newTemplate -> loadTemplates(newTemplate))
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
            loadTemplates(updatedTemplate);
            assignTemplate(updatedTemplate);
            //highscoreCardsController.refresh(gameRepresentation, templates, true);
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

          loadTemplates(defaultTemplate);

          assignTemplate(defaultTemplate);
          //highscoreCardsController.refresh(gameRepresentation, templates, true);
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

  public CardTemplateBinder getBeanBinder() {
    return templateBeanBinder;
  }

  private void setTemplate(CardTemplate cardTemplate) {
    deleteBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));
    renameBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));

    // interrupt propety changes
    templateBeanBinder.setPaused(true);

    // set the bean  and resolution
    templateBeanBinder.setBean(cardTemplate);

    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS);
    CardResolution res = cardSettings.getCardResolution();
    resolutionLabel.setText(res.toWidth() + " x " + res.toHeight());

    templateBeanBinder.setResolution(res);

    layerEditorOverlayController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorBackgroundController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorFrameController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorCanvasController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorTitleController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorTableNameController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorWheelController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorScoresController.setTemplate(cardTemplate, res, this.gameRepresentation);

    templateBeanBinder.setPaused(false);

    cardPreview.setTemplate(cardTemplate);
    refreshPreview(this.gameRepresentation, true);
  }

  private void refreshTransparency() {
    if (getCardTemplate().isOverlayMode()) {
      if (getCardTemplate().getOverlayScreen() == null) {
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

    cardPreview.setData(null, null);

    if (game.isPresent()) {
      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);
      refreshTransparency();
      refreshOverlayBackgroundPreview();

      JFXFuture.supplyAsync(() -> client.getCardData(game.get(), templateCombo.getValue()))
          .thenAcceptLater(cardData -> {
            CardResolution res = templateBeanBinder.getResolution();
            cardPreview.setData(cardData, res);

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

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendUtil.replaceName(folderBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    try {
      this.deleteBtn.setDisable(true);
      this.renameBtn.setDisable(true);

      loadTemplates(null);

      templateCombo.valueProperty().addListener(new ChangeListener<CardTemplate>() {
        @Override
        public void changed(ObservableValue<? extends CardTemplate> observable, CardTemplate oldValue, CardTemplate newValue) {
          if (newValue != null) {
            setTemplate(newValue);
            if (gameRepresentation.isPresent()) {
              assignTemplate(newValue);
              //highscoreCardsController.refresh(gameRepresentation, templates, false);
            }
          }
        }
      });

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Card...");

      // Initialize bindings between CardTemplate and sidebar
      initBindings();

      // Resize handlers
      previewStack.widthProperty().addListener((obs, o, n) -> resizeCardPreview(n.doubleValue(), previewStack.getHeight(), true));
      previewStack.heightProperty().addListener((obs, o, n) -> resizeCardPreview(previewStack.getWidth(), n.doubleValue(), false));

      previewPanel.getChildren().add(cardPreview);

      // selector
      cardPreview.setOnMousePressed(e -> onDragboxEnter(e));
      previewPanel.setOnMousePressed(e -> onDragboxExit(e));
    }
    catch (Exception e) {
      LOG.error("Failed to initialize template editor: " + e.getMessage(), e);
    }
  }

  private void initBindings() {
    try {
      templateBeanBinder = new CardTemplateBinder();
      templateBeanBinder.addListener((bean, key, value) -> {
        // refresh the preview immediately
        cardPreview.setTemplate((CardTemplate) bean);
        // and background save with debounce
        saveCardTemplate((CardTemplate) bean);
      });

      templateBeanBinder.setBean(this.getCardTemplate());

      layerEditorOverlayController.initialize(this, accordion);
      layerEditorBackgroundController.initialize(this, accordion);
      layerEditorFrameController.initialize(this, accordion);
      layerEditorCanvasController.initialize(this, accordion);
      layerEditorTitleController.initialize(this, accordion);
      layerEditorTableNameController.initialize(this, accordion);
      layerEditorWheelController.initialize(this, accordion);
      layerEditorScoresController.initialize(this, accordion);
    }
    catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
    }
  }

  private void loadTemplates(CardTemplate selectedTemplate) {
    JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().getTemplates())
        .thenAcceptLater(templates -> {
          this.templates = templates;
          templateCombo.setItems(FXCollections.observableList(templates));
          if (selectedTemplate != null) {
            this.templateCombo.setValue(selectedTemplate);
          }
        });
  }

  private void resizeCardPreview(double width, double height, boolean forceWidth) {
    // make sure the panel is full size always
    if (width > 0 && height > 0) {
      double aspectRatio = 16.0 / 9.0;

      Insets in1 = previewStack.getInsets();
      width -= in1.getRight() + in1.getLeft();
      height -= in1.getTop() + in1.getBottom();

      previewPanel.resizeRelocate(0, 0, width, height);

      Insets in2 = previewPanel.getInsets();
      width -= in2.getLeft() + in2.getRight();
      height -= in2.getTop() + in2.getBottom();

      // now adjust with aspect ratio and center in previewPanel
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

      // in case dragboxes was here, deselect it
      unloadDragBox();

      Rectangle cliprect = new Rectangle(newWidth, newHeight);
      cardPreview.setClip(cliprect);
    }
  }

  //------------------------------------------- SELECTION ---

  public void onDragboxEnter(MouseEvent me) {
    unloadDragBox();

    CardLayer layer = cardPreview.selectCardLayer(me.getX(), me.getY());
    loadDragBox(layer, me);

    me.consume();
  }

  public void onDragboxExit(MouseEvent me) {
    unloadDragBox();
    me.consume();
  }

  private void unloadDragBox() {
    // first delete previous boxes
    if (dragBox != null) {
      dragBox.removeFromPane(cardPreview);

      if (dragBox.getUserData() instanceof CardLayer) {
        CardLayer layer = (CardLayer) dragBox.getUserData();
        layerToController(layer).unbindDragBox(dragBox);
      }
      dragBox = null;
    }
  }


  private void loadDragBox(CardLayer layer, MouseEvent me) {
    if (layer != null) {

      LayerEditorBaseController controller = layerToController(layer);
      controller.layerSelected();

      // The canvas box
      this.dragBox = new PositionResizer();

      CardResolution res = cardPreview.getCardResolution();
      double zoomX = res == null ? 1.0 : cardPreview.getWidth() / res.toWidth();
      double WIDTH = cardPreview.getWidth() / zoomX;
      double zoomY = res == null ? 1.0 : cardPreview.getHeight() / res.toHeight();
      double HEIGHT = cardPreview.getHeight() / zoomY;

      // keep the order of setters !
      dragBox.setZoomX(zoomX);
      dragBox.setZoomY(zoomY);
      dragBox.setBounds(0, 0, (int) WIDTH, (int) HEIGHT);
      dragBox.setAcceptOutsidePart(true, 50);

      dragBox.setWidth((int) (layer.getWidth() / zoomX));
      dragBox.setHeight((int) (layer.getHeight() / zoomY));
      dragBox.setX((int) (layer.getLocX() / zoomX));
      dragBox.setY((int) (layer.getLocY() / zoomY));

      controller.bindDragBox(dragBox);
      dragBox.setUserData(layer);

      dragBox.addToPane(cardPreview);
      dragBox.select();
    }
  }

  public <T extends CardLayer> T getLayer(Class<T> clazz) {
    for (CardLayer cardLayer : cardPreview.getLayers()) {
      if (cardLayer.getClass().equals(clazz)) {
        @SuppressWarnings("unchecked")
        T cardLayerT = (T) cardLayer;
        return cardLayerT;
      }
    }
    return null;
  }

  protected LayerEditorBaseController layerToController(CardLayer layer) {
    if (layer instanceof CardLayerBackground) {
      if (templateBeanBinder.getBean().isRenderFrame()) {
        return layerEditorFrameController;
      } else {
        return layerEditorBackgroundController;
      }
    }
    else if (layer instanceof CardLayerCanvas) {
      return layerEditorCanvasController;
    }
    else if (layer instanceof CardLayerText) {
      switch (((CardLayerText) layer).getType()) {
        case Title:
          return layerEditorTitleController;
        case TableName:
          return layerEditorTableNameController;
      }
    }
    else if (layer instanceof CardLayerWheel) {
      return layerEditorWheelController;
    }
    else if (layer instanceof CardLayerScores) {
      return layerEditorScoresController;
    }
    //else
    throw new RuntimeException("CardLayer not mapped");
  }

  //-----------------------------------------

  public CardTemplate getCardTemplateForGame(GameRepresentation game) {
    if (templates != null) {
      if (game.getTemplateId() != null) {
        Optional<CardTemplate> first = templates.stream().filter(g -> g.getId().equals(game.getTemplateId())).findFirst();
        if (first.isPresent()) {
          return first.get();
        }
      }
      // else 
      return templates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst().orElse(null);
    }
    return null;
  }

  private void assignTemplate(CardTemplate newValue) {
    List<GameRepresentation> selection = highscoreCardsController.getSelections();
    ProgressDialog.createProgressDialog(new TemplateAssigmentProgressModel(selection, newValue.getId()));
  }

  public void setCardsController(HighscoreCardsController highscoreCardsController) {
    this.highscoreCardsController = highscoreCardsController;
  }

  public void selectTable(Optional<GameRepresentation> gameRepresentation) {
    this.gameRepresentation = gameRepresentation;
    if (this.gameRepresentation.isPresent()) {
      GameRepresentation game = gameRepresentation.get();
      CardTemplate template = getCardTemplateForGame(game);
      if (template != null) {
        if (template.equals(templateCombo.getValue())) {
          setTemplate(template);
        }
        else {
          templateCombo.setValue(template);
        }
      }
    }
  }

  //-------------- MediaPlayerListener
  @Override
  public void onReady(Media media) {
  }

  @Override
  public void onDispose() {
  }

}

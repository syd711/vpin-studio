package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.cards.*;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
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
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.*;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TemplateEditorController implements Initializable, MediaPlayerListener, StudioEventListener {
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
  private Label nagBarLabel;

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
  private LayerEditorManufacturerController layerEditorManufacturerController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorOtherMediaController layerEditorOtherMediaController; //fxml magic! Not unused -> id + "Controller"
  @FXML
  private LayerEditorScoresController layerEditorScoresController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private StackPane previewStack;
  @FXML
  private Pane previewPanel;
  @FXML
  private BorderPane previewOverlayPanel;

  private Parent waitOverlay;

  @FXML
  private ToggleButton cardModeBtn;
  @FXML
  private ToggleButton templateModeBtn;

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

  @FXML
  private Pane nagBar;


  /**
   * the dragboxes, today only used at a time
   */
  private List<PositionResizer> dragBoxes = new ArrayList<>();

  public Debouncer cardTemplateSaveDebouncer = new Debouncer();

  private CardTemplateBinder templateBeanBinder;

  private HighscoreCardsController highscoreCardsController;

  private AssetMediaPlayer assetMediaPlayer;

  private Optional<GameRepresentation> gameRepresentation;
  private TemplateComboChangeListener templateComboChangeListener;


  @FXML
  private void onOpenImage() {
    if (gameRepresentation.isPresent()) {
      TableDialogs.openMediaDialog(Studio.stage, "Highscore Card", client.getHighscoreCardsService().getHighscoreCardUrl(gameRepresentation.get()), "image/png");
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
      assetMediaPlayer.play();
    }
  }

  @FXML
  private void onStop() {
    if (assetMediaPlayer != null) {
      assetMediaPlayer.pause();
    }
  }

  @FXML
  private void onCreate(ActionEvent e) {
    CardTemplate template = getSelectedCardTemplate();
    String gameName = gameRepresentation.get().getGameName();
    if (gameName.contains("(")) {
      gameName = gameName.substring(0, gameName.indexOf("("));
    }

    if (gameName.contains("[")) {
      gameName = gameName.substring(0, gameName.indexOf("["));
    }

    gameName = gameName.trim();

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String s = WidgetFactory.showInputDialog(stage, "New Template", "Enter Template Name", "Enter a meaningful name that identifies the card design.", "The values of the selected template \"" + template.getName() + "\" will be used as default.", gameName);
    if (!StringUtils.isEmpty(s)) {
      List<CardTemplate> items = client.getHighscoreCardTemplatesClient().getTemplates();
      Optional<CardTemplate> duplicate = items.stream().filter(t -> t.getName().equals(s)).findFirst();
      if (duplicate.isPresent()) {
        WidgetFactory.showAlert(stage, "Error", "A template with the name \"" + s + "\" already exist.");
        return;
      }

      template.setName(s);
      template.setParentId(null);
      template.setId(null);
      JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().save(template))
          .thenAcceptLater(newTemplate -> {
            refreshTemplates(newTemplate);
            selectTemplateInCombo(newTemplate);
            assignTemplate(newTemplate);
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
    CardTemplate cardTemplate = getSelectedCardTemplate();
    String s = WidgetFactory.showInputDialog(stage, "Rename Template", "Enter Template Name", "Enter the new template name.", null, cardTemplate.getName());
    if (!StringUtils.isEmpty(s) && !cardTemplate.getName().equals(s)) {
      cardTemplate.setName(s);

      JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().save(cardTemplate))
          .thenAcceptLater(updatedTemplate -> {
            refreshTemplates(updatedTemplate);
            assignTemplate(updatedTemplate);
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
    CardTemplate cardTemplate = getSelectedCardTemplate();
    String msg1 = "Delete template \"" + cardTemplate.getName() + "\"?";
    String msg2 = "Assigned tables will use the default template again.";
    if (!cardTemplate.isTemplate()) {
      msg1 = "Delete template for table \"" + gameRepresentation.get().getGameDisplayName() + "\"?";
      msg2 = "The table will use the default template again.";
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Template", msg1, msg2, "Delete");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getHighscoreCardTemplatesClient().deleteTemplate(cardTemplate.getId());
        Platform.runLater(() -> {
          CardTemplate defaultTemplate = client.getHighscoreCardTemplatesClient().getDefaultTemplate();
          refreshTemplates(defaultTemplate);
          assignTemplate(defaultTemplate);
        });
      }
      catch (Exception ex) {
        LOG.error("Failed to delete template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Template Deletion Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
    }
  }

  public void applyFontOnAllTemplates(Consumer<CardTemplate> font) {
    List<CardTemplate> items = client.getHighscoreCardTemplatesClient().getTemplates();
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

  public CardTemplate getSelectedCardTemplate() {
    return this.templateBeanBinder.getBean();
  }

  public CardTemplateBinder getBeanBinder() {
    return templateBeanBinder;
  }

  private void setTemplate(CardTemplate cardTemplate) {
    // deselect any element if any
    unloadDragBoxes();

    deleteBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));
    renameBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT) || !cardTemplate.isTemplate());

    // interrupt property changes
    templateBeanBinder.setPaused(true);

    // set the selected template on the TemplateBinder
    templateBeanBinder.setBean(cardTemplate);

    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS);
    CardResolution res = cardSettings.getCardResolution();
    resolutionLabel.setText(res.toWidth() + " x " + res.toHeight());

    templateBeanBinder.setResolution(res);

    templateModeBtn.setSelected(cardTemplate.isTemplate());
    cardModeBtn.setSelected(!cardTemplate.isTemplate());

    refreshNagBar(cardTemplate, this.gameRepresentation);

    layerEditorOverlayController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorBackgroundController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorFrameController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorCanvasController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorTitleController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorTableNameController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorWheelController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorManufacturerController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorOtherMediaController.setTemplate(cardTemplate, res, this.gameRepresentation);
    layerEditorScoresController.setTemplate(cardTemplate, res, this.gameRepresentation);

    templateBeanBinder.setPaused(false);

    cardPreview.setTemplate(cardTemplate);
    refreshPreview(this.gameRepresentation);
  }

  private void refreshNagBar(CardTemplate cardTemplate, Optional<GameRepresentation> gameRepresentation) {
    nagBarLabel.setVisible(gameRepresentation.isPresent());
    if (gameRepresentation.isPresent()) {
      if (cardTemplate.isTemplate()) {
        nagBar.setStyle("-fx-background-color: #333366;");
        nagBarLabel.setText("Editing template \"" + cardTemplate.getName() + "\", previewing game \"" + gameRepresentation.get().getGameDisplayName() + "\"");
      }
      else {
        nagBar.setStyle("-fx-background-color: #116611;");
        Optional<CardTemplate> parent = this.templateCombo.getItems().stream().filter(t -> t.getId() == cardTemplate.getParentId()).findFirst();
        if (!parent.isPresent()) {
          parent = this.templateCombo.getItems().stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst();
        }
        nagBarLabel.setText("Editing highscore card for \"" + gameRepresentation.get().getGameDisplayName() + "\", using template \"" + parent.get().getName() + "\".");
      }
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
      refreshPreview(this.gameRepresentation);
    }
  }

  @FXML
  private void onGenerateClick() {
    JFXFuture.runAsync(() -> client.getHighscoreCardTemplatesClient().save((CardTemplate) this.templateBeanBinder.getBean()))
        .thenLater(() -> refreshPreview(this.gameRepresentation))
        .onErrorLater(e -> {
          LOG.error("Failed to save template: {}", e.getMessage(), e);
          WidgetFactory.showAlert(stage, "Error", "Failed to save template: " + e.getMessage());
        });
  }

  private void refreshPreview(Optional<GameRepresentation> game) {

    if (game.isPresent()) {

      // change game, so empty and add the wait overlay
      // else full background reload
      if (!cardPreview.isForGame(game.get().getId())) {
        this.openImageBtn.setDisable(true);
        this.generateBtn.setDisable(true);
        this.generateAllBtn.setDisable(true);

        cardPreview.setData(null, null);
        previewStack.getChildren().remove(waitOverlay);
        previewStack.getChildren().add(waitOverlay);
      }

       refreshOverlayBackgroundPreview();

      JFXFuture.supplyAsync(() -> client.getCardData(game.get(), getSelectedCardTemplate()))
          .thenAcceptLater(cardData -> {
            CardResolution res = templateBeanBinder.getResolution();
            cardPreview.setData(cardData, res);

            previewStack.getChildren().remove(waitOverlay);
            this.openImageBtn.setDisable(false);
            this.generateBtn.setDisable(false);
            this.generateAllBtn.setDisable(false);
          });
    }
    else {
      this.openImageBtn.setDisable(true);
      this.generateBtn.setDisable(true);
      this.generateAllBtn.setDisable(true);
      mediaPlayerControl.setVisible(false);

      cardPreview.setData(null, null);
    }
  }

  private void refreshOverlayBackgroundPreview() {
    if (assetMediaPlayer != null) {
      assetMediaPlayer.disposeMedia();
    }
    mediaPlayerControl.setVisible(false);
    previewOverlayPanel.setVisible(false);

    CardTemplate template = getSelectedCardTemplate();
    if (this.gameRepresentation.isPresent() && template.isOverlayMode() && StringUtils.isNotEmpty(template.getOverlayScreen())) {
      JFXFuture.supplyAsync(() -> client.getFrontendService().getFrontendMedia(this.gameRepresentation.get().getId()))
          .thenAcceptLater(frontendMedia -> {
            VPinScreen overlayScreen = VPinScreen.valueOf(template.getOverlayScreen());
            FrontendMediaItemRepresentation defaultMediaItem = frontendMedia.getDefaultMediaItem(overlayScreen);
            if (defaultMediaItem != null) {
              assetMediaPlayer = WidgetFactory.createAssetMediaPlayer(client, defaultMediaItem, true, false);
              assetMediaPlayer.addListener(this);
              assetMediaPlayer.setMediaViewSize(cardPreview.getWidth(), cardPreview.getHeight());
              previewOverlayPanel.setCenter(assetMediaPlayer);

              //images do not have a media player
              if (assetMediaPlayer.hasMediaPlayer()) {
                mediaPlayerControl.setVisible(true);
              }
              previewOverlayPanel.setVisible(true);
            }
          });
    }
    else if (template.getTransparentPercentage() > 0) {

      assetMediaPlayer = new AssetMediaPlayer() {};
      Region p = new Region() {
        @Override public boolean isResizable() {
          return false;
        }
      };
      Image backgroundImage = new Image(Studio.class.getResourceAsStream("transparent.png"));
      BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
      p.setBackground(new Background(myBI));
      assetMediaPlayer.setCenter(p);
      assetMediaPlayer.setMediaViewSize(cardPreview.getWidth(), cardPreview.getHeight());

      previewOverlayPanel.setCenter(assetMediaPlayer);
      previewOverlayPanel.setVisible(true);
    }
  }

  private void saveCardTemplate(CardTemplate cardTemplate) {
    cardTemplateSaveDebouncer.debounce("cardTemplate", () -> {
      JFXFuture.runAsync(() -> client.getHighscoreCardTemplatesClient().save(cardTemplate))
          .thenLater(() -> refreshPreview(this.gameRepresentation))
          .onErrorLater(e -> {
            LOG.error("Failed to save template: {}", e.getMessage(), e);
            WidgetFactory.showAlert(stage, "Error", "Failed to save template: " + e.getMessage());
          });
    }, 1000);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    folderBtn.setVisible(SystemUtil.isFolderActionSupported());
    nagBarLabel.setText("Loading...");

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendUtil.replaceName(folderBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    try {
      this.deleteBtn.setDisable(true);
      this.renameBtn.setDisable(true);

      templateComboChangeListener = new TemplateComboChangeListener();
      templateCombo.valueProperty().addListener(templateComboChangeListener);

      // load overlay
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Refreshing  Card...");

      previewStack.setBackground(new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null)));
      previewPanel.setBackground(Background.EMPTY);
      previewOverlayPanel.setBackground(Background.EMPTY);

      // Initialize bindings between CardTemplate and sidebar
      initBindings();

      // Resize handlers
      previewStack.widthProperty().addListener((obs, o, n) -> resizeCardPreview(n.doubleValue(), previewStack.getHeight(), true));
      previewStack.heightProperty().addListener((obs, o, n) -> resizeCardPreview(previewStack.getWidth(), n.doubleValue(), false));

      previewPanel.getChildren().add(cardPreview);

      // selector
      cardPreview.setOnMousePressed(e -> onDragboxEnter(e));
      previewPanel.setOnMousePressed(e -> onDragboxExit(e));

      // auto selection of layer on tabbed opening
      accordion.expandedPaneProperty().addListener((obs, o, n) -> {
        if (n != null) {
          CardLayer layer = titledPaneToLayer(n);
          loadDragBox(layer);
        }
      });

      cardModeBtn.getToggleGroup().selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
        @Override
        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
          if (!templateBeanBinder.isPaused() && gameRepresentation.isPresent() && newValue != null) {
            //create new card template
            if (newValue.equals(cardModeBtn)) {
              assignTemplate(null);
            }
            else {
              //switch back to the template
              assignTemplate(templateCombo.getValue());
            }
          }
        }
      });
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

      layerEditorOverlayController.initialize(this, accordion);
      layerEditorBackgroundController.initialize(this, accordion);
      layerEditorFrameController.initialize(this, accordion);
      layerEditorCanvasController.initialize(this, accordion);
      layerEditorTitleController.initialize(this, accordion);
      layerEditorTableNameController.initialize(this, accordion);
      layerEditorWheelController.initialize(this, accordion);
      layerEditorManufacturerController.initialize(this, accordion);
      layerEditorOtherMediaController.initialize(this, accordion);
      layerEditorScoresController.initialize(this, accordion);
    }
    catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
    }
  }

  public void loadTemplates(List<CardTemplate> templates, CardTemplate selectedTemplate) {
    templateCombo.valueProperty().removeListener(templateComboChangeListener);
    List<CardTemplate> realTemplates = templates.stream().filter(t -> t.isTemplate()).collect(Collectors.toList());
    templateCombo.setItems(FXCollections.observableList(realTemplates));

    if (selectedTemplate != null) {
      templateCombo.setValue(selectedTemplate);
    }
    templateCombo.valueProperty().addListener(templateComboChangeListener);
  }

  public void refreshTemplates(CardTemplate selectedTemplate) {
    JFXFuture.supplyAsync(() -> client.getHighscoreCardTemplatesClient().getTemplates())
        .onErrorSupply(e -> {
          Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Loading templates failed: " + e.getMessage()));
          return Collections.emptyList();
        })
        .thenAcceptLater(templates -> {
          loadTemplates(templates, selectedTemplate);
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
      unloadDragBoxes();

      Rectangle cliprect = new Rectangle(newWidth, newHeight);
      cardPreview.setClip(cliprect);

      // also resize the media within the preview
      if (assetMediaPlayer != null) {
        assetMediaPlayer.setMediaViewSize(newWidth, newHeight);
      }
    }
  }

  //------------------------------------------- SELECTION ---

  public void onDragboxEnter(MouseEvent me) {
    CardLayer layer = cardPreview.selectCardLayer(me.getX(), me.getY());
    if (layer != null) {
      LayerEditorBaseController controller = layerToController(layer);
      if (controller != null && controller.isNotLocked()) {
        // expanding the pane will make the layer selected
        if (!controller.expandSettingsPane()) {
          // if already expanded, just select the layer
          loadDragBox(layer);
        }
        me.consume();
      }
    }
  }

  public void onDragboxExit(MouseEvent e) {
    unloadDragBoxes();
    e.consume();
  }

  private void unloadDragBoxes() {
    // first delete previous boxes
    for (PositionResizer dragBox : dragBoxes) {
      dragBox.removeFromPane(cardPreview);

      if (dragBox.getUserData() instanceof CardLayer) {
        CardLayer layer = (CardLayer) dragBox.getUserData();
        layerToController(layer).unbindDragBox(dragBox);
      }
    }
    dragBoxes.clear();
  }


  private void loadDragBox(CardLayer layer) {
    // make sure first old draggbox is unselected
    unloadDragBoxes();
    if (layer != null) {

      // The canvas box
      PositionResizer dragBox = new PositionResizer();

      CardResolution res = cardPreview.getCardResolution();
      double zoomX = res == null ? 1.0 : cardPreview.getWidth() / res.toWidth();
      double WIDTH = cardPreview.getWidth() / zoomX;
      double zoomY = res == null ? 1.0 : cardPreview.getHeight() / res.toHeight();
      double HEIGHT = cardPreview.getHeight() / zoomY;

      // keep the order of setters !
      dragBox.setZoomX(zoomX);
      dragBox.setZoomY(zoomY);

      dragBox.setWidth((int) (layer.getWidth() / zoomX));
      dragBox.setHeight((int) (layer.getHeight() / zoomY));
      dragBox.setX((int) (layer.getLocX() / zoomX));
      dragBox.setY((int) (layer.getLocY() / zoomY));

      // call this once width and height have been set
      dragBox.setBounds(0, 0, (int) WIDTH, (int) HEIGHT);
      dragBox.setAcceptOutsidePart(true, 50);

      LayerEditorBaseController controller = layerToController(layer);
      controller.bindDragBox(dragBox);
      dragBox.setUserData(layer);

      dragBox.addToPane(cardPreview);
      dragBox.select();
      dragBoxes.add(dragBox);
    }
  }

  public void selectLayer(LayerEditorBaseController controller) {
    if (controller.isNotLocked()) {
      // expanding the pane will make the layer selected
      if (!controller.expandSettingsPane()) {
        // if already expanded, just select the layer
        CardLayer layer = controllerToLayer(controller);
        loadDragBox(layer);
      }
    }
  }

  public void deselectLayer(LayerEditorBaseController controller) {
    for (Iterator<PositionResizer> iter = dragBoxes.iterator(); iter.hasNext();) {
      PositionResizer dragBox = iter.next();
      if (dragBox.getUserData() instanceof CardLayer) {
        CardLayer layer = (CardLayer) dragBox.getUserData();
        LayerEditorBaseController associatedController = layerToController(layer);
        if (associatedController == controller) {
          dragBox.removeFromPane(cardPreview);
          associatedController.unbindDragBox(dragBox);
          iter.remove();
        }
      }
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

  protected CardLayer titledPaneToLayer(TitledPane pane) {
    CardTemplate template = getSelectedCardTemplate();

    if (layerEditorOverlayController.getSettingsPane() == pane) {
      return null;
    }
    else if (layerEditorBackgroundController.getSettingsPane() == pane && template.isRenderBackground()) {
      return null;
    }
    else if (layerEditorFrameController.getSettingsPane() == pane && template.isRenderFrame()) {
      return null;      // do not return the layer as it is not selectable
    }
    else if (layerEditorCanvasController.getSettingsPane() == pane && template.isRenderCanvas()) {
      return getLayer(CardLayerCanvas.class);
    }
    else if (layerEditorTitleController.getSettingsPane() == pane && template.isRenderTitle()) {
      return getLayer(CardLayerTitle.class);
    }
    else if (layerEditorTableNameController.getSettingsPane() == pane && template.isRenderTableName()) {
      return getLayer(CardLayerTableName.class);
    }
    else if (layerEditorWheelController.getSettingsPane() == pane && template.isRenderWheelIcon()) {
      return getLayer(CardLayerWheel.class);
    }
    else if (layerEditorManufacturerController.getSettingsPane() == pane && template.isRenderManufacturerLogo()) {
      return getLayer(CardLayerManufacturer.class);
    }
    else if (layerEditorOtherMediaController.getSettingsPane() == pane && template.isRenderOtherMedia()) {
      return getLayer(CardLayerOtherMedia.class);
    }
    else if (layerEditorScoresController.getSettingsPane() == pane && template.isRenderScores()) {
      return getLayer(CardLayerScores.class);
    }
    return null;
  }

  protected LayerEditorBaseController layerToController(CardLayer layer) {
    if (layer instanceof CardLayerBackground) {
      if (templateBeanBinder.getBean().isRenderFrame()) {
        return layerEditorFrameController;
      }
      else {
        return layerEditorBackgroundController;
      }
    }
    else if (layer instanceof CardLayerCanvas) {
      return layerEditorCanvasController;
    }
    else if (layer instanceof CardLayerTitle) {
      return layerEditorTitleController;
    }
    else if (layer instanceof CardLayerTableName) {
      return layerEditorTableNameController;
    }
    else if (layer instanceof CardLayerWheel) {
      return layerEditorWheelController;
    }
    else if (layer instanceof CardLayerManufacturer) {
      return layerEditorManufacturerController;
    }
    else if (layer instanceof CardLayerOtherMedia) {
      return layerEditorOtherMediaController;
    }
    else if (layer instanceof CardLayerScores) {
      return layerEditorScoresController;
    }
    //else
    throw new RuntimeException("CardLayer not mapped");
  }

  private CardLayer controllerToLayer(LayerEditorBaseController controller) {
    for (CardLayer layer : cardPreview.getLayers()) {
      if (layerToController(layer) == controller) {
        return layer;
      }
    }
    return null;
  }

  //-----------------------------------------

  /**
   * Null when the games should receive a custom template
   *
   * @param cardTemplate
   */
  private void assignTemplate(@Nullable CardTemplate cardTemplate) {
    CardTemplate baseTemplate = templateCombo.getValue();
    List<GameRepresentation> selection = highscoreCardsController.getSelections();
    ProgressDialog.createProgressDialog(new TemplateAssigmentProgressModel(selection, baseTemplate, cardTemplate));
    highscoreCardsController.refreshView(this.gameRepresentation.get());
  }

  public void setCardsController(HighscoreCardsController highscoreCardsController) {
    this.highscoreCardsController = highscoreCardsController;
  }

  public void selectTable(Optional<GameRepresentation> gameRepresentation) {
    this.gameRepresentation = gameRepresentation;
    if (this.gameRepresentation.isPresent()) {
      GameRepresentation game = gameRepresentation.get();
      CardTemplate template = client.getHighscoreCardTemplatesClient().getCardTemplateForGame(game);
      if (template != null) {
        if (template.isTemplate()) {
          selectTemplateInCombo(template);
        }
        else if (template.getParentId() != null) {
          CardTemplate parentTemplate = client.getHighscoreCardTemplatesClient().getTemplateById(template.getParentId());
          selectTemplateInCombo(parentTemplate);
        }

        setTemplate(template);
      }
    }
    else {
      refreshPreview(Optional.empty());
    }
  }

  private void selectTemplateInCombo(CardTemplate template) {
    templateCombo.valueProperty().removeListener(templateComboChangeListener);
    templateCombo.setValue(template);
    templateCombo.valueProperty().addListener(templateComboChangeListener);
  }

  //-------------- MediaPlayerListener
  @Override
  public void onReady(Media media) {
  }

  @Override
  public void onDispose() {
  }

  //-------------- Studio Event Listener
  @Override
  public void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {
    GameRepresentation game = client.getGameService().getGame(id);
    highscoreCardsController.refreshView(game);
    refreshNagBar(templateBeanBinder.getBean(), Optional.of(game));
  }

  class TemplateComboChangeListener implements ChangeListener<CardTemplate> {
    @Override
    public void changed(ObservableValue<? extends CardTemplate> observable, CardTemplate oldValue, CardTemplate newValue) {
      if (newValue != null) {
        if (gameRepresentation.isPresent()) {
          CardTemplate existingTemplate = client.getHighscoreCardTemplatesClient().getCardTemplateForGame(gameRepresentation.get());
          if (existingTemplate != null && !existingTemplate.isTemplate()) {
            Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply the template \"" + existingTemplate.getName() + "\" to the table \"" + gameRepresentation.get().getGameDisplayName() + "\"?", "This will delete the existing custom template.");
            if (!result.get().equals(ButtonType.OK)) {
              selectTemplateInCombo(oldValue);
              return;
            }
          }
        }
        // all good, assign template
        setTemplate(newValue);
        if (gameRepresentation.isPresent()) {
          assignTemplate(newValue);
        }
      }
    }
  }
}

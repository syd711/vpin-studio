package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.fx.cards.CardLayerBackground;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.PositionResizer;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorBackgroundController extends LayerEditorBaseController {

  @FXML
  private RadioButton coloredBackgroundRadio;
  @FXML
  private RadioButton defaultBackgroundRadio;
  @FXML
  private RadioButton fallbackBackgroundRadio;

  @FXML
  private VBox coloredBackgroundPane;

  @FXML
  private VBox fallbackBackgroundPane;

  @FXML
  private ColorPicker backgroundColorSelector;

  @FXML
  private ComboBox<String> backgroundImageCombo;

  @FXML
  private Button falbackUploadBtn;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private ImageView rawDirectB2SImage;

  @FXML
  private CheckBox grayScaleCheckbox;
  @FXML
  private Slider alphaPercentageSlider;
  @FXML
  private Slider brightenSlider;
  @FXML
  private Slider darkenSlider;
  @FXML
  private Slider blurSlider;

  private ObservableList<String> imageList;

  private Optional<GameRepresentation> game = Optional.empty();

  private void refreshRawPreview(GameRepresentation game) {
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

  @FXML
  private void onDefaultPictureUpload() {
    if (game.isPresent()) {
      boolean uploaded = TableDialogs.openDefaultBackgroundUploadDialog(game.get());
      if (uploaded) {
        refreshRawPreview(this.game.get());
        // refresh card preview
        this.templateEditorController.getLayer(CardLayerBackground.class).forceRefresh();
        EventManager.getInstance().notifyTableChange(game.get().getId(), null);
      }
    }
  }

  @FXML
  private void onOpenDefaultPicture() {
    if (this.game.isPresent()) {
      TableDialogs.openMediaDialog(Studio.stage, "Default Picture", client.getBackglassServiceClient().getDefaultPictureUrl(game.get()));
    }
  }

  @FXML
  private void onBackgroundReset() {
    if (this.game.isPresent()) {
      GameRepresentation game = this.game.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-generate default background for \"" + game.getGameDisplayName() + "\"?",
          "This will re-generate the existing default background.", null, "Generate Background");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Studio.client.getAssetService().deleteGameAssets(game.getId());
        // refresh card preview
        this.templateEditorController.getLayer(CardLayerBackground.class).forceRefresh();
        EventManager.getInstance().notifyTableChange(game.getId(), null);
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

  @Override
  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game, boolean locked) {
    super.setTemplate(cardTemplate, res, game, cardTemplate.isLockBackground());


    this.game = game;
    setIconVisibility(cardTemplate.isRenderBackground());

    // background
    if (cardTemplate.isUseColoredBackground()) {
      coloredBackgroundRadio.setSelected(true);
    }
    else if (cardTemplate.isUseDefaultBackground()) {
      defaultBackgroundRadio.setSelected(true);
    }
    else {
      fallbackBackgroundRadio.setSelected(true);
    }

    CardTemplateBinder.setColorPickerValue(backgroundColorSelector, cardTemplate, "backgroundColor");

    grayScaleCheckbox.setSelected(cardTemplate.isGrayScale());
    alphaPercentageSlider.setValue(cardTemplate.getTransparentPercentage());
    brightenSlider.setValue(cardTemplate.getAlphaWhite());
    darkenSlider.setValue(cardTemplate.getAlphaBlack());
    blurSlider.setValue(cardTemplate.getBlur());

    // fallback background
    String backgroundName = cardTemplate.getBackground();
    if (StringUtils.isEmpty(backgroundName)) {
      backgroundImageCombo.setValue(imageList.get(0));
    }

    if (game.isPresent()) {
      refreshRawPreview(game.get());
    }
  }

  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderBackground");

    ToggleGroup radioGroup = new ToggleGroup();
    coloredBackgroundRadio.setToggleGroup(radioGroup);
    defaultBackgroundRadio.setToggleGroup(radioGroup);
    fallbackBackgroundRadio.setToggleGroup(radioGroup);


    radioGroup.selectedToggleProperty().addListener((obs, o, n) -> {
      coloredBackgroundPane.setDisable(n != coloredBackgroundRadio);
      fallbackBackgroundPane.setDisable(n != fallbackBackgroundRadio);

      templateBeanBinder.setProperty("useColoredBackground", n == coloredBackgroundRadio);
      templateBeanBinder.setProperty("useDefaultBackground", n == defaultBackgroundRadio);
    });

    // default background
    imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
    backgroundImageCombo.setItems(imageList);
    backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
    backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

    templateBeanBinder.bindColorPicker(backgroundColorSelector, "backgroundColor");

    templateBeanBinder.bindComboBox(backgroundImageCombo, "background");

    // Other properties
    templateBeanBinder.bindCheckbox(grayScaleCheckbox, "grayScale");
    templateBeanBinder.bindSlider(alphaPercentageSlider, "transparentPercentage");
    templateBeanBinder.bindSlider(brightenSlider, "alphaWhite");
    templateBeanBinder.bindSlider(darkenSlider, "alphaBlack");
    templateBeanBinder.bindSlider(blurSlider, "blur");

    templateBeanBinder.bindToggleButton(lockBtn, "lockBackground");

    /* coloredBackgroundRadio.selectedProperty().addListener((obs, old, enabled) -> {
      grayScaleCheckbox.setDisable(enabled);
      blurSlider.setDisable(enabled);
      brightenSlider.setDisable(enabled);
      darkenSlider.setDisable(enabled);
      if (enabled && alphaPercentageSlider.getValue() <= 0) {
        alphaPercentageSlider.setValue(50);
      }
    }); */
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
  }

  @Override
  public void unbindDragBox(PositionResizer dragBox) {
  }
}

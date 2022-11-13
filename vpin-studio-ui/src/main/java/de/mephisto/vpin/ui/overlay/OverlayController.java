package de.mephisto.vpin.ui.overlay;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.ObservedPropertyChangeListener;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.ImageUtil;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.application.Platform;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

public class OverlayController implements Initializable, ObservedPropertyChangeListener, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayController.class);

  @FXML
  private ImageView overlayPreview;

  @FXML
  private Label titleFontLabel;

  @FXML
  private Label scoreFontLabel;

  @FXML
  private Label tableFontLabel;

  @FXML
  private ComboBox<String> imageScalingCombo;

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
  private Spinner<Integer> marginTopSpinner;

  @FXML
  private Spinner<Integer> rowSeparatorSpinner;

  @FXML
  private BorderPane imageCenter;

  @FXML
  private Button openImageBtn;

  @FXML
  private Button generateBtn;

  @FXML
  private Label imageMetaDataLabel;

  @FXML
  private StackPane previewStack;

  @FXML
  private CheckBox grayScaleCheckbox;


  private VPinStudioClient client;

  private ObservedProperties properties;

  private List<String> ignoreList = new ArrayList<>();
  private ObservableList<String> imageList;
  private Parent waitOverlay;
  private boolean autoRefresh = true;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      client = Studio.client;
      properties = client.getProperties("overlay-generator");
      properties.addObservedPropertyChangeListener(this);

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Overlay...");

      initFields();

      overlayPreview.setPreserveRatio(true);
      stage.widthProperty().addListener((obs, oldVal, newVal) -> {
        try {
          Thread.sleep(400);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        overlayPreview.setFitWidth(newVal.intValue() / 2);
        refreshPreview(false);
      });

      stage.heightProperty().addListener((obs, oldVal, newVal) -> {

      });
    } catch (Exception e) {
      LOG.error("Failed to init overlay: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onUploadButton() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.jpeg"),
        new FileChooser.ExtensionFilter("JPG", "*.jpg"),
        new FileChooser.ExtensionFilter("PNG", "*.png"));
    File file = fileChooser.showOpenDialog(stage);
    if (file != null && file.exists()) {
      try {
        boolean result = client.uploadOverlayBackgroundImage(file);
        if (result) {
          String baseName = FilenameUtils.getBaseName(file.getName());
          if (!imageList.contains(baseName)) {
            imageList.add(baseName);
          }
        }
      } catch (Exception e) {
        WidgetFactory.showAlert("Uploading image failed, check log file for details:\n\n" + e.getMessage());
      }
    }
  }

  @FXML
  private void onOpenImage() {
    InputStream image = client.getOverlayImage();
    MediaUtil.openMedia(image);
  }

  @FXML
  private void onFontTitleSelect() {
    BindingUtil.bindFontSelector(properties, "overlay.title", titleFontLabel);
  }

  @FXML
  private void onFontTableSelect() {
    BindingUtil.bindFontSelector(properties, "overlay.table", tableFontLabel);
  }

  @FXML
  private void onFontScoreSelect() {
    BindingUtil.bindFontSelector(properties, "overlay.score", scoreFontLabel);
  }

  @FXML
  private void onAutoRefreshChange(ActionEvent event) {
    CheckBox autoRefreshCheckbox = (CheckBox) event.getSource();
    this.autoRefresh = autoRefreshCheckbox.isSelected();
  }

  @FXML
  private void onGenerateClick() {
    refreshPreview(true);
  }

  public OverlayController() {
  }

  private void initFields() {
    overlayPreview.rotateProperty().set(90);

    NavigationController.setBreadCrumb(Arrays.asList("Overlay Generation"));

    BindingUtil.bindFontLabel(titleFontLabel, properties, "overlay.title");
    BindingUtil.bindFontLabel(tableFontLabel, properties, "overlay.table");
    BindingUtil.bindFontLabel(scoreFontLabel, properties, "overlay.score");
    BindingUtil.bindCheckbox(grayScaleCheckbox, properties, "overlay.grayScale");

    BindingUtil.bindColorPicker(fontColorSelector, properties, "overlay.font.color");

    imageScalingCombo.setItems(FXCollections.observableList(Arrays.asList("1280x720", "1920x1080", "2560x1440", "3840x2160")));
    BindingUtil.bindComboBox(imageScalingCombo, properties, "overlay.resolution", "2.560x1.440");

    imageList = FXCollections.observableList(new ArrayList<>(client.getOverlayBackgrounds()));
    backgroundImageCombo.setItems(imageList);
    backgroundImageCombo.setCellFactory(c -> new WidgetFactory.OverlayBackgroundImageListCell(client));
    backgroundImageCombo.setButtonCell(new WidgetFactory.OverlayBackgroundImageListCell(client));
    BindingUtil.bindComboBox(backgroundImageCombo, properties, "overlay.background");

    BindingUtil.bindTextField(titleText, properties, "overlay.title.text");
    BindingUtil.bindSlider(brightenSlider, properties, "overlay.alphacomposite.white");
    BindingUtil.bindSlider(darkenSlider, properties, "overlay.alphacomposite.black");
    BindingUtil.bindSlider(blurSlider, properties, "overlay.blur");
    BindingUtil.bindSpinner(marginTopSpinner, properties, "overlay.padding");
    BindingUtil.bindSpinner(rowSeparatorSpinner, properties, "overlay.highscores.row.separator");

    onGenerateClick();
  }

  private void refreshPreview(boolean regenerate) {
    int offset = 70;
    Platform.runLater(() -> {
      this.generateBtn.setDisable(regenerate);
      this.openImageBtn.setDisable(regenerate);
      this.imageMetaDataLabel.setText("");


      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);

      try {
        new Thread(() -> {
          if (regenerate) {
            client.generateOverlayImage();
          }

          InputStream input = client.getOverlayImage();
          Image image = new Image(input);
          overlayPreview.setImage(image);
          overlayPreview.setVisible(true);
          overlayPreview.setFitWidth(imageCenter.getHeight() - offset);
          overlayPreview.setFitHeight(imageCenter.getWidth() - offset);

          Platform.runLater(() -> {
            imageMetaDataLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
            previewStack.getChildren().remove(waitOverlay);
            this.generateBtn.setDisable(false);
            this.openImageBtn.setDisable(false);
          });

        }).start();
        overlayPreview.setFitHeight(imageCenter.getWidth() - offset);
        overlayPreview.setFitWidth(imageCenter.getHeight() - offset);

      } catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    if (!ignoreList.contains(key) && autoRefresh) {
      onGenerateClick();
    }
  }
}

package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.ObservedPropertyChangeListener;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.MediaUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

public class HighscoreCardsController implements Initializable, ObservedPropertyChangeListener, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

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
//
//  @FXML
//  private ComboBox<String> imageRatioCombo;
//
//  @FXML
//  private ComboBox<String> imageScalingCombo;

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
  private CheckBox renderRawHighscore;

  @FXML
  private ComboBox<GameRepresentation> tableCombo;

  @FXML
  private BorderPane imageCenter;

  @FXML
  private FontIcon rawHighscoreHelp;

  @FXML
  private Button openImageBtn;

  @FXML
  private Button generateBtn;

  @FXML
  private Label imageMetaDataLabel;

  @FXML
  private StackPane previewStack;

  @FXML
  private TitledPane backgroundSettingsPane;

  @FXML
  private Accordion accordion;


  private VPinStudioClient client;

  private ObservedProperties properties;

  private List<String> ignoreList = new ArrayList<>();
  private ObservableList<String> imageList;
  private Parent waitOverlay;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      client = Studio.client;
      properties = client.getProperties("card-generator");
      ignoreList.addAll(Arrays.asList("popper.screen"));
      properties.addObservedPropertyChangeListener(this);

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Card...");

      onTableRefresh();
      initFields();

      cardPreview.setPreserveRatio(true);
      stage.widthProperty().addListener((obs, oldVal, newVal) -> {
        try {
          Thread.sleep(400);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        cardPreview.setFitWidth(newVal.intValue() / 2);
        refreshPreview(Optional.ofNullable(tableCombo.getValue()), false);
      });

      stage.heightProperty().addListener((obs, oldVal, newVal) -> {

      });
    } catch (Exception e) {
      LOG.error("Failed to init highscores: " + e.getMessage(), e);
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
        boolean result = client.uploadHighscoreBackgroundImage(file, null);
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
    GameRepresentation game = tableCombo.getValue();
    if (game != null) {
      ByteArrayInputStream s = Studio.client.getHighscoreCard(game);
      MediaUtil.openMedia(s);
    }
  }

  @FXML
  private void onDefaultPictureUpload() {
    GameRepresentation game = tableCombo.getValue();
    boolean uploaded = Dialogs.openDefaultBackgroundUploadDialog(game);
    if (uploaded) {
      refreshRawPreview(Optional.of(game));
      onGenerateClick();
    }
  }

  @FXML
  private void onGenerateAll() {
    ObservedProperties properties = Studio.client.getProperties("card-generator");
    String targetScreen = properties.getProperty("popper.screen", null);
    if(StringUtils.isEmpty(targetScreen)) {
      WidgetFactory.showAlert(Studio.stage, "Not target screen selected.", "Select a target screen in the preferences.");
    }
    else {
      Dialogs.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Cards"));
    }
  }

  @FXML
  private void onTableRefresh() {
    List<GameRepresentation> games = client.getGamesWithScores();
    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);

    GameRepresentation game = tableCombo.getSelectionModel().getSelectedItem();
    tableCombo.getItems().clear();
    tableCombo.getItems().addAll(gameRepresentations);

    if(game != null) {
      tableCombo.getSelectionModel().select(game);
    }
    onGenerateClick();
  }

  @FXML
  private void onFontTitleSelect() {
    BindingUtil.bindFontSelector(properties, "card.title", titleFontLabel);
  }

  @FXML
  private void onFontTableSelect() {
    BindingUtil.bindFontSelector(properties, "card.table", tableFontLabel);
  }

  @FXML
  private void onFontScoreSelect() {
    BindingUtil.bindFontSelector(properties, "card.score", scoreFontLabel);
  }

  @FXML
  private void onOpenDefaultPicture() {
    GameRepresentation game = tableCombo.getValue();
    if (game != null) {
      ByteArrayInputStream s = Studio.client.getDefaultPicture(game);
      MediaUtil.openMedia(s);
    }
  }

  @FXML
  private void onGenerateClick() {
    GameRepresentation value = tableCombo.getValue();
    refreshPreview(Optional.ofNullable(value), true);
  }

  public HighscoreCardsController() {
  }

  private void initFields() {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));

    BindingUtil.bindFontLabel(titleFontLabel, properties, "card.title");
    BindingUtil.bindFontLabel(tableFontLabel, properties, "card.table");
    BindingUtil.bindFontLabel(scoreFontLabel, properties, "card.score");

    BindingUtil.bindColorPicker(fontColorSelector, properties, "card.font.color");

    BindingUtil.bindHighscoreTablesComboBox(client, tableCombo, properties, "card.sampleTable");


    BindingUtil.bindCheckbox(useDirectB2SCheckbox, properties, "card.useDirectB2S");
    BindingUtil.bindCheckbox(grayScaleCheckbox, properties, "card.grayScale");
//    useDirectB2SCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> imageRatioCombo.setDisable(!t1));
//    useDirectB2SCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> imageScalingCombo.setDisable(!t1));

//    imageRatioCombo.setItems(FXCollections.observableList(Arrays.asList("RATIO_16x9", "RATIO_4x3")));
//    imageRatioCombo.setDisable(!useDirectB2SCheckbox.selectedProperty().get());
//
//    imageRatioCombo.setCellFactory(c -> new WidgetFactory.RationListCell());
//    imageRatioCombo.setButtonCell(new WidgetFactory.RationListCell());
//    BindingUtil.bindComboBox(imageRatioCombo, properties, "card.ratio");
//
//    imageScalingCombo.setItems(FXCollections.observableList(Arrays.asList("1024", "1280", "1920", "2560")));
//    imageScalingCombo.setDisable(!useDirectB2SCheckbox.selectedProperty().get());
//    BindingUtil.bindComboBox(imageScalingCombo, properties, "card.scaling", "1280");

    imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreBackgroundImages()));
    backgroundImageCombo.setItems(imageList);
    backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
    backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));
    BindingUtil.bindComboBox(backgroundImageCombo, properties, "card.background");

    BindingUtil.bindTextField(titleText, properties, "card.title.text");
    BindingUtil.bindSlider(brightenSlider, properties, "card.alphacomposite.white");
    BindingUtil.bindSlider(darkenSlider, properties, "card.alphacomposite.black");
    BindingUtil.bindSlider(blurSlider, properties, "card.blur");
    BindingUtil.bindSlider(borderSlider, properties, "card.border.width");
    BindingUtil.bindSpinner(marginTopSpinner, properties, "card.padding");
    BindingUtil.bindSpinner(wheelImageSpinner, properties, "card.highscores.row.padding.left");
    BindingUtil.bindSpinner(rowSeparatorSpinner, properties, "card.highscores.row.separator");

    BindingUtil.bindCheckbox(renderRawHighscore, properties, "card.rawHighscore");
    renderRawHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      wheelImageSpinner.setDisable(t1);
      rowSeparatorSpinner.setDisable(t1);
    });

    wheelImageSpinner.setDisable(renderRawHighscore.isSelected());
    rowSeparatorSpinner.setDisable(renderRawHighscore.isSelected());

    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      if (t1 == null) {
        refreshRawPreview(Optional.empty());
      }
      else {
        refreshRawPreview(Optional.of(t1));
      }
    });

    rawHighscoreHelp.setCursor(javafx.scene.Cursor.HAND);
    Tooltip tooltip = new Tooltip();
    tooltip.setGraphic(rawHighscoreHelp);
    Tooltip.install(rawHighscoreHelp, new Tooltip("The font size of the highscore text will be adapted according to the number of lines."));

    GameRepresentation value = tableCombo.getValue();
    refreshRawPreview(Optional.ofNullable(value));
    refreshPreview(Optional.ofNullable(value), false);

    accordion.setExpandedPane(backgroundSettingsPane);
  }

  private void refreshRawPreview(Optional<GameRepresentation> game) {
    try {
      resolutionLabel.setText("");
      openDefaultPictureBtn.setVisible(false);
      rawDirectB2SImage.setImage(null);

      if (game.isPresent()) {
        openDefaultPictureBtn.setTooltip(new Tooltip("Open directb2s image"));
        InputStream input = client.getDefaultPicture(game.get());
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
    if (!game.isPresent()) {
      return;
    }

    int offset = 150;
    Platform.runLater(() -> {
      this.generateBtn.setDisable(!game.isPresent());
      this.openImageBtn.setDisable(!game.isPresent());
      this.imageMetaDataLabel.setText("");


      previewStack.getChildren().remove(waitOverlay);
      previewStack.getChildren().add(waitOverlay);

      try {
        new Thread(() -> {
          if (regenerate) {
            client.generateHighscoreCardSample(game.get());
          }

          InputStream input = client.getHighscoreCard(game.get());
          Image image = new Image(input);
          cardPreview.setImage(image);
          cardPreview.setVisible(true);

//          int resolution = Integer.parseInt(imageScalingCombo.getValue());
//          if (image.getWidth() >= resolution && image.getWidth() < imageCenter.getWidth()) {
////              cardPreview.setFitHeight(image.getHeight());
////              cardPreview.setFitWidth(image.getWidth());
//          }
//          else {
//            cardPreview.setFitHeight(imageCenter.getHeight() - offset);
//            cardPreview.setFitWidth(imageCenter.getWidth() - offset);
//          }

          Platform.runLater(() -> {
            imageMetaDataLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
            previewStack.getChildren().remove(waitOverlay);
          });

        }).start();
        cardPreview.setFitHeight(imageCenter.getHeight() - offset);
        cardPreview.setFitWidth(imageCenter.getWidth() - offset);

      } catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    if (!ignoreList.contains(key)) {
      onGenerateClick();
    }
  }

  @Override
  public void onViewActivated() {

  }
}
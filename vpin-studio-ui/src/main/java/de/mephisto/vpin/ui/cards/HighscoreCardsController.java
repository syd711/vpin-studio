
package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.ObservedPropertyChangeListener;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.VPinStudioApplication;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.VPinStudioApplication.stage;

public class HighscoreCardsController implements Initializable, ObservedPropertyChangeListener, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button openDirectB2SImageButton;

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
  private CheckBox enableCardGenerationCheckbox;

  @FXML
  private ComboBox<String> popperScreenCombo;

  @FXML
  private CheckBox useDirectB2SCheckbox;

  @FXML
  private ComboBox<String> imageRatioCombo;

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

  private VPinStudioClient client;

  private ObservedProperties properties;

  private List<String> ignoreList = new ArrayList<>();
  private ObservableList<String> imageList;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      client = new VPinStudioClient();
      properties = client.getProperties("card-generator");
      ignoreList.addAll(Arrays.asList("card.generation.enabled", "popper.screen"));
      properties.addObservedPropertyChangeListener(this);

      List<GameRepresentation> games = client.getGames();
      ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);
      tableCombo.getItems().addAll(gameRepresentations);

      initFields();

      cardPreview.setPreserveRatio(true);
      stage.widthProperty().addListener((obs, oldVal, newVal) -> {
        try {
          Thread.sleep(400);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        cardPreview.setFitWidth(newVal.intValue() / 2);
        refreshPreview(tableCombo.getValue(), false);
      });

      stage.heightProperty().addListener((obs, oldVal, newVal) -> {

      });
    } catch (Exception e) {
      LOG.error("Failed to init highscores: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onUploadButton() throws IOException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.jpeg"),
        new FileChooser.ExtensionFilter("JPG", "*.jpg"),
        new FileChooser.ExtensionFilter("PNG", "*.png"));
    File file = fileChooser.showOpenDialog(stage);
    if (file != null && file.exists()) {
      boolean result = client.uploadHighscoreBackgroundImage(file);
      if (result) {
        String baseName = FilenameUtils.getBaseName(file.getName());
        if (!imageList.contains(baseName)) {
          imageList.add(baseName);
        }
      }
      else {
        WidgetFactory.showAlert("Uploading image failed, check log file for details.");
      }
    }
  }

  @FXML
  private void onOpenImage() {
    GameRepresentation game = tableCombo.getValue();
    if (game != null) {
      try {
        ByteArrayInputStream s = client.getHighscoreCard(game);
        byte[] bytes = s.readAllBytes();
        File png = File.createTempFile("vpin-studio", ".png");
        png.deleteOnExit();
        IOUtils.write(bytes, new FileOutputStream(png));
        s.close();

        Desktop.getDesktop().open(png);
      } catch (IOException e) {
        LOG.error("Failed to create image temp file: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onGenerateAll() {
    WidgetFactory.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Cards"));
  }

  @FXML
  private void onTableRefresh() {
    List<GameRepresentation> games = client.getGames();
    tableCombo.getItems().clear();
    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);
    tableCombo.getItems().addAll(gameRepresentations);
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
  private void onOpenDirectB2SBackground() {
    GameRepresentation game = tableCombo.getValue();
    if (game != null) {
      try {
        ByteArrayInputStream s = client.getDirectB2SImage(game);
        byte[] bytes = s.readAllBytes();
        File png = File.createTempFile("vpin-studio-directb2s-", ".png");
        png.deleteOnExit();
        IOUtils.write(bytes, new FileOutputStream(png));
        s.close();

        Desktop.getDesktop().open(png);
      } catch (IOException e) {
        LOG.error("Failed to create image temp file: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onGenerateClick() {
    GameRepresentation value = tableCombo.getValue();
    refreshPreview(value, true);
  }

  public HighscoreCardsController() {
  }

  private void initFields() {
    BindingUtil.bindCheckbox(enableCardGenerationCheckbox, properties, "card.generation.enabled");
    enableCardGenerationCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> popperScreenCombo.setDisable(!t1));
    popperScreenCombo.setDisable(!enableCardGenerationCheckbox.selectedProperty().get());

    BindingUtil.bindFontLabel(titleFontLabel, properties, "card.title");
    BindingUtil.bindFontLabel(tableFontLabel, properties, "card.table");
    BindingUtil.bindFontLabel(scoreFontLabel, properties, "card.score");

    BindingUtil.bindColorPicker(fontColorSelector, properties, "card.font.color");

    BindingUtil.bindTableComboBox(client, tableCombo, properties, "card.sampleTable");

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("Other2", "GameInfo", "GameHelp")));
    BindingUtil.bindComboBox(popperScreenCombo, properties, "popper.screen");

    BindingUtil.bindCheckbox(useDirectB2SCheckbox, properties, "card.useDirectB2S");
    useDirectB2SCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> imageRatioCombo.setDisable(!t1));
    useDirectB2SCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> imageScalingCombo.setDisable(!t1));

    imageRatioCombo.setItems(FXCollections.observableList(Arrays.asList("RATIO_16x9", "RATIO_4x3")));
    imageRatioCombo.setDisable(!useDirectB2SCheckbox.selectedProperty().get());

    imageRatioCombo.setCellFactory(c -> new WidgetFactory.RationListCell());
    imageRatioCombo.setButtonCell(new WidgetFactory.RationListCell());
    BindingUtil.bindComboBox(imageRatioCombo, properties, "card.ratio");

    imageScalingCombo.setItems(FXCollections.observableList(Arrays.asList("1024", "1280", "1920", "2560", "3840")));
    imageScalingCombo.setDisable(!useDirectB2SCheckbox.selectedProperty().get());
    BindingUtil.bindComboBox(imageScalingCombo, properties, "card.scaling", "1280");

    imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreBackgroundImages()));
    backgroundImageCombo.setItems(imageList);
    backgroundImageCombo.setCellFactory(c -> new WidgetFactory.ImageListCell(client));
    backgroundImageCombo.setButtonCell(new WidgetFactory.ImageListCell(client));
    BindingUtil.bindComboBox(backgroundImageCombo, properties, "card.background");

    BindingUtil.bindTextField(titleText, properties, "card.title.text");
    BindingUtil.bindSlider(brightenSlider, properties, "card.alphacomposite.white");
    BindingUtil.bindSlider(darkenSlider, properties, "card.alphacomposite.black");
    BindingUtil.bindSlider(blurSlider, properties, "card.blur");
    BindingUtil.bindSlider(borderSlider, properties, "card.border.width");
    BindingUtil.bindSpinner(marginTopSpinner, properties, "card.title.y.offset");
    BindingUtil.bindSpinner(wheelImageSpinner, properties, "card.highscores.row.padding.left");
    BindingUtil.bindSpinner(rowSeparatorSpinner, properties, "card.highscores.row.separator");

    BindingUtil.bindCheckbox(renderRawHighscore, properties, "card.rawScoreData");

    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> refreshRawPreview(t1));

    rawHighscoreHelp.setCursor(javafx.scene.Cursor.HAND);
    Tooltip tooltip = new Tooltip();
    tooltip.setGraphic(rawHighscoreHelp);
    Tooltip.install(rawHighscoreHelp, new Tooltip("The font size of the highscore text will be adapted according to the number of lines."));

    GameRepresentation value = tableCombo.getValue();
    refreshRawPreview(value);
    onGenerateClick();
  }

  private void refreshRawPreview(@Nullable GameRepresentation game) {
    try {
      openDirectB2SImageButton.setVisible(false);
      openDirectB2SImageButton.setTooltip(new Tooltip("Open directb2s image"));
      InputStream input = client.getDirectB2SImage(game);
      Image image = new Image(input);
      rawDirectB2SImage.setImage(image);
      input.close();

      if (image.getWidth() > 300) {
        openDirectB2SImageButton.setVisible(true);
        resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
      }
      else {
        resolutionLabel.setText("");
      }
    } catch (IOException e) {
      LOG.error("Failed to load raw b2s: " + e.getMessage(), e);
    }
  }

  private void refreshPreview(@Nullable GameRepresentation game, boolean regenerate) {
    if (game == null) {
      return;
    }
    Platform.runLater(() -> {
      try {
        if (regenerate) {
          new Thread(() -> {
            setBusy(true);
            InputStream input = client.getHighscoreCard(game);
            Image image = new Image(input);
            cardPreview.setImage(image);
            cardPreview.setVisible(true);
            setBusy(false);

            cardPreview.setFitHeight(imageCenter.getHeight() - 250);
            cardPreview.setFitWidth(imageCenter.getWidth() - 250);
          }).start();
        }
        cardPreview.setFitHeight(imageCenter.getHeight() - 250);
        cardPreview.setFitWidth(imageCenter.getWidth() - 250);

      } catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void changed(@NonNull String propertiesName, @NonNull String key, @Nullable String updatedValue) {
    if (!ignoreList.contains(key)) {
      onGenerateClick();
    }
  }

  private RotateTransition transition;

  private void setBusy(boolean b) {
    if(b) {
      Image image = new Image(VPinStudioApplication.class.getResourceAsStream("loading.png"));
      cardPreview.setImage(image);
      cardPreview.setFitWidth(300);
    }
    else {
      cardPreview.setFitWidth(imageCenter.getWidth() - 60);
      cardPreview.setFitHeight(imageCenter.getHeight() - 60);
    }
  }

  @Override
  public void dispose() {

  }
}
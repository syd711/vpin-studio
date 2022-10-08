
package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.ObservedPropertyChangeListener;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.VPinStudioApplication;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.TransitionUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HighscoreCardsController implements Initializable, ObservedPropertyChangeListener {
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
  private ComboBox<GameRepresentation> tableCombo;


  private VPinStudioClient client;

  private ObservedProperties properties;

  private List<String> ignoreList = new ArrayList<>();

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
    } catch (Exception e) {
      LOG.error("Failed to init highscores: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onOpenImage() {
    GameRepresentation game = tableCombo.getValue();
    if(game != null) {
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
  private void onGenerateAll() throws IOException {
    WidgetFactory.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Cards"), VPinStudioApplication.stage);
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
    if(game != null) {
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
    refreshPreview(value);
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

    imageRatioCombo.setItems(FXCollections.observableList(Arrays.asList("RATIO_16x9", "RATIO_4x3")));
    imageRatioCombo.setDisable(!useDirectB2SCheckbox.selectedProperty().get());

    imageRatioCombo.setCellFactory(c -> new WidgetFactory.RationListCell());
    imageRatioCombo.setButtonCell(new WidgetFactory.RationListCell());

    BindingUtil.bindComboBox(imageRatioCombo, properties, "card.ratio");

    backgroundImageCombo.setItems(FXCollections.observableList(client.getBackgroundImages()));
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

    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> refreshRawPreview(t1));

    GameRepresentation value = tableCombo.getValue();
    refreshRawPreview(value);

    this.onGenerateClick();
  }

  private void refreshRawPreview(@Nullable GameRepresentation game) {
    try {
      openDirectB2SImageButton.setVisible(false);
      openDirectB2SImageButton.setTooltip(new Tooltip("Open directb2s image"));
      InputStream input = client.getDirectB2SImage(game);
      Image image = new Image(input);
      rawDirectB2SImage.setImage(image);
      input.close();

      if(image.getWidth() > 300) {
        openDirectB2SImageButton.setVisible(true);
        resolutionLabel.setText("Resolution: " + (int)image.getWidth() + " x " + (int)image.getHeight());
      }
      else {
        resolutionLabel.setText("");
      }
    } catch (IOException e) {
      LOG.error("Failed to load raw b2s: " + e.getMessage(), e);
    }
  }

  private void refreshPreview(@Nullable GameRepresentation game) {
    cardPreview.setOpacity(1);
    if(game == null) {
      return;
    }

    Platform.runLater(() -> {
      try {
        TransitionUtil.createOutFader(cardPreview, 300).play();
        InputStream input = client.getHighscoreCard(game);
        Image image = new Image(input);
        cardPreview.setImage(image);
        input.close();

        TransitionUtil.createInFader(cardPreview, 300).play();
      } catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void changed(@NonNull String propertiesName, @NonNull String key, @Nullable String updatedValue) {
    if(!ignoreList.contains(key)) {
      this.onGenerateClick();
    }
  }
}
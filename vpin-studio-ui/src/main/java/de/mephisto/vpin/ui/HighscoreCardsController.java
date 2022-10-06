
package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.ObservedPropertyChangeListener;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.TransitionUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.controlsfx.dialog.FontSelectorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HighscoreCardsController implements Initializable, ObservedPropertyChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardsController.class);

  @FXML
  private ImageView cardPreview;

  @FXML
  private Button generateBtn;

  @FXML
  private Button generateAllBtn;

  @FXML
  private Button openBtn;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Label selectedTableLabel;

  @FXML
  private ComboBox<String> popperScreenCombo;

  @FXML
  private CheckBox useDirectB2SCheckbox;

  @FXML
  private ComboBox<String> imageRatioCombo;

  @FXML
  private ComboBox backgroundImageCombo;

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
  private Spinner marginTopSpinner;

  @FXML
  private Spinner wheelImageSpinner;

  @FXML
  private Spinner rowSeparatorSpinner;

  @FXML
  private ComboBox<GameRepresentation> tableCombo;

  private VPinStudioClient client;

  private ObservedProperties properties;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      client = new VPinStudioClient();
      properties = client.getProperties("card-generator");
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
  private void onTableRefresh() {
    List<GameRepresentation> games = client.getGames();
    tableCombo.getItems().clear();
    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);
    tableCombo.getItems().addAll(gameRepresentations);
    onGenerateClick();
  }

  @FXML
  private void onFontTitleSelect() {
   BindingUtil.bindFontSelector(properties, "card");
  }

  @FXML
  private void onColorSelect() {
    ColorPicker p = new ColorPicker(Color.WHITE);
    p.show();
  }

  @FXML
  private void onGenerateClick() {
    GameRepresentation value = tableCombo.getValue();
    refreshPreview(value);
  }

  public HighscoreCardsController() {
  }

  private void initFields() throws Exception {
    BindingUtil.bindTableComboBox(client, tableCombo, properties, "card.sampleTable");

    popperScreenCombo.setItems(FXCollections.observableList(Arrays.asList("Other2", "GameInfo", "GameHelp")));
    BindingUtil.bindComboBox(popperScreenCombo, properties, "popper.screen");

    BindingUtil.bindCheckbox(useDirectB2SCheckbox, properties, "card.useDirectB2S");
    useDirectB2SCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        imageRatioCombo.setDisable(!t1);
      }
    });

    imageRatioCombo.setItems(FXCollections.observableList(Arrays.asList("RATIO_16x9", "RATIO_4x3")));
    imageRatioCombo.setDisable(!useDirectB2SCheckbox.selectedProperty().get());

    BindingUtil.bindComboBox(imageRatioCombo, properties, "card.ratio");

// backgroundImageCombo;
    BindingUtil.bindTextField(titleText, properties, "card.title.text");
    BindingUtil.bindSlider(brightenSlider, properties, "card.alphacomposite.white");
    BindingUtil.bindSlider(darkenSlider, properties, "card.alphacomposite.black");
    BindingUtil.bindSlider(blurSlider, properties, "card.blur");
    BindingUtil.bindSlider(borderSlider, properties, "card.border.width");
    BindingUtil.bindSpinner(marginTopSpinner, properties, "card.title.y.offset");
    BindingUtil.bindSpinner(wheelImageSpinner, properties, "card.highscores.row.padding.left");
    BindingUtil.bindSpinner(rowSeparatorSpinner, properties, "card.highscores.row.separator");

    this.onGenerateClick();
  }

  private void refreshPreview(@Nullable GameRepresentation game) {
    cardPreview.setOpacity(1);
    if(game == null) {
      return;
    }

    Platform.runLater(() -> {
      try {
        TransitionUtil.createOutFader(cardPreview, 300).play();
        String url = "http://localhost:8089/api/v1/generator/card/" + game.getId();
        InputStream input = new URL(url).openStream();
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
    this.onGenerateClick();
  }
}
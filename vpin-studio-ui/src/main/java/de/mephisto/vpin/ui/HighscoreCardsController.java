
package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.TransitionUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class HighscoreCardsController implements Initializable {
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
  private ComboBox popperScreenCombo;

  @FXML
  private CheckBox useDirectB2SCheckbox;

  @FXML
  private ComboBox imageRatioCombo;

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

  @FXML
  private void onGenerateClick() {
    GameRepresentation value = tableCombo.getValue();
    refreshPreview(value);
  }

  @FXML
  private void onTableSelect() {
    GameRepresentation value = tableCombo.getValue();
    if (value != null) {
      selectedTableLabel.setText("Table: " + value);
      if(cardPreview.getImage() != null) {
        resolutionLabel.setText("Resolution: " + cardPreview.getImage().getWidth() + " x " + cardPreview.getImage().getHeight());
      }
      client.setBundleProperty("card-generator", "card.sampleTable", String.valueOf(value.getId()));
    }
    else {
      selectedTableLabel.setText("Table: -");
      resolutionLabel.setText("Resolution: -");
      client.setBundleProperty("card-generator", "card.sampleTable", "");
    }
    refreshPreview(value);
  }

  // Add a public no-args constructor
  public HighscoreCardsController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      client = new VPinStudioClient();
      List<GameRepresentation> games = client.getGames();
      ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);
      tableCombo.getItems().addAll(gameRepresentations);

      initFields();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initFields() throws Exception {
    ObservedProperties properties = client.getProperties("card-generator");
    String property = properties.getProperty("card.title.text");
    int marginTop = Integer.parseInt(properties.getProperty("card.title.y.offset"));
    BindingUtil.bindTextField(titleText, properties, "card.title.text");
//    popperScreenCombo;
// useDirectB2SCheckbox;
// imageRatioCombo;
// backgroundImageCombo;
// titleText;
// brightenSlider;
// darkenSlider;
// blurSlider;
    BindingUtil.bindSlider(borderSlider, properties, "card.border.width");
    BindingUtil.bindSpinner(marginTopSpinner, properties, "card.title.y.offset");
// wheelImageSpinner;
// rowSeparatorSpinner;
  }

  private void refreshPreview(@Nullable GameRepresentation game) {
    cardPreview.setOpacity(1);
    if(game == null) {
      return;
    }
    cardPreview.setOpacity(1);

    Platform.runLater(() -> {
      try {
        TransitionUtil.createOutFader(cardPreview, 300).play();
        String url = "http://localhost:8089/api/v1/generator/card/" + game.getId();
        System.out.println(url);
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
}
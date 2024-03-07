
package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplates;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.util.BeanBindingUtil;
import de.mephisto.vpin.ui.util.MediaUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
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

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class HighscoreCardsController implements Initializable, StudioFXController {
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

  @FXML
  private CheckBox transparentBackgroundCheckbox;

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
  private Slider alphaPercentageSpinner;

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

  @FXML
  private CheckBox renderTableNameCheckbox;

  private List<String> ignoreList = new ArrayList<>();
  private ObservableList<String> imageList;
  private Parent waitOverlay;

  private CardTemplates cardTemplates;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      cardTemplates = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_TEMPLATES, CardTemplates.class);
      ignoreList.addAll(Arrays.asList("popperScreen"));

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

      if (!tableCombo.getItems().isEmpty()) {
        tableCombo.setValue(tableCombo.getItems().get(0));
      }
    } catch (Exception e) {
      LOG.error("Failed to init highscores: " + e.getMessage(), e);
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
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Uploading image failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onOpenImage() {
    GameRepresentation game = tableCombo.getValue();
    if (game != null) {
      ByteArrayInputStream s = client.getHighscoreCardsService().getHighscoreCard(game);
      MediaUtil.openMedia(s);
    }
  }

  @FXML
  private void onDefaultPictureUpload() {
    GameRepresentation game = tableCombo.getValue();
    boolean uploaded = TableDialogs.openDefaultBackgroundUploadDialog(game);
    if (uploaded) {
      refreshRawPreview(Optional.of(game));
      onGenerateClick();
    }
  }

  @FXML
  private void onGenerateAll() {
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    String targetScreen = cardSettings.getPopperScreen();
    if (StringUtils.isEmpty(targetScreen)) {
      WidgetFactory.showAlert(Studio.stage, "Not target screen selected.", "Select a target screen in the preferences.");
    }
    else {
      ProgressDialog.createProgressDialog(new HighscoreGeneratorProgressModel(client, "Generating Highscore Cards"));
    }
  }

  @FXML
  private void onTableRefresh() {
    List<GameRepresentation> games = client.getGameService().getGamesWithScores();
    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);

    GameRepresentation game = tableCombo.getSelectionModel().getSelectedItem();
    tableCombo.getItems().clear();
    tableCombo.getItems().addAll(gameRepresentations);

    if (game != null) {
      tableCombo.getSelectionModel().select(game);
    }
    onGenerateClick();
  }

  @FXML
  private void onFontTitleSelect() {
    BeanBindingUtil.bindFontSelector(getCardTemplate(), "cardTitle", titleFontLabel);
  }

  @FXML
  private void onFontTableSelect() {
    BeanBindingUtil.bindFontSelector(getCardTemplate(), "cardTable", tableFontLabel);
  }

  @FXML
  private void onFontScoreSelect() {
    BeanBindingUtil.bindFontSelector(getCardTemplate(), "cardScore", scoreFontLabel);
  }

  @FXML
  private void onOpenDefaultPicture() {
    GameRepresentation game = tableCombo.getValue();
    if (game != null) {
      ByteArrayInputStream s = client.getBackglassServiceClient().getDefaultPicture(game);
      MediaUtil.openMedia(s);
    }
  }

  @FXML
  private void onGenerateClick() {
    doSave();
    GameRepresentation value = tableCombo.getValue();
    refreshPreview(Optional.ofNullable(value), true);
  }

  private CardTemplate getCardTemplate() {
    //TODO add combo
    return this.cardTemplates.getDefaultTemplate();
  }

  private void doSave() {
    client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_TEMPLATES, cardTemplates);
  }

  public HighscoreCardsController() {
  }

  private void initFields() {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));

    try {      
      BeanBindingUtil.bindFontLabel(titleFontLabel, getCardTemplate(), "cardTitle");
      BeanBindingUtil.bindFontLabel(tableFontLabel, getCardTemplate(), "cardTable");
      BeanBindingUtil.bindFontLabel(scoreFontLabel, getCardTemplate(), "cardScore");

      BeanBindingUtil.bindColorPicker(fontColorSelector, getCardTemplate(), "cardFontColor");
      BeanBindingUtil.bindHighscoreTablesComboBox(client, tableCombo, getCardTemplate(), "cardSampleTable");

      BeanBindingUtil.bindCheckbox(useDirectB2SCheckbox, getCardTemplate(), "useDirectB2S");
      BeanBindingUtil.bindCheckbox(grayScaleCheckbox, getCardTemplate(), "grayScale");
      BeanBindingUtil.bindCheckbox(transparentBackgroundCheckbox, getCardTemplate(), "transparentBackground");
      BeanBindingUtil.bindCheckbox(renderTableNameCheckbox, getCardTemplate(), "renderTableName");

      imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
      backgroundImageCombo.setItems(imageList);
      backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
      backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

      BeanBindingUtil.bindComboBox(backgroundImageCombo, getCardTemplate(), "background");
      String backgroundName = getCardTemplate().getBackground();
      if (StringUtils.isEmpty(backgroundName)) {
        backgroundImageCombo.setValue(imageList.get(0));
      }

      BeanBindingUtil.bindTextField(titleText, getCardTemplate(), "title", "Highscores");
      BeanBindingUtil.bindSlider(brightenSlider, getCardTemplate(), "alphaWhite");
      BeanBindingUtil.bindSlider(darkenSlider, getCardTemplate(), "alphaBlack");
      BeanBindingUtil.bindSlider(blurSlider, getCardTemplate(), "blur");
      BeanBindingUtil.bindSlider(borderSlider, getCardTemplate(), "borderWidth");
      BeanBindingUtil.bindSlider(alphaPercentageSpinner, getCardTemplate(), "transparentPercentage");
      BeanBindingUtil.bindSpinner(marginTopSpinner, getCardTemplate(), "padding");
      BeanBindingUtil.bindSpinner(wheelImageSpinner, getCardTemplate(), "wheelPadding");
      BeanBindingUtil.bindSpinner(rowSeparatorSpinner, getCardTemplate(), "rowMargin");

      transparentBackgroundCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          updateTransparencySettings(newValue);
        }
      });
      updateTransparencySettings(transparentBackgroundCheckbox.isSelected());

      BeanBindingUtil.bindCheckbox(renderRawHighscore, getCardTemplate(), "rawScore");
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
    } catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
    }

    rawHighscoreHelp.setCursor(javafx.scene.Cursor.HAND);

    Tooltip tooltip = new Tooltip();
    tooltip.setGraphic(rawHighscoreHelp);
    Tooltip.install(rawHighscoreHelp, new Tooltip("The font size of the highscore text will be adapted according to the number of lines."));

    GameRepresentation value = tableCombo.getValue();
    refreshRawPreview(Optional.ofNullable(value));
    refreshPreview(Optional.ofNullable(value), false);

    accordion.setExpandedPane(backgroundSettingsPane);
  }

  private void updateTransparencySettings(Boolean newValue) {
    Platform.runLater(() -> {
      grayScaleCheckbox.setDisable(newValue);
      useDirectB2SCheckbox.setDisable(newValue);
      blurSlider.setDisable(newValue);
      brightenSlider.setDisable(newValue);
      darkenSlider.setDisable(newValue);
      backgroundImageCombo.setDisable(newValue);
      alphaPercentageSpinner.setDisable(!newValue);

      if (newValue) {
        Image backgroundImage = new Image(Studio.class.getResourceAsStream("transparent.png"));
        BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
        imageCenter.setBackground(new Background(myBI));
      }
      else {
        imageCenter.setBackground(new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null)));
      }
    });
  }

  private void refreshRawPreview(Optional<GameRepresentation> game) {
    try {
      resolutionLabel.setText("");
      openDefaultPictureBtn.setVisible(false);
      rawDirectB2SImage.setImage(null);

      if (game.isPresent()) {
        openDefaultPictureBtn.setTooltip(new Tooltip("Open directb2s image"));
        InputStream input = client.getBackglassServiceClient().getDefaultPicture(game.get());
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
            client.getHighscoreCardsService().generateHighscoreCardSample(game.get());
          }

          InputStream input = client.getHighscoreCardsService().getHighscoreCard(game.get());
          Image image = new Image(input);
          cardPreview.setImage(image);
          cardPreview.setVisible(true);

          Platform.runLater(() -> {
            imageMetaDataLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
            previewStack.getChildren().remove(waitOverlay);
            updateTransparencySettings(this.transparentBackgroundCheckbox.isSelected());
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
  public void onViewActivated() {
    NavigationController.setBreadCrumb(Arrays.asList("Highscore Cards"));
    onTableRefresh();
  }
}
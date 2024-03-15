package de.mephisto.vpin.ui.cards.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.cards.CardsDialogs;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TemplateManagerDialogController implements Initializable, DialogController, BindingChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(TemplateManagerDialogController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

  @FXML
  private ComboBox<CardTemplate> templateCombo;

  @FXML
  private StackPane previewStack;

  @FXML
  private Button renameBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private Label titleFontLabel;

  @FXML
  private Label scoreFontLabel;

  @FXML
  private Label tableFontLabel;

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
  private FontIcon rawHighscoreHelp;

  @FXML
  private Button falbackUploadBtn;

  @FXML
  private TitledPane backgroundSettingsPane;

  @FXML
  private Accordion accordion;

  @FXML
  private CheckBox renderTitleCheckbox;

  @FXML
  private CheckBox renderTableNameCheckbox;

  @FXML
  private CheckBox renderWheelIconCheckbox;

  @FXML
  private Pane previewPanel;

  @FXML
  private ImageView cardPreview;

  private BeanBinder templateBeanBinder;
  private ObservableList<String> imageList;

  private Parent waitOverlay;
  private HighscoreCardsController highscoreCardsController;


  @FXML
  private void onCreate(ActionEvent e) {

  }
  @FXML
  private void onRename(ActionEvent e) {

  }

  @FXML
  private void onDuplicate(ActionEvent e) {

  }

  @FXML
  private void onDelete(ActionEvent e) {

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
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onFontTitleSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "title", titleFontLabel);
  }

  @FXML
  private void onFontTableSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "table", tableFontLabel);
  }

  @FXML
  private void onFontScoreSelect() {
    templateBeanBinder.bindFontSelector(getCardTemplate(), "score", scoreFontLabel);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      this.renameBtn.setDisable(true);
      this.duplicateBtn.setDisable(true);
      this.deleteBtn.setDisable(true);

      List<CardTemplate> items = new ArrayList<>(client.getHighscoreCardTemplatesClient().getTemplates());
      if (items.isEmpty()) {
        items.add(new CardTemplate());
      }
      templateCombo.setItems(FXCollections.observableList(items));
      templateCombo.getSelectionModel().select(0);

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Card...");


      cardPreview.setPreserveRatio(true);
      previewPanel.widthProperty().addListener((obs, oldVal, newVal) -> {
        debouncer.debounce("refresh", () -> {
          Platform.runLater(() -> {
            cardPreview.setFitWidth(newVal.intValue() / 2);
            refreshPreview(Optional.ofNullable(highscoreCardsController.getSelectedTable()), false);
          });
        }, 300);
      });
    } catch (Exception e) {
      LOG.error("Failed to initialize template editor: " + e.getMessage(), e);
    }
  }

  public CardTemplate getCardTemplate() {
    return this.templateCombo.getValue();
  }


  private void initFields() {
    try {
      templateBeanBinder = new BeanBinder(this);

      templateBeanBinder.bindFontLabel(titleFontLabel, getCardTemplate(), "title");
      templateBeanBinder.bindFontLabel(tableFontLabel, getCardTemplate(), "table");
      templateBeanBinder.bindFontLabel(scoreFontLabel, getCardTemplate(), "score");

      templateBeanBinder.bindColorPicker(fontColorSelector, getCardTemplate(), "fontColor");

      templateBeanBinder.bindCheckbox(useDirectB2SCheckbox, getCardTemplate(), "useDirectB2S");
      useDirectB2SCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          backgroundImageCombo.setDisable(newValue);
          falbackUploadBtn.setDisable(newValue);
        }
      });
      backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
      falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

      templateBeanBinder.bindCheckbox(grayScaleCheckbox, getCardTemplate(), "grayScale");
      templateBeanBinder.bindCheckbox(transparentBackgroundCheckbox, getCardTemplate(), "transparentBackground");
      templateBeanBinder.bindCheckbox(renderTableNameCheckbox, getCardTemplate(), "renderTableName");
      templateBeanBinder.bindCheckbox(renderWheelIconCheckbox, getCardTemplate(), "renderWheelIcon");
      templateBeanBinder.bindCheckbox(renderTitleCheckbox, getCardTemplate(), "renderTitle");

      imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
      backgroundImageCombo.setItems(imageList);
      backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
      backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

      templateBeanBinder.bindComboBox(backgroundImageCombo, getCardTemplate(), "background");
      String backgroundName = getCardTemplate().getBackground();
      if (StringUtils.isEmpty(backgroundName)) {
        backgroundImageCombo.setValue(imageList.get(0));
      }

      templateBeanBinder.bindTextField(titleText, getCardTemplate(), "title", "Highscores");
      templateBeanBinder.bindSlider(brightenSlider, getCardTemplate(), "alphaWhite");
      templateBeanBinder.bindSlider(darkenSlider, getCardTemplate(), "alphaBlack");
      templateBeanBinder.bindSlider(blurSlider, getCardTemplate(), "blur");
      templateBeanBinder.bindSlider(borderSlider, getCardTemplate(), "borderWidth");
      templateBeanBinder.bindSlider(alphaPercentageSpinner, getCardTemplate(), "transparentPercentage");
      templateBeanBinder.bindSpinner(marginTopSpinner, getCardTemplate(), "padding");
      templateBeanBinder.bindSpinner(wheelImageSpinner, getCardTemplate(), "wheelPadding");
      templateBeanBinder.bindSpinner(rowSeparatorSpinner, getCardTemplate(), "rowMargin");

      transparentBackgroundCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          updateTransparencySettings(newValue);
        }
      });
      updateTransparencySettings(transparentBackgroundCheckbox.isSelected());

      templateBeanBinder.bindCheckbox(renderRawHighscore, getCardTemplate(), "rawScore");
      renderRawHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
        wheelImageSpinner.setDisable(t1);
        rowSeparatorSpinner.setDisable(t1);
      });

      wheelImageSpinner.setDisable(renderRawHighscore.isSelected());
      rowSeparatorSpinner.setDisable(renderRawHighscore.isSelected());
    } catch (Exception e) {
      LOG.error("Error initializing highscore editor fields:" + e.getMessage(), e);
    }

    rawHighscoreHelp.setCursor(javafx.scene.Cursor.HAND);

    Tooltip tooltip = new Tooltip();
    tooltip.setGraphic(rawHighscoreHelp);
    Tooltip.install(rawHighscoreHelp, new Tooltip("The font size of the highscore text will be adapted according to the number of lines."));

    GameRepresentation value = highscoreCardsController.getSelectedTable();
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
      backgroundImageCombo.setDisable(newValue || getCardTemplate().isUseDirectB2S());
      alphaPercentageSpinner.setDisable(!newValue);

      if (newValue) {
        Image backgroundImage = new Image(Studio.class.getResourceAsStream("transparent.png"));
        BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
        previewPanel.setBackground(new Background(myBI));
      }
      else {
        previewPanel.setBackground(new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null)));
      }
    });
  }

  private void refreshPreview(Optional<GameRepresentation> game, boolean regenerate) {
    if (!game.isPresent()) {
      return;
    }

    int offset = 36;
    Platform.runLater(() -> {
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
            previewStack.getChildren().remove(waitOverlay);
            updateTransparencySettings(this.transparentBackgroundCheckbox.isSelected());
          });

        }).start();
        cardPreview.setFitHeight(previewPanel.getHeight() - offset);
        cardPreview.setFitWidth(previewPanel.getWidth() - offset);

      } catch (Exception e) {
        LOG.error("Failed to refresh card preview: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void onDialogCancel() {
  }

  public void setHighscoreCardsController(HighscoreCardsController highscoreCardsController) {
    this.highscoreCardsController = highscoreCardsController;
    initFields();
  }

  @Override
  public void beanPropertyChanged(Object bean, String key, Object value) {
    if (bean instanceof CardTemplate) {
//      onGenerateClick();
    }
//    else if (bean instanceof CardSettings) {
//      client.getPreferenceService().setJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, this.cardSettings);
////      onGenerateClick();
//    }
  }
}

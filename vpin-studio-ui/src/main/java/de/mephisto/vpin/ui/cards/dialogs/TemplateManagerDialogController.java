package de.mephisto.vpin.ui.cards.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import de.mephisto.vpin.ui.util.binding.BindingChangedListener;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String s = WidgetFactory.showInputDialog(stage, "New Template", "Enter Template Name", "Enter a meaningful name that identifies the card design.", null, null);
    if (!StringUtils.isEmpty(s)) {
      ObservableList<CardTemplate> items = this.templateCombo.getItems();
      Optional<CardTemplate> first = items.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst();
      if (first.isPresent()) {
        CardTemplate template = first.get();
        template.setName(s);
        template.setId(null);
        try {
          CardTemplate newTemplate = client.getHighscoreCardTemplatesClient().save(template);

          Platform.runLater(() -> {
            List<CardTemplate> templates = client.getHighscoreCardTemplatesClient().getTemplates();
            this.templateCombo.setItems(FXCollections.observableList(templates));
            this.templateCombo.setValue(newTemplate);
          });
        } catch (Exception ex) {
          LOG.error("Failed to create new template: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Creating Template Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
        }
      }
    }
  }

  @FXML
  private void onRename(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    CardTemplate cardTemplate = getCardTemplate();
    String s = WidgetFactory.showInputDialog(stage, "Rename Template", "Enter Template Name", "Enter the new template name.", null, cardTemplate.getName());
    if (!StringUtils.isEmpty(s) && !cardTemplate.getName().equals(s)) {
      cardTemplate.setName(s);

      try {
        CardTemplate card = client.getHighscoreCardTemplatesClient().save(cardTemplate);
        Platform.runLater(() -> {
          List<CardTemplate> templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
          this.templateCombo.setValue(card);
        });
      } catch (Exception ex) {
        LOG.error("Failed to rename template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Renaming Template Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
    }
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String s = WidgetFactory.showInputDialog(stage, "Duplicate Template \"" + getCardTemplate().getName() + "\"", "Enter Template Name", "Enter a meaningful name that identifies the card design.", null, null);
    if (!StringUtils.isEmpty(s)) {
      ObservableList<CardTemplate> items = this.templateCombo.getItems();
      CardTemplate template = getCardTemplate();
      template.setName(s);
      template.setId(null);
      try {
        CardTemplate card = client.getHighscoreCardTemplatesClient().save(template);

        Platform.runLater(() -> {
          List<CardTemplate> templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
          this.templateCombo.setValue(card);
        });
      } catch (Exception ex) {
        LOG.error("Failed to create new template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Template Duplication Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    CardTemplate cardTemplate = getCardTemplate();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Template", "Delete Template \"" + cardTemplate.getName() + "\"?", null, "Delete");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getHighscoreCardTemplatesClient().deleteTemplate(cardTemplate.getId());
        Platform.runLater(() -> {
          List<CardTemplate> templates = client.getHighscoreCardTemplatesClient().getTemplates();
          this.templateCombo.setItems(FXCollections.observableList(templates));
          this.templateCombo.setValue(templates.stream().filter(t -> t.getName().equals(CardTemplate.DEFAULT)).findFirst().get());
        });
      } catch (Exception ex) {
        LOG.error("Failed to delete template: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Template Deletion Failed", "Please check the log file for details.", "Error: " + ex.getMessage());
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
      this.deleteBtn.setDisable(true);
      this.renameBtn.setDisable(true);

      List<CardTemplate> items = new ArrayList<>(client.getHighscoreCardTemplatesClient().getTemplates());
      templateCombo.setItems(FXCollections.observableList(items));

      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      waitOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Generating Card...");

      rawHighscoreHelp.setCursor(javafx.scene.Cursor.HAND);

      Tooltip tooltip = new Tooltip();
      tooltip.setGraphic(rawHighscoreHelp);
      Tooltip.install(rawHighscoreHelp, new Tooltip("The font size of the highscore text will be adapted according to the number of lines."));

      accordion.setExpandedPane(backgroundSettingsPane);

      cardPreview.setPreserveRatio(true);
    } catch (Exception e) {
      LOG.error("Failed to initialize template editor: " + e.getMessage(), e);
    }
  }

  public CardTemplate getCardTemplate() {
    return this.templateCombo.getValue();
  }

  private void setTemplate(CardTemplate cardTemplate) {
    deleteBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));
    renameBtn.setDisable(cardTemplate.getName().equals(CardTemplate.DEFAULT));

    templateBeanBinder.setBean(cardTemplate);
    templateBeanBinder.setPaused(true);

    titleFontLabel.setText(cardTemplate.getTitleFontName());
    tableFontLabel.setText(cardTemplate.getTableFontName());
    scoreFontLabel.setText(cardTemplate.getScoreFontName());

    templateBeanBinder.setColorPickerValue(fontColorSelector, getCardTemplate(), "fontColor");

    useDirectB2SCheckbox.setSelected(cardTemplate.isUseDirectB2S());
    backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
    falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

    grayScaleCheckbox.setSelected(cardTemplate.isGrayScale());
    transparentBackgroundCheckbox.setSelected(cardTemplate.isTransparentBackground());
    renderTableNameCheckbox.setSelected(cardTemplate.isRenderTableName());
    renderWheelIconCheckbox.setSelected(cardTemplate.isRenderWheelIcon());
    renderTitleCheckbox.setSelected(cardTemplate.isRenderTitle());

    titleText.setText(cardTemplate.getTitle());
    brightenSlider.setValue(cardTemplate.getAlphaWhite());
    darkenSlider.setValue(cardTemplate.getAlphaBlack());
    blurSlider.setValue(cardTemplate.getBlur());
    borderSlider.setValue(cardTemplate.getBorderWidth());
    alphaPercentageSpinner.setValue(cardTemplate.getTransparentPercentage());
    marginTopSpinner.getValueFactory().setValue(cardTemplate.getPadding());
    wheelImageSpinner.getValueFactory().setValue(cardTemplate.getWheelPadding());
    rowSeparatorSpinner.getValueFactory().setValue(cardTemplate.getRowMargin());

    updateTransparencySettings(transparentBackgroundCheckbox.isSelected());

    renderRawHighscore.setSelected(cardTemplate.isRawScore());
    wheelImageSpinner.setDisable(renderRawHighscore.isSelected());
    rowSeparatorSpinner.setDisable(renderRawHighscore.isSelected());

    templateBeanBinder.setPaused(false);

    refreshPreview(Optional.ofNullable(highscoreCardsController.getSelectedTable()), true);
  }


  private void initBindings() {
    try {
      templateBeanBinder = new BeanBinder(this);
      templateBeanBinder.setBean(this.getCardTemplate());

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

  @FXML
  private void onGenerateClick() {
    Platform.runLater(() -> {
      GameRepresentation value = highscoreCardsController.getSelectedTable();
      try {
        client.getHighscoreCardTemplatesClient().save((CardTemplate) this.templateBeanBinder.getBean());
        refreshPreview(Optional.ofNullable(value), true);
      } catch (Exception e) {
        LOG.error("Failed to save template: " + e.getMessage());
        WidgetFactory.showAlert(stage, "Error", "Failed to save template: " + e.getMessage());
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
            InputStream input = client.getHighscoreCardsService().getHighscoreCardPreview(game.get(), templateCombo.getValue());
            Image image = new Image(input);
            cardPreview.setImage(image);
            cardPreview.setVisible(true);
          }

          Platform.runLater(() -> {
            previewStack.getChildren().remove(waitOverlay);
          });

        }).start();
//        cardPreview.setFitHeight(previewPanel.getHeight() - offset);
//        cardPreview.setFitWidth(previewPanel.getWidth() - offset);

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
    templateCombo.setValue(highscoreCardsController.getSelectedTemplate());
    initBindings();

    templateCombo.valueProperty().addListener(new ChangeListener<CardTemplate>() {
      @Override
      public void changed(ObservableValue<? extends CardTemplate> observable, CardTemplate oldValue, CardTemplate newValue) {
        if (newValue != null) {
          setTemplate(newValue);
        }
      }
    });
    setTemplate(templateCombo.getValue());
  }

  @Override
  public void beanPropertyChanged(Object bean, String key, Object value) {
    if (bean instanceof CardTemplate) {
      onGenerateClick();
    }
  }

  @Override
  public void onResized(int x, int y, int width, int height) {
    cardPreview.setFitWidth(width- 500);
    cardPreview.setFitHeight(height - 200);
    refreshPreview(Optional.ofNullable(highscoreCardsController.getSelectedTable()), false);
  }
}

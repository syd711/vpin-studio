package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorBackgroundController extends LayerEditorBaseController {

  @FXML
  private RadioButton transparentBackgroundRadio;
  @FXML
  private RadioButton defaultBackgroundRadio;
  @FXML
  private RadioButton fallbackBackgroundRadio;

  @FXML
  private VBox transparentBackgroundPane;
  @FXML
  private VBox defaultBackgroundPane;
  @FXML
  private VBox fallbackBackgroundPane;

  @FXML
  private Slider alphaPercentageSpinner;

  @FXML
  private ComboBox<String> backgroundImageCombo;

  @FXML
  private Button falbackUploadBtn;

  @FXML
  private CheckBox grayScaleCheckbox;
  @FXML
  private Slider brightenSlider;
  @FXML
  private Slider darkenSlider;
  @FXML
  private Slider blurSlider;

  private ObservableList<String> imageList;

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
  public void setTemplate(CardTemplate cardTemplate, CardResolution res) {
    // background
    if (cardTemplate.isTransparentBackground()) {
      transparentBackgroundRadio.setSelected(true);
    }
    else if (cardTemplate.isUseDefaultBackground()) {
      defaultBackgroundRadio.setSelected(true);
    }
    else {
      fallbackBackgroundRadio.setSelected(true);
    }

    grayScaleCheckbox.setSelected(cardTemplate.isGrayScale());
    brightenSlider.setValue(cardTemplate.getAlphaWhite());
    darkenSlider.setValue(cardTemplate.getAlphaBlack());
    blurSlider.setValue(cardTemplate.getBlur());

    // transparent background
    alphaPercentageSpinner.setValue(cardTemplate.getTransparentPercentage());

    // fallback background
    String backgroundName = cardTemplate.getBackground();
    if (StringUtils.isEmpty(backgroundName)) {
      backgroundImageCombo.setValue(imageList.get(0));
    }

  }

  public void initBindings(CardTemplateBinder templateBeanBinder) {

    ToggleGroup radioGroup = new ToggleGroup();
    transparentBackgroundRadio.setToggleGroup(radioGroup);
    defaultBackgroundRadio.setToggleGroup(radioGroup);
    fallbackBackgroundRadio.setToggleGroup(radioGroup);

    // TODO OLE : make pane invisible for time being, to be implemented
    defaultBackgroundPane.setVisible(false);
    defaultBackgroundPane.setManaged(false);


    radioGroup.selectedToggleProperty().addListener((obs, o, n) -> {
      transparentBackgroundPane.setDisable(n != transparentBackgroundRadio);
      defaultBackgroundPane.setDisable(n != defaultBackgroundRadio);
      fallbackBackgroundPane.setDisable(n != fallbackBackgroundRadio);

      templateBeanBinder.setProperty("transparentBackground", n == transparentBackgroundRadio);
      templateBeanBinder.setProperty("useDefaultBackground", n == defaultBackgroundRadio);
    });

    // transparent background
    templateBeanBinder.bindSlider(alphaPercentageSpinner, "transparentPercentage");

    // default background
    imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
    backgroundImageCombo.setItems(imageList);
    backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
    backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

    templateBeanBinder.bindComboBox(backgroundImageCombo, "background");

    // Other properties
    templateBeanBinder.bindCheckbox(grayScaleCheckbox, "grayScale");
    templateBeanBinder.bindSlider(brightenSlider, "alphaWhite");
    templateBeanBinder.bindSlider(darkenSlider, "alphaBlack");
    templateBeanBinder.bindSlider(blurSlider, "blur");

    transparentBackgroundRadio.selectedProperty().addListener((obs, old, enabled) -> {
      grayScaleCheckbox.setDisable(enabled);
      blurSlider.setDisable(enabled);
      brightenSlider.setDisable(enabled);
      darkenSlider.setDisable(enabled);
    });

    transparentBackgroundRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue && alphaPercentageSpinner.getValue() <= 0) {
          alphaPercentageSpinner.setValue(50);
        }
      }
    });
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
  }
}

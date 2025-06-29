package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.*;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorBackgroundController extends LayerEditorBaseController {

  @FXML
  private CheckBox useDirectB2SCheckbox;
  @FXML
  private CheckBox grayScaleCheckbox;
  @FXML
  private CheckBox transparentBackgroundCheckbox;
  @FXML
  private CheckBox overlayModeCheckbox;
  @FXML
  private ComboBox<String> backgroundImageCombo;
  @FXML
  private Slider brightenSlider;
  @FXML
  private Slider darkenSlider;
  @FXML
  private Slider blurSlider;
  @FXML
  private Slider alphaPercentageSpinner;
  @FXML
  private Button falbackUploadBtn;


  @FXML
  private ComboBox<String> screensComboBox;

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
  public void setTemplate(CardTemplate cardTemplate) {
    // background
    useDirectB2SCheckbox.setSelected(cardTemplate.isUseDirectB2S());
    falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());
    grayScaleCheckbox.setSelected(cardTemplate.isGrayScale());
    transparentBackgroundCheckbox.setSelected(cardTemplate.isTransparentBackground());
    overlayModeCheckbox.setSelected(cardTemplate.isOverlayMode());
    brightenSlider.setValue(cardTemplate.getAlphaWhite());
    darkenSlider.setValue(cardTemplate.getAlphaBlack());
    blurSlider.setValue(cardTemplate.getBlur());
    alphaPercentageSpinner.setValue(cardTemplate.getTransparentPercentage());

    screensComboBox.setDisable(!cardTemplate.isOverlayMode());

    String backgroundName = cardTemplate.getBackground();
    if (StringUtils.isEmpty(backgroundName)) {
      backgroundImageCombo.setValue(imageList.get(0));
    }

    boolean enabled = cardTemplate.isTransparentBackground();
    grayScaleCheckbox.setDisable(enabled);
    useDirectB2SCheckbox.setDisable(enabled);
    blurSlider.setDisable(enabled);
    brightenSlider.setDisable(enabled);
    darkenSlider.setDisable(enabled);
    backgroundImageCombo.setDisable(enabled || cardTemplate.isUseDirectB2S());
    alphaPercentageSpinner.setDisable(!enabled);
  }

  public void initBindings(BeanBinder templateBeanBinder) {

    templateBeanBinder.bindCheckbox(useDirectB2SCheckbox, "useDirectB2S");
    useDirectB2SCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        backgroundImageCombo.setDisable(newValue);
        falbackUploadBtn.setDisable(newValue);
      }
    });
    backgroundImageCombo.setDisable(useDirectB2SCheckbox.isSelected());
    falbackUploadBtn.setDisable(useDirectB2SCheckbox.isSelected());

    List<VPinScreen> VPinScreens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    VPinScreens.remove(VPinScreen.Audio);
    VPinScreens.remove(VPinScreen.AudioLaunch);
    VPinScreens.remove(VPinScreen.GameInfo);
    VPinScreens.remove(VPinScreen.GameHelp);
    VPinScreens.remove(VPinScreen.DMD);
    VPinScreens.remove(VPinScreen.Wheel);
    VPinScreens.remove(VPinScreen.Other2);
    VPinScreens.remove(VPinScreen.PlayField);
    VPinScreens.remove(VPinScreen.Loading);
    screensComboBox.setItems(FXCollections.observableList(VPinScreens.stream().map(p -> p.name()).collect(Collectors.toList())));

    templateBeanBinder.bindCheckbox(grayScaleCheckbox, "grayScale");
    templateBeanBinder.bindCheckbox(transparentBackgroundCheckbox, "transparentBackground");

    templateBeanBinder.bindCheckbox(overlayModeCheckbox, "overlayMode");

    overlayModeCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        screensComboBox.setDisable(!newValue);
      }
    });

    imageList = FXCollections.observableList(new ArrayList<>(client.getHighscoreCardsService().getHighscoreBackgroundImages()));
    backgroundImageCombo.setItems(imageList);
    backgroundImageCombo.setCellFactory(c -> new WidgetFactory.HighscoreBackgroundImageListCell(client));
    backgroundImageCombo.setButtonCell(new WidgetFactory.HighscoreBackgroundImageListCell(client));

    templateBeanBinder.bindComboBox(backgroundImageCombo, "background");

    templateBeanBinder.bindComboBox(screensComboBox, "overlayScreen");

    templateBeanBinder.bindSlider(brightenSlider, "alphaWhite");
    templateBeanBinder.bindSlider(darkenSlider, "alphaBlack");
    templateBeanBinder.bindSlider(blurSlider, "blur");
    templateBeanBinder.bindSlider(alphaPercentageSpinner, "transparentPercentage");

    transparentBackgroundCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        boolean overlayMode = overlayModeCheckbox.isSelected();
        boolean transparentBackground = transparentBackgroundCheckbox.isSelected();
        overlayModeCheckbox.setDisable(!transparentBackground);
        overlayModeCheckbox.setSelected(overlayMode);
        screensComboBox.setDisable(!overlayMode || !transparentBackground);

        if (newValue && alphaPercentageSpinner.getValue() <= 0) {
          alphaPercentageSpinner.setValue(50);
        }
      }
    });
  }
}

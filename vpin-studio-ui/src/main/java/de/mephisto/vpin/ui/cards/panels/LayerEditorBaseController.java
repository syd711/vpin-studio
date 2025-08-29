package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;

import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public abstract class LayerEditorBaseController {
  final protected static Logger LOG = LoggerFactory.getLogger(LayerEditorBaseController.class);

  @FXML
  protected LayerSubEditorPositionController positionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  protected TitledPane settingsPane;
  @FXML
  protected Button eyeBtn;
  @FXML
  protected ToggleButton lockBtn;

  /**
   * The top accordion
   */
  protected Accordion accordion;

  /**
   * Link to the parent controller
   */
  protected TemplateEditorController templateEditorController;

  private int settingsPanelIndex = -1;
  private String lockProperty;

  public void initialize(TemplateEditorController templateEditorController, Accordion accordion, String lockProperty) {
    LOG.info("initBindings for {}", getClass().getSimpleName());
    this.lockProperty = lockProperty;
    this.templateEditorController = templateEditorController;
    this.accordion = accordion;
    settingsPane.managedProperty().bindBidirectional(settingsPane.visibleProperty());
    this.lockBtn.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        toggleLockButton(newValue);
      }
    });

    initBindings(templateEditorController.getBeanBinder());
  }

  public abstract void initBindings(CardTemplateBinder templateBeanBinder);

  public void setTemplate(CardTemplate template, CardResolution res, Optional<GameRepresentation> game) {
    if (!StringUtils.isEmpty(lockProperty) && template != null) {

      CardTemplateBinder beanBinder = templateEditorController.getBeanBinder();
      boolean locked = beanBinder.getProperty(lockProperty, null);
      lockBtn.setSelected(locked);

      if (template.isTemplate()) {
        setVisible(true);
      }
      else {
        lockBtn.setSelected(locked);
        CardTemplate parent = client.getHighscoreCardTemplatesClient().getTemplateById(beanBinder.getBean().getParentId());
        if (lockProperty != null && parent != null) {
          BeanBinder binder = new BeanBinder();
          binder.setBean(parent);
          locked = (boolean) binder.getProperty(lockProperty, null);

          setVisible(!locked);
        }
      }
    }
  }

  public abstract void bindDragBox(PositionResizer dragBox);

  public abstract void unbindDragBox(PositionResizer dragBox);

  public void setVisible(boolean b) {
    if (lockProperty != null) {
      if (b) {
        if (!templateEditorController.getAccordion().getPanes().contains(settingsPane)) {
          templateEditorController.getAccordion().getPanes().add(settingsPanelIndex, settingsPane);
          settingsPane.setVisible(true);
        }
      }
      else {
        settingsPanelIndex = templateEditorController.getAccordion().getPanes().indexOf(settingsPane);
        templateEditorController.getAccordion().getPanes().remove(settingsPane);
      }
    }
  }

  @FXML
  private void onLockToggle(ActionEvent ae) {
    toggleLockButton(this.lockBtn.isSelected());
  }

  protected void toggleLockButton(boolean lock) {
    if (lock) {
      FontIcon icon = WidgetFactory.createIcon("mdi2l-lock", 12, null);
      lockBtn.setGraphic(icon);
    }
    else {
      FontIcon icon = WidgetFactory.createIcon("mdi2l-lock-open-variant-outline", 12, null);
      lockBtn.setGraphic(icon);
    }

    templateEditorController.getBeanBinder().setProperty(lockProperty, lock);
  }

  /**
   * Called when the associated element is selected in the preview
   */
  public void expandSettingsPane() {
    TitledPane expandedPane = accordion.getExpandedPane();
    if (expandedPane != null) {
      expandedPane.setAnimated(false);
    }
    settingsPane.setAnimated(false);
    accordion.setExpandedPane(settingsPane);
    settingsPane.setAnimated(true);

    if (expandedPane != null) {
      expandedPane.setAnimated(true);
    }
  }

  public TitledPane getSettingsPane() {
    return settingsPane;
  }

  //---------------------------------------- Common Utilities ---

  public void setIconVisibility(boolean visible) {
    if (eyeBtn != null) {
      FontIcon icon = (FontIcon) eyeBtn.getGraphic();
      icon.setIconLiteral(visible ? "mdi2e-eye-outline" : "mdi2e-eye-off-outline");
    }
    settingsPane.getContent().setDisable(!visible);
  }

  public void bindVisibilityIcon(CardTemplateBinder templateBeanBinder, String property) {
    if (eyeBtn != null) {
      eyeBtn.setOnMouseReleased(e -> {
        try {
          boolean visible = !templateBeanBinder.getProperty(property, true);
          templateBeanBinder.setProperty(property, visible);
          setIconVisibility(visible);
        }
        catch (Exception ex) {
          LOG.error("Cannot read property {} from template", property, ex);
        }
        e.consume();
      });
    }
  }
}

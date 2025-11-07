package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LayerEditorBaseController {
  final protected static Logger LOG = LoggerFactory.getLogger(LayerEditorBaseController.class);

  @FXML
  protected LayerSubEditorPositionController positionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private TitledPane settingsPane;
  @FXML
  private Button eyeBtn;
  @FXML
  private ToggleButton lockBtn;

  /**
   * The top accordion
   */
  private Accordion accordion;

  /**
   * Link to the parent controller
   */
  protected TemplateEditorController templateEditorController;

  public void initialize(TemplateEditorController templateEditorController, Accordion accordion) {
    LOG.info("initBindings for {}", getClass().getSimpleName());
    this.templateEditorController = templateEditorController;
    this.accordion = accordion;

    int position = accordion.getPanes().indexOf(settingsPane);
    settingsPane.setUserData(position);
    settingsPane.managedProperty().bindBidirectional(settingsPane.visibleProperty());

    lockBtn.managedProperty().bindBidirectional(lockBtn.visibleProperty());

    initBindings(templateEditorController.getBeanBinder());
  }

  public abstract void initBindings(CardTemplateBinder templateBeanBinder);

  public abstract void bindDragBox(PositionResizer dragBox);

  public abstract void unbindDragBox(PositionResizer dragBox);


  @FXML
  private void onLockToggle(ActionEvent ae) {
    toggleLockButton(this.lockBtn.isSelected());
  }


  /**
   * Called when the associated element is selected in the preview
   */
  public boolean expandSettingsPane() {
    TitledPane expandedPane = accordion.getExpandedPane();
    if (expandedPane == settingsPane) {
      return false;
    }

    if (expandedPane != null) {
      expandedPane.setAnimated(false);
    }
    settingsPane.setAnimated(false);
    accordion.setExpandedPane(settingsPane);
    settingsPane.setAnimated(true);

    if (expandedPane != null) {
      expandedPane.setAnimated(true);
    }
    return true;
  }

  public TitledPane getSettingsPane() {
    return settingsPane;
  }

  //---------------------------------------- Layer visibilities ---

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
          if (visible) {
            templateEditorController.selectLayer(this);
          }
          else {
            templateEditorController.deselectLayer(this);
          }
        }
        catch (Exception ex) {
          LOG.error("Cannot read property {} from template", property, ex);
        }
        e.consume();
      });
    }
  }

  //---------------------------------------- Lock management ---

  public void setIconLock(boolean locked, boolean isTemplate) {
    if (isTemplate) {
      lockBtn.setVisible(true);
      lockBtn.setSelected(locked);
      setSettingsPaneVisible(true);
    }
    else {
      lockBtn.setVisible(false);
      lockBtn.setSelected(locked);
      setSettingsPaneVisible(!locked);
    }
  }

  protected void setSettingsPaneVisible(boolean b) {
    List<TitledPane> panes = accordion.getPanes();
    if (b) {
      if (!panes.contains(settingsPane)) {
        panes.add(settingsPane);
        panes.sort((o1, o2) -> {
            return String.valueOf(o1.getUserData()).compareTo(String.valueOf(o2.getUserData()));
          });
      }
    }
    else {
      panes.remove(settingsPane);
    }
  }

  protected void toggleLockButton(boolean lock) {
    if (lock) {
      FontIcon icon = WidgetFactory.createIcon("mdi2l-lock", 13, null);
      lockBtn.setGraphic(icon);
    }
    else {
      FontIcon icon = WidgetFactory.createIcon("mdi2l-lock-open-variant-outline", 13, null);
      lockBtn.setGraphic(icon);
    }
  }

  public void bindLockIcon(CardTemplateBinder templateBeanBinder, String property) {
    if (lockBtn != null) {
      lockBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
        toggleLockButton(newValue);
        templateBeanBinder.setProperty(property, newValue);
      });
    }
  }

  public boolean isNotLocked() {
    List<TitledPane> panes = accordion.getPanes();
    return panes.contains(settingsPane);
  }
}

package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class LayerEditorBaseController {
  final protected static Logger LOG = LoggerFactory.getLogger(LayerEditorBaseController.class);

  @FXML
  protected LayerSubEditorPositionController positionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  protected TitledPane settingsPane;
  @FXML
  protected Button eyeBtn;
  @FXML
  protected Button lockBtn;

  /**
   * The top accordion
   */
  protected Accordion accordion;

  /**
   * Link to the parent controller
   */
  protected TemplateEditorController templateEditorController;

  public void initialize(TemplateEditorController templateEditorController, Accordion accordion) {
    LOG.info("initBindings for {}", getClass().getSimpleName());
    this.templateEditorController = templateEditorController;
    this.accordion = accordion;
    initBindings(templateEditorController.getBeanBinder());
  }

  public abstract void initBindings(CardTemplateBinder templateBeanBinder);

  public abstract void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game);

  public abstract void bindDragBox(PositionResizer dragBox);

  public abstract void unbindDragBox(PositionResizer dragBox);

  @FXML
  private void onLockToggle() {
    FontIcon icon = WidgetFactory.createIcon("mdi2l-lock", 12, null);

    lockBtn.setGraphic(icon);
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

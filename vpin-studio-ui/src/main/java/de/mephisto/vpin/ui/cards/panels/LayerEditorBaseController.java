package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;

import java.util.Optional;

public abstract class LayerEditorBaseController {
  final protected static Logger LOG = LoggerFactory.getLogger(LayerEditorBaseController.class);

  @FXML
  protected LayerSubEditorPositionController positionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  protected TitledPane settingsPane;
  @FXML
  protected Button eyeBtn;

  /** The top accordion */
  protected Accordion accordion;

  /** Link to the parent controller */
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


  /**
   * Called when the associated element is selected in the preview
   */
  public void layerSelected() {

    TitledPane expandedPane = accordion.getExpandedPane();
    if (expandedPane != null) {
      expandedPane.setAnimated(false);
      settingsPane.setAnimated(false);

      accordion.setExpandedPane(settingsPane);
      
      expandedPane.setAnimated(true);
      settingsPane.setAnimated(true);
    }
  }


  //---------------------------------------- Common Utilities ---

  public void setIconVisibility(boolean visible) {
    if (eyeBtn != null) {
      FontIcon icon = (FontIcon) eyeBtn.getGraphic();
      icon.setIconLiteral(visible  ? "mdi2e-eye-outline" : "mdi2e-eye-off-outline");
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

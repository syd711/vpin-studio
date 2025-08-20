package de.mephisto.vpin.ui.cards.panels;

import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.util.PositionResizer;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;

public abstract class LayerEditorBaseController {
  final protected static Logger LOG = LoggerFactory.getLogger(LayerEditorBaseController.class);

  @FXML
  protected LayerSubEditorPositionController positionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  protected TitledPane settingsPane;
  @FXML
  protected Button eyeBtn;

  /** Link to the parent controller */
  protected TemplateEditorController templateEditorController;

  public void initialize(TemplateEditorController templateEditorController) {
    LOG.info("initBindings for {}", getClass().getSimpleName());
    this.templateEditorController = templateEditorController;
    initBindings(templateEditorController.getBeanBinder());
  }

  public abstract void initBindings(CardTemplateBinder templateBeanBinder);

  public abstract void setTemplate(CardTemplate cardTemplate, CardResolution res);

  public abstract void bindDragBox(PositionResizer dragBox);

  public abstract void unbindDragBox(PositionResizer dragBox);


  //---------------------------------------- Common Utilities ---

    public void setIconVisibility(boolean visible) {
    FontIcon icon = (FontIcon) eyeBtn.getGraphic();
    icon.setIconLiteral(visible  ? "mdi2e-eye-outline" : "mdi2e-eye-off-outline");
    settingsPane.getContent().setDisable(!visible);
  }

  public void bindVisibilityIcon(CardTemplateBinder templateBeanBinder, String property) {
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

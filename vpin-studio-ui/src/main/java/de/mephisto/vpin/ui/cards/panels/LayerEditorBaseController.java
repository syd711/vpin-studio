package de.mephisto.vpin.ui.cards.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TitledPane;

public abstract class LayerEditorBaseController {
  final protected static Logger LOG = LoggerFactory.getLogger(LayerEditorBaseController.class);

  @FXML
  protected LayerSubEditorPositionController positionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  protected TitledPane settingsPane;

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

  //---------------------------------------- Common Utilities ---

  protected void bindSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                             ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {
    IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
  }

}

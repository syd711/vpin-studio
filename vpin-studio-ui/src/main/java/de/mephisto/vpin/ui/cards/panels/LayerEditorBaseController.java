package de.mephisto.vpin.ui.cards.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TitledPane;

public abstract class LayerEditorBaseController {
  final static Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);

  @FXML
  protected TitledPane settingsPane;

  protected TemplateEditorController templateEditorController;

  public void initialize(TemplateEditorController templateEditorController) {
    this.templateEditorController = templateEditorController;
    initBindings(templateEditorController.getBeanBinder());
  }

  public abstract void initBindings(BeanBinder templateBeanBinder);

  public abstract void setTemplate(CardTemplate cardTemplate);


  protected void bindSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                             ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {
    IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
  }

}

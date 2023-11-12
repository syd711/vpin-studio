package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentType;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TabFreezyDMDController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabFreezyDMDController.class);

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.freezy;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
  }
}

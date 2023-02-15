package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class RepositorySidebarController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(RepositorySidebarController.class);

  @FXML
  private Accordion repositoryAccordion;

  @FXML
  private VBox repositoryAccordionVBox;

  @Override
  public void onViewActivated() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    repositoryAccordionVBox.managedProperty().bindBidirectional(repositoryAccordion.visibleProperty());
  }

  public void setVisible(boolean b) {
    this.repositoryAccordion.setVisible(b);
  }
}

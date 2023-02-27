package de.mephisto.vpin.poppermenu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuController.class);

  @FXML
  private Node installPanel;

  @FXML
  private Node uninstallPanel;

  @FXML
  private Node baseSelector;

  private boolean installToggle;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void toggleInstall() {
    installToggle = !installToggle;
    if(installToggle) {
      baseSelector.setStyle("-fx-background-color: #33CC00;");
      TransitionUtil.createOutFader(installPanel).play();
      TransitionUtil.createInFader(uninstallPanel).play();
    }
    else {
      baseSelector.setStyle("-fx-background-color: #FF3333;");
      TransitionUtil.createOutFader(uninstallPanel).play();
      TransitionUtil.createInFader(installPanel).play();
    }
  }
}

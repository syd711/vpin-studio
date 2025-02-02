package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.components.AbstractComponentTab;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ScreensController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreensController.class);

  @FXML
  private Pane screenRoot;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    //TODO wip
    SystemSummary systemSummary = client.getSystemService().getSystemSummary();
    List<ScreenInfo> screenInfos = systemSummary.getScreenInfos();

    for (ScreenInfo screenInfo : screenInfos) {
      try {
        FXMLLoader loader = new FXMLLoader(ManagedScreenController.class.getResource("managed-screen.fxml"));
        Parent builtInRoot = loader.load();
        ManagedScreenController controller = loader.getController();
        controller.setData(screenInfo);
        screenRoot.getChildren().add(builtInRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to load managed screen: " + e.getMessage(), e);
      }
    }
  }
}

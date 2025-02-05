package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    List<MonitorInfo> monitorInfos = systemSummary.getScreenInfos();

    for (MonitorInfo monitorInfo : monitorInfos) {
      try {
        FXMLLoader loader = new FXMLLoader(ManagedScreenController.class.getResource("managed-screen.fxml"));
        Parent builtInRoot = loader.load();
        ManagedScreenController controller = loader.getController();
        controller.setData(monitorInfo);
        screenRoot.getChildren().add(builtInRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to load managed screen: " + e.getMessage(), e);
      }
    }
  }
}

package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabOverviewController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabOverviewController.class);

  @FXML
  private Pane componentList;

  @FXML
  private void onVersionRefresh() {
    ComponentChecksProgressModel model = new ComponentChecksProgressModel(true);
    ProgressDialog.createProgressDialog(model);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    JFXFuture.supplyAsync(() -> client.getComponentService().getComponents())
      .thenAcceptLater(components -> {
        for (ComponentRepresentation component : components) {
          try {
            FXMLLoader loader = new FXMLLoader(ComponentShortSummaryController.class.getResource("component-short-summary-panel.fxml"));
            Parent builtInRoot = loader.load();
            ComponentShortSummaryController controller = loader.getController();
            controller.refresh(component);
            componentList.getChildren().add(builtInRoot);
          }
          catch (IOException e) {
            LOG.error("Failed to load tab: " + e.getMessage(), e);
          }
        }
      });
  }
}

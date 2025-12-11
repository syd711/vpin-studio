package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.ui.monitor.panels.ScreenPanelController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.ui.Studio.client;

public class MonitorsView implements IMonitoringView {
  private final static Logger LOG = LoggerFactory.getLogger(MonitorsView.class);
  private final FlowPane flowPane;

  private Map<MonitorInfo, ScreenPanelController> controllers = new HashMap<>();

  public MonitorsView(Stage stage, CabMonitorController recorderController, ScrollPane scrollPane) {
    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);

    SystemSummary systemSummary = client.getSystemService().getSystemSummary();
    List<MonitorInfo> monitorInfos = systemSummary.getMonitorInfos();
    flowPane = new FlowPane();
    for (MonitorInfo monitorInfo : monitorInfos) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreenPanelController.class.getResource("screen-monitor-panel.fxml"));
        Parent panelRoot = loader.load();
        ScreenPanelController screenPanelController = loader.getController();
        controllers.put(monitorInfo, screenPanelController);
        screenPanelController.setZoom(settings.getScaling());
        screenPanelController.setData(stage, recorderController, monitorInfo);
        flowPane.getChildren().add(panelRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load monitoring panel: " + e.getMessage(), e);
      }
    }

    scrollPane.setContent(flowPane);
  }

  @Override
  public void setZoom(double zoom) {
    for (ScreenPanelController controller : controllers.values()) {
      controller.setZoom(zoom);
    }
  }

  @Override
  public void refresh() {
    for (ScreenPanelController controller : controllers.values()) {
      controller.refresh();
    }
  }

  @Override
  public void updateScreens(List<VPinScreen> disabledScreens) {

  }

  @Override
  public void dispose() {
    flowPane.getChildren().removeAll(flowPane.getChildren());
  }
}

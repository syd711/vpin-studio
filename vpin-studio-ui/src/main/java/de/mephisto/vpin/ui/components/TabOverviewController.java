package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabOverviewController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabOverviewController.class);

  @FXML
  private ComboBox<String> presetCombo;

  @FXML
  private Button refreshBtn;

  @FXML
  private Pane componentList;

  @FXML
  private void onVersionRefresh() {
    ComponentChecksProgressModel model = new ComponentChecksProgressModel("Running Component Checks");
    Dialogs.createProgressDialog(model);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    presetCombo.setItems(FXCollections.observableList(Arrays.asList(PreferenceNames.SYSTEM_PRESET_32_BIT, PreferenceNames.SYSTEM_PRESET_64_BIT)));

    String preset = client.getSystemPreset();
    presetCombo.setValue(preset);
    presetCombo.valueProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(PreferenceNames.SYSTEM_PRESET, t1));

    List<ComponentRepresentation> components = client.getComponentService().getComponents();
    for (ComponentRepresentation component : components) {
      try {
        FXMLLoader loader = new FXMLLoader(ComponentShortSummaryController.class.getResource("component-short-summary-panel.fxml"));
        Parent builtInRoot = loader.load();
        ComponentShortSummaryController controller = loader.getController();
        controller.refresh(component);
        componentList.getChildren().add(builtInRoot);
      } catch (IOException e) {
        LOG.error("Failed to load tab: " + e.getMessage(), e);
      }
    }
  }
}

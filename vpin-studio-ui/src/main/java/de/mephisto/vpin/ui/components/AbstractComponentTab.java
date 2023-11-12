package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static de.mephisto.vpin.ui.Studio.client;

abstract public class AbstractComponentTab implements StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(AbstractComponentTab.class);

  @FXML
  public BorderPane componentInstallerPane;

  @FXML
  public VBox componentSummaryPane;

  protected ComponentRepresentation component;
  protected ComponentUpdateController componentUpdateController;
  protected ComponentSummaryController componentSummaryController;

  public static String getSystemPreset() {
    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.SYSTEM_PRESET);
    String preset = preference.getValue();
    if (preset == null) {
      preset = PreferenceNames.SYSTEM_PRESET_64_BIT;
    }
    return preset;
  }

  protected void refresh() {
    component = client.getComponentService().getComponent(getComponentType());
    componentSummaryController.refreshComponent(component);
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    Platform.runLater(() -> {
      if (getComponentType().equals(type)) {
        refresh();
      }
    });
  }

  public void postProcessing(boolean simulate) {

  }

  protected void initialize() {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentUpdateController.class.getResource("component-update-panel.fxml"));
      Parent builtInRoot = loader.load();
      componentUpdateController = loader.getController();
      componentInstallerPane.setCenter(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(ComponentSummaryController.class.getResource("component-summary-panel.fxml"));
      Parent builtInRoot = loader.load();
      componentSummaryController = loader.getController();
      componentSummaryPane.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    component = client.getComponentService().getComponent(getComponentType());
    componentUpdateController.setComponent(this, component);
    componentSummaryController.refreshComponent(component);

    EventManager.getInstance().addListener(this);
  }

  abstract protected ComponentType getComponentType();
}

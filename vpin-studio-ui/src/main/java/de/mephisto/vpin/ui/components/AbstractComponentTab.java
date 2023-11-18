package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.PreferenceChangeListener;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static de.mephisto.vpin.ui.Studio.client;

abstract public class AbstractComponentTab implements StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(AbstractComponentTab.class);

  @FXML
  public BorderPane componentInstallerPane;

  @FXML
  public Button openFolderButton;

  @FXML
  public VBox componentSummaryPane;

  protected ComponentRepresentation component;
  protected ComponentUpdateController componentUpdateController;
  protected ComponentSummaryController componentSummaryController;

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
    openFolderButton.setDisable(!client.getSystemService().isLocal());

    client.getPreferenceService().addListener(this);

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

  protected ComponentCustomValueController addCustomValue(String key, String value) {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentCustomValueController.class.getResource("component-custom-value.fxml"));
      Parent builtInRoot = loader.load();
      ComponentCustomValueController controller = loader.getController();
      componentSummaryPane.getChildren().add(builtInRoot);

      if (!key.endsWith(":")) {
        key += ":";
      }
      controller.refresh(key, value);
      return controller;
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
    return null;
  }

  protected void openFolder(File file) {
    try {
      if (file.exists()) {
        new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Folder Not Found", "The folder\"" + file.getAbsolutePath() + "\" does not exist.");
      }
    } catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  protected void openFile(File file) {
    try {
      if (file.exists()) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
          try {
            desktop.open(file);
          } catch (Exception e) {
            WidgetFactory.showAlert(Studio.stage, "Error", "Failed to execute \"" + file.getAbsolutePath() + "\": " + e.getMessage());
          }
        }
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Folder Not Found", "The folder \"" + file.getAbsolutePath() + "\" does not exist.");
      }
    } catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  protected void editFile(File file) {
    try {
      if (file.exists()) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.EDIT)) {
          try {
            desktop.edit(file);
          } catch (Exception e) {
            WidgetFactory.showAlert(Studio.stage, "Error", "Failed to execute \"" + file.getAbsolutePath() + "\": " + e.getMessage());
          }
        }
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Folder Not Found", "The folder \"" + file.getAbsolutePath() + "\" does not exist.");
      }
    } catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if(key.equals(PreferenceNames.SYSTEM_PRESET)) {
      componentUpdateController.refresh();
    }
  }

  abstract protected ComponentType getComponentType();
}

package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static de.mephisto.vpin.ui.Studio.client;

abstract public class AbstractComponentTab implements StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(AbstractComponentTab.class);

  @FXML
  public BorderPane componentInstallerPane;

  @FXML
  public Button openFolderButton;

  @FXML
  public VBox componentSummaryPane;

  @FXML
  private VBox componentCustomValues;

  protected ComponentRepresentation component;
  protected ComponentUpdateController componentUpdateController;
  protected ComponentSummaryController componentSummaryController;

  protected void refresh() {
    String savedTargetFolder = component.getTargetFolder();
    JFXFuture.supplyAsync(() -> client.getComponentService().getComponent(getComponentType()))
      .thenAcceptLater(comp -> {
        component = comp;
        // set the target folder the user may have changed
        if (!component.isInstalled()) {
          component.setTargetFolder(savedTargetFolder);
        }

        componentSummaryController.refreshComponent(component);
        componentUpdateController.refreshComponent(component);
        refreshTab(component);
      });
  }

  /**
   * Called when a component is updated, gives opportunity to the tab to refresh itself
   * @param component The updated component
  */
  protected void refreshTab(ComponentRepresentation component2) {
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (getComponentType().equals(type)) {
      Platform.runLater(() -> {
        refresh();
      });
    }
  }

  /**
   * Additional and specific installation processing
   */
  public void postProcessing(boolean simulate) {

  }

  protected void initialize() {
    openFolderButton.setDisable(!SystemUtil.isFolderActionSupported());

    try {
      FXMLLoader loader = new FXMLLoader(ComponentUpdateController.class.getResource("component-update-panel.fxml"));
      Parent builtInRoot = loader.load();
      componentUpdateController = loader.getController();
      componentInstallerPane.setCenter(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(ComponentSummaryController.class.getResource("component-summary-panel.fxml"));
      Parent builtInRoot = loader.load();
      componentSummaryController = loader.getController();
      componentSummaryPane.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    JFXFuture.supplyAsync(() -> client.getComponentService().getComponent(getComponentType()))
      .thenAcceptLater(comp -> {
        this.component = comp;
        componentSummaryController.setComponent(this, component);
        componentUpdateController.setComponent(this, component);
        refreshTab(component);

        EventManager.getInstance().addListener(this);
      });
  }

  public void clearCustomValues() {
    componentCustomValues.getChildren().removeAll(componentCustomValues.getChildren());
  }

  protected ComponentSummaryEntryController addCustomValue(ComponentSummaryEntry entry) {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentSummaryEntryController.class.getResource("component-summary-entry.fxml"));
      Parent builtInRoot = loader.load();
      ComponentSummaryEntryController controller = loader.getController();

      if (componentCustomValues.getChildren().isEmpty()) {
        Label label = new Label("Installation Details");
        label.getStyleClass().add("preference-subtitle");
        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 12, 12, 12));
        componentCustomValues.getChildren().add(box);
      }

      componentCustomValues.getChildren().add(builtInRoot);
      controller.refresh(entry);
      return controller;
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
    return null;
  }

  @FXML
  public final void onFolder() {
    String folder = component.getTargetFolder();
    if (/*client.getSystemService().isLocal() &&*/ StringUtils.isNotBlank(folder)) {
      openFolder(new File(folder));
    }
    else {
      WidgetFactory.showAlert(Studio.stage, "Error", "The server was unable to determine the target folder.");
    }
  }

  protected void openFolder(File file) {
    try {
      if (file.exists()) {
        SystemUtil.openFolder(file);
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Folder Not Found", "The folder\"" + file.getAbsolutePath() + "\" does not exist.");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  protected void openFile(File file) {
    boolean open = Studio.open(file);
    if (!open) {
      WidgetFactory.showAlert(Studio.stage, "Folder Not Found", "The folder \"" + file.getAbsolutePath() + "\" does not exist.");
    }
  }

  protected void editFile(File file) {
    Dialogs.editFile(file);
  }

  abstract protected ComponentType getComponentType();
}

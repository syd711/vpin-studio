package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class ComponentsController implements Initializable, StudioFXController, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentsController.class);

  @FXML
  private Parent root;

  @FXML
  private Tab overviewTab;

  @FXML
  private Tab vpxTab;

  @FXML
  private Tab mameTab;

  @FXML
  private Tab b2sTab;

  @FXML
  private Tab freezyTab;

  @FXML
  private Tab serumTab;

  @FXML
  private Tab doflinxTab;

  @FXML
  private Tab dofTab;

  @FXML
  private Tab flexDMDTab;

  @FXML
  private TabPane tabPane;

  @FXML
  private Label loaderLabel;

  @FXML
  private VBox componentLoader;

  private boolean initialized = false;

  // Add a public no-args constructor
  public ComponentsController() {
  }


  private void updateBreadCrumb() {
    List<String> crumbs = new ArrayList<>();
    crumbs.add(Messages.get("components.components.updates"));
    Tab selected = tabPane.getSelectionModel().getSelectedItem();
    if (selected != null && selected != overviewTab) {
      crumbs.add(selected.getText());
    }
    NavigationController.setBreadCrumb(crumbs);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);
    client.getPreferenceService().addListener(this);
    tabPane.managedProperty().bindBidirectional(tabPane.visibleProperty());
    componentLoader.managedProperty().bindBidirectional(componentLoader.visibleProperty());
    tabPane.setVisible(false);


    try {
      FXMLLoader loader = new FXMLLoader(TabOverviewController.class.getResource("tab-overview.fxml"));
      loader.setResources(Messages.getBundle());
      Parent builtInRoot = loader.load();
      overviewTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    loadTab(vpxTab, "tab-vpx.fxml");
    loadTab(b2sTab, "tab-b2s.fxml");
    loadTab(mameTab, "tab-mame.fxml");
    loadTab(flexDMDTab, "tab-flexdmd.fxml");
    loadTab(freezyTab, "tab-freezy.fxml");
//    loadTab(serumTab, "tab-serum.fxml");
    loadTab(doflinxTab, "tab-doflinx.fxml");
    loadTab(dofTab, "tab-dof.fxml");

    tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> updateBreadCrumb());
    updateBreadCrumb();

    preferencesChanged(PreferenceNames.DOFLINX_SETTINGS, null);
  }

  private void loadTab(Tab tab, String file) {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentsController.class.getResource(file));
      loader.setResources(Messages.getBundle());
      Parent builtInRoot = loader.load();
      tab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    updateBreadCrumb();

    if (!initialized) {
      tabPane.setVisible(false);
      componentLoader.setVisible(true);
      List<ComponentType> list = Arrays.asList(ComponentType.getValues());

      JFXFuture.runAsync(() -> {
        try {
          for (ComponentType componentType : list) {
            ComponentInstallation install = new ComponentInstallation();
            install.setComponent(componentType);
            install.setReleaseTag("-latest-");
            install.setArtifactName("-latest-");
            install.setTargetFolder("-any-");
            ComponentActionLogRepresentation check = client.getComponentService().check(install, false);
            if (!StringUtils.isEmpty(check.getStatus())) {
              LOG.error("Failed to check component " + componentType + ": " + check.getStatus());
            }
            EventManager.getInstance().notify3rdPartyVersionUpdate(componentType);
          }
        }
        catch (Exception e) {
          LOG.error("Component update check failed: {}", e.getMessage(), e);
        }
      }).thenLater(() -> {
        componentLoader.setVisible(false);
        tabPane.setVisible(true);
      });
      initialized = true;
    }

    if (options != null && options.getModel() instanceof ComponentType componentType) {
      Tab tab = getComponentTab(componentType);
      if (tab != null && tabPane.getTabs().contains(tab)) {
        tabPane.getSelectionModel().select(tab);
      }
    }
  }

  private Tab getComponentTab(ComponentType componentType) {
    return switch (componentType) {
      case vpinball -> vpxTab;
      case vpinmame -> mameTab;
      case b2sbackglass -> b2sTab;
      case freezy -> freezyTab;
      case flexdmd -> flexDMDTab;
      case doflinx -> doflinxTab;
      case dof -> dofTab;
      default -> null;
    };
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.DOFLINX_SETTINGS)) {
      JFXFuture.supplyAsync(() -> client.getDofLinxService().isValid())
      .thenAcceptLater(isDofLinxValid -> {
        if (isDofLinxValid) {
          if (!tabPane.getTabs().contains(doflinxTab)) {
            tabPane.getTabs().add(doflinxTab);
          }
        }
        else {
          tabPane.getTabs().remove(doflinxTab);
        }
      });
    }
    else if (key.equals(PreferenceNames.DOF_SETTINGS)) {
      JFXFuture.supplyAsync(() -> client.getDofService().isValid())
          .thenAcceptLater(isDofLinxValid -> {
            if (isDofLinxValid) {
              if (!tabPane.getTabs().contains(dofTab)) {
                tabPane.getTabs().add(dofTab);
              }
            }
            else {
              tabPane.getTabs().remove(dofTab);
            }
          });
    }

  }
}
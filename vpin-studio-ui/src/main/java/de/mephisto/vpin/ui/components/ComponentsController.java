package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.components.emulators.EmulatorsController;
import de.mephisto.vpin.ui.components.screens.ScreensController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ComponentsController implements Initializable, StudioFXController, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentsController.class);
  public static final int TAB_UPDATES = 0;
  public static final int TAB_EMULATORS = 1;
  public static final int TAB_SCREENS = 2;

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
  private TabPane rootTabPane;

  @FXML
  private Tab screensTab;

  @FXML
  private Tab emulatorsTab;

  @FXML
  private Label loaderLabel;

  @FXML
  private VBox componentLoader;

  @FXML
  private Pane hint;

  private boolean initialized = false;
  private EmulatorsController emulatorsController;

  // Add a public no-args constructor
  public ComponentsController() {
  }


  @FXML
  public void onDismiss() {
    ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage, "Hide this warning?", "Hide Warning", "Select the checkbox below if you do not wish to see this warning anymore.", null, "Do not show again", false);
    if (!confirmationResult.isApplyClicked()) {
      hint.setVisible(false);
    }

    if (!confirmationResult.isApplyClicked() && confirmationResult.isChecked()) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      uiSettings.setHideComponentWarning(true);
      client.getPreferenceService().setJsonPreference(uiSettings);
    }
  }

  @FXML
  public void onHyperlink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
  }

  private void updateForTabSelection(int index) {
    if (index == 0) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Overview"));
    }
    else if (index == 1) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Visual Pinball"));
    }
    else if (index == 2) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "VPin MAME"));
    }
    else if (index == 3) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "B2S Server"));
    }
    else if (index == 4) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Freezy"));
    }
    else if (index == 5) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "FlexDMD"));
    }
    else if (index == 6) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Serum"));
    }
    else if (index == 7) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "DOFLinx"));
    }
    else if (index == 8) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "DOF"));
    }
  }

  private void refreshView(Number t1) {
    if (t1.intValue() == TAB_UPDATES) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Updates"));
      if (emulatorsController != null) {
        emulatorsController.onViewDeactivated();
      }
    }
    else if (t1.intValue() == TAB_EMULATORS) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Emulators"));
      if (emulatorsController != null) {
        emulatorsController.onViewActivated();
      }
    }
    else if (t1.intValue() == TAB_SCREENS) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Screens"));
      if (emulatorsController != null) {
        emulatorsController.onViewDeactivated();
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab id");
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);
    client.getPreferenceService().addListener(this);
    tabPane.managedProperty().bindBidirectional(tabPane.visibleProperty());
    componentLoader.managedProperty().bindBidirectional(componentLoader.visibleProperty());
    tabPane.setVisible(false);

    tabPane.setTabMaxWidth(100);
    tabPane.setTabMinWidth(100);

    hint.managedProperty().bindBidirectional(hint.visibleProperty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();

    NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Updates"));
    try {
      FXMLLoader loader = new FXMLLoader(TabOverviewController.class.getResource("tab-overview.fxml"));
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

    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      updateForTabSelection(t1.intValue());
    });

    updateForTabSelection(0);

    if (Features.SCREEN_MANAGER_ENABLED && !Features.IS_STANDALONE) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreensController.class.getResource("tab-screens.fxml"));
        Parent builtInRoot = loader.load();
        screensTab.setContent(builtInRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to load tab: " + e.getMessage(), e);
      }
    }
    else {
      rootTabPane.getTabs().remove(screensTab);
    }

    if (Features.EMULATORS_ENABLED) {
      try {
        FXMLLoader loader = new FXMLLoader(EmulatorsController.class.getResource("tab-emulators.fxml"));
        Parent builtInRoot = loader.load();
        emulatorsController = loader.getController();
        emulatorsTab.setContent(builtInRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to load tab: " + e.getMessage(), e);
      }
    }
    else {
      rootTabPane.getTabs().remove(emulatorsTab);
    }

    rootTabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      refreshView(t1);
    });

    preferencesChanged(PreferenceNames.UI_SETTINGS, null);
    preferencesChanged(PreferenceNames.DOFLINX_SETTINGS, null);
  }

  private void loadTab(Tab tab, String file) {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentsController.class.getResource(file));
      Parent builtInRoot = loader.load();
      tab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
  }

  @Override
  public void onViewDeactivated() {
    if (emulatorsController != null) {
      emulatorsController.onViewDeactivated();
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    updateForTabSelection(tabPane.getSelectionModel().getSelectedIndex());

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

    if (options != null) {
      if (options.getModel() instanceof GameEmulatorRepresentation) {
        rootTabPane.getSelectionModel().select(TAB_EMULATORS);
        if (emulatorsController != null) {
          emulatorsController.setSelection(Optional.of((GameEmulatorRepresentation) options.getModel()));
        }
      }
    }
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      JFXFuture.supplyAsync(() -> client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class))
        .thenAcceptLater(uiSettings -> {
          hint.setVisible(!uiSettings.isHideComponentWarning());
        });
    }
    else if (key.equals(PreferenceNames.DOFLINX_SETTINGS)) {
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
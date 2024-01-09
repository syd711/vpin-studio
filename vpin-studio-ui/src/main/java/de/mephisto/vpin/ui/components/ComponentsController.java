package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ComponentsController implements Initializable, StudioFXController, StudioEventListener {
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
  private Tab flexDMDTab;

  @FXML
  private TabPane tabPane;

  @FXML
  private Pane alx1;

  @FXML
  private Pane hint;

  @FXML
  private Pane center;

  private boolean initialized = false;

  // Add a public no-args constructor
  public ComponentsController() {
  }


  @FXML
  public void onDismiss() {
    ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage, "Hide this warning?", "Hide Warning", "Select the checkbox below if you do not wish to see this warning anymore.", null, "Do not shown again", false);
    if (!confirmationResult.isApplyClicked()) {
      hint.setVisible(false);
    }

    if (!confirmationResult.isApplyClicked() && confirmationResult.isChecked()) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      uiSettings.setHideComponentWarning(true);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    }
  }

  @FXML
  public void onHyperlink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (linkText != null && linkText.startsWith("http") && desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(linkText));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
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
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);

    hint.managedProperty().bindBidirectional(hint.visibleProperty());

    preferencesChanged();


    NavigationController.setInitialController("scene-components.fxml", this, root);
    NavigationController.setBreadCrumb(Arrays.asList("System Manager"));
    try {
      FXMLLoader loader = new FXMLLoader(TabOverviewController.class.getResource("tab-overview.fxml"));
      Parent builtInRoot = loader.load();
      overviewTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    loadTab(vpxTab, "tab-vpx.fxml");
    loadTab(b2sTab, "tab-b2s.fxml");
    loadTab(mameTab, "tab-mame.fxml");
    loadTab(flexDMDTab, "tab-flexdmd.fxml");
    loadTab(freezyTab, "tab-freezy.fxml");

    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      updateForTabSelection(t1.intValue());
    });

    updateForTabSelection(0);
  }

  private void loadTab(Tab tab, String file) {
    try {
      FXMLLoader loader = new FXMLLoader(ComponentsController.class.getResource(file));
      Parent builtInRoot = loader.load();
      tab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
  }

  @Override
  public void onViewActivated() {
    updateForTabSelection(tabPane.getSelectionModel().getSelectedIndex());

    if (!initialized) {
      ComponentChecksProgressModel model = new ComponentChecksProgressModel(false);
      ProgressDialog.createProgressDialog(model);
      initialized = true;
    }
  }

  @Override
  public void preferencesChanged() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    hint.setVisible(!uiSettings.isHideComponentWarning());
  }
}
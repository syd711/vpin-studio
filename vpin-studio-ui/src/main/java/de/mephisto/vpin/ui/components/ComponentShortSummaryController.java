package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.*;
import static de.mephisto.vpin.ui.Studio.client;

public class ComponentShortSummaryController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentShortSummaryController.class);


  @FXML
  private BorderPane root;

  @FXML
  private Label titleLabel;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Hyperlink link;

  private ComponentRepresentation component;

  @FXML
  private void onLink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
  }

  public void refresh(@NonNull ComponentRepresentation component) {
    this.component = component;
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");
    link.setText(component.getUrl());

    if (component.isVersionDiff()) {
      latestVersionLabel.getStyleClass().add("orange-label");
    }
    else if (component.getInstalledVersion() != null && !component.getInstalledVersion().equals("?")) {
      latestVersionLabel.getStyleClass().add("green-label");
    }

    titleLabel.setText(component.getType().toString());

    if (component.getInstalledVersion() != null && !component.getInstalledVersion().equals("?")) {
      installedVersionLabel.setText(component.getInstalledVersion());
    }
    else {
      createHelpIcon(installedVersionLabel, "The existing installation version can't be matched against the release version read from github.\nIt will be set after the first update.");
    }
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");

    if (component.getType().equals(ComponentType.doflinx)) {
      preferencesChanged(PreferenceNames.DOFLINX_SETTINGS, null);
    }
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.DOFLINX_SETTINGS) && component.getType().equals(ComponentType.doflinx)) {
      JFXFuture.supplyAsync(() -> client.getDofLinxService().isValid())
        .thenAcceptLater(isDofLinxValid -> root.setVisible(isDofLinxValid));
    }
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (component.getType().equals(type)) {
      JFXFuture.supplyAsync(() -> client.getComponentService().getComponent(type))
        .thenAcceptLater(updatedComponent -> refresh(updatedComponent));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client.getPreferenceService().addListener(this);
    root.managedProperty().bindBidirectional(root.visibleProperty());
    EventManager.getInstance().addListener(this);
  }
}

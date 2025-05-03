package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentSummaryController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentSummaryController.class);

  @FXML
  private Button setVersionBtn;

  @FXML
  private Button resetVersionBtn;

  @FXML
  private Button ignoreBtn;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Label folderLabel;

  @FXML
  private Button folderBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label lastCheckLabel;

  @FXML
  private Hyperlink githubLink;

  private ComponentRepresentation component;

  @FXML
  public void onHyperlink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
  }

  @FXML
  public void onVersionSet() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Apply Version", "Apply \"" + latestVersionLabel.getText() + "\" as the current version of " + component.getType() + "?", null, "Apply");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to apply version: " + e.getMessage());
      }
    }
  }

  @FXML
  public void onVersionReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Reset Version", "Reset version \"" + latestVersionLabel.getText() + "\"?", null, "Reset");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), "-");
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to reset version: " + e.getMessage());
      }
    }
  }

  @FXML
  public void onVersionIgnore() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Ignore Version", "Ignore version \"" + latestVersionLabel.getText() + "\"?", "The previous version will be used as \"Latest Relase\" instead.", "Ignore Version");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().ignoreVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to ignore version: " + e.getMessage());
      }
    }
  }

  @FXML
  public void onFolderSelect() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle("Select Target Folder");
    File targetFolder = chooser.showOpenDialog(Studio.stage);
    if (targetFolder != null && targetFolder.exists()) {
      folderLabel.setText(targetFolder.getAbsolutePath());
      component.setTargetFolder(targetFolder.getAbsolutePath());
    }
  }

  protected void setComponent(AbstractComponentTab componentTab, ComponentRepresentation component) {
      refreshComponent(component);
  }

  protected void refreshComponent(ComponentRepresentation component) {
    this.component = component;

    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");

    installedVersionLabel.setText("?");
    latestVersionLabel.setText("?");
    lastCheckLabel.setText("?");
    lastModifiedLabel.setText("?");
    folderLabel.setText("-");
    folderBtn.setVisible(false);

    if (component != null) {
      setVersionBtn.setVisible(component.isInstalled());
      setVersionBtn.setDisable(!StringUtils.isEmpty(component.getInstalledVersion()) &&  !component.getInstalledVersion().equals("?") && component.getInstalledVersion().equals(component.getLatestReleaseVersion()));

      resetVersionBtn.setVisible(component.isInstalled());
      resetVersionBtn.setDisable(StringUtils.isEmpty(component.getInstalledVersion()) || component.getInstalledVersion().equals("?"));

      if (component.isVersionDiff()) {
        latestVersionLabel.getStyleClass().add("orange-label");
      }

      installedVersionLabel.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
      latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
      ignoreBtn.setVisible(component.isInstalled() &&
        component.getLatestReleaseVersion() != null && !component.getLatestReleaseVersion().equals("?") && component.getReleases().size() > 1);

      lastCheckLabel.setText(component.getLastCheck() != null ? DateFormat.getDateTimeInstance().format(component.getLastCheck()) : "?");
      lastModifiedLabel.setText(component.getLastModified() != null ? DateFormat.getDateTimeInstance().format(component.getLastModified()) : "?");

      folderLabel.setText(component.getTargetFolder() != null ? component.getTargetFolder() : "?");
      folderBtn.setVisible(!component.isInstalled());

      githubLink.setText(component.getUrl());
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.ignoreBtn.setVisible(false);

    this.folderBtn.managedProperty().bind(folderBtn.visibleProperty());
  }
}

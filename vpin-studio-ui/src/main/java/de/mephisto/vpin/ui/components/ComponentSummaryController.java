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
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

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
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.apply_version"), Messages.get("dialog.apply") + latestVersionLabel.getText() + Messages.get("dialog.as_the_current_version_of") + component.getType() + "?Messages.get("dialog.null")Apply");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to_apply_version") + e.getMessage());
      }
    }
  }

  @FXML
  public void onVersionReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.reset_version"), Messages.get("dialog.reset_version_2") + latestVersionLabel.getText() + "\"?Messages.get("dialog.null")Reset");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), "-");
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to_reset_version") + e.getMessage());
      }
    }
  }

  @FXML
  public void onVersionIgnore() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.ignore_version"), Messages.get("dialog.ignore_version_2") + latestVersionLabel.getText() + "\"?", Messages.get("dialog.the_previous_version_will_be_used_as"), Messages.get("dialog.ignore_version"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().ignoreVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to_ignore_version") + e.getMessage());
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

      lastCheckLabel.setText(component.getLastCheck() != null ? DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(component.getLastCheck()) : "?");
      lastModifiedLabel.setText(component.getLastModified() != null ? DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(component.getLastModified()) : "?");

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

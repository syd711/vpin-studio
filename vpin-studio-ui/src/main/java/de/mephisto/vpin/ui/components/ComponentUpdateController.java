package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.components.GithubReleaseRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentUpdateController implements Initializable, ChangeListener<GithubReleaseRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentUpdateController.class);

  @FXML
  private Button installBtn;

  @FXML
  private Button checkBtn;

  @FXML
  private Button simBtn;

  @FXML
  private TextArea textArea;

  @FXML
  private TextArea releaseNotes;

  @FXML
  private ComboBox<String> artifactCombo;

  @FXML
  private ComboBox<GithubReleaseRepresentation> releasesCombo;

  private AbstractComponentTab componentTab;
  private ComponentRepresentation component;

  private boolean localInstallOnly = true;

  @FXML
  private void onFetch() {
    ComponentType type = component.getType();
    ComponentInstallation install = new ComponentInstallation();
    install.setComponent(type);
    install.setReleaseTag("-latest-");
    install.setArtifactName("-latest-");
    install.setTargetFolder(component.getTargetFolder());

    ComponentCheckProgressModel model = new ComponentCheckProgressModel("Checking Status for " + type, install);
    ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
    if (!resultModel.getResults().isEmpty()) {
      ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
      setText(log.toString());
      EventManager.getInstance().notify3rdPartyVersionUpdate(type);
    }
    else {
      textArea.setText("Check failed. See log for details.");
    }
  }

  @FXML
  private void onInstall() {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        runInstall();
      }
    }
    else {
      runInstall();
    }
  }

  @FXML
  private void onInstallSimulate() {
    run(true, Collections.emptyList());
  }

  @FXML
  private void onCheck() {
    Platform.runLater(() -> {
      try {
        ComponentType type = component.getType();
        GithubReleaseRepresentation release = releasesCombo.getValue();
        String artifact = artifactCombo.getValue();

        ComponentInstallation install = new ComponentInstallation();
        install.setComponent(type);
        install.setReleaseTag(release.getTag());
        install.setArtifactName(artifact);
        install.setTargetFolder(component.getTargetFolder());
  
        ComponentCheckProgressModel model = new ComponentCheckProgressModel("Component Check for " + type, install);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);

        if (!resultModel.getResults().isEmpty()) {
          ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
          setText(log.toString());
        }
        else {
          textArea.setText("Check failed. See log for details.");
        }

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      }
      catch (Exception e) {
        LOG.error("Failed to execute component check: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to execute component check: " + e.getMessage());
      }
    });
  }

  private void runInstall() {
    String artifact = artifactCombo.getValue();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Install Update \"" + artifact + "\"?", "Existing files will be overwritten.", "Make sure to follow the additional instructions shown below.", "Continue");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      run(false, Collections.emptyList());
    }
  }

  //TODO exclusion dialog for selection files not finished, e.g. skip or not to skip mame mappings
  private void run(boolean simulate, List<String> exclusions) {
    textArea.setText("");
    Platform.runLater(() -> {
      try {
        ComponentType type = component.getType();
        GithubReleaseRepresentation release = releasesCombo.getValue();
        String artifact = artifactCombo.getValue();

        ComponentInstallation install = new ComponentInstallation();
        install.setComponent(type);
        install.setReleaseTag(release.getTag());
        install.setArtifactName(artifact);
        install.setTargetFolder(component.getTargetFolder());

        ComponentInstallProgressModel model = new ComponentInstallProgressModel(install, simulate);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);

        if (resultModel.getResults().size() > 0) {
          ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
          setText(log.toString());
        }

        componentTab.postProcessing(simulate);

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      }
      catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        textArea.setText("Action failed: " + ex.getMessage());
      }
    });
  }

  public void setComponent(AbstractComponentTab tab, ComponentRepresentation component) {
    this.componentTab = tab;
    this.component = component;
    refresh(null, null);
  }

  public void refreshComponent(ComponentRepresentation component) {
    this.component = component;
    refresh(releasesCombo.getValue(), artifactCombo.getValue());
  }

  public void setLocalInstallOnly(boolean localInstallOnly) {
    this.localInstallOnly = localInstallOnly;
  }

  public void refresh(@Nullable GithubReleaseRepresentation release, @Nullable String artifactId) {
    releasesCombo.setItems(FXCollections.observableList(component.getReleases()));
    releasesCombo.setDisable(component.getReleases().isEmpty());

    if(localInstallOnly && !client.getSystemService().isLocal()) {
      installBtn.setText("Start Installation (on cabinet only)");
    }

    checkBtn.setVisible(component.isInstalled());
    checkBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null);
    simBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null);
    installBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null || (localInstallOnly && !client.getSystemService().isLocal()));
    if (installBtn.isDisabled()) {
      installBtn.setTooltip(new Tooltip("The component can not be updated via remote client."));
    }
    else {
      installBtn.setTooltip(new Tooltip(""));
    }

    releaseNotes.setText("");
    if (!component.getReleases().isEmpty()) {
      if (release != null) {
        releasesCombo.setValue(release);
      }
      else {
        releasesCombo.setValue(component.getReleases().get(0));
      }

      release = releasesCombo.getValue();
      releaseNotes.setText(release.getReleaseNotes() != null ? release.getReleaseNotes().trim() : "");

      List<String> artifacts = release.getArtifacts();
      artifactCombo.setItems(FXCollections.observableList(artifacts));
      List<String> collect = release.getArtifacts();
      if (!collect.isEmpty()) {
        artifactCombo.setItems(FXCollections.observableList(collect));
        artifactCombo.setValue(collect.get(0));
      }
      if (artifactId != null) {
        artifactCombo.setValue(artifactId);
      }

      artifactCombo.setDisable(component.getReleases().isEmpty());
    }
  }

  @Override
  public void changed(ObservableValue<? extends GithubReleaseRepresentation> observable, GithubReleaseRepresentation oldValue, GithubReleaseRepresentation newValue) {
    this.releasesCombo.valueProperty().removeListener(this);
    refresh(newValue, null);
    this.releasesCombo.valueProperty().addListener(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    simBtn.setDisable(true);
    installBtn.setDisable(true);
    artifactCombo.setDisable(true);
    releasesCombo.setDisable(true);
    checkBtn.setDisable(true);

    releasesCombo.valueProperty().addListener(this);

    artifactCombo.valueProperty().addListener((observableValue, s, t1) -> {
      checkBtn.setDisable(!component.isInstalled() || t1 == null);
      installBtn.setDisable(t1 == null || (localInstallOnly && !client.getSystemService().isLocal()));
      simBtn.setDisable(t1 == null);
    });
  }

  private void setText(String text) {
    textArea.setText("");
    textArea.appendText(text);
    Platform.runLater(() -> {
      textArea.setScrollTop(Double.MAX_VALUE);
      textArea.selectPositionCaret(textArea.getLength());
      textArea.positionCaret(textArea.getLength());
      textArea.deselect();

      new Thread(() -> {
        try {
          Thread.sleep(100);

          Platform.runLater(() -> {
            textArea.setText("");
            textArea.appendText(text);
            textArea.setScrollTop(Double.MAX_VALUE);
            textArea.selectPositionCaret(textArea.getLength());
            textArea.positionCaret(textArea.getLength());
            textArea.deselect();
          });
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
    });
  }
}

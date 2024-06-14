package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.components.GithubReleaseRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentUpdateController implements Initializable, StudioEventListener, ChangeListener<String> {
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
  private ComboBox<String> artifactCombo;

  @FXML
  private ComboBox<String> releasesCombo;

  private AbstractComponentTab componentTab;
  private ComponentType type;
  private ComponentRepresentation component;

  private boolean localInstallOnly = true;

  @FXML
  private void onFetch() {
    ComponentCheckProgressModel model = new ComponentCheckProgressModel("Checking Status for " + type, type, "-latest-", "-latest-");
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
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
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
        String release = releasesCombo.getValue();
        String artifact = artifactCombo.getValue();
        ComponentCheckProgressModel model = new ComponentCheckProgressModel("Component Check for " + type, type, release, artifact);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);

        if (!resultModel.getResults().isEmpty()) {
          ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
          setText(log.toString());
        }
        else {
          textArea.setText("Check failed. See log for details.");
        }

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      } catch (Exception e) {
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
        String release = releasesCombo.getValue();
        String artifact = artifactCombo.getValue();
        ComponentInstallProgressModel model = new ComponentInstallProgressModel(type, simulate, release, artifact);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);

        if (resultModel.getResults().size()>0) {
          ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
          setText(log.toString());
        }

        componentTab.postProcessing(simulate);

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      } catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        textArea.setText("Action failed: " + ex.getMessage());
      }
    });
  }

  public void setComponent(AbstractComponentTab tab, ComponentRepresentation component) {
    this.componentTab = tab;
    this.type = component.getType();
    this.component = component;

    artifactCombo.valueProperty().addListener((observableValue, s, t1) -> {
      checkBtn.setDisable(t1 == null);
      installBtn.setDisable(t1 == null || (localInstallOnly && !client.getSystemService().isLocal()));
      simBtn.setDisable(t1 == null);
    });

    refresh(null, null);
  }

  public void setLocalInstallOnly(boolean localInstallOnly) {
    this.localInstallOnly = localInstallOnly;
  }

  public void refresh(@Nullable String releaseTag, @Nullable String artifactId) {
    List<String> releases = new ArrayList<>();
    List<String> releaseArtifacts = new ArrayList<>();

    for (GithubReleaseRepresentation release : component.getReleases()) {
      releases.add(release.getTag());
    }

    releasesCombo.setItems(FXCollections.observableList(releases));
    releasesCombo.setDisable(releases.isEmpty());

    checkBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null);
    simBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null);
    installBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null || (localInstallOnly && !client.getSystemService().isLocal()));

    if (!component.getReleases().isEmpty()) {
      GithubReleaseRepresentation release = component.getReleases().get(0);
      if (releaseTag != null) {
        releasesCombo.setValue(releaseTag);
      }
      else {
        releasesCombo.setValue(release.getTag());
      }

      release = component.getReleases().stream().filter(r -> r.getTag().equals(releasesCombo.getValue())).findFirst().get();
      List<String> artifacts = release.getArtifacts();
      artifactCombo.setItems(FXCollections.observableList(artifacts));
      String systemPreset = client.getSystemPreset();
      if (systemPreset.equals(PreferenceNames.SYSTEM_PRESET_64_BIT)) {
        List<String> collect = release.getArtifacts().stream().filter(r -> r.contains("x64")).collect(Collectors.toList());
        if (!collect.isEmpty()) {
          artifactCombo.setItems(FXCollections.observableList(collect));
          artifactCombo.setValue(collect.get(0));
        }
      }
      else {
        List<String> collect = release.getArtifacts().stream().filter(r -> !r.contains("x64")).collect(Collectors.toList());
        if (!collect.isEmpty()) {
          artifactCombo.setItems(FXCollections.observableList(collect));
          artifactCombo.setValue(collect.get(0));
        }
      }

      if (artifactId != null) {
        artifactCombo.setValue(artifactId);
      }

      artifactCombo.setDisable(releases.isEmpty());
    }
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (type.equals(this.type)) {
      Platform.runLater(() -> {
        this.component = client.getComponentService().getComponent(type);
        refresh(releasesCombo.getValue(), artifactCombo.getValue());
      });
    }
  }

  @Override
  public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    this.releasesCombo.valueProperty().removeListener(this);
    refresh(newValue, null);
    this.releasesCombo.valueProperty().addListener(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);

    simBtn.setDisable(true);
    installBtn.setDisable(true);
    artifactCombo.setDisable(true);
    releasesCombo.setDisable(true);
    checkBtn.setDisable(true);

    releasesCombo.valueProperty().addListener(this);
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
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
    });
  }
}

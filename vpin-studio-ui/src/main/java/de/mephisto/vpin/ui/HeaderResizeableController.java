package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class HeaderResizeableController implements Initializable {
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private BorderPane header;

  private static MouseEvent event;

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
      helper.switchWindowedMode(e);
    }
  }

  private Stage getStage() {
    return (Stage) header.getScene().getWindow();
  }

  @FXML
  private void onCloseClick() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    if (!uiSettings.isHideFrontendLaunchQuestion()) {
      Frontend frontend = Studio.client.getFrontendService().getFrontendCached();
      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage, "Exit and Launch " + frontend.getName(), "Exit and Launch " + frontend.getName(), "Exit", "Select the checkbox below if you do not wish to see this question anymore.", null, "Do not show again", false);
      if (!confirmationResult.isApplyClicked()) {
        client.getFrontendService().restartFrontend();
      }

      if (confirmationResult.isChecked()) {
        uiSettings.setHideFrontendLaunchQuestion(true);
        client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
      }
    }

    AtomicBoolean polling = new AtomicBoolean(false);
    try {
      final ExecutorService executor = Executors.newFixedThreadPool(1);
      final Future<?> future = executor.submit(() -> {
        client.getSystemService().setMaintenanceMode(false);
        polling.set(JobPoller.getInstance().isPolling());
      });
      future.get(2000, TimeUnit.MILLISECONDS);
      executor.shutdownNow();
    }
    catch (Exception e) {
      //ignore
    }


    if (polling.get()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Jobs Running", "There are still jobs running.", "These jobs will continue after quitting.", "Got it, continue");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        System.exit(0);
      }
    }
    else {
      System.exit(0);
    }
  }

  @FXML
  private void onDragDone() {
    if (titleLabel.getText() != null && !titleLabel.getText().contains("Launcher")) {
      debouncer.debounce("position", () -> {
        int y = (int) getStage().getY();
        int x = (int) getStage().getX();
        int width = (int) getStage().getWidth();
        int height = (int) getStage().getHeight();
        if (width > 0 && height > 0) {
          LocalUISettings.saveLocation(x, y, width, height);
        }
      }, 500);
    }
  }

  @FXML
  private void onMaximize() {
    FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
    helper.switchWindowedMode(event);
  }

  @FXML
  private void onHideClick() {
    getStage().setIconified(true);
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setUserData(this);
    titleLabel.setText("VPin Studio (" + Studio.getVersion() + ")");
    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (!StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }
    titleLabel.setText("VPin Studio (" + Studio.getVersion() + ") - " + name);

    Platform.runLater(() -> {
      getStage().xProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      getStage().yProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      getStage().widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      getStage().heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());

      header.setOnMouseMoved(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          HeaderResizeableController.event = event;
        }
      });
    });

  }
}

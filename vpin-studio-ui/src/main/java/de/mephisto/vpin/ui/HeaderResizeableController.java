package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.LocalUISettings;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.FXResizeHelper;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
      FXResizeHelper helper = (FXResizeHelper) stage.getUserData();
      helper.switchWindowedMode(e);
    }
  }

  @FXML
  private void onCloseClick() {
    AtomicBoolean polling = new AtomicBoolean(false);
    try {
      final ExecutorService executor = Executors.newFixedThreadPool(1);
      final Future<?> future = executor.submit(() -> {
        client.getSystemService().setMaintenanceMode(false);
        polling.set(JobPoller.getInstance().isPolling());
      });
      future.get(2000, TimeUnit.MILLISECONDS);
      executor.shutdownNow();
    } catch (Exception e) {
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
        int y = (int) stage.getY();
        int x = (int) stage.getX();
        int width = (int) stage.getWidth();
        int height = (int) stage.getHeight();
        if (width > 0 && height > 0) {
          LocalUISettings.saveLocation(x, y, width, height);
        }
      }, 500);
    }
  }

  @FXML
  private void onMaximize() {
    FXResizeHelper helper = (FXResizeHelper) stage.getUserData();
    helper.switchWindowedMode(event);
  }

  @FXML
  private void onHideClick() {
    stage.setIconified(true);
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

    stage.xProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.yProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());

    header.setOnMouseMoved(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        HeaderResizeableController.event = event;
      }
    });
  }
}

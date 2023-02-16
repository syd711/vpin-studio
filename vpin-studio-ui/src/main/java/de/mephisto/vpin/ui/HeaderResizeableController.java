package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class HeaderResizeableController implements Initializable {

  private static boolean toggleMaximize = true;

  private double xOffset;
  private double yOffset;

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private BorderPane header;

  @FXML
  private void onCloseClick() {
    if(JobPoller.getInstance().isPolling()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Jobs Running", "There are still jobs running.", "Do you want to continue?", "Yes, continue.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        System.exit(0);
      }
    }
    else {
      System.exit(0);
    }
  }

  @FXML
  private void onMaximize() {
    if(toggleMaximize) {
      Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
      stage.setX(primaryScreenBounds.getMinX());
      stage.setY(primaryScreenBounds.getMinY());
      stage.setMaxHeight(primaryScreenBounds.getHeight());
      stage.setMinHeight(primaryScreenBounds.getHeight());
      stage.setMaxWidth(primaryScreenBounds.getWidth());
      stage.setMinWidth(primaryScreenBounds.getWidth());
    }
    else {
      stage.setX(400);
      stage.setY(200);
      stage.setMaxHeight(1280);
      stage.setMinHeight(1280);
      stage.setMaxWidth(1920);
      stage.setMinWidth(1920);
    }
    toggleMaximize = !toggleMaximize;
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
    titleLabel.setText("VPin Studio");
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });

    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (!StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }
    titleLabel.setText("VPin Studio - " + name);
  }
}

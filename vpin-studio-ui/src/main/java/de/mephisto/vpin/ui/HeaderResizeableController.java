package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.preferences.ManiaPreferencesController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class HeaderResizeableController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(HeaderResizeableController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  @FXML
  private Button friendsBtn;

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

  @FXML
  private void onFriends() {
    //TODO move this into a util and cache it!
    Cabinet cabinet = null;
    try {
      cabinet = maniaClient.getCabinetClient().getCabinet();
    }
    catch (Exception e) {
      LOG.error("Failed to load cabinet setting: {}", e.getMessage());
    }

    if (cabinet == null) {
      WidgetFactory.showInformation(Studio.stage, "Registration Required", "You need to register your cabinet for the VPin Mania services to connect your cabinet with friends.", "Go to the preferences and register your cabinet.");
      return;
    }

    boolean open = FriendsController.toggle();
    if (open) {
      friendsBtn.getStyleClass().add("friends-button-selected");
    }
    else {
      friendsBtn.getStyleClass().remove("friends-button-selected");
    }
  }

  private Stage getStage() {
    if (header.getScene() != null) {
      return (Stage) header.getScene().getWindow();
    }
    return null;
  }

  @FXML
  private void onCloseClick() {
    Studio.exit();
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

    friendsBtn.managedProperty().bindBidirectional(friendsBtn.visibleProperty());
    friendsBtn.setVisible(Features.MANIA_ENABLED);

    titleLabel.setText("VPin Studio (" + Studio.getVersion() + ")");
    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (!StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }
    titleLabel.setText("VPin Studio (" + Studio.getVersion() + ") - " + name);

    Platform.runLater(() -> {
      Stage stage = getStage();
      if (stage != null) {
        stage.xProperty().addListener((observable, oldValue, newValue) -> onDragDone());
        stage.yProperty().addListener((observable, oldValue, newValue) -> onDragDone());
        stage.widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
        stage.heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      }

      header.setOnMouseMoved(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          HeaderResizeableController.event = event;
        }
      });
    });

  }
}

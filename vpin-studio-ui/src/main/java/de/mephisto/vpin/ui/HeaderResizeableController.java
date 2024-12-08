package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
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

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class HeaderResizeableController implements Initializable {
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
    boolean open = FriendsController.toggle();
    if(open) {
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

    //TODO add cached
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
//    friendsBtn.setVisible(cabinet != null);

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

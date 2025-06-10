package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.mania.ManiaSettingsController;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
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
  private Button maniaBtn;

  @FXML
  private Label maniaIconLabel;

  @FXML
  private Label titleLabel;

  @FXML
  private BorderPane header;

  private static MouseEvent event;
  private static Button FRIENDS_BTN;

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
      helper.switchWindowedMode(e);
    }
  }

  @FXML
  private void onMania() {
    if (ToolbarController.newVersion != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Update " + ToolbarController.newVersion, "You need the latest VPin Studio version to use these services.", null, "Update");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Dialogs.openUpdateDialog();
      }
      return;
    }

    Cabinet cabinet = null;
    try {
      cabinet = maniaClient.getCabinetClient().getCabinet();
    }
    catch (Exception e) {
      LOG.error("Failed to load cabinet setting: {}", e.getMessage());
    }

    if (cabinet == null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Registration Required", "You need to register your cabinet for the VPin Mania services.", null, "Register Cabinet");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        boolean register = ManiaHelper.register();
        if (register) {
          toggleFriendsView();
        }
      }
      return;
    }
    toggleFriendsView();
  }

  public static void toggleFriendsView() {
    boolean open = ManiaSettingsController.toggle();
    if (open) {
      if (!FRIENDS_BTN.getStyleClass().contains("friends-button-selected")) {
        FRIENDS_BTN.getStyleClass().add("friends-button-selected");
      }
    }
    else {
      FRIENDS_BTN.getStyleClass().remove("friends-button-selected");
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
    refreshWindowMaximizedState();
  }

  private void refreshWindowMaximizedState() {
    boolean mIsMaximized = Studio.stage.getX() == 0 && Studio.stage.getY() == 0;
    if (mIsMaximized) {
      FontIcon icon = WidgetFactory.createIcon("mdi2w-window-restore");
      icon.setIconSize(16);
      maximizeBtn.setGraphic(icon);
    }
    else {
      FontIcon icon = WidgetFactory.createIcon("mdi2w-window-maximize");
      icon.setIconSize(16);
      maximizeBtn.setGraphic(icon);
    }
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

    FRIENDS_BTN = maniaBtn;

    maniaBtn.managedProperty().bindBidirectional(maniaBtn.visibleProperty());
    maniaBtn.setVisible(Features.MANIA_SOCIAL_ENABLED && Features.MANIA_ENABLED);
    Image maniaImage = new Image(Studio.class.getResourceAsStream("mania.png"));
    ImageView iconMedia = new ImageView(maniaImage);
    iconMedia.setFitWidth(18);
    iconMedia.setFitHeight(18);
    maniaIconLabel.setGraphic(iconMedia);

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


      refreshWindowMaximizedState();
    });

  }
}

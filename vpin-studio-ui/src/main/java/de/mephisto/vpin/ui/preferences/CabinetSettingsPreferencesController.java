package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class CabinetSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CabinetSettingsPreferencesController.class);

  @FXML
  private BorderPane avatarBorderPane;

  @FXML
  private TextField vpinNameText;

  @FXML
  private CheckBox stickyKeysCheckbox;

  @FXML
  private Spinner<Integer> idleSpinner;

  @FXML
  private Button shutdownBtn;

  public static Debouncer debouncer = new Debouncer();
  private Cabinet cabinet;


  @FXML
  private void onShutdown() {
    Optional<ButtonType> result = WidgetFactory.showAlertOption(Studio.stage, "Remote System Shutdown", "Cancel", "Shutdown System", "Are you sure you want to shutdown the remote system?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getSystemService().systemShutdown();
      WidgetFactory.showInformation(Studio.stage, "Remote System Shutdown", "The remote system will shutdown in less than a minute.");
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      ProgressDialog.createProgressDialog(new AvatarUploadProgressModel(selection));
      refreshAvatar(true);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (Features.MANIA_ENABLED) {
      try {
        cabinet = maniaClient.getCabinetClient().getCabinet();
      }
      catch (Exception e) {
        LOG.error("Failed to read cabinet info: {}", e.getMessage());
      }
      if (cabinet != null) {
        vpinNameText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("cabinetName", () -> {
          if (StringUtils.isEmpty(t1)) {
            cabinet.setDisplayName(UIDefaults.VPIN_NAME);
          }
          else {
            cabinet.setDisplayName(t1);
          }

          try {
            maniaClient.getCabinetClient().update(cabinet);
          }
          catch (Exception e) {
            LOG.error("Failed to update cabinet name for VPin Mania: " + e.getMessage(), e);
            WidgetFactory.showAlert(stage, "Error", "Failed to update cabinet name for VPin Mania: " + e.getMessage());
          }
        }, 500)); 
      }
    }

    PreferenceBindingUtil.bindTextField(vpinNameText, PreferenceNames.SYSTEM_NAME, UIDefaults.VPIN_NAME);
    refreshAvatar(false);

    shutdownBtn.setDisable(client.getSystemService().isLocal());

    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    PreferenceEntryRepresentation idle = ServerFX.client.getPreference(PreferenceNames.IDLE_TIMEOUT);
    int timeout = idle.getIntValue();
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, timeout);
    idleSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      client.getPreferenceService().setPreference(PreferenceNames.IDLE_TIMEOUT, String.valueOf(value1));
    }, 500));

    stickyKeysCheckbox.setSelected(!serverSettings.isStickyKeysEnabled());
    stickyKeysCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setStickyKeysEnabled(!t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });
  }

  private void refreshAvatar(boolean forceRefresh) {
    InputStream av = client.getAssetService().getAvatar(forceRefresh);
    if (av == null) {
      av = ServerFX.class.getResourceAsStream("avatar-default.png");
    }
    Image image = new Image(av);
    ImageView avatar = new ImageView();
    avatar.setImage(image);
    avatar.setFitWidth(200);
    avatar.setFitHeight(200);

    avatarBorderPane.setCenter(null);
    avatarBorderPane.setCenter(avatar);
    NavigationController.refreshAvatar();
  }
}

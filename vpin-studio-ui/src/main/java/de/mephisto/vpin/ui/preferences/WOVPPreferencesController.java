package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class WOVPPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPPreferencesController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Button testBtn1;
  @FXML
  private Button testBtn2;
  @FXML
  private Button testBtn3;
  @FXML
  private Button testBtn4;
  @FXML
  private Button testBtn5;

  @FXML
  private Button invalidateAllBtn;

  @FXML
  private TextField apiKeyText1;
  @FXML
  private TextField apiKeyText2;
  @FXML
  private TextField apiKeyText3;
  @FXML
  private TextField apiKeyText4;
  @FXML
  private TextField apiKeyText5;

  @FXML
  private CheckBox subscriptionCheckbox;

  @FXML
  private CheckBox badgeCheckbox;

  @FXML
  private CheckBox resetCheckbox;

  @FXML
  private CheckBox desktopModeCheckbox;

  @FXML
  private Pane tagPane;

  @FXML
  private CheckBox taggingEnabledCheckbox;


  private PauseMenuSettings pauseMenuSettings;
  private WOVPSettings wovpSettings;

  @FXML
  private void onClearCache() {
    invalidateAllBtn.setDisable(true);
    JFXFuture.supplyAsync(() -> {
      return client.getWovpService().clearCache();
    }).thenAcceptLater(success -> {
      invalidateAllBtn.setDisable(false);
    });
  }

  @FXML
  private void onLink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (linkText != null && linkText.startsWith("http") && desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(linkText));
      }
      catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onApiKeyTest(ActionEvent event) {
    Button btn = (Button) event.getSource();
    TextField source = (TextField) btn.getUserData();
    if (!StringUtils.isEmpty(source.getText())) {
      String key = source.getText().trim();
      if (!StringUtils.isEmpty(key)) {
        ApiKeyValidationResponse test = client.getWovpService().test(key);
        if (test != null && test.isSuccess()) {
          WidgetFactory.showInformation(Studio.stage, "Information", "API key validation successful.", "The user \"" + test.getName() + "\" has been authenticated.");
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "Error", "API key validation failed!");
        }
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    testBtn1.setUserData(apiKeyText1);
    testBtn2.setUserData(apiKeyText2);
    testBtn3.setUserData(apiKeyText3);
    testBtn4.setUserData(apiKeyText4);
    testBtn5.setUserData(apiKeyText5);

    subscriptionCheckbox.setSelected(wovpSettings.isEnabled());
    subscriptionCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      wovpSettings.setEnabled(newValue);
      try {
        client.getPreferenceService().setJsonPreference(wovpSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    badgeCheckbox.setSelected(wovpSettings.isBadgeEnabled());
    badgeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      wovpSettings.setBadgeEnabled(newValue);
      try {
        client.getPreferenceService().setJsonPreference(wovpSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    resetCheckbox.setSelected(wovpSettings.isResetHighscores());
    resetCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      wovpSettings.setResetHighscores(newValue);
      try {
        client.getPreferenceService().setJsonPreference(wovpSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    desktopModeCheckbox.setSelected(pauseMenuSettings.isDesktopUser());
    desktopModeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      pauseMenuSettings.setDesktopUser(newValue);
      try {
        client.getPreferenceService().setJsonPreference(pauseMenuSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    apiKeyText1.setText(wovpSettings.getApiKey1());
    apiKeyText1.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          wovpSettings.setApiKey1(t1);
          client.getPreferenceService().setJsonPreference(wovpSettings);
          PreferencesController.markDirty(PreferenceType.competitionSettings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 100);
    });
    apiKeyText2.setText(wovpSettings.getApiKey2());
    apiKeyText2.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          wovpSettings.setApiKey2(t1);
          client.getPreferenceService().setJsonPreference(wovpSettings);
          PreferencesController.markDirty(PreferenceType.competitionSettings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 100);
    });
    apiKeyText3.setText(wovpSettings.getApiKey3());
    apiKeyText3.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          wovpSettings.setApiKey3(t1);
          client.getPreferenceService().setJsonPreference(wovpSettings);
          PreferencesController.markDirty(PreferenceType.competitionSettings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 100);
    });
    apiKeyText4.setText(wovpSettings.getApiKey4());
    apiKeyText4.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          wovpSettings.setApiKey4(t1);
          client.getPreferenceService().setJsonPreference(wovpSettings);
          PreferencesController.markDirty(PreferenceType.competitionSettings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 100);
    });
    apiKeyText5.setText(wovpSettings.getApiKey5());
    apiKeyText5.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          wovpSettings.setApiKey5(t1);
          client.getPreferenceService().setJsonPreference(wovpSettings);
          PreferencesController.markDirty(PreferenceType.competitionSettings);
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }, 100);
    });


    List<String> suggestions = client.getTaggingService().getTags();
    TagField tagField = new TagField(suggestions);
    tagField.setInputDisabled(!wovpSettings.isTaggingEnabled());
    tagField.setAllowCustomTags(true);
    tagField.setPreferredWidth(500);
    tagField.setTags(wovpSettings.getTags());
    tagField.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        List<String> list = (List<String>) c.getList();
        wovpSettings.setTags(new ArrayList<>(list));
        client.getPreferenceService().setJsonPreference(wovpSettings);
      }
    });
    tagPane.getChildren().add(tagField);

    taggingEnabledCheckbox.setSelected(wovpSettings.isTaggingEnabled());
    taggingEnabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        wovpSettings.setTaggingEnabled(newValue);
        tagField.setInputDisabled(!newValue);
        client.getPreferenceService().setJsonPreference(wovpSettings);
      }
    });
  }
}

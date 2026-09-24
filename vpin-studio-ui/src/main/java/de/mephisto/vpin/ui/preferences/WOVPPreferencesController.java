package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.restclient.PreferenceNames;
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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

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
  private CheckBox enabledCheckbox1;
  @FXML
  private CheckBox enabledCheckbox2;
  @FXML
  private CheckBox enabledCheckbox3;
  @FXML
  private CheckBox enabledCheckbox4;
  @FXML
  private CheckBox enabledCheckbox5;

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
  private Button upBtn1;
  @FXML
  private Button upBtn2;
  @FXML
  private Button upBtn3;
  @FXML
  private Button upBtn4;
  @FXML
  private Button upBtn5;

  @FXML
  private Button downBtn1;
  @FXML
  private Button downBtn2;
  @FXML
  private Button downBtn3;
  @FXML
  private Button downBtn4;
  @FXML
  private Button downBtn5;

  @FXML
  private CheckBox subscriptionCheckbox;

  @FXML
  private CheckBox addInputCheckbox;

  @FXML
  private CheckBox badgeCheckbox;

  @FXML
  private CheckBox resetCheckbox;


  @FXML
  private Pane tagPane;

  @FXML
  private CheckBox taggingEnabledCheckbox;


  private WOVPSettings wovpSettings;

  private TextField[] apiKeyTexts;
  private CheckBox[] enabledCheckboxes;
  private Button[] upButtons;
  private Button[] downButtons;

  /**
   * Set while the fields are populated from the settings, so that the listeners do not write them back.
   */
  private boolean updatingKeys = false;

  @FXML
  private void onMoveUp(ActionEvent event) {
    moveApiKey(indexOf(upButtons, event.getSource()), -1);
  }

  @FXML
  private void onMoveDown(ActionEvent event) {
    moveApiKey(indexOf(downButtons, event.getSource()), 1);
  }

  private static int indexOf(Button[] buttons, Object source) {
    return Arrays.asList(buttons).indexOf(source);
  }

  /**
   * Swaps the API key (and its enabled state) at the given slot with its neighbour.
   * The order of the keys defines the order of the players in the pause menu.
   */
  private void moveApiKey(int index, int offset) {
    int target = index + offset;
    if (index < 0 || target < 0 || target >= apiKeyTexts.length) {
      return;
    }

    // the settings use 1-based slots
    wovpSettings.swapApiKeys(index + 1, target + 1);
    try {
      client.getPreferenceService().setJsonPreference(wovpSettings);
      PreferencesController.markDirty(PreferenceType.competitionSettings);
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), e.getMessage());
    }
    refreshApiKeys();
  }

  private void refreshApiKeys() {
    updatingKeys = true;
    try {
      for (int i = 0; i < apiKeyTexts.length; i++) {
        boolean enabled = wovpSettings.isApiKeyEnabled(i + 1);
        apiKeyTexts[i].setText(wovpSettings.getApiKey(i + 1));
        apiKeyTexts[i].setDisable(!enabled);
        enabledCheckboxes[i].setSelected(enabled);
        upButtons[i].setDisable(i == 0);
        downButtons[i].setDisable(i == apiKeyTexts.length - 1);
      }
    }
    finally {
      updatingKeys = false;
    }
  }

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
  private void onPauseMenuLink(ActionEvent event) {
    PreferencesController.navigate("pause-menu");
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
          WidgetFactory.showInformation(Studio.stage, "Information", Messages.get("dialog.api_key_validation_successful"), Messages.get("dialog.the_user") + test.getName() + Messages.get("dialog.has_been_authenticated"));
        }
        else {
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.api_key_validation_failed"));
        }
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);

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
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), e.getMessage());
      }
    });

    addInputCheckbox.setSelected(wovpSettings.isAllowAdditionalInput());
    addInputCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      wovpSettings.setAllowAdditionalInput(newValue);
      try {
        client.getPreferenceService().setJsonPreference(wovpSettings);
        PreferencesController.markDirty(PreferenceType.competitionSettings);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), e.getMessage());
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
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), e.getMessage());
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
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), e.getMessage());
      }
    });

    apiKeyTexts = new TextField[]{apiKeyText1, apiKeyText2, apiKeyText3, apiKeyText4, apiKeyText5};
    enabledCheckboxes = new CheckBox[]{enabledCheckbox1, enabledCheckbox2, enabledCheckbox3, enabledCheckbox4, enabledCheckbox5};
    upButtons = new Button[]{upBtn1, upBtn2, upBtn3, upBtn4, upBtn5};
    downButtons = new Button[]{downBtn1, downBtn2, downBtn3, downBtn4, downBtn5};

    refreshApiKeys();
    for (int i = 0; i < apiKeyTexts.length; i++) {
      final int slot = i + 1;
      final TextField apiKeyText = apiKeyTexts[i];
      apiKeyText.textProperty().addListener((observableValue, oldValue, newValue) -> {
        if (updatingKeys) {
          return;
        }
        debouncer.debounce("apiKeyText" + slot, () -> {
          try {
            wovpSettings.setApiKey(slot, newValue);
            client.getPreferenceService().setJsonPreference(wovpSettings);
            PreferencesController.markDirty(PreferenceType.competitionSettings);
          }
          catch (Exception e) {
            WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), e.getMessage());
          }
        }, 100);
      });

      enabledCheckboxes[i].selectedProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingKeys) {
          return;
        }
        wovpSettings.setApiKeyEnabled(slot, newValue);
        apiKeyText.setDisable(!newValue);
        client.getPreferenceService().setJsonPreference(wovpSettings);
      });
    }


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

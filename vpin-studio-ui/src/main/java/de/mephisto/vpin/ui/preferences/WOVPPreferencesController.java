package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
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
  private TextField apiKeyText;

  @FXML
  private Button testBtn;

  @FXML
  private CheckBox subscriptionCheckbox;

  @FXML
  private CheckBox badgeCheckbox;

  @FXML
  private CheckBox resetCheckbox;

  @FXML
  private Pane tagPane;

  @FXML
  private CheckBox taggingEnabledCheckbox;


  private WOVPSettings wovpSettings;

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
  private void onApiKeyTest() {
    String key = apiKeyText.getText().trim();
    if (!StringUtils.isEmpty(key)) {
      String test = client.getWovpService().test();
      if (test == null) {
        WidgetFactory.showInformation(Studio.stage, "Information", "API key validation successful.");
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error", "API key validation failed!", test);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);

    subscriptionCheckbox.setSelected(wovpSettings.isEnabled());
    subscriptionCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      wovpSettings.setEnabled(newValue);
      try {
        client.getPreferenceService().setJsonPreference(wovpSettings);
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
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
      }
    });

    apiKeyText.setText(wovpSettings.getApiKey());
    apiKeyText.textProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("apiKeyText", () -> {
        try {
          wovpSettings.setApiKey(t1);
          client.getPreferenceService().setJsonPreference(wovpSettings);
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

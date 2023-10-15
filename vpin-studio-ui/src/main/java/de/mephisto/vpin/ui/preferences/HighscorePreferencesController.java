package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class HighscorePreferencesController implements Initializable {

  @FXML
  private TextField titlesField;

  @FXML
  private TextField tagInputField;

  @FXML
  private Pane tags;

  @FXML
  private CheckBox filterCheckbox;

  private List<String> allowList = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PreferenceEntryRepresentation entry = OverlayWindowFX.client.getPreference(PreferenceNames.HIGHSCORE_TITLES);

    String titles = entry.getValue();
    if (StringUtils.isEmpty(titles)) {
      titles = String.join(",", DefaultHighscoresTitles.DEFAULT_TITLES);
    }
    titlesField.setText(titles);

    titlesField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(PreferenceNames.HIGHSCORE_TITLES, () -> {
      client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_TITLES, t1);
      PreferencesController.markDirty();
    }, 500));


    boolean filerEnabled = OverlayWindowFX.client.getPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED).getBooleanValue(false);
    filterCheckbox.setSelected(filerEnabled);
    filterCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_FILTER_ENABLED, t1);
      tagInputField.setDisable(!t1);
      tags.setVisible(t1);
    });

    tags.setVisible(filerEnabled);
    tagInputField.setDisable(!filerEnabled);

    String allowListString = OverlayWindowFX.client.getPreference(PreferenceNames.HIGHSCORE_ALLOW_LIST).getValue();
    if (!StringUtils.isEmpty(allowListString)) {
      String[] split = allowListString.split(",");
      for (String s : split) {
        if (s.length() == 3) {
          if(!allowList.contains(s.toUpperCase())) {
            allowList.add(s.toUpperCase());
            tagButton(tags, s);
          }
        }
      }
    }

    tagInputField.setOnKeyPressed(event -> {
      String text = tagInputField.getText();
      if (event.getCode() == KeyCode.ENTER && text.length() == 3 && !text.contains(",") && !allowList.contains(text.toUpperCase())) {
        allowList.add(text.toUpperCase());
        saveAllowList();

        tagButton(tags, text.toUpperCase());
        tagInputField.clear();
      }
    });
  }

  private void tagButton(Pane box, String tag) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(18);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral("mdi2a-account-remove-outline");
    Button result = new Button(tag.toUpperCase(), fontIcon);
    result.setStyle("-fx-font-size: 14px;");
    result.setPrefHeight(20);
    result.setContentDisplay(ContentDisplay.RIGHT);

    result.setOnAction(event -> {
      tags.getChildren().remove(result);
      allowList.remove(result.getText());
      saveAllowList();
    });
    box.getChildren().add(result);
  }

  private void saveAllowList() {
    String allowListString = String.join(",", this.allowList);
    client.getPreferenceService().setPreference(PreferenceNames.HIGHSCORE_ALLOW_LIST, allowListString);
  }
}

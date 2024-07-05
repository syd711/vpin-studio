package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FilesSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class AutoFillSelectionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AutoFillSelectionController.class);

  @FXML
  private Node root;

  @FXML
  private CheckBox gameVersion;
  @FXML
  private CheckBox gameYear;
  @FXML
  private CheckBox gameType;
  @FXML
  private CheckBox gameTheme;
  @FXML
  private CheckBox manufacturer;
  @FXML
  private CheckBox numberOfPlayers;
  @FXML
  private CheckBox author;
  @FXML
  private CheckBox category;
  @FXML
  private CheckBox ipdbNumber;
  @FXML
  private CheckBox url;
  @FXML
  private CheckBox designBy;
  @FXML
  private CheckBox notes;
  @FXML
  private CheckBox tags;
  @FXML
  private CheckBox details;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL u, ResourceBundle resourceBundle) {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    gameVersion.setSelected(uiSettings.getAutoFillSettings().isGameVersion());
    gameVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setGameVersion(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    gameYear.setSelected(uiSettings.getAutoFillSettings().isGameYear());
    gameYear.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setGameYear(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    gameType.setSelected(uiSettings.getAutoFillSettings().isGameType());
    gameType.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setGameType(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    gameTheme.setSelected(uiSettings.getAutoFillSettings().isGameTheme());
    gameTheme.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setGameTheme(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    manufacturer.setSelected(uiSettings.getAutoFillSettings().isManufacturer());
    manufacturer.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setManufacturer(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    numberOfPlayers.setSelected(uiSettings.getAutoFillSettings().isNumberOfPlayers());
    numberOfPlayers.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setNumberOfPlayers(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    author.setSelected(uiSettings.getAutoFillSettings().isAuthor());
    author.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setAuthor(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    category.setSelected(uiSettings.getAutoFillSettings().isCategory());
    category.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setCategory(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    ipdbNumber.setSelected(uiSettings.getAutoFillSettings().isIpdbNumber());
    ipdbNumber.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setIpdbNumber(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    url.setSelected(uiSettings.getAutoFillSettings().isUrl());
    url.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setUrl(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    designBy.setSelected(uiSettings.getAutoFillSettings().isDesignBy());
    designBy.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setDesignBy(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    notes.setSelected(uiSettings.getAutoFillSettings().isNotes());
    notes.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setNotes(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    tags.setSelected(uiSettings.getAutoFillSettings().isTags());
    tags.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setTags(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
    details.setSelected(uiSettings.getAutoFillSettings().isDetails());
    details.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.getAutoFillSettings().setDetails(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });
  }

  @Override
  public void onDialogCancel() {

  }

}

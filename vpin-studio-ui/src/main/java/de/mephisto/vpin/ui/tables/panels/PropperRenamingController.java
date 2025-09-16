package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.preferences.UISettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PropperRenamingController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PropperRenamingController.class);

  @FXML
  private VBox root;

  @FXML
  private Label displayName;
  @FXML
  private Label fileName;
  @FXML
  private Label gameName;

  @FXML
  private CheckBox displayNameCheckBox;
  @FXML
  private CheckBox fileNameCheckBox;
  @FXML
  private CheckBox gameNameCheckBox;

  @FXML
  private ToggleButton authorBtn;
  @FXML
  private ToggleButton versionBtn;
  @FXML
  private ToggleButton modBtn;
  @FXML
  private ToggleButton vrBtn;

  @FXML
  private Button applyBtn;


  private String gameFileName;
  private UISettings uiSettings;
  private TextField screenNameField;
  private TextField fileNameField;
  private TextField gameNameField;

  private VpsTable vpsTable;
  private VpsTableVersion vpsTableVersion;

  @FXML
  private void onApply() {
    if (displayNameCheckBox.isSelected() && !displayName.getText().equals("-")) {
      screenNameField.setText(displayName.getText());
    }
    if (fileNameCheckBox.isSelected() && !fileName.getText().equals("-")) {
      String base = fileNameField.getText();
      if (base.contains("\\")) {
        base = base.substring(0, base.indexOf("\\") + 1);
      }
      else {
        base = "";
      }
      String value = fileName.getText();
      fileNameField.setText(base + FileUtils.replaceWindowsChars(value));
    }
    if (gameNameCheckBox.isSelected() && !gameName.getText().equals("-")) {
      gameNameField.setText(FileUtils.replaceWindowsChars(gameName.getText()));
    }
  }

  public void setVpsTable(VpsTable vpsTable) {
    this.vpsTable = vpsTable;
    refreshNames();
  }

  public void setVpsTableVersion(VpsTableVersion vpsTableVersion) {
    this.vpsTableVersion = vpsTableVersion;
    refreshNames();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    displayNameCheckBox.setSelected(true);
    fileNameCheckBox.setSelected(true);
    gameNameCheckBox.setSelected(true);

    displayNameCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshNames());
    fileNameCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshNames());
    gameNameCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshNames());

    authorBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setPropperAuthorField(newValue);
      client.getPreferenceService().setJsonPreference(uiSettings);
      refreshNames();
    });
    versionBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setPropperVersionField(newValue);
      client.getPreferenceService().setJsonPreference(uiSettings);
      refreshNames();
    });
    modBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setPropperModField(newValue);
      client.getPreferenceService().setJsonPreference(uiSettings);
      refreshNames();
    });
    vrBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setPropperVRField(newValue);
      client.getPreferenceService().setJsonPreference(uiSettings);
      refreshNames();
    });
  }

  /**
   * Do this once after initialize method is called
   */
  public void initBindings(int width, UISettings uiSettings, TextField screenNameField, TextField fileNameField, TextField gameNameField) {
    root.setMinWidth(width);
    this.uiSettings = uiSettings;

    this.screenNameField = screenNameField;
    this.fileNameField = fileNameField;
    this.gameNameField = gameNameField;

    authorBtn.setSelected(uiSettings.isPropperAuthorField());
    versionBtn.setSelected(uiSettings.isPropperVersionField());
    modBtn.setSelected(uiSettings.isPropperModField());
    vrBtn.setSelected(uiSettings.isPropperVRField());  
  }

  public void setGame(String gameFileName) {
    this.gameFileName = gameFileName;
    refreshNames();
  }

  private void refreshNames() {

    boolean screenNameDisabled = screenNameField.isDisable();
    boolean gameNameDisabled = gameNameField.isDisable();
    boolean fileNameDisabled = fileNameField.isDisable();

    displayNameCheckBox.setDisable(vpsTable == null || screenNameDisabled);
    gameNameCheckBox.setDisable(vpsTable == null || gameNameDisabled);
    fileNameCheckBox.setDisable(vpsTable == null || fileNameDisabled);

    this.applyBtn.setDisable(vpsTable == null || screenNameDisabled && gameNameDisabled && fileNameDisabled);

    displayName.setDisable(!displayNameCheckBox.isSelected() || screenNameDisabled);
    fileName.setDisable(!fileNameCheckBox.isSelected() || fileNameDisabled);
    gameName.setDisable(!gameNameCheckBox.isSelected() || gameNameDisabled);

    authorBtn.setDisable(vpsTableVersion == null);
    versionBtn.setDisable(vpsTableVersion == null);
    modBtn.setDisable(vpsTableVersion == null);
    vrBtn.setDisable(vpsTableVersion == null);

    displayName.setText("-");
    fileName.setText("-");
    gameName.setText("-");

    if (vpsTable != null) {
      StringBuilder builder = new StringBuilder();
      builder.append(vpsTable.getName());

      StringBuilder manufacturerSuffix = new StringBuilder();
      if (!StringUtils.isEmpty(vpsTable.getManufacturer())) {
        manufacturerSuffix.append(vpsTable.getManufacturer());
      }
      manufacturerSuffix.append(" ");
      if (vpsTable.getYear() > 0) {
        manufacturerSuffix.append(vpsTable.getYear());
      }

      String suffix = manufacturerSuffix.toString().trim();
      if (!StringUtils.isEmpty(suffix)) {
        builder.append(" (");
        builder.append(suffix);
        builder.append(")");
      }

      if (gameNameCheckBox.isSelected() && ! gameNameDisabled) {
        gameName.setText(builder.toString());
      }


      if (vpsTableVersion != null) {
        if (authorBtn.isSelected() && vpsTableVersion.getAuthors() != null && !vpsTableVersion.getAuthors().isEmpty()) {
          builder.append(" ");
          builder.append(vpsTableVersion.getAuthors().get(0));
        }

        if (versionBtn.isSelected() && !StringUtils.isEmpty(vpsTableVersion.getVersion())) {
          builder.append(" ");
          builder.append(vpsTableVersion.getVersion());
        }

        if (modBtn.isSelected() && vpsTableVersion.getFeatures() != null && vpsTableVersion.getFeatures().contains("MOD")) {
          builder.append(" ");
          builder.append("MOD");
        }

        if (vrBtn.isSelected() && vpsTableVersion.getFeatures() != null && vpsTableVersion.getFeatures().contains("VR")) {
          builder.append(" ");
          builder.append("VR");
        }
      }

      if (displayNameCheckBox.isSelected() && ! screenNameDisabled) {
        displayName.setText(builder.toString());
      }

      if (fileNameCheckBox.isSelected() && ! fileNameDisabled) {
        String fileSuffix = "vpx";
        if(gameFileName != null) {
          fileSuffix = FilenameUtils.getExtension(gameFileName);
        }
        fileName.setText(builder + "." + fileSuffix);
      }
    }
  }
}

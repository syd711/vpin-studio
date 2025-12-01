package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.panels.AssetFilterPanelController;
import de.mephisto.vpin.ui.tables.panels.PropperRenamingController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PatchUploadController extends BaseUploadController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox uploadReplaceBox;

  @FXML
  private VBox uploadCloneBox;

  @FXML
  private TextField patchVersionField;

  @FXML
  private RadioButton patchAndReplaceRadio;

  @FXML
  private RadioButton patchAndCloneRadio;

  @FXML
  private Label readmeLabel;

  @FXML
  private Label tableNameLabel;

  @FXML
  private Button readmeBtn;

  private Parent assetsFilterPanel;

  @FXML
  private VBox assetsView;

  private Optional<UploadDescriptor> result;

  public PatchUploadController() {
    super(AssetType.DIF, false, false, "zip", "7z", "rar", "dif");
  }

  private AssetFilterPanelController assetFilterPanelController;
  private GameRepresentation game;
  private UploaderAnalysis analysis;

  @Override
  protected UploadProgressModel createUploadModel() {
    return null;
  }

  @Override
  protected void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    //instance variables are cleaned up, so safe them
    UploaderAnalysis uploaderAnalysis = this.analysis;

    String version = patchVersionField.getText();
    Platform.runLater(() -> {
      stage.close();
    });

    UploadType type = patchAndCloneRadio.isSelected() ? UploadType.uploadAndClone:
                      patchAndReplaceRadio.isSelected() ? UploadType.uploadAndReplace:
                      null;
    result = UniversalUploadUtil.upload(getSelection(), game.getId(), type, game.getEmulatorId());
    if (result.isPresent()) {
      try {
        UploadDescriptor uploadDescriptor = result.get();
        uploadDescriptor.setPatchVersion(version);
        uploadDescriptor.setExcludedFiles(uploaderAnalysis != null ? uploaderAnalysis.getExcludedFiles() : Collections.emptyList());
        uploadDescriptor.setExcludedFolders(uploaderAnalysis != null ? uploaderAnalysis.getExcludedFolders() : Collections.emptyList());
        uploadDescriptor.setAutoFill(false);
        LOG.info("Created Upload Descriptor for patching");

        GameRepresentation gameRepresentation = client.getGameService().getGame(uploadDescriptor.getGameId());
        LOG.info("Fetched Game " + gameRepresentation.getGameDisplayName());
        GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(gameRepresentation.getEmulatorId());
        LOG.info("Fetched Emulator " + emulatorRepresentation.getGamesDirectory());

        File gameFile = new File(gameRepresentation.getGameFilePath());
        File emuDir = new File(emulatorRepresentation.getGamesDirectory());
        if (!gameFile.getAbsoluteFile().getParentFile().equals(emuDir)) {
          uploadDescriptor.setFolderBasedImport(true);
          uploadDescriptor.setSubfolderName(FilenameUtils.getBaseName(gameRepresentation.getGameFileName()) + "_[Patched]");
        }


        LOG.info("Starting Game Patcher");
        GamePatcherUploadPostProcessingProgressModel progressModel = new GamePatcherUploadPostProcessingProgressModel("Patching Game", uploadDescriptor, game);
        result = UniversalUploadUtil.postProcess(progressModel);
        if (result.isPresent()) {
          // notify listeners of table import done
          EventManager.getInstance().notifyTableUploaded(result.get());
        }
      }
      catch (Exception e) {
        LOG.error("Patching table post upload failed: {}", e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Patching table post upload failed: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onReadme(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String value = (String) ((Button) e.getSource()).getUserData();
    Dialogs.openTextEditor("readme", stage, new MonitoredTextFile(value), "README");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    readmeBtn.setVisible(false);

    readmeLabel.managedProperty().bindBidirectional(readmeLabel.visibleProperty());
    readmeBtn.managedProperty().bindBidirectional(readmeBtn.visibleProperty());

    ToggleGroup toggleGroup = new ToggleGroup();
    patchAndReplaceRadio.setToggleGroup(toggleGroup);
    patchAndCloneRadio.setToggleGroup(toggleGroup);

    patchAndCloneRadio.setSelected(true);
    uploadCloneBox.getStyleClass().add("selection-panel-selected");

    patchAndReplaceRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!uploadReplaceBox.getStyleClass().contains("selection-panel-selected")) {
            uploadReplaceBox.getStyleClass().add("selection-panel-selected");
          }
          uploadCloneBox.getStyleClass().remove("selection-panel-selected");
        }
        else {
          uploadReplaceBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    patchAndCloneRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!uploadCloneBox.getStyleClass().contains("selection-panel-selected")) {
            uploadCloneBox.getStyleClass().add("selection-panel-selected");
          }
          uploadReplaceBox.getStyleClass().remove("selection-panel-selected");
        }
        else {
          uploadCloneBox.getStyleClass().remove("selection-panel-selected");
        }
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(AssetFilterPanelController.class.getResource("asset-filter-panel.fxml"));
      assetsFilterPanel = loader.load();
      assetsFilterPanel.managedProperty().bindBidirectional(assetsFilterPanel.visibleProperty());
      assetFilterPanelController = loader.getController();
      assetsView.getChildren().add(assetsFilterPanel);
      assetsFilterPanel.setVisible(false);
    }
    catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }
  }

  @Override
  protected void endAnalysis(@Nullable String analysis, @Nullable UploaderAnalysis uploaderAnalysis) {
    super.endAnalysis(analysis, uploaderAnalysis);
    assetsFilterPanel.setVisible(uploaderAnalysis != null && uploaderAnalysis.isArchive());
    assetFilterPanelController.refresh(analysis == null ? getSelection() : null, uploaderAnalysis);

    this.readmeBtn.setVisible(false);
    this.readmeLabel.setVisible(true);
    if (uploaderAnalysis != null && analysis == null) {
      String readmeText = uploaderAnalysis.getReadMeText();
      if (!StringUtils.isEmpty(readmeText)) {
        this.readmeBtn.setUserData(readmeText);
        this.readmeBtn.setVisible(true);
        this.readmeLabel.setVisible(false);
      }
    }

    this.analysis = uploaderAnalysis;
  }

  @Override
  public void setFile(Stage stage, File file, UploaderAnalysis analysis, Runnable finalizer) {
    super.setFile(stage, file, analysis, finalizer);
    assetFilterPanelController.setData(stage, game, AssetType.DIF);
  }

  public void setData(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;
    this.tableNameLabel.setText(game.getGameDisplayName());
  }
}

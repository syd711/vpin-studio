package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.TableDialogs;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AssetFilterPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(AssetFilterPanelController.class);

  @FXML
  private Label assetPupPackLabel;
  @FXML
  private Label assetPatchFileLabel;
  @FXML
  private Label assetAltSoundLabel;
  @FXML
  private Label assetAltColorLabel;
  @FXML
  private Label assetMediaLabel;
  @FXML
  private Label assetMusicLabel;
  @FXML
  private Label assetBackglassLabel;
  @FXML
  private Label assetRomLabel;
  @FXML
  private Label assetPovLabel;
  @FXML
  private Label assetIniLabel;
  @FXML
  private Label assetResLabel;
  @FXML
  private Label assetCfgLabel;
  @FXML
  private Label assetNvRamLabel;
  @FXML
  private Label assetDmdLabel;


  @FXML
  private VBox assetsView;

  @FXML
  private VBox assetsBox;

  @FXML
  private Button assetFilterBtn;

  private Stage parentStage;
  private GameRepresentation game;
  private AssetType filteringMode;
  private UploaderAnalysis uploaderAnalysis;
  private File file;

  @FXML
  private void onAssetFilter() {
    TableDialogs.openMediaUploadDialog(parentStage, this.game, file, uploaderAnalysis, filteringMode, -1);
    updateAnalysis();
  }


  private void updateAnalysis() {
    if (uploaderAnalysis == null || this.file == null) {
      assetsView.setVisible(false);
      return;
    }

    assetFilterBtn.setText("Filter Selection");
    if (!uploaderAnalysis.getExclusions().isEmpty()) {
      if (uploaderAnalysis.getExclusions().size() == 1) {
        assetFilterBtn.setText("Filter Selection (" + uploaderAnalysis.getExclusions().size() + " excluded asset)");
      }
      else {
        assetFilterBtn.setText("Filter Selection (" + uploaderAnalysis.getExclusions().size() + " excluded assets)");
      }
    }

    assetPupPackLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.PUP_PACK) == null);
    assetAltSoundLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.ALT_SOUND) == null);
    assetAltColorLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.ALT_COLOR) == null);
    assetMediaLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.FRONTEND_MEDIA) == null);
    assetMusicLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.MUSIC) == null);
    assetBackglassLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.DIRECTB2S) == null);
    assetPatchFileLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.DIF) == null);
    assetIniLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.INI) == null);
    assetPovLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.POV) == null);
    assetResLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.RES) == null);
    assetDmdLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.DMD_PACK) == null);
    assetRomLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.ROM) == null);
    assetCfgLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.CFG) == null);
    assetNvRamLabel.setVisible(uploaderAnalysis.validateAssetTypeInArchive(AssetType.NV) == null);


    assetCfgLabel.setText("- .cfg File");
    if (assetCfgLabel.isVisible()) {
      assetCfgLabel.setText("- .cfg File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.CFG) + ")");
    }

    assetDmdLabel.setText("- DMD Pack");
    if (assetDmdLabel.isVisible()) {
      assetDmdLabel.setText("- DMD Pack (" + uploaderAnalysis.getDMDPath() + ")");
    }

    assetNvRamLabel.setText("- .nv File");
    if (assetNvRamLabel.isVisible()) {
      assetNvRamLabel.setText("- .nv File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.NV) + ")");
    }

    assetPupPackLabel.setText("- PUP Pack");
    if (assetPupPackLabel.isVisible()) {
      assetPupPackLabel.setText("- PUP Pack (" + uploaderAnalysis.getRomFromPupPack() + ")");
    }

    assetIniLabel.setText("- .ini File");
    if (assetIniLabel.isVisible()) {
      assetIniLabel.setText("- .ini File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.INI) + ")");
    }

    assetPatchFileLabel.setText("- .dif File");
    if (assetPatchFileLabel.isVisible()) {
      assetPatchFileLabel.setText("- .dif File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.DIF) + ")");
    }

    assetResLabel.setText("- .res File");
    if (assetResLabel.isVisible()) {
      assetResLabel.setText("- .res File (" + uploaderAnalysis.getFileNameForAssetType(AssetType.RES) + ")");
    }

    assetRomLabel.setText("- ROM");
    if (assetRomLabel.isVisible()) {
      assetRomLabel.setText("- ROM (" + uploaderAnalysis.getRomFromArchive() + ")");
    }

    assetAltSoundLabel.setText("- ALT Sound");
    if (assetAltSoundLabel.isVisible()) {
      assetAltSoundLabel.setText("- ALT Sound");
    }

    assetsView.setVisible(assetBackglassLabel.isVisible()
        || assetAltSoundLabel.isVisible()
        || assetAltColorLabel.isVisible()
        || assetPovLabel.isVisible()
        || assetIniLabel.isVisible()
        || assetResLabel.isVisible()
        || assetPatchFileLabel.isVisible()
        || assetCfgLabel.isVisible()
        || assetNvRamLabel.isVisible()
        || assetMusicLabel.isVisible()
        || assetMediaLabel.isVisible()
        || assetBackglassLabel.isVisible()
        || assetPupPackLabel.isVisible()
        || assetRomLabel.isVisible());
  }

  public void setData(Stage parentStage, GameRepresentation game, AssetType filteringMode) {
    this.parentStage = parentStage;
    this.game = game;
    this.filteringMode = filteringMode;
  }

  public boolean refresh(@Nullable File file, UploaderAnalysis uploaderAnalysis) {
    this.file = file;
    this.uploaderAnalysis = uploaderAnalysis;
    updateAnalysis();
    return assetsView.isVisible();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    assetsView.setVisible(false);
    assetsView.managedProperty().bindBidirectional(assetsView.visibleProperty());

    assetPupPackLabel.managedProperty().bindBidirectional(assetPupPackLabel.visibleProperty());
    assetAltSoundLabel.managedProperty().bindBidirectional(assetAltSoundLabel.visibleProperty());
    assetAltColorLabel.managedProperty().bindBidirectional(assetAltColorLabel.visibleProperty());
    assetMediaLabel.managedProperty().bindBidirectional(assetMediaLabel.visibleProperty());
    assetMusicLabel.managedProperty().bindBidirectional(assetMusicLabel.visibleProperty());
    assetBackglassLabel.managedProperty().bindBidirectional(assetBackglassLabel.visibleProperty());
    assetIniLabel.managedProperty().bindBidirectional(assetIniLabel.visibleProperty());
    assetPatchFileLabel.managedProperty().bindBidirectional(assetPatchFileLabel.visibleProperty());
    assetPovLabel.managedProperty().bindBidirectional(assetPovLabel.visibleProperty());
    assetResLabel.managedProperty().bindBidirectional(assetResLabel.visibleProperty());
    assetDmdLabel.managedProperty().bindBidirectional(assetDmdLabel.visibleProperty());
    assetRomLabel.managedProperty().bindBidirectional(assetRomLabel.visibleProperty());
    assetCfgLabel.managedProperty().bindBidirectional(assetCfgLabel.visibleProperty());
    assetNvRamLabel.managedProperty().bindBidirectional(assetNvRamLabel.visibleProperty());
  }
}

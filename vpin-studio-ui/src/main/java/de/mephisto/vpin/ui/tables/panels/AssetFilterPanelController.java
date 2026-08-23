package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.TableDialogs;
import org.jspecify.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class AssetFilterPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    assetFilterBtn.setText(Messages.get("tables.table_upload.filter_selection"));
    assetFilterBtn.getStyleClass().remove("error-title");
    if (!uploaderAnalysis.getExclusions().isEmpty()) {
      assetFilterBtn.getStyleClass().add("error-title");
      if (uploaderAnalysis.getExclusions().size() == 1) {
        assetFilterBtn.setText(Messages.get("tables.table_upload.filter_selection_excluded_asset", uploaderAnalysis.getExclusions().size()));
      }
      else {
        assetFilterBtn.setText(Messages.get("tables.table_upload.filter_selection_excluded_assets", uploaderAnalysis.getExclusions().size()));
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


    assetCfgLabel.setText(Messages.get("tables.table_upload.cfg_file"));
    if (assetCfgLabel.isVisible()) {
      assetCfgLabel.setText(Messages.get("tables.table_upload.cfg_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.CFG)));
    }

    assetDmdLabel.setText(Messages.get("tables.table_upload.dmd_pack"));
    if (assetDmdLabel.isVisible()) {
      assetDmdLabel.setText(Messages.get("tables.table_upload.dmd_pack_named", uploaderAnalysis.getDMDPath()));
    }

    assetNvRamLabel.setText(Messages.get("tables.table_upload.nv_file"));
    if (assetNvRamLabel.isVisible()) {
      assetNvRamLabel.setText(Messages.get("tables.table_upload.nv_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.NV)));
    }

    assetPupPackLabel.setText(Messages.get("tables.table_upload.pup_pack"));
    if (assetPupPackLabel.isVisible()) {
      assetPupPackLabel.setText(Messages.get("tables.table_upload.pup_pack_named", uploaderAnalysis.getRomFromPupPack()));
    }

    assetIniLabel.setText(Messages.get("tables.table_upload.ini_file"));
    if (assetIniLabel.isVisible()) {
      assetIniLabel.setText(Messages.get("tables.table_upload.ini_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.INI)));
    }

    assetPatchFileLabel.setText(Messages.get("tables.asset_filter_panel.dif_file"));
    if (assetPatchFileLabel.isVisible()) {
      assetPatchFileLabel.setText(Messages.get("tables.asset_filter_panel.dif_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.DIF)));
    }

    assetResLabel.setText(Messages.get("tables.table_upload.res_file"));
    if (assetResLabel.isVisible()) {
      assetResLabel.setText(Messages.get("tables.table_upload.res_file_named", uploaderAnalysis.getFileNameForAssetType(AssetType.RES)));
    }

    assetRomLabel.setText(Messages.get("tables.table_upload.rom"));
    if (assetRomLabel.isVisible()) {
      assetRomLabel.setText(Messages.get("tables.table_upload.rom_named", uploaderAnalysis.getRomFromArchive()));
    }

    assetAltSoundLabel.setText(Messages.get("tables.table_upload.alt_sound"));
    if (assetAltSoundLabel.isVisible()) {
      assetAltSoundLabel.setText(Messages.get("tables.table_upload.alt_sound"));
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

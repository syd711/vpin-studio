package de.mephisto.vpin.ui.tables.models;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaUploadArchiveItem extends BaseLoadingModel<String, MediaUploadArchiveItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private AssetType assetType;
  private String targetDisplayName;
  private String name;
  private final GameEmulatorRepresentation emulator;
  private boolean folder;
  private final UploaderAnalysis uploaderAnalysis;
  private final AssetType filterMode;
  private boolean selected = true;
  private Image image;

  public MediaUploadArchiveItem(String bean, @NonNull GameEmulatorRepresentation emulator, UploaderAnalysis uploaderAnalysis, AssetType filterMode) {
    super(bean);
    this.name = bean;
    this.emulator = emulator;
    this.folder = uploaderAnalysis.getFoldersWithPath().contains(name);
    this.uploaderAnalysis = uploaderAnalysis;
    this.filterMode = filterMode;
    load();
  }

  public boolean isSelected() {
    return selected;
  }

  public boolean isTableAsset() {
    return assetType.equals(AssetType.VPX) || assetType.equals(AssetType.FPT);
  }

  public boolean isPatch() {
    return assetType.equals(AssetType.DIF);
  }

  public void setSelected(boolean selected) {
    //you can't de-select on selection mode
    if(filterMode != null && filterMode.equals(AssetType.TABLE) && isTableAsset()) {
      return;
    }
    else if(filterMode != null && filterMode.equals(AssetType.DIF) && isPatch()) {
      return;
    }
    this.selected = selected;
  }

  @Override
  public boolean sameBean(String object) {
    return false;
  }

  @Override
  public void load() {
    String fileNameWithPath = getName();
    this.selected = !uploaderAnalysis.getExclusions().contains(fileNameWithPath);
    LOG.info("Loading " + name);

    String pupPackDir = uploaderAnalysis.getPUPPackFolder();
    if (pupPackDir != null) {
      //check if we have the PUP pack folder here
      if (pupPackDir.equals(this.getName()) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.PUP_PACK) == null) {
        assetType = AssetType.PUP_PACK;
        targetDisplayName = client.getFrontendService().getFrontendCached().getInstallationDirectory() + "\\PUPVideos";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }

      //ignore children of PUP packs
      if (FileUtils.isFileBelowFolder(pupPackDir, fileNameWithPath)) {
        return;
      }
    }

    String dmdDir = uploaderAnalysis.getDMDPath();
    if (dmdDir != null && uploaderAnalysis.validateAssetTypeInArchive(AssetType.DMD_PACK) == null) {
      //check if we have the DMD bundle here
      if (dmdDir.equals(this.getName())) {
        assetType = AssetType.DMD_PACK;
        targetDisplayName = emulator.getGamesDirectory();
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }

      if (FileUtils.isFileBelowFolder(dmdDir, fileNameWithPath)) {
        return;
      }
    }

    String altSoundFolder = uploaderAnalysis.getAltSoundFolder();
    if (altSoundFolder != null) {
      if (altSoundFolder.equals(this.getName()) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.ALT_SOUND) == null) {
        assetType = AssetType.ALT_SOUND;
        targetDisplayName = "VPin MAME \"altsound\" folder";  //emulator.getMameDirectory();
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }

      if (FileUtils.isFileBelowFolder(altSoundFolder, fileNameWithPath)) {
        return;
      }
    }

    if (uploaderAnalysis.isMusic()) {
      String musicFolder = uploaderAnalysis.getMusicFolder();
      //check if we have the musicFolder bundle here
      if (musicFolder.equals(this.getName()) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.MUSIC) == null) {
        assetType = AssetType.MUSIC_BUNDLE;
        targetDisplayName = emulator.getInstallationDirectory() + "\\Music\\" + uploaderAnalysis.getRelativeMusicPath(false).replaceAll("/", "\\\\");
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }

      if (FileUtils.isFileBelowFolder(musicFolder, fileNameWithPath)) {
        return;
      }
    }

    //ignore all folders from here
    if (folder) {
      return;
    }

    VPinScreen[] screens = VPinScreen.values();
    for (VPinScreen screen : screens) {
      List<String> popperMediaFiles = uploaderAnalysis.getPopperMediaFiles(screen);
      if (popperMediaFiles.contains(fileNameWithPath)) {
        assetType = AssetType.FRONTEND_MEDIA;
        targetDisplayName = "Screen \"" + screen.name() + "\"";
        LOG.info(fileNameWithPath + ": " + assetType.name());
        return;
      }
    }

    if (resolveTableFileAssets(Arrays.asList(AssetType.INI, AssetType.POV, AssetType.RES, AssetType.DIRECTB2S, AssetType.VPX, AssetType.FPT))) {
      LOG.info(fileNameWithPath + ": " + assetType.name());
      return;
    }

    String extension = FilenameUtils.getExtension(fileNameWithPath);
    AssetType asset = AssetType.fromExtension(emulator.getType(), extension);
    if (asset != null) {
      if (asset.equals(AssetType.RAR) || asset.equals(AssetType.SEVENZIP)) {
        return;
      }

      if (asset.equals(AssetType.BAM_CFG) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.BAM_CFG) == null) {
        this.assetType = asset;
        this.targetDisplayName = emulator.getInstallationDirectory() + "BAM/cfg/";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (asset.equals(AssetType.NV) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.NV) == null) {
        this.assetType = asset;
        this.targetDisplayName = "VPin MAME \"nvram\" folder";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (asset.equals(AssetType.FPL) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.FPL) == null) {
        this.assetType = asset;
        this.targetDisplayName = "Future Pinball \"Libraries\" folder";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (asset.equals(AssetType.ROM) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.ROM) == null && !uploaderAnalysis.isFpTable()) {
        this.assetType = asset;
        this.targetDisplayName = "VPin MAME \"roms\" folder";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (uploaderAnalysis.validateAssetTypeInArchive(AssetType.ALT_COLOR) == null && (asset.equals(AssetType.PAL) || asset.equals(AssetType.PAC) || asset.equals(AssetType.CRZ) || asset.equals(AssetType.CROMC) || asset.equals(AssetType.VNI))) {
        this.assetType = asset;
        this.targetDisplayName = "VPin MAME \"altcolor\" folder";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (asset.equals(AssetType.ZIP) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.ROM) == null && !uploaderAnalysis.isFpTable()) {
        this.assetType = AssetType.ROM;
        this.targetDisplayName = "VPin MAME \"roms\" folder";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (asset.equals(AssetType.ZIP) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.FRONTEND_MEDIA) == null && uploaderAnalysis.isFpTable()) {
        this.assetType = AssetType.FP_MODEL_PACK;
        this.targetDisplayName = "FP Table Model Pack";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
      else if (asset.equals(AssetType.DIF) && uploaderAnalysis.validateAssetTypeInArchive(AssetType.DIF) == null) {
        this.assetType = AssetType.DIF;
        this.targetDisplayName = "-";
        LOG.info(fileNameWithPath + ": " + assetType.name());
      }
    }

    LOG.info("Unsupported asset type for \"" + fileNameWithPath + "\"");
  }

  @Override
  public String getName() {
    return bean;
  }

  public String getTargetDisplayName() {
    return targetDisplayName;
  }

  public String getAssetType() {
    if (assetType != null) {
      return assetType.toString();
    }
    return null;
  }

  public boolean isFolder() {
    return this.folder;
  }

  public boolean isImage() {
    return name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".gif");
  }

  public Image getPreview() {
    String name = getName();
    if (isImage()) {
      if (image == null) {
        try {
          LOG.info("Generating preview for file " + name);
          InputStream in = new ByteArrayInputStream(uploaderAnalysis.readFile(name));
          image = new Image(in);
        }
        catch (Exception e) {
          LOG.error("Failed to create preview: {}", e.getMessage(), e);
        }
      }
      return image;
    }
    return null;
  }

  private boolean resolveTableFileAssets(List<AssetType> tableAssetTypes) {
    String extension = FilenameUtils.getExtension(getName());
    for (AssetType tableAssetType : tableAssetTypes) {
      if (extension.equalsIgnoreCase(tableAssetType.name())) {
        targetDisplayName = emulator.getGamesDirectory();
        assetType = tableAssetType;
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    MediaUploadArchiveItem that = (MediaUploadArchiveItem) object;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name);
  }
}
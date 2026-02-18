package de.mephisto.vpin.server.frontend;


import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.assets.AssetService;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * Base class extended by GameMediaService and PLaylistMediaService to shae common code
 */
public abstract class MediaService  {
  private final static Logger LOG = LoggerFactory.getLogger(MediaService.class);

  public static final byte[] EMPTY_MP4 = Base64.getDecoder().decode("AAAAGGZ0eXBpc29tAAAAAGlzb21tcDQxAAAACGZyZWUAAAAmbWRhdCELUCh9wBQ+4cAhC1AAfcAAPuHAIQtQAH3AAD7hwAAAAlNtb292AAAAbG12aGQAAAAAxzFHd8cxR3cAAV+QAAAYfQABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAG2lvZHMAAAAAEA0AT////xX/DgQAAAACAAABxHRyYWsAAABcdGtoZAAAAAfHMUd3xzFHdwAAAAIAAAAAAAAYfQAAAAAAAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAWBtZGlhAAAAIG1kaGQAAAAAxzFHd8cxR3cAAKxEAAAL/xXHAAAAAAA0aGRscgAAAAAAAAAAc291bgAAAAAAAAAAAAAAAFNvdW5kIE1lZGlhIEhhbmRsZXIAAAABBG1pbmYAAAAQc21oZAAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAAAyHN0YmwAAABkc3RzZAAAAAAAAAABAAAAVG1wNGEAAAAAAAAAAQAAAAAAAAAAAAIAEAAAAACsRAAAAAAAMGVzZHMAAAAAA4CAgB8AQBAEgICAFEAVAAYAAAANdQAADXUFgICAAhIQBgECAAAAGHN0dHMAAAAAAAAAAQAAAAMAAAQAAAAAHHN0c2MAAAAAAAAAAQAAAAEAAAADAAAAAQAAABRzdHN6AAAAAAAAAAoAAAADAAAAFHN0Y28AAAAAAAAAAQAAACg=");
  public static final byte[] EMPTY_MP3 = Base64.getDecoder().decode("SUQzAwAAAAADJVRGTFQAAAAPAAAB//5NAFAARwAvADMAAABDT01NAAAAggAAAGRldWlUdW5TTVBCACAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMDAwMDAxMmMxIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/+7RAAAAE4ABLgAAACAAACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8=");

  @Autowired
  protected FrontendService frontendService;

  public boolean setDefaultAsset(int objectId, VPinScreen screen, String defaultName) {
    List<File> mediaFiles = getMediaFiles(objectId, screen);
    File torename = mediaFiles.stream().filter(f -> f.getName().equals(defaultName)).findFirst().orElse(null);
    if (torename == null) {
      LOG.info("Cannot set default asset as {} does not exist in the assets for game/playlist {} and screen {}", defaultName, objectId, screen);
      return false;
    }

    File temp = new File(torename.getParentFile(), "temp_" + defaultName);
    if (torename.renameTo(temp)) {
      String extension = FilenameUtils.getExtension(defaultName);
      for (File file : mediaFiles) {
        // find existing default files, mind there could be several with different extensions
        String fileext = FilenameUtils.getExtension(file.getName());
        if (FileUtils.isDefaultAsset(file.getName()) && StringUtils.equalsIgnoreCase(fileext, extension)) {
          File defaultFile = FileUtils.uniqueAsset(file);
          if (file.renameTo(defaultFile)) {
            LOG.info("Renamed \"{}\" to \"{}\"", file.getAbsolutePath(), defaultFile.getName());
            notifyGameScreenAssetsChanged(objectId, screen, defaultFile);
          }
          else {
            LOG.warn("Cannot rename \"{}\" to \"{}\", state may be inconsistent", file.getAbsolutePath(), defaultFile.getName());
            return false;
          }
        }
      }
      // ends by renaming temp file to default
      File newFile = new File(torename.getParent(), FileUtils.baseUniqueAsset(defaultName) + "." + extension);
      if (temp.renameTo(newFile)) {
        LOG.info("New default asset set \"{} \"for game {} and screen{}", newFile.getAbsolutePath(), objectId, screen);
        return true;
      }
      else {
        LOG.warn("Cannot rename \"{}\" to \"{}\", state may be inconsistent", temp.getAbsolutePath(), newFile.getName());
      }

    }
    else {
      LOG.warn("Cannot rename \"{}\", set as default operation ignored", torename.getAbsolutePath());
    }
    return false;
  }

  public boolean renameAsset(int objectId, VPinScreen screen, String oldName, String newName) {
    File mediaFile = getMediaFile(objectId, screen, oldName);
    if (mediaFile != null && mediaFile.exists()) {
      File renamed = new File(mediaFile.getParentFile(), newName);
      if (mediaFile.renameTo(renamed)) {
        LOG.info("Renamed \"" + mediaFile.getAbsolutePath() + "\" to \"" + renamed.getAbsolutePath() + "\"");
        return true;
      }
    }
    return false;
  }

  public boolean copyAsset(int objectId, VPinScreen screen, String name, VPinScreen target) {
    try {
      File mediaFile = getMediaFile(objectId, screen, name);
      if (mediaFile != null && mediaFile.exists()) {
        String extension = FilenameUtils.getExtension(name);
        File targetFile = uniqueMediaAsset(objectId, target, extension, true);
        FileUtil.copyFile(mediaFile, targetFile);
        notifyGameScreenAssetsChanged(objectId, screen, targetFile);
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy asset {} to {}: {}", name, target, e.getMessage(), e);
    }
    return false;
  }

  public boolean toFullscreenMedia(int objectId, VPinScreen screen) throws IOException {
    List<File> mediaFiles = getMediaFiles(objectId, screen);
    if (mediaFiles.size() == 1) {
      File mediaFile = mediaFiles.get(0);
      String name = mediaFile.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String suffix = FilenameUtils.getExtension(name);
      String updatedBaseName = baseName + "(SCREEN3)." + suffix;

      LOG.info("Renaming " + mediaFile.getAbsolutePath() + " to '" + updatedBaseName + "'");
      boolean renamed = mediaFile.renameTo(new File(mediaFile.getParentFile(), updatedBaseName));
      if (!renamed) {
        LOG.error("Renaming to " + updatedBaseName + " failed.");
        return false;
      }

      File target = new File(mediaFile.getParentFile(), name);

      LOG.info("Copying blank asset to " + target.getAbsolutePath());
      FileOutputStream out = new FileOutputStream(target);
      //copy base64 encoded 0s video
      IOUtils.write(EMPTY_MP4, out);
      out.close();

      return true;
    }
    return false;
  }

  public boolean addBlank(int objectId, VPinScreen screen) throws IOException {

    String suffix = "mp4";
    if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
      suffix = "mp3";
    }

    File target = uniqueMediaAsset(objectId, screen, suffix, true);
    try (FileOutputStream out = new FileOutputStream(target)) {
      // copy base64 asset
      if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
        IOUtils.write(EMPTY_MP3, out);
      }
      else {
        IOUtils.write(EMPTY_MP4, out);
      }
      LOG.info("Written blank asset \"" + target.getAbsolutePath() + "\"");
    }
    return true;
  }

  public boolean deleteMedia(int objectId, VPinScreen screen, String filename) {
    File media = getMediaFile(objectId, screen, filename);
    if (media != null && media.exists()) {
      LOG.info("Deleted {} of screen {}", media.getAbsolutePath(), screen.name());
      if (screen.equals(VPinScreen.Wheel)) {
        new WheelAugmenter(media).deAugment();
        new WheelIconDelete(media).delete();
      }
      if (media.delete()) {
        notifyGameScreenAssetsChanged(objectId, screen, media);
        return true;
      }
    }
    return false;
  }

  public boolean deleteMedia(int objectId, VPinScreen screen) {
    List<File> files = getMediaFiles(objectId, screen);
    boolean success = true;
    for (File file : files) {
      success &= deleteMedia(objectId, screen, file.getName());
    }
    return success;
  }

  public boolean deleteMedia(int gameId) {
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen screen : values) {
      List<File> files = getMediaFiles(gameId, screen);
      for (File file : files) {
        if (screen.equals(VPinScreen.Wheel)) {
          new WheelAugmenter(file).deAugment();
          new WheelIconDelete(file).delete();
        }
        if (file.delete()) {
          LOG.info("Deleted game media {} of screen {}", file.getAbsolutePath(), screen.name());
          notifyGameScreenAssetsChanged(gameId, screen, file);
        }
      }
    }
    return true;
  }

  public AssetMetaData getMetadata(int objectId, VPinScreen screen, String filename) {
    File mediaFile = getMediaFile(objectId, screen, filename);
    return AssetService.getMetadata(mediaFile);
  }


  public File getMediaFile(int objectId, VPinScreen screen, String name) {
    List<File> mediaFiles = getMediaFiles(objectId, screen);
    return mediaFiles.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
  }
  

  public abstract @NonNull List<File> getMediaFiles(int objectId, VPinScreen screen);

  protected abstract File uniqueMediaAsset(int objectId, VPinScreen screen, String suffix, boolean append);

  protected abstract void notifyGameScreenAssetsChanged(int objectId, VPinScreen screen, File asset);


}
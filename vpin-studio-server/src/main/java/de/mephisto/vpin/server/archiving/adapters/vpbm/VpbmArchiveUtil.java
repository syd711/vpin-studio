package de.mephisto.vpin.server.archiving.adapters.vpbm;

import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.commons.fx.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class VpbmArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmArchiveUtil.class);

  public static TableDetails readTableDetails(@NonNull File file) {
    try {
      String basename = FilenameUtils.getBaseName(file.getName());
      TableDetails tableDetails = new TableDetails();
      tableDetails.setGameDisplayName(basename);
      tableDetails.setGameFileName(basename + ".vpx");
      tableDetails.setGameName(basename);
      return tableDetails;
    } catch (Exception e) {
      LOG.error("Failed to read manifest information from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }

  public static ArchivePackageInfo generatePackageInfo(@NonNull File file, @Nullable File wheelImage) {
    try {
      ArchivePackageInfo info = new ArchivePackageInfo();
      if (wheelImage != null && wheelImage.exists()) {
        BufferedImage image = ImageUtil.loadImage(wheelImage);
        BufferedImage resizedImage = ImageUtil.resizeImage(image, ArchivePackageInfo.TARGET_WHEEL_SIZE_WIDTH);

        byte[] bytes = ImageUtil.toBytes(resizedImage);
        info.setThumbnail(Base64.getEncoder().encodeToString(bytes));

        byte[] original = Files.readAllBytes(wheelImage.toPath());
        info.setIcon(Base64.getEncoder().encodeToString(original));
      }

      if (file.exists()) {
        File manifestFolder = new File(file.getParentFile(), "manifests");
        if (manifestFolder.exists()) {
          File manifestFile = new File(manifestFolder, FilenameUtils.getBaseName(file.getName()) + ".json");
          if (manifestFile.exists()) {
            String manifestString = FileUtils.readFileToString(manifestFile, StandardCharsets.UTF_8);

            info.setPopperMedia(manifestString.contains("POPMedia"));
            info.setRegistryData(true);
            info.setMusic(manifestString.contains("Music\\\\"));
            info.setHighscore(manifestString.contains("User\\\\") || manifestString.contains(".nvram"));
            info.setAltSound(manifestString.contains("altsound"));
            info.setAltColor(manifestString.contains("altcolor"));
            info.setFlexDMD(manifestString.contains("FlexDMD"));
            info.setUltraDMD(manifestString.contains("UltraDMD"));
            info.setPupPack(manifestString.contains("PUPVideos"));
            info.setVpx(manifestString.contains(".vpx"));
            info.setRes(manifestString.contains(".res"));
            info.setPov(manifestString.contains(".pov"));
            info.setRom(manifestString.contains("VPinMAME\\\\roms\\\\"));
            info.setDirectb2s(manifestString.contains(".directb2s"));
            return info;
          }
        }
      }

      return info;
    } catch (Exception e) {
      LOG.error("Failed to read manifest information from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }
}

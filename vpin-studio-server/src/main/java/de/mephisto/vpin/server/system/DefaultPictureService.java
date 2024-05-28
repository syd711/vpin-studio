package de.mephisto.vpin.server.system;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.directb2s.DirectB2SDataExtractor;
import de.mephisto.vpin.server.directb2s.DirectB2SImageExporter;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class DefaultPictureService {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultPictureService.class);

  public final static int DEFAULT_MEDIA_SIZE = 1280;
  public final static int DEFAULT_MEDIA_HEIGHT = 720;
  private final static DirectB2SImageRatio DEFAULT_MEDIA_RATIO = DirectB2SImageRatio.RATIO_16X9;

  @Autowired
  private PupPacksService pupPackService;

  public DefaultPictureService() {

  }

  public void extractDefaultPicture(@NonNull Game game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return;
    }

    File rawDefaultPicture = game.getRawDefaultPicture();
    if (!rawDefaultPicture.getParentFile().exists()) {
      rawDefaultPicture.getParentFile().mkdirs();
    }

    File target = game.getRawDefaultPicture();
    if (game.getDirectB2SFile().exists()) {
      try {
        DirectB2SDataExtractor data = new DirectB2SDataExtractor();
        data.extractData(game.getDirectB2SFile(), game.getEmulatorId(), game.getId());
        DirectB2SImageExporter extractor = new DirectB2SImageExporter(data);
        extractor.extractBackground(target);
        extractor.extractDMD(game.getDMDPicture());
        return;
      } catch (VPinStudioException e) {
        LOG.error("Failed to extract background image: " + e.getMessage(), e);
      }
    }

    GameMediaItem backGlassItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.BackGlass);
    if (backGlassItem != null && backGlassItem.getFile().exists()) {
      String name = backGlassItem.getFile().getName();
      if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
        try {
          FileUtils.copyFile(backGlassItem.getFile(), target);
          return;
        } catch (IOException e) {
          LOG.error("Failed to copy popper resource file as background: " + e.getMessage(), e);
        }
      }
      else if (name.endsWith(".mp4") || name.endsWith(".m4v") || name.endsWith(".mov")) {
        if (JCodec.export(backGlassItem.getFile(), target)) {
          return;
        }
      }
    }

    PupPack pupPack = game.getPupPack();
    if (pupPack != null) {
      pupPackService.exportDefaultPicture(pupPack, target);
    }

  }

  public void deleteDefaultPictures(@NonNull Game game) {
    if (game.getCroppedDefaultPicture() != null && game.getCroppedDefaultPicture().exists()) {
      if (game.getCroppedDefaultPicture().delete()) {
        LOG.info("Deleted " + game.getCroppedDefaultPicture().getAbsolutePath());
      }
    }

    if (game.getRawDefaultPicture() != null && !game.getRawDefaultPicture().exists()) {
      if (game.getRawDefaultPicture().delete()) {
        LOG.info("Deleted " + game.getCroppedDefaultPicture().getAbsolutePath());
      }
    }
  }

  @Nullable
  public File generateCroppedDefaultPicture(@NonNull Game game) {
    try {
      //try to use existing file first
      if (game.getCroppedDefaultPicture() != null && game.getCroppedDefaultPicture().exists()) {
        return game.getCroppedDefaultPicture();
      }

      if (game.getRawDefaultPicture() != null && !game.getRawDefaultPicture().exists()) {
        extractDefaultPicture(game);
      }

      if (game.getRawDefaultPicture() != null && game.getRawDefaultPicture().exists()) {
        File backgroundImageFile = game.getRawDefaultPicture();

        BufferedImage image = ImageIO.read(backgroundImageFile);
        BufferedImage crop = ImageUtil.crop(image, DEFAULT_MEDIA_RATIO.getXRatio(), DEFAULT_MEDIA_RATIO.getYRatio());
        BufferedImage resized = ImageUtil.resizeImage(crop, DEFAULT_MEDIA_SIZE);

        File target = game.getCroppedDefaultPicture();
        if (target == null) {
          return null;
        }

        if (target.exists()) {
          if (!target.delete()) {
            LOG.error("Failed to delete crop picture " + target.getAbsolutePath());
          }
        }

        if (!target.getParentFile().exists()) {
          if (!target.getParentFile().mkdirs()) {
            LOG.error("Failed to create crop image directory " + target.getParentFile().getAbsolutePath());
          }
        }

        if (target.getParentFile().exists() && target.getParentFile().canWrite()) {
          ImageUtil.write(resized, target);
          LOG.info("Written cropped default background for " + game.getRom());
        }
        else {
          LOG.error("No permission to write cropped default picture, folder " + game.getRom() + " does not exist.");
        }
        return target;
      }
    } catch (Exception e) {
      LOG.error("Error extracting default picture: " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public BufferedImage generateCompetitionBackgroundImage(@NonNull Game game, int cropWidth, int cropHeight) {
    try {
      if (game.getRawDefaultPicture() == null || !game.getRawDefaultPicture().exists()) {
        extractDefaultPicture(game);
      }

      File backgroundImageFile = game.getRawDefaultPicture();
      if (backgroundImageFile == null || !backgroundImageFile.exists()) {
        return null;
      }

      BufferedImage image = ImageIO.read(backgroundImageFile);

      if (image.getWidth() < image.getHeight()) {
        image = ImageUtil.crop(image, DirectB2SImageRatio.RATIO_16X9.getXRatio(), DirectB2SImageRatio.RATIO_16X9.getYRatio());
      }


      BufferedImage resized = ImageUtil.resizeImage(image, cropWidth);
      LOG.info("Resized to " + resized.getWidth() + "x" + resized.getHeight());
      BufferedImage crop = resized.getSubimage(0, 0, cropWidth, cropHeight);
      BufferedImage blurred = ImageUtil.blurImage(crop, 8);
//          ImageUtil.applyAlphaComposites(blurred, 0f, 10f);

      Color start = new Color(0f, 0f, 0f, .1f);
      Color end = Color.decode("#111111");
      ImageUtil.gradient(blurred, cropHeight, cropWidth, start, end);
      return blurred;
    } catch (Exception e) {
      LOG.warn("Error creating competition image for " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }
    return null;
  }
}

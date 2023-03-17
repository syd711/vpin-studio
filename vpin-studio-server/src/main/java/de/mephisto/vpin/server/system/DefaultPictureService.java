package de.mephisto.vpin.server.system;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.directb2s.DirectB2SImageExtractor;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.vpa.JCodec;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private final static DirectB2SImageRatio DEFAULT_MEDIA_RATIO = DirectB2SImageRatio.RATIO_16X9;

  public DefaultPictureService() {

  }

  public void extractDefaultPicture(@NonNull Game game) {
    DirectB2SImageExtractor extractor = new DirectB2SImageExtractor();

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
        extractor.extractImage(game.getDirectB2SFile(), target);
        return;
      } catch (VPinStudioException e) {
        //ignore
      }
    }

    File backGlass = game.getPinUPMedia(PopperScreen.BackGlass);
    if (backGlass != null && backGlass.exists()) {
      String name = backGlass.getName();
      if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
        try {
          FileUtils.copyFile(backGlass, target);
          return;
        } catch (IOException e) {
          LOG.error("Failed to copy popper resource file as background: " + e.getMessage(), e);
        }
      }
      else if (name.endsWith(".mp4") || name.endsWith(".m4v") || name.endsWith(".mov")) {
        if (JCodec.export(backGlass, target)) {
          return;
        }
      }
    }


    if (game.getPupPack().isAvailable()) {
      game.getPupPack().exportDefaultPicture();
    }
  }

  @Nullable
  public File generateCroppedDefaultPicture(@NonNull Game game) {
    try {
      if (game.getRawDefaultPicture() == null || !game.getRawDefaultPicture().exists()) {
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
          target.delete();
        }

        if (!target.getParentFile().exists()) {
          target.getParentFile().mkdirs();
        }

        if (target.getParentFile().exists() && target.getParentFile().canWrite()) {
          ImageUtil.write(resized, target);
        }
        else {
          LOG.error("No permission to write cropped default picture, folder " + game.getRom() + " does not exist.");
        }
        LOG.info("Written cropped default background for " + game.getRom());
        return target;
      }
    } catch (Exception e) {
      LOG.error("Error extracting default picture: " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public BufferedImage generateB2SCompetitionImage(@NonNull Game game, int cropWidth, int cropHeight) throws VPinStudioException {
    try {
      if (game.getRawDefaultPicture() == null || !game.getRawDefaultPicture().exists()) {
        extractDefaultPicture(game);
      }

      File backgroundImageFile = game.getRawDefaultPicture();
      if (backgroundImageFile == null || !backgroundImageFile.exists()) {
        return null;
      }

      BufferedImage image = ImageIO.read(backgroundImageFile);

      BufferedImage resized = ImageUtil.resizeImage(image, cropWidth);
      BufferedImage crop = resized.getSubimage(0, 0, cropWidth, cropHeight);
      BufferedImage blurred = ImageUtil.blurImage(crop, 8);
//          ImageUtil.applyAlphaComposites(blurred, 0f, 10f);

      Color start = new Color(0f, 0f, 0f, .1f);
      Color end = Color.decode("#111111");
      ImageUtil.gradient(blurred, cropHeight, cropWidth, start, end);
      return blurred;
    } catch (IOException e) {
      LOG.error("Error creating competition image: " + e.getMessage(), e);
      throw new VPinStudioException(e);
    } catch (Exception e) {
      LOG.error("Failed to generate competition image: " + e.getMessage(), e);
      throw new VPinStudioException(e);
    }
  }
}

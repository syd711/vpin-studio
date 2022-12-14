package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class DirectB2SService {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SService.class);

  public DirectB2SService() {

  }

  public void extractDirectB2SBackgroundImage(@NonNull Game game) throws VPinStudioException {
    DirectB2SImageExtractor extractor = new DirectB2SImageExtractor();

    //always prefer the media file
    if (game.getDirectB2SMediaFile().exists()) {
      extractor.extractImage(game.getDirectB2SMediaFile(), game.getRawDirectB2SBackgroundImage());
    }
    else if (game.getDirectB2SFile().exists()) {
      extractor.extractImage(game.getDirectB2SFile(), game.getRawDirectB2SBackgroundImage());
    }
  }

  @Nullable
  public File generateCroppedB2SImage(@NonNull Game game, @NonNull DirectB2SImageRatio ratio, int cropWidth) throws VPinStudioException {
    try {
      if (!game.getRawDirectB2SBackgroundImage().exists()) {
        extractDirectB2SBackgroundImage(game);
      }

      if (game.getRawDirectB2SBackgroundImage().exists()) {
        File backgroundImageFile = game.getRawDirectB2SBackgroundImage();

        BufferedImage image = ImageIO.read(backgroundImageFile);
        BufferedImage crop = ImageUtil.crop(image, ratio.getXRatio(), ratio.getYRatio());
        BufferedImage resized = ImageUtil.resizeImage(crop, cropWidth);

        File target = game.getCroppedDirectB2SBackgroundImage();
        if (target.exists()) {
          target.delete();
        }

        if (target.getParentFile().exists() && target.getParentFile().canWrite()) {
          ImageUtil.write(resized, target);
        }
        else {
          LOG.error("No permission to write " + target.getAbsolutePath() + ", folder exists: " + target.getParentFile().exists());
        }
        LOG.info("Written cropped directb2s background " + target.getAbsolutePath());
        return target;
      }
    } catch (IOException e) {
      LOG.error("Error extracting directb2s image: " + e.getMessage(), e);
      throw new VPinStudioException(e);
    } catch (Exception e) {
      throw new VPinStudioException(e);
    }
    return null;
  }

  @Nullable
  public BufferedImage generateB2SCompetitionImage(@NonNull Game game, int cropWidth, int cropHeight) throws VPinStudioException {
    try {
      if (!game.getRawDirectB2SBackgroundImage().exists()) {
        extractDirectB2SBackgroundImage(game);
      }

      File backgroundImageFile = game.getRawDirectB2SBackgroundImage();
      if(!backgroundImageFile.exists()) {
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
      throw new VPinStudioException(e);
    }
  }
}

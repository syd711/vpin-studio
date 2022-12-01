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

  @Nullable
  public File extractDirectB2SBackgroundImage(@NonNull Game game) throws VPinStudioException {
    if (game.getDirectB2SFile().exists()) {
      DirectB2SImageExtractor extractor = new DirectB2SImageExtractor(game);
      return extractor.extractImage(game.getDirectB2SFile());
    }
    return null;
  }

  @Nullable
  public File generateB2SImage(@NonNull Game game, @NonNull DirectB2SImageRatio ratio, int cropWidth) throws VPinStudioException {
    try {
      if (game.getDirectB2SFile().exists()) {
        DirectB2SImageExtractor extractor = new DirectB2SImageExtractor(game);
        File tempFile = extractor.extractImage(game.getDirectB2SFile());
        if (tempFile != null) {
          BufferedImage image = ImageIO.read(tempFile);
          BufferedImage crop = ImageUtil.crop(image, ratio.getXRatio(), ratio.getYRatio());
          BufferedImage resized = ImageUtil.resizeImage(crop, cropWidth);
          File target = game.getDirectB2SBackgroundImage();
          if(target.getParentFile().exists() && target.getParentFile().canWrite()) {
            ImageUtil.write(resized, target);
          }
          else {
            LOG.error("No permission to write " + target.getAbsolutePath() + ", folder exists: " + target.getParentFile().exists());
          }
          LOG.info("Written cropped directb2s background " + target.getAbsolutePath());
          tempFile.delete();
          return target;
        }
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
      if (game.getDirectB2SFile().exists()) {
        DirectB2SImageExtractor extractor = new DirectB2SImageExtractor(game);
        File tempFile = extractor.extractImage(game.getDirectB2SFile());
        if (tempFile != null) {
          BufferedImage image = ImageIO.read(tempFile);
          tempFile.delete();

          BufferedImage resized = ImageUtil.resizeImage(image, cropWidth);
          BufferedImage crop = resized.getSubimage(0, 0, cropWidth, cropHeight);
          BufferedImage blurred= ImageUtil.blurImage(crop, 10);
//          ImageUtil.applyAlphaComposites(blurred, 0f, 10f);

          Color start=new Color(0f,0f,0f,.1f );
          Color end= Color.decode("#111111");
          ImageUtil.gradient(blurred, cropHeight, cropWidth, start, end);
          return blurred;
        }
      }
    } catch (IOException e) {
      LOG.error("Error creating competition image: " + e.getMessage(), e);
      throw new VPinStudioException(e);
    } catch (Exception e) {
      throw new VPinStudioException(e);
    }
    return null;
  }
}

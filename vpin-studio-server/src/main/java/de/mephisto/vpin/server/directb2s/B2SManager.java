package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.VPinServiceException;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class B2SManager {
  private final static Logger LOG = LoggerFactory.getLogger(B2SManager.class);

  public B2SManager() {

  }

  @Nullable
  public File extractDirectB2SBackgroundImage(@NonNull GameInfo game) throws VPinServiceException {
    if (game.getDirectB2SFile().exists()) {
      B2SImageExtractor extractor = new B2SImageExtractor(game);
      return extractor.extractImage(game.getDirectB2SFile());
    }
    return null;
  }

  @Nullable
  public File generateB2SImage(@NonNull GameInfo game, @NonNull B2SImageRatio ratio, int cropWidth) throws VPinServiceException {
    try {
      if (game.getDirectB2SFile().exists()) {
        B2SImageExtractor extractor = new B2SImageExtractor(game);
        File tempFile = extractor.extractImage(game.getDirectB2SFile());
        if (tempFile != null) {
          BufferedImage image = ImageIO.read(tempFile);
          BufferedImage crop = ImageUtil.crop(image, ratio.getXRatio(), ratio.getYRatio());
          BufferedImage resized = ImageUtil.resizeImage(crop, cropWidth, cropWidth / ratio.getXRatio() * ratio.getYRatio());
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
      throw new VPinServiceException(e);
    } catch (Exception e) {
      throw new VPinServiceException(e);
    }
    return null;
  }
}

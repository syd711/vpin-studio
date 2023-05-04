package de.mephisto.vpin.server.system;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class JCodec {
  private final static Logger LOG = LoggerFactory.getLogger(JCodec.class);

  public static boolean export(@NonNull File file, @NonNull File defaultPicture) {
    try {
      long time = System.currentTimeMillis();
      for (int i = 50; i < 51; i++) {
        Picture picture = FrameGrab.getFrameFromFile(file, i);
        BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
        ImageIO.write(bufferedImage, "png", defaultPicture);
      }
      LOG.info("Extraction from " + file.getAbsolutePath() + " took " + (System.currentTimeMillis() - time) + "ms");
      return defaultPicture.exists();
    } catch (Exception e) {
      LOG.warn("Failed to extract video: " + e.getMessage());
      return false;
    }
  }
}

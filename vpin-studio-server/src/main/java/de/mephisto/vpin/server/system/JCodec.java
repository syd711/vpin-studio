package de.mephisto.vpin.server.system;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class JCodec {
  private final static Logger LOG = LoggerFactory.getLogger(JCodec.class);

  public static boolean export(@NonNull File file, @NonNull File defaultPicture) {
    try {
      long time = System.currentTimeMillis();
      int frame = 50;
      Picture picture = FrameGrab.getFrameFromFile(file, frame);
      BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
      ImageIO.write(bufferedImage, "png", defaultPicture);

      LOG.info("Extraction from " + file.getAbsolutePath() + " took " + (System.currentTimeMillis() - time) + "ms");
      return defaultPicture.exists();
    } catch (Exception e) {
      LOG.warn("Failed to extract video: " + e.getMessage());
      return false;
    }
  }

   /**
   * Extracts a frame from a video file.
   * @param file the video file
   */
  public static byte[] grab(@NonNull File file) {
    try {
      long time = System.currentTimeMillis();
      int frame = 50;
      Picture picture = FrameGrab.getFrameFromFile(file, frame);
      BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", baos);

      LOG.info("Extraction from " + file.getAbsolutePath() + " took " + (System.currentTimeMillis() - time) + "ms");
      return baos.toByteArray();
    } catch (Exception e) {
      LOG.warn("Failed to extract video: " + e.getMessage());
      return null;
    }
  }

}

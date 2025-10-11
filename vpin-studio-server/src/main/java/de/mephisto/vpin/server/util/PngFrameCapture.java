package de.mephisto.vpin.server.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PngFrameCapture {

  public static byte[] captureFirstFrame(File orig) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BufferedImage image = ImageIO.read(orig);
    ImageIO.write(image, "png", out);
    return out.toByteArray();
  }
}

package de.mephisto.vpin.server.vpa;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JCodec {
  public static void main(String[] args) throws IOException, JCodecException {
    long time = System.currentTimeMillis();
    for (int i = 50; i < 51; i++) {
      Picture picture = FrameGrab.getFrameFromFile(new File("C:\\vPinball\\PinUPSystem\\PUPVideos\\btmn_106\\backglass\\AttractMode.m4v"), i);
      BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
      long start = System.currentTimeMillis();
      ImageIO.write(bufferedImage, "png", new File("E:\\temp\\frame_" + i + ".png"));
      System.out.println("Duration " + (System.currentTimeMillis() - start));
    }
    System.out.println("Time Used:" + (System.currentTimeMillis() - time) + " Milliseconds");
  }
}

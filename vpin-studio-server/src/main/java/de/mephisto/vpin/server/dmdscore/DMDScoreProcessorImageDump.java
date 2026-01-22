package de.mephisto.vpin.server.dmdscore;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A Processor that dump frames in a folder as png
 */
public class DMDScoreProcessorImageDump implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorImageDump.class);

  private String gameName;

  @Override
  public void onFrameStart(String gameName) {
    this.gameName = gameName;
    (new File("c:/temp/" + gameName)).mkdirs(); 
  }

  @Override
  public void onFrameReceived(Frame frame) {

    // Create a ARGB palette
    int[] palette = frame.getPalette();
    int[] argbPalette = new int[palette.length];
    for (int i = 0; i < palette.length; i++) {
      argbPalette[i] = (255 << 24) | palette[i];
    }
    PixelFormat<ByteBuffer> format = PixelFormat.createByteIndexedInstance(argbPalette);

    // generate our new image
    int width = frame.getWidth();
    int height = frame.getHeight();

    WritableImage img = new WritableImage(width, height);
    PixelWriter pw = img.getPixelWriter();
    pw.setPixels(0, 0, width, height, format, frame.getPlane(), 0, width);

    // save it to file
    try {
      ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", 
        new File("c:/temp/" + gameName + "/" + frame.getTimeStamp() + ".png"));
    } catch (IOException e) {
      LOG.error("cannot generate image " + frame.getTimeStamp());
    }
  }

}

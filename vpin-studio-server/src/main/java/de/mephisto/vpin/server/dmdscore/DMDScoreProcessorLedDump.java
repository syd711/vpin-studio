package de.mephisto.vpin.server.dmdscore;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * A Processor that dump frames in a folder as png
 */
public class DMDScoreProcessorLedDump implements DMDScoreProcessor {

  public final static int dotsize = 8;

  /** The root folder where images are stored */
  private final File root;

  private String gameName;

  public DMDScoreProcessorLedDump(File root) {
    this.root = root;
  }

  @Override
  public void onFrameStart(String gameName) {
    this.gameName = gameName;
    new File(root, gameName).mkdirs();
  }

  @Override
  public void onFrameReceived(Frame frame) {

    BufferedImage img = frameToImage(frame);
    try {
      File f = new File(root, gameName + "/" + frame.getTimeStamp() + ".png");
      ImageIO.write(img, "png", f);
    } 
    catch (IOException e) {
    }
  }

  public static BufferedImage frameToImage(Frame frame) {
    // Create a ARGB palette
    int[] palette = frame.getPalette();
    int[] argbPalette = new int[palette.length];
    for (int i = 0; i < palette.length; i++) {
      argbPalette[i] = (255 << 24) | palette[i];
    }

    int width = frame.getWidth();
    int height = frame.getHeight();
    BufferedImage img = new BufferedImage(width * dotsize, height * dotsize, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) img.createGraphics();
    int p = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        byte b = frame.getPlane()[p++];
        Color ledColor = new Color(argbPalette[b]);
        Color brightColor = ledColor.brighter();
        Color alphaColor =  new Color(ledColor.getRed(), ledColor.getGreen(), ledColor.getBlue(), 0);

        Paint radial = new RadialGradientPaint(x * dotsize + dotsize / 2, y * dotsize + dotsize / 2, dotsize / 2, 
          new float[] { 0.0f, 0.6f, 1f},
          new Color[] { brightColor, ledColor, alphaColor},
          CycleMethod.NO_CYCLE);

        g.setPaint(radial);

        g.fillOval(x * dotsize, y * dotsize, dotsize, dotsize);
      }
    }
    return img;
  }

}

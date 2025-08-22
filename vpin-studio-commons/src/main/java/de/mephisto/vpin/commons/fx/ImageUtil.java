package de.mephisto.vpin.commons.fx;

import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.GrayscaleFilter;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.util.Date;

public class ImageUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ImageUtil.class);

  /**
   * Enables the anti aliasing for fonts
   */
  public static void setRendingHints(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }

  public static int convertFontPosture(String posture) {
    if (posture != null) {
      if (posture.toLowerCase().contains("italic")) {
        return Font.ITALIC;
      }
      if (posture.toLowerCase().contains("regular") || posture.toLowerCase().contains("plain")) {
        return Font.PLAIN;
      }
      if (posture.toLowerCase().contains("bold")) {
        return Font.BOLD;
      }
    }

    return Font.PLAIN;
  }

  public static BufferedImage loadImage(File file) throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException("File not found " + file.getAbsolutePath());
    }
    try {
      return ImageIO.read(file);
    }
    catch (IOException e) {
      LOG.error("Failed to read " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      throw e;
    }
  }

  public static void drawTimestamp(File file) throws IOException {
    drawWatermark(file, DateUtil.formatDateTime(new Date()), Color.RED);
  }

  public static void drawWatermark(File file, String watermark, Color color) throws IOException {
    BufferedImage bufferedImage = loadImage(file);
    if (bufferedImage != null) {
      Graphics g = bufferedImage.getGraphics();
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setColor(color);
      g2d.setFont(new Font("TimesRoman", Font.BOLD, 24));
      g2d.drawString(watermark, 12, 30);
      file.delete();
      write(bufferedImage, file);
    }
  }

  public static BufferedImage rotateLeft(BufferedImage image) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    return create(image, -Math.PI / 2, gc);
  }

  public static BufferedImage rotateRight(BufferedImage image) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    return create(image, Math.PI / 2, gc);
  }

  public static BufferedImage rotate180(BufferedImage image) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    return create(image, Math.PI, gc);
  }

  private static BufferedImage create(BufferedImage image, double angle, GraphicsConfiguration gc) {
    double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
    int w = image.getWidth(), h = image.getHeight();
    int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h
        * cos + w * sin);
    int transparency = image.getColorModel().getTransparency();
    BufferedImage result = gc.createCompatibleImage(neww, newh, transparency);
    Graphics2D g = result.createGraphics();
    g.translate((neww - w) / 2, (newh - h) / 2);
    g.rotate(angle, w / 2, h / 2);
    g.drawRenderedImage(image, null);
    return result;
  }

  public static BufferedImage clone(BufferedImage image) {
    BufferedImage b = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    Graphics2D g = b.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return b;
  }

  public static void setDefaultColor(GraphicsContext g, String fontColor) {
    Paint paint = Paint.valueOf(fontColor);
    g.setStroke(paint);
//    setRendingHints(g);
  }

  public static void applyAlphaComposites(BufferedImage image, float alphaWhite, float alphaBlack) {
    Graphics g = image.getGraphics();
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    if (alphaWhite > 0) {
      float value = alphaWhite / 100;
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setColor(Color.WHITE);
      Rectangle rect = new Rectangle(0, 0, imageWidth, imageHeight);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, value));
      g2d.fill(rect);
      g2d.dispose();
    }

    if (alphaBlack > 0) {
      float value = alphaBlack / 100;
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setColor(Color.BLACK);
      Rectangle rect = new Rectangle(0, 0, imageWidth, imageHeight);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, value));
      g2d.fill(rect);
      g2d.dispose();
    }
  }

  public static void drawBorder(GraphicsContext g, int strokeWidth, int width, int height) {
    if (strokeWidth > 0) {
      g.setStroke(g.getFill());
      g.setLineWidth(strokeWidth);
      g.strokeRect(strokeWidth / 2, strokeWidth / 2, width - strokeWidth, height - strokeWidth);
    }
  }

  public static void fill(BufferedImage gradientImage, int height, int width, Color color) {
    Graphics2D g2 = (Graphics2D) gradientImage.getGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, width, height);
    g2.dispose();
  }

  public static void gradient(BufferedImage gradientImage, int width, int height, Color start, Color end) {
    GradientPaint gradient = new GradientPaint(0, 0, start, width, 0, end, false);
    Graphics2D g2 = (Graphics2D) gradientImage.getGraphics();
    g2.setPaint(gradient);
    g2.fillRect(0, 0, width, height);
    g2.dispose();
  }

  public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
    return Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, targetWidth, Scalr.OP_ANTIALIAS);
  }

  public static BufferedImage crop(BufferedImage image, int xRatio, int yRatio) {
    int width = image.getWidth();
    int height = image.getHeight();

    int targetWidth = width;
    int targetHeight = width / xRatio * yRatio;
    if (targetHeight > height) {
      targetWidth = image.getHeight() / yRatio * xRatio;
      targetHeight = height;
    }

    int x = 0;
    int y = 0;
    if (targetWidth < width) {
      x = (width / 2) - (targetWidth / 2);
    }

    LOG.info("Cropping image from " + width + "x" + height + " to " + targetWidth + "x" + targetHeight);
    return image.getSubimage(x, y, targetWidth, targetHeight);
  }

  public static BufferedImage blurImage(BufferedImage originalImage, int radius) {
    GaussianFilter filter = new GaussianFilter(radius);
    return filter.filter(originalImage, null);
  }

  public static BufferedImage boxBlurImage(BufferedImage originalImage, int radius) {
    BoxBlurFilter filter = new BoxBlurFilter(2*radius, 2*radius, 1);
    return filter.filter(originalImage, null);
  }

  public static BufferedImage grayScaleImage(BufferedImage originalImage) {
    GrayscaleFilter filter = new GrayscaleFilter();
    return filter.filter(originalImage, null);
  }

  public static void write(BufferedImage image, File file) throws IOException {
    if (file.getName().endsWith(".png")) {
      writePNG(image, file);
    }
    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
      writeJPG(image, file);
    }
  }

  public static byte[] toBytes(BufferedImage image) throws IOException {
    if (image != null) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(image, "PNG", out);
      return out.toByteArray();
    }
    return null;
  }

  public static void writeJPG(BufferedImage image, File file) throws IOException {
    FileOutputStream fileOutputStream = null;
    try {
      long writeDuration = System.currentTimeMillis();
      fileOutputStream = new FileOutputStream(file);
      BufferedOutputStream imageOutputStream = new BufferedOutputStream(fileOutputStream);
      ImageIO.write(image, "JPG", imageOutputStream);
      imageOutputStream.close();
      long duration = System.currentTimeMillis() - writeDuration;
      LOG.info("Writing \"" + file.getAbsolutePath() + "\" took " + duration + "ms., " + FileUtils.readableFileSize(file.length()));
    }
    finally {
      if (fileOutputStream != null) {
        fileOutputStream.close();
      }
    }
  }

  public static void writeJPG(BufferedImage image, OutputStream out) throws IOException {
    try {
      ImageIO.write(image, "JPG", out);
    }
    catch (Exception e) {
      LOG.error("Failed to stream image: " + e.getMessage(), e);
    }
  }

  private static void writePNG(BufferedImage image, File file) throws IOException {
    FileOutputStream fileOutputStream = null;
    try {
      long writeDuration = System.currentTimeMillis();
      fileOutputStream = new FileOutputStream(file);
      BufferedOutputStream imageOutputStream = new BufferedOutputStream(fileOutputStream);
      ImageIO.write(image, "PNG", imageOutputStream);
      imageOutputStream.close();
      long duration = System.currentTimeMillis() - writeDuration;
      LOG.info("Writing \"" + file.getAbsolutePath() + "\" took " + duration + "ms., " + FileUtils.readableFileSize(file.length()));
    }
    finally {
      if (fileOutputStream != null) {
        fileOutputStream.close();
      }
    }
  }

  public static BufferedImage fastBlur(BufferedImage src, int radius) {
      int width = src.getWidth();
      int height = src.getHeight();

      ColorModel destCM = src.getColorModel();
      BufferedImage dst = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), 
        destCM.isAlphaPremultiplied(), null);

      int[] srcPixels = new int[width * height];
      int[] dstPixels = new int[width * height];

      // Unmanages the image
      src.getRGB(0, 0, width, height, srcPixels, 0, width);
      // horizontal pass
      blur(srcPixels, dstPixels, width, height, radius);
      // vertical pass
      blur(dstPixels, srcPixels, height, width, radius);
      // the result is now stored in srcPixels due to the 2nd pass
      dst.setRGB(0, 0, width, height, srcPixels, 0, width);

      return dst;
  }

  /**
   * <p>Blurs the source pixels into the destination pixels. The force of
   * the blur is specified by the radius which must be greater than 0.</p>
   * <p>The source and destination pixels arrays are expected to be in the
   * INT_ARGB format.</p>
   * <p>After this method is executed, dstPixels contains a transposed and
   * filtered copy of srcPixels.</p>
   *
   * @param srcPixels the source pixels
   * @param dstPixels the destination pixels
   * @param width the width of the source picture
   * @param height the height of the source picture
   * @param radius the radius of the blur effect
   */
  private static void blur(int[] srcPixels, int[] dstPixels, int width, int height, int radius) {
    final int windowSize = radius * 2 + 1;
    final int radiusPlusOne = radius + 1;

    int sumAlpha;
    int sumRed;
    int sumGreen;
    int sumBlue;

    int srcIndex = 0;
    int dstIndex;
    int pixel;

    int[] sumLookupTable = new int[256 * windowSize];
    for (int i = 0; i < sumLookupTable.length; i++) {
      sumLookupTable[i] = i / windowSize;
    }

    int[] indexLookupTable = new int[radiusPlusOne];
    if (radius < width) {
      for (int i = 0; i < indexLookupTable.length; i++) {
        indexLookupTable[i] = i;
      }
    } else {
      for (int i = 0; i < width; i++) {
        indexLookupTable[i] = i;
      }
      for (int i = width; i < indexLookupTable.length; i++) {
        indexLookupTable[i] = width - 1;
      }
    }

    for (int y = 0; y < height; y++) {
      sumAlpha = sumRed = sumGreen = sumBlue = 0;
      dstIndex = y;

      pixel = srcPixels[srcIndex];
      sumAlpha += radiusPlusOne * ((pixel >> 24) & 0xFF);
      sumRed   += radiusPlusOne * ((pixel >> 16) & 0xFF);
      sumGreen += radiusPlusOne * ((pixel >>  8) & 0xFF);
      sumBlue  += radiusPlusOne * ( pixel        & 0xFF);

      for (int i = 1; i <= radius; i++) {
        pixel = srcPixels[srcIndex + indexLookupTable[i]];
        sumAlpha += (pixel >> 24) & 0xFF;
        sumRed   += (pixel >> 16) & 0xFF;
        sumGreen += (pixel >>  8) & 0xFF;
        sumBlue  +=  pixel        & 0xFF;
      }

      for (int x = 0; x < width; x++) {
        dstPixels[dstIndex] = sumLookupTable[sumAlpha] << 24 |
                              sumLookupTable[sumRed]   << 16 |
                              sumLookupTable[sumGreen] <<  8 |
                              sumLookupTable[sumBlue];
        dstIndex += height;

        int nextPixelIndex = x + radiusPlusOne;
        if (nextPixelIndex >= width) {
          nextPixelIndex = width - 1;
        }

        int previousPixelIndex = x - radius;
        if (previousPixelIndex < 0) {
          previousPixelIndex = 0;
        }

        int nextPixel = srcPixels[srcIndex + nextPixelIndex];
        int previousPixel = srcPixels[srcIndex + previousPixelIndex];

        sumAlpha += (nextPixel     >> 24) & 0xFF;
        sumAlpha -= (previousPixel >> 24) & 0xFF;

        sumRed += (nextPixel     >> 16) & 0xFF;
        sumRed -= (previousPixel >> 16) & 0xFF;

        sumGreen += (nextPixel     >> 8) & 0xFF;
        sumGreen -= (previousPixel >> 8) & 0xFF;

        sumBlue += nextPixel & 0xFF;
        sumBlue -= previousPixel & 0xFF;
      }

      srcIndex += width;
    }
  }
}

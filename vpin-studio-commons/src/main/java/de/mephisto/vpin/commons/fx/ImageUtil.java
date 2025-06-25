package de.mephisto.vpin.commons.fx;

import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;
import de.mephisto.vpin.restclient.util.DateUtil;
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
      g2d.setFont(new Font("TimesRoman", Font.BOLD, 18));
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
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "PNG", out);
    return out.toByteArray();
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
      LOG.info("Writing \"" + file.getAbsolutePath() + "\" took " + duration + "ms.");
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
      LOG.info("Writing " + file.getAbsolutePath() + " took " + duration + "ms.");
    }
    finally {
      if (fileOutputStream != null) {
        fileOutputStream.close();
      }
    }
  }
}

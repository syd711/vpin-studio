package de.mephisto.vpin.commons.fx;

import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.GrayscaleFilter;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

  public static BufferedImage flipHorizontal(BufferedImage image) {
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    // offset so that image stays in frame
    tx.translate(-image.getWidth(), 0);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(image, null);
  }

  public static BufferedImage flipVertical(BufferedImage image) {
    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    // offset so that image stays in frame
    tx.translate(0, -image.getHeight());
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(image, null);
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

  public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    g.dispose();
    return resizedImage;
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


  /**
   * Iterate over every stepPx pixels of the image and calcul
   * Use the K-Means algorithm to find the dominant colors.
   * @return the [red, green, blue] values of the Color
   */
  public static int[] getDominantColor(BufferedImage image, int stepPx) {
    int[][] colors = getDominantColors(image, 1, stepPx);
    return colors[0];
  }

  /**
   * Return an array of nbColors dominant colors
   */
  public static int[][] getDominantColors(BufferedImage image, int nbColors, int stepPx) {
    int width = image.getWidth();
    int height = image.getHeight();
    // Use DoublePoint for K-Means
    List<DoublePoint> points = new ArrayList<>();

    for (int y = 0; y < height; y+=stepPx) {
      for (int x = 0; x < width; x+=stepPx) {
        int rgb = image.getRGB(x, y);
        DoublePoint p = new DoublePoint(new double[] {
          (rgb >> 16) & 0xFF, // Red
          (rgb >> 8) & 0xFF,  // Green
          rgb & 0xFF          // Blue
        });
        points.add(p);
      }
    }

    // Run K-Means
    KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(nbColors);
    List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);
    // Extract cluster centers (dominant colors)
    int[][] dominantColors = new int[nbColors][3];
    for (int i = 0; i < nbColors; i++) {
        double[] center = clusters.get(i).getCenter().getPoint();
        dominantColors[i] = new int[]{(int) center[0], (int) center[1], (int) center[2]};
    }
    return dominantColors;
  }

  //----------------------------------------------------------

  /**
   * Apply a perspective effect to an image
   * @param img The Image to be transformed
   * @param side The "left" or "right" value for the perspective
   * @param depthX the sheer effect of the perspective on x (good value are 0.1 or 0.2)
   * @param depthY the increase effect of Y
   */
  public static BufferedImage applyPerspective(BufferedImage img, String side, double depthX, double depthY) {
    int w = img.getWidth();
    int h = img.getHeight();

    double[][] src, dst;
    if (side.equals("left")) {
      src = new double[][] {{0, 0}, {w, 0}, {w, h}, {0, h}};
      dst = new double[][] {{w * depthX, 0}, {w, - h * depthY}, {w, h + h * depthY}, {w * depthX, h}};
    } 
    else if (side.equals("right")) {
      src = new double[][] {{0, 0}, {w, 0}, {w, h}, {0, h}};
      dst = new double[][] {{0, - h * depthY}, {w - w * depthX, 0}, {w - w * depthX, h}, {0, h + h * depthY}};
    }
    if (side.equals("top")) {
      src = new double[][] {{0, 0}, {w, 0}, {w, h}, {0, h}};
      dst = new double[][] {{-w * depthX, 0}, {w + w * depthX, 0}, {w, h - h * depthY}, {0, h - h * depthY}};
    } 
    else if (side.equals("bottom")) {
      src = new double[][] {{0, 0}, {w, 0}, {w, h}, {0, h}};
      dst = new double[][] {{0, h * depthY}, {w, h * depthY}, {w + w * depthX, h}, {- w * depthX, h}};
    }
    else {
      return img;
    }

    double[] coeffs = getPerspectiveCoeffs(dst, src);

    BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = dest.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setColor(Color.BLACK);
    g2d.fillRect(0, 0, w, h);
    g2d.setClip(0, 0, w, h);

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        // Apply inverse transform to get source coordinates
        double[] pt = inversePerspectiveTransform(x, y, coeffs);
        int srcX = (int) pt[0];
        int srcY = (int) pt[1];
        if (srcX >= 0 && srcX < w && srcY >= 0 && srcY < h) {
          dest.setRGB(x, y, img.getRGB(srcX, srcY));
        }
      }
    }
    g2d.dispose();
    return dest;
  }

  private static double[] inversePerspectiveTransform(double x, double y, double[] coeffs) {
      // coeffs = [a, b, c, d, e, f, g, h]
      // Solve for (u, v) in:
      // x = (a*u + b*v + c) / (g*u + h*v + 1)
      // y = (d*u + e*v + f) / (g*u + h*v + 1)
      // Here, we need to solve for u, v given x, y

    double a = coeffs[0], b = coeffs[1], c = coeffs[2];
    double d = coeffs[3], e = coeffs[4], f = coeffs[5];
    double g = coeffs[6], h = coeffs[7];

    double A11 = x * g - a;
    double A12 = x * h - b;
    double A21 = y * g - d;
    double A22 = y * h - e;

    double detA = A11 * A22 - A12 * A21;
    if (Math.abs(detA) < 1e-10) {
        // Avoid division by zero; return a default or throw an exception
        return new double[] {x, y};
    }

    double B1 = c - x;
    double B2 = f - y;
    double u = (A22 * B1 - A12 * B2) / detA;
    double v = (A11 * B2 - A21 * B1) / detA;

    return new double[]{u, v};
  }

  private static double[] getPerspectiveCoeffs(double[][] srcCoords, double[][] dstCoords) {
    int numPoints = srcCoords.length;
    int numRows = 2 * numPoints;
    int numCols = 8;
    double[][] matrix = new double[numRows][numCols];
    double[] b = new double[numRows];

    for (int i = 0; i < numPoints; i++) {
        double[] p1 = dstCoords[i];
        double[] p2 = srcCoords[i];
        int row = 2 * i;

        // First row
        matrix[row][0] = p1[0];
        matrix[row][1] = p1[1];
        matrix[row][2] = 1;
        matrix[row][3] = 0;
        matrix[row][4] = 0;
        matrix[row][5] = 0;
        matrix[row][6] = -p2[0] * p1[0];
        matrix[row][7] = -p2[0] * p1[1];
        b[row] = p2[0];

        // Second row
        matrix[row + 1][0] = 0;
        matrix[row + 1][1] = 0;
        matrix[row + 1][2] = 0;
        matrix[row + 1][3] = p1[0];
        matrix[row + 1][4] = p1[1];
        matrix[row + 1][5] = 1;
        matrix[row + 1][6] = -p2[1] * p1[0];
        matrix[row + 1][7] = -p2[1] * p1[1];
        b[row + 1] = p2[1];
    }

    RealMatrix A = MatrixUtils.createRealMatrix(matrix);
    RealVector B = MatrixUtils.createRealVector(b);

    DecompositionSolver solver = new QRDecomposition(A).getSolver();
    RealVector solution = solver.solve(B);

    return solution.toArray();
  }

  //----------------------------------------------------------

  public static void write(BufferedImage image, File file) throws IOException {
    if (file.getName().endsWith(".png")) {
      writeImage(image, file, "PNG");
    }
    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
      writeImage(image, file, "JPG");
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

  public static void writeImage(BufferedImage image, File file, String format) throws IOException {
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      long writeDuration = System.currentTimeMillis();
      BufferedOutputStream imageOutputStream = new BufferedOutputStream(fileOutputStream);
      ImageIO.write(image, format, imageOutputStream);
      imageOutputStream.close();
      long duration = System.currentTimeMillis() - writeDuration;
      LOG.info("Writing \"" + file.getAbsolutePath() + "\" took " + duration + "ms., " + FileUtils.readableFileSize(file.length()));
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

  //---------------------------

  private static final String MARKER = "VPIN-STUDIO:File Type";

  /**
   * cf https://www.silverbaytech.com/2014/06/04/iiometadata-tutorial-part-3-writing-metadata/
   */
  public static void writePNG(BufferedImage image, File outputFile, String marker) throws IOException {
    IIOMetadataNode newMetadata = createMetadata(MARKER, marker);
    ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromBufferedImageType(image.getType());
    ImageOutputStream stream = null;
    try {
      Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("PNG");
      while(writers.hasNext()) {
          ImageWriter writer = writers.next();
          ImageWriteParam writeParam = writer.getDefaultWriteParam();
          IIOMetadata imageMetadata = writer.getDefaultImageMetadata(imageType, writeParam);
          if (!imageMetadata.isStandardMetadataFormatSupported()) {
              continue;
          }
          if (imageMetadata.isReadOnly()) {
              continue;
          }

          imageMetadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, newMetadata);

          IIOImage imageWithMetadata = new IIOImage(image, null, imageMetadata);

          stream = ImageIO.createImageOutputStream(outputFile);
          writer.setOutput(stream);
          writer.write(null, imageWithMetadata, writeParam);
      }
    }
    finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  private static IIOMetadataNode createMetadata(String name, String value) {
    IIOMetadataNode marker = new IIOMetadataNode("TextEntry");
    marker.setAttribute("keyword", name);
    marker.setAttribute("value", value);

    IIOMetadataNode text = new IIOMetadataNode("Text");
    text.appendChild(marker);

    IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
    root.appendChild(text);
    return root;
  }

  //---

  public static File backupPNGByMarker(File target, String marker) {
    if (target.exists()) {
      File backup = uniqueAssetByMarker(target, marker);
      // when no marker and target exists, backup forcibly doesn't exist
      if (backup.exists()) {
        // when backup exists, it has been identified by marker, then returns it
        return backup;
      }
      // else, rename target to backup
      if (!target.renameTo(backup)) {
        LOG.error("Cannot rename {} to {}, existing file will be overwritten", target, backup);
      }
    }
    return target;
  }

  /**
   * Like uniqueAsset but identify an already existing file by a marker
   * In that case, return that file
   */
  public static File uniqueAssetByMarker(File target, String marker) {
    int index = 1;
    String originalBaseName = FilenameUtils.getBaseName(target.getName());
    String suffix = FilenameUtils.getExtension(target.getName());

    while (target.exists()) {
      // detect previously marked file
      if (marker != null) {
        String mark = getAttribute(target, MARKER);
        if (marker.equals(mark)) {
          return target;
        }
      }
      String segment = String.format("%02d", index++);
      target = new File(target.getParentFile(), originalBaseName + segment + "." + suffix);
    }
    return target;
  }

  /**
   * cf https://www.silverbaytech.com/2014/05/29/iiometadata-tutorial-part-2-retrieving-image-metadata/
   */
  private static String getAttribute(File file, String marker) {
    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("PNG");
    while (readers.hasNext()) {
      ImageReader reader = readers.next();
      ImageReaderSpi spi = reader.getOriginatingProvider();
      if (spi.isStandardImageMetadataFormatSupported()) {

        try (ImageInputStream stream = ImageIO.createImageInputStream(file)) {
          reader.setInput(stream, true);
          IIOMetadata metadata = reader.getImageMetadata(0);
          Node parent = metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
          // Get first Text element
          Element text = getChildElement(parent, "Text");
          if (text != null) {
            // Then look at TextEntry element
            NodeList children = text.getChildNodes();
            int count = children.getLength();
            for (int i = 0; i < count; i++) {
              Node child = children.item(i);
              if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("TextEntry")) {
                Element textentry = (Element) child;
                // is it our marker ?
                if (marker.equals(textentry.getAttribute("keyword"))) {
                  return textentry.getAttribute("value");
                }
              }
            }
          }
        }
        catch (IOException ioe) {
          LOG.error("Error while parsing image: {}", ioe.getMessage());
        }
      }
    }
    return null;
  }

  private static Element getChildElement(Node parent, String name) {
    NodeList children = parent.getChildNodes();
    int count = children.getLength();
    for (int i = 0; i < count; i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(name)) {
        return (Element)child;
      }
    }
    return null;
  }

  //---------------------------------------

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

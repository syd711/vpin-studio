package de.mephisto.vpin.commons.utils;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import de.mephisto.vpin.commons.fx.ServerFX;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jspecify.annotations.Nullable;


import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;

public class CommonImageUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Image setClippedImage(ImageView imageView, int radius) {
    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
        imageView.getFitWidth(), imageView.getFitHeight()
    );
    clip.setArcWidth(radius);
    clip.setArcHeight(radius);
    imageView.setClip(clip);

    // snapshot the rounded image.
    SnapshotParameters parameters = new SnapshotParameters();
    parameters.setFill(javafx.scene.paint.Color.TRANSPARENT);
    WritableImage clipped = imageView.snapshot(parameters, null);

    // remove the rounding clip so that our effect can show through.
    imageView.setClip(null);

    // store the rounded image in the imageView.
    imageView.setImage(clipped);
    return clipped;
  }

  public static Image createAvatar(String initials) {
    try {
      BufferedImage image = ImageIO.read(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      Graphics2D g = (Graphics2D) image.getGraphics();
      setRendingHints(g);

      int fontSize = 40;
      g.setFont(new Font("System", 1, 42));
      g.setColor(Color.BLACK);

      int y = image.getHeight() / 2 + 14;
      int x = image.getWidth() / 2 - g.getFontMetrics().stringWidth(initials) / 2;
      g.drawString(initials, x, y);


      return SwingFXUtils.toFXImage(image, null);
    } catch (IOException e) {
      LOG.error("Failed to generate avatar image: " + e.getMessage(), e);
    }
    return null;
  }

  public static Image createAvatarFromBytes(byte[] data) {
    try {
      BufferedImage image = ImageIO.read(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      Graphics2D g = (Graphics2D) image.getGraphics();
      setRendingHints(g);

      int offset = 24;
      Image fxImage = new Image(new ByteArrayInputStream(data));
      BufferedImage avatarImage = SwingFXUtils.fromFXImage(fxImage, null);
      avatarImage = resizeImage(avatarImage, image.getWidth() - offset, image.getHeight() - offset);
      int width = avatarImage.getWidth();
      g.setClip(new Ellipse2D.Float(offset/2, offset/2, width, width));
      g.drawImage(avatarImage, offset/2, offset/2, width, width, null);

      return SwingFXUtils.toFXImage(image, null);
    } catch (Exception e) {
      LOG.error("Failed to generate avatar image: " + e.getMessage(), e);
    }
    return null;
  }

  public static Image createAvatarFromUrl(String url) {
    try {
      BufferedImage image = ImageIO.read(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      Graphics2D g = (Graphics2D) image.getGraphics();
      setRendingHints(g);

      int offset = 24;
      Image fxImage = new Image(url);
      BufferedImage avatarImage = SwingFXUtils.fromFXImage(fxImage, null);
      avatarImage = resizeImage(avatarImage, image.getWidth() - offset, image.getHeight() - offset);
      int width = avatarImage.getWidth();
      g.setClip(new Ellipse2D.Float(offset/2, offset/2, width, width));
      g.drawImage(avatarImage, offset/2, offset/2, width, width, null);

      return SwingFXUtils.toFXImage(image, null);
    } catch (Exception e) {
      LOG.error("Failed to generate avatar image: " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * Enables the anti aliasing for fonts
   */
  public static void setRendingHints(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
  }

  @SuppressWarnings("unused")
  public static BufferedImage loadBackground(File file) throws Exception {
    if (!file.exists()) {
      throw new FileNotFoundException("File not found " + file.getAbsolutePath());
    }
    return ImageIO.read(file);
  }

  @SuppressWarnings("unused")
  public static BufferedImage rotateLeft(BufferedImage image) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    return create(image, -Math.PI / 2, gc);
  }

  @SuppressWarnings("unused")
  public static BufferedImage rotateRight(BufferedImage image) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    return create(image, Math.PI / 2, gc);
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

  @SuppressWarnings("unused")
  public static void setDefaultColor(Graphics g, String fontColor) {
    Color decode = Color.decode(fontColor);
    g.setColor(decode);
    setRendingHints(g);
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  public static void drawBorder(BufferedImage image, int strokeWidth) {
    if (strokeWidth > 0) {
      Graphics2D graphics = (Graphics2D) image.getGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setStroke(new BasicStroke(strokeWidth));
      int width = image.getWidth();
      int height = image.getHeight();

      graphics.setColor(Color.WHITE);
      graphics.drawRect(strokeWidth / 2, strokeWidth / 2, width - strokeWidth, height - strokeWidth);
    }
  }

  public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    return Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
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

    return image.getSubimage(x, y, targetWidth, targetHeight);
  }

  @SuppressWarnings("unused")
  public static BufferedImage blurImage(BufferedImage originalImage, int radius) {
      Planar<GrayF32> input = ConvertBufferedImage.convertFrom(originalImage, true, ImageType.pl(3, GrayF32.class));
      Planar<GrayF32> output = input.createSameShape();
      BlurImageOps.gaussian(input, output, -1, radius, null);
      return ConvertBufferedImage.convertTo(output, null, true);
  }

  public static void write(BufferedImage image, File file) throws IOException {
    if (file.getName().endsWith(".png")) {
      writePNG(image, file);
    }
    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
      writeJPG(image, file);
    }
  }

  private static void writeJPG(BufferedImage image, File file) throws IOException {
    long writeDuration = System.currentTimeMillis();
    BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    ImageIO.write(image, "JPG", imageOutputStream);
    imageOutputStream.close();
    long duration = System.currentTimeMillis() - writeDuration;
    LOG.info("Writing " + file.getAbsolutePath() + " took " + duration + "ms.");
  }

  private static void writePNG(BufferedImage image, File file) throws IOException {
    long writeDuration = System.currentTimeMillis();
    BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    ImageIO.write(image, "PNG", imageOutputStream);
    imageOutputStream.close();
    long duration = System.currentTimeMillis() - writeDuration;
    LOG.info("Writing " + file.getAbsolutePath() + " took " + duration + "ms.");
  }

    /**
     * Loads a JavaFX {@link Image} from an HTTP/HTTPS URL, with transparent
     * handling for servers that return WebP data despite a {@code .png} extension
     * or {@code image/png} Content-Type header.
     *
     * <p>The response bytes are fetched first so the format can be sniffed before
     * handing off to the appropriate decoder:
     * <ul>
     *   <li>WebP ({@code RIFF....WEBP} header): decoded via
     *       {@link ImageIO} using the TwelveMonkeys {@code imageio-webp} plugin,
     *       then converted to a JavaFX {@link Image} via {@link SwingFXUtils}.</li>
     *   <li>Everything else: passed directly to {@code new Image(InputStream)},
     *       which handles PNG, JPEG, GIF, and APNG via the registered loaders.</li>
     * </ul>
     *
     * @param -url the image URL (HTTP or HTTPS)
     * @return the decoded {@link Image}, or {@code null} if loading failed
     *         (error is logged; the caller need not log again)
     */

    @Nullable
    public static Image loadImageFromUrl(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(30_000);
            conn.setRequestProperty("Accept", "image/*");

            try (InputStream is = conn.getInputStream()) {
                byte[] bytes = is.readAllBytes();

                if (isWebP(bytes)) {
                    LOG.info("Detected WebP content at URL, decoding via TwelveMonkeys ImageIO: {}", url);
                    BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (buffered == null) {
                        LOG.error("WebP decode returned null for URL: {}", url);
                        return null;
                    }
                    return SwingFXUtils.toFXImage(buffered, null);
                }

                // All other formats — delegate to JavaFX's registered image loaders
                return new Image(new ByteArrayInputStream(bytes));
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            LOG.error("Failed to load image from URL {}: {}", url, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns {@code true} if {@code bytes} begins with a WebP file header
     * ({@code RIFF} at offset 0, {@code WEBP} at offset 8).
     */
    private static boolean isWebP(byte[] bytes) {
        return bytes.length >= 12
                && bytes[0]  == (byte) 'R'
                && bytes[1]  == (byte) 'I'
                && bytes[2]  == (byte) 'F'
                && bytes[3]  == (byte) 'F'
                && bytes[8]  == (byte) 'W'
                && bytes[9]  == (byte) 'E'
                && bytes[10] == (byte) 'B'
                && bytes[11] == (byte) 'P';
    }
}

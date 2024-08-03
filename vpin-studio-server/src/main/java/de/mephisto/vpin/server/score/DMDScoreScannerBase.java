package de.mephisto.vpin.server.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.server.system.SystemService;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A simple Processor that writes frames in a file
 */
public abstract class DMDScoreScannerBase implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreScannerBase.class);

  public static String TESSERACT_FOLDER = SystemService.RESOURCES + "tessdata";

  protected File folder;

  // Resize images
  protected int scale = 4;
  // Add border arround to avoid text too closed to borders
  protected int border = 2;
  // Apply a blur effect
  protected int radius = 1;
  

  @Override
  public void onFrameStart(String gameName) {
    try {
      this.folder = //Files.createTempDirectory(gameName + "_").toFile();
        new File("c:/temp/" + gameName);
      this.folder.mkdirs();
      LOG.info("Use temp folder to store images : " + folder.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Cannot create temp folder");
    }    
  }

 @Override
  public void onFrameStop(String gameName) {
    //FIXME delete folder
  }


  @Override
  public String onFrameReceived(Frame frame, int[] palette) {
    int W = (frame.getWidth() + 2 * border) * scale;
    int H = (frame.getHeight() + 2 * border) * scale;
    int size = 2 * radius + 1;
    size *= size;

    // Apply the transformations, add an empty border, rescale and recolor, then blur
    byte[] pixels = rescale(frame.getPlane(), frame.getWidth(), frame.getHeight(), border, scale, (byte) size);
    pixels = blur(pixels, W, H, radius);
  
    return extractText(frame, pixels, W, H);
  }

  protected abstract String extractText(Frame frame, byte[] pixels, int W, int H);

  //--------------------

  protected byte[] rescale(byte[] plane, int width, int height, int border, int scale, byte black) {
    return crop(plane, width, height, border, 0, width, 0, height, scale, black);
  }

  protected byte[] crop(byte[] plane, int width, int height, int _border, 
    int xFrom, int xTo, int yFrom, int yTo, int _scale, byte black) {
          
    int newWith = xTo - xFrom + 2 * _border;
    int newHeight = yTo - yFrom + 2 * _border;
    byte[] scaledPlane = new byte[newWith * _scale * newHeight * _scale];

    for (int y = yFrom; y < yTo; y++) {
      for (int x = xFrom; x < xTo; x++) {
        for (int dy = 0; dy < _scale; dy++) {
          for (int dx = 0; dx < _scale; dx++) {
            scaledPlane[ ((y - yFrom + _border) * _scale + dy) * newWith * _scale + (x - xFrom + _border) * _scale + dx] = (plane[y * width + x] == 0 ? 0: black);
          }
        }
      }
    }
    return scaledPlane;
  }

  protected byte[] blur(byte[] pixels, int width, int height, int _radius) {
    int size = _radius * 2 + 1;
    size *= size;
    byte[] outPixels = new byte[pixels.length];

    if (_radius > 0) {
      float f = 1.0f / size;

      int index = 0;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          float color = 0;

          for (int row = -_radius; row <= _radius; row++) {
            int iy = y + row;
            if (iy < 0 || iy >= height) continue;

            for (int col = -_radius; col <= _radius; col++) {
              int ix = x + col;
              if (ix < 0 || ix >= width) continue;

              color += f * pixels[iy*width+ix];
            }
          }
          double gray = 255.0 - 255.0 * (color<= size ? color : size) / size;
          outPixels[index++] = (byte) gray;
        }
      }
		}
    else {
      for (int index = 0; index < width * height; index++) {
        double gray = 255.0 - 255.0 * pixels[index] / size;
        outPixels[index] = (byte) gray;
      }
    }
    return outPixels;
  }

  //-----------------------------------
  
  protected File saveImage(byte[] pixels, int width, int height, PixelFormat<ByteBuffer> palette, String filename) {
    // generate our new image
    WritableImage img = new WritableImage(width, height);
    PixelWriter pw = img.getPixelWriter();
    pw.setPixels(0, 0, width, height, palette, pixels, 0, width);


    // save it to file
    File imgFile = new File("c:/temp/test", filename + ".png");

    try {
      java.awt.image.BufferedImage i = SwingFXUtils.fromFXImage(img, null);
      int bpp = i.getColorModel().getPixelSize();
      System.out.println("BPP=" + bpp);

      ImageIO.write(i, "png", imgFile);
    } catch (IOException e) {
      LOG.error("cannot generate image " + filename);
    }

    return imgFile;
  }

  protected PixelFormat<ByteBuffer> generateBlurPalette(int size) {
    // Create the blur B&W ARGB palette using same radius
    int[] bwPalette = new int[size + 1];
    for (int i = 0; i <= size; i++) {
      int colorComponent = (size - i) * 255 / size;
      bwPalette[i] = (255 << 24) | (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    PixelFormat<ByteBuffer> format = PixelFormat.createByteIndexedInstance(bwPalette);
    return format;
  }
}

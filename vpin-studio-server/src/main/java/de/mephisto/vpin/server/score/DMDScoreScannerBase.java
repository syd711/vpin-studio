package de.mephisto.vpin.server.score;

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
 * A simple Processor that writes frames in a file
 */
public abstract class DMDScoreScannerBase implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreScannerBase.class);

  protected File folder;

  // Resize images
  protected int SCALE = 3;
  // Add border arround to avoid text too closed to borders
  protected int BORDER = 2;
  // Apply a blur effect
  protected int RADIUS = 1;
    

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
  public String onFrameReceived(Frame frame) {
    int W = (frame.getWidth() + 2 * BORDER) * SCALE;
    int H = (frame.getHeight() + 2 * BORDER) * SCALE;
    int size = 2 * RADIUS + 1;
    size *= size;
    byte idxBlank = getBlankIndex(frame.getPalette());

    // Apply the transformations, add an empty border, rescale and recolor, then blur    
    byte[] pixels = rescale(frame.getPlane(), frame.getWidth(), frame.getHeight(), BORDER, SCALE, idxBlank, (byte) size);
    pixels = blur(pixels, W, H, RADIUS);
  
    return extractText(frame, "", pixels, W, H, size);
  }

  protected abstract String extractText(Frame frame, String name, byte[] pixels, int w, int h, int size);

  //--------------------

  protected byte[] rescale(byte[] plane, int width, int height, int border, int scale, byte idxBlank, byte black) {
    return crop(plane, width, height, border, 0, width, 0, height, scale, idxBlank, black);
  }

  protected byte[] crop(byte[] plane, int width, int height, int _border, 
    int xF, int xT, int yF, int yT, int _scale, byte idxBlank, byte black) {
          
    int newWith = xT - xF + 2 * _border;
    int newHeight = yT - yF + 2 * _border;
    byte[] scaledPlane = new byte[newWith * _scale * newHeight * _scale];

    for (int y = yF; y < yT; y++) {
      for (int x = xF; x < xT; x++) {
        for (int dy = 0; dy < _scale; dy++) {
          for (int dx = 0; dx < _scale; dx++) {
            scaledPlane[ ((y - yF + _border) * _scale + dy) * newWith * _scale + (x - xF + _border) * _scale + dx] = (plane[y * width + x] == idxBlank ? 0 : black);
          }
        }
      }
    }
    return scaledPlane;
  }

  protected boolean removeImages(byte[] plane, int w, int h, int xF, int xT, int yF, int yT, byte blank) {
    boolean haveImage = false;
    for (int y = yF; y < yT; y++) {
      for (int x = xF; x < xT; x++) {
        if (checkImage(plane, new boolean[plane.length], w, xF, xT, yF, yT, x, y, (byte) -1, blank)) {
          // remove image, call same check         
          removeImage(plane, w, xF, xT, yF, yT, x, y, blank);
          haveImage = true;
        }
      }
    }
    removeSinglePixels(plane, w, h, xF, xT, yF, yT, blank);
    return haveImage;
  }


  private boolean checkImage(byte[] plane, boolean[] checked, int w, int xF, int xT, int yF, int yT, int x, int y, byte firstColor, byte blank) {
    byte c = plane[y * w + x];
    if (c == blank) {
      return false;
    }
    // if already checked, ignore the pixel
    if (checked[y * w + x]) {
      return false;
    }
    // flag to avoid circular loop
    checked[y * w + x] = true;

    // first pixel so keep color
    if (firstColor < 0) {
      firstColor = c;
    }
    // else it is an adjacent pixel, then if of different color, it is in an image
    else if (c != firstColor) {
      return true;
    }
    // else first pixel or same color, check adjacent pixels

    // check right
    if ((x + 1 < xT) && checkImage(plane, checked, w, xF, xT, yF, yT, x + 1, y, firstColor, blank)) {
      return true;
    }
    // check 
    if ((y + 1 < yT) && checkImage(plane, checked, w, xF, xT, yF, yT, x, y + 1, firstColor, blank)) {
      return true;
    }
    if ((x > xF) && checkImage(plane, checked, w, xF, xT, yF, yT, x - 1, y, firstColor, blank)) {
      return true;
    }
    if ((y > yF) && checkImage(plane, checked, w, xF, xT, yF, yT, x, y - 1, firstColor, blank)) {
      return true;
    }
    return false;
  }

  private void removeImage(byte[] plane, int w, int xF, int xT, int yF, int yT, int x, int y, byte blank) {
    if (plane[y * w + x] != blank) {
      // blank pixel first to avoid circular loop
      plane[y * w + x] = blank;

      // remove all adjacent non blank pixels too
      if (x + 1 < xT) {
        removeImage(plane, w, xF, xT, yF, yT, x + 1, y, blank);
      }
      if (y + 1 < yT) {
        removeImage(plane, w, xF, xT, yF, yT, x, y + 1, blank);
      }
      if (x > xF) {
        removeImage(plane, w, xF, xT, yF, yT, x - 1, y, blank);
      }
      if (y > yF) {
        removeImage(plane, w, xF, xT, yF, yT, x, y - 1, blank);
      }
    }
  }

  private void removeSinglePixels(byte[] plane, int w, int h, int xF, int xT, int yF, int yT, byte blank) {
    for (int y = yF; y < yT; y++) {
      for (int x = xF; x < xT; x++) {
        if (plane[y * w + x] != blank) {
          boolean hasNonAdjacentColor = false;
          for (int row = -1; row <= 1; row++) {
            int iy = y + row;
            if (iy < yF || iy >= yT) continue;

            for (int col = -1; col <= 1; col++) {
              int ix = x + col;
              if (ix < xF || ix >= xT) continue;

              hasNonAdjacentColor |= (col!=0 || row!=0) && plane[iy*w+ix] != blank;
            }
          }
          if (!hasNonAdjacentColor) {
            plane[y * w + x] = blank;
          }
        }
      }
    }
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

  protected byte getBlankIndex(int[] palette) {
    for (int i = 0; i < palette.length; i++) {
      if (palette[i] == 0) {
        return (byte) i;
      }
    }
    return -1;
  }

  protected File saveImage(byte[] pixels, int width, int height, PixelFormat<ByteBuffer> palette, File imgFile) {
    // generate our new image
    WritableImage img = new WritableImage(width, height);
    PixelWriter pw = img.getPixelWriter();
    pw.setPixels(0, 0, width, height, palette, pixels, 0, width);

    try {
      ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", imgFile);
    } catch (IOException e) {
      LOG.error("cannot generate image " + imgFile.getName());
    }

    return imgFile;
  }

  protected PixelFormat<ByteBuffer> generatePalette(int[] palette) {
    // Create an ARGB palette from given palette
    int[] argbPalette = new int[palette.length];
    for (int i = 0; i < palette.length; i++) {
      argbPalette[i] = (255 << 24) | palette[i];
    }
    PixelFormat<ByteBuffer> format = PixelFormat.createByteIndexedInstance(argbPalette);
    return format;
  }

  protected PixelFormat<ByteBuffer> generateBlurPalette(int size) {

    size = 255;

    // Create the blur B&W ARGB palette using same radius
    int[] bwPalette = new int[size + 1];
    for (int colorComponent = 0; colorComponent <= size; colorComponent++) {
      bwPalette[colorComponent] = (255 << 24) | (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    PixelFormat<ByteBuffer> format = PixelFormat.createByteIndexedInstance(bwPalette);
    return format;
  }
}

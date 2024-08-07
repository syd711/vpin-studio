package de.mephisto.vpin.server.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

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
public abstract class DMDScoreProcessorBase implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorBase.class);

  /** Switch to true for debugging as images are generated in C:/temp/ and folder is not deleted  */
  protected boolean DEV_MODE = true;

  /** Folder where all images are generated, essentially for debug purposes */
  protected File folder;

  @Override
  public void onFrameStart(String gameName) {
    try {
      this.folder = DEV_MODE ? new File("c:/temp/" + gameName) :
        Files.createTempDirectory("vpin_" + gameName + "_").toFile();

      folder.mkdirs();
      LOG.info("Use temp folder to store images : " + folder.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Cannot create temp folder");
    }    
  }

  @Override
  public void onFrameStop(String gameName) {
    if (!DEV_MODE) {
      folder.delete();
    }
  }

  //--------------------

  protected byte[] rescale(byte[] plane, int width, int height, int border, int scale, byte idxBlank, byte black) {
    return crop(plane, width, height, 0, width, 0, height, border, scale, idxBlank, black);
  }

  protected byte[] crop(byte[] plane, int width, int height, 
    int xF, int xT, int yF, int yT, int border, int scale, byte blank, byte black) {
          
    int newWith = xT - xF + 2 * border;
    int newHeight = yT - yF + 2 * border;
    byte[] scaledPlane = new byte[newWith * scale * newHeight * scale];

    for (int y = yF; y < yT; y++) {
      for (int x = xF; x < xT; x++) {
        for (int dy = 0; dy < scale; dy++) {
          for (int dx = 0; dx < scale; dx++) {
            scaledPlane[ ((y - yF + border) * scale + dy) * newWith * scale + (x - xF + border) * scale + dx] = (plane[y * width + x] == blank ? 0 : black);
          }
        }
      }
    }
    return scaledPlane;
  }

  protected boolean removeImages(byte[] plane, int w, int xF, int xT, int yF, int yT, byte blank) {
    boolean haveImage = false;
    for (int y = yF; y < yT; y++) {
      for (int x = xF; x < xT; x++) {
        if (checkImage(plane, new boolean[plane.length], w, xF, xT, yF, yT, x, y, 0, (byte) -1, (byte) -1, blank) > THRESHOLD_IMAGE_X) {
          // remove image, call same check         
          removeImage(plane, w, xF, xT, yF, yT, x, y, blank);
          haveImage = true;
        }
      }
    }
    removeSinglePixels(plane, w, xF, xT, yF, yT, blank);
    return haveImage;
  }

  private static int THRESHOLD_IMAGE_X = 20;

  private int checkImage(byte[] plane, boolean[] checked, int w, int xF, int xT, int yF, int yT, int x, int y, int nbX, byte firstColor, byte secondColor, byte blank) {
    byte c = plane[y * w + x];
    if (c == blank) {
      return 0;
    }
    // if already checked, ignore the pixel
    if (checked[y * w + x]) {
      return 0;
    }
    // flag to avoid circular loop
    checked[y * w + x] = true;

    // first time we detect the color so keep color, else if of different color, it is in an image
    if (firstColor < 0) {
      firstColor = c;
    }
    else if (c != firstColor) {
      if (secondColor < 0) {
        secondColor = c;
      }
      else if (c != secondColor) {
        return plane.length;
      }
    }
    // else color already known, check adjacent pixels
    nbX++;

    if (nbX >  THRESHOLD_IMAGE_X) {
      return nbX;
    }

    // check right
    if ((x + 1 < xT) && (nbX += checkImage(plane, checked, w, xF, xT, yF, yT, x + 1, y, nbX, firstColor, secondColor, blank)) > THRESHOLD_IMAGE_X) {
      return nbX;
    }
    if ((x > xF) && (nbX += checkImage(plane, checked, w, xF, xT, yF, yT, x - 1, y, nbX, firstColor, secondColor, blank)) > THRESHOLD_IMAGE_X) {
      return nbX;
    }

    // check 
    if ((y + 1 < yT) && (nbX = checkImage(plane, checked, w, xF, xT, yF, yT, x, y + 1, 0, firstColor, secondColor, blank)) > THRESHOLD_IMAGE_X) {
      return nbX;
    }
    if ((y > yF) && (nbX = checkImage(plane, checked, w, xF, xT, yF, yT, x, y - 1, 0, firstColor, secondColor, blank)) > THRESHOLD_IMAGE_X) {
      return nbX;
    }
    // not an image
    return 0;
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

  private void removeSinglePixels(byte[] plane, int w, int xF, int xT, int yF, int yT, byte blank) {
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

  protected byte[] blur(byte[] pixels, int width, int height, int radius) {
    int size = radius * 2 + 1;
    size *= size;
    byte[] outPixels = new byte[pixels.length];

    if (radius > 0) {
      float f = 1.0f / size;

      int index = 0;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          float color = 0;

          for (int row = -radius; row <= radius; row++) {
            int iy = y + row;
            if (iy < 0 || iy >= height) continue;

            for (int col = -radius; col <= radius; col++) {
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

  /**
   * [20736,16777215,65280,0]
   * [0,16711680,0,0]
   * [4868863,0,7829367,0]
   * [16767492,0,16711680,151]
   * [16760962,8405056,0,11310948]
   */
  public byte getBlankIndex(int[] palette) {
    boolean hasColor = false;
    // start from end, skip trailing 0 and take the 0 index
    for (int i = palette.length - 1; i >= 0; i--) {
      if (palette[i] != 0) {
        hasColor = true;
      }
      else if (hasColor && palette[i] == 0) {
        return (byte) i;
      }
    }
    // fallback when no 0 in palette, take first color
    return 0;
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

  protected PixelFormat<ByteBuffer> generateBlurPalette() {
    int size = 255;
    // Create the blur B&W ARGB palette using same radius
    int[] bwPalette = new int[size + 1];
    for (int colorComponent = 0; colorComponent <= size; colorComponent++) {
      bwPalette[colorComponent] = (255 << 24) | (colorComponent << 16) | (colorComponent << 8) | colorComponent;
    }
    PixelFormat<ByteBuffer> format = PixelFormat.createByteIndexedInstance(bwPalette);
    return format;
  }
}

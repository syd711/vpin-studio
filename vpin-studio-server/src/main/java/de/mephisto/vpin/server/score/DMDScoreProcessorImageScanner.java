package de.mephisto.vpin.server.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.system.SystemService;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreProcessorImageScanner implements DMDScoreProcessor {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorImageScanner.class);

  public static String TESSERACT_FOLDER = SystemService.RESOURCES + "tesseract";

  private File folder;

  // Resize images
  protected int scale = 4;
  // Add border arround to avoid text too closed to borders
  protected int border = 4;
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
  public String onFrameReceived(Frame frame, int[] palette, int width, int height) {
    int W = (width + 2 * border) * scale;
    int H = (height + 2 * border) * scale;
    int size = 2 * radius + 1;
    size *= size;

    // Apply the transformations, add an empty border, rescale and recolor, then blur
    byte[] pixels = rescale(frame.getPlane(), width, height, border, scale, (byte) size);
    if (radius > 0) {
      pixels = blur(pixels, W, H, radius);
    }
  
    PixelFormat<ByteBuffer> format = generateBlurPalette(size);

    File img = saveImage(pixels, W, H, format, Integer.toString(frame.getTimeStamp()));
    return extractText(img);
    //FIXME img.delete(); or leave images until full folder is deleted but mind disk size 
  }

  @Override
  public void onFrameStop(String gameName) {
    //FIXME delete folder
  }

  //--------------------

  protected File saveImage(byte[] pixels, int width, int height, PixelFormat<ByteBuffer> palette, String filename) {
    // generate our new image
    WritableImage img = new WritableImage(width, height);
    PixelWriter pw = img.getPixelWriter();
    pw.setPixels(0, 0, width, height, palette, pixels, 0, width);

    // save it to file
    File imgFile = new File(folder, filename + ".png");

    try {
      ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", imgFile);
    } catch (IOException e) {
      LOG.error("cannot generate image " + filename);
    }

    return imgFile;
  }

  protected String extractText(File imgFile) {

    File outFile = new File(folder, "out");

    try {
      File tessExe = new File(TESSERACT_FOLDER, "tesseract.exe");

      List<String> commands = Arrays.asList(tessExe.getAbsolutePath(), imgFile.getAbsolutePath(), outFile.getAbsolutePath(), "--psm", "6");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(folder);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (StringUtils.isNotEmpty(standardErrorFromCommand)) {
        LOG.error("Ignored error executing Tesseract; " + standardErrorFromCommand);
      }
      if (StringUtils.isNotEmpty(standardOutputFromCommand)) {
        LOG.info("Warning executing Tesseract: " + standardOutputFromCommand);
      }

      outFile = new File(folder, "out.txt");
      if (outFile.exists()) {
        String content = Files.readString(outFile.toPath());
        LOG.info("Text recognized from " + imgFile.getName() + " :\n" + content);
        outFile.delete();

        return content;
      }  
    }
    catch (Exception e) {
      LOG.error("Error executing Tesseract: " + e.getMessage());
    }    
    return null;
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

  protected byte[] rescale(byte[] plane, int width, int height, int border, int scale, byte black) {
    return crop(plane, width, height, border, 0, width, 0, height, scale, black);
  }

  protected byte[] crop(byte[] plane, int width, int height, int border, 
    int xFrom, int xTo, int yFrom, int yTo, int scale, byte black) {
          
    int newWith = xTo - xFrom + 2 * border;
    int newHeight = yTo - yFrom + 2 * border;
    byte[] scaledPlane = new byte[newWith * scale * newHeight * scale];

    for (int y = yFrom; y < yTo; y++) {
      for (int x = xFrom; x < xTo; x++) {
        for (int dy = 0; dy < scale; dy++) {
          for (int dx = 0; dx < scale; dx++) {
            scaledPlane[ ((y - yFrom + border) * scale + dy) * newWith * scale + (x - xFrom + border) * scale + dx] = (plane[y * width + x] == 0 ? 0: black);
          }
        }
      }
    }
    return scaledPlane;
  }

  protected byte[] blur(byte[] pixels, int width, int height, int radius) {
    int size = radius * 2 + 1;
    size *= size;
    float f = 1.0f / size;

    byte[] outPixels = new byte[pixels.length];
    int index = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
        float color = 0;

        for (int row = -radius; row <= radius; row++) {
					int iy = y+row;
					if (iy < 0 || iy >= height) continue;

					for (int col = -radius; col <= radius; col++) {
            int ix = x+col;
            if (ix < 0 || ix >= width) continue;

            color += f * pixels[iy*width+ix];
					}
				}
				outPixels[index++] = (byte) (color<= size ? color : size);
			}
		}
    return outPixels;
  }
}

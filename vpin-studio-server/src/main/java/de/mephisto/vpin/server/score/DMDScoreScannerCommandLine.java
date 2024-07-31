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
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreScannerCommandLine extends DMDScoreScannerBase {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreScannerCommandLine.class);

  protected File folder;

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
  protected String extractText(Frame frame, byte[] pixels, int W, int H) {
    int size = 2 * radius + 1;
    size *= size;
    PixelFormat<ByteBuffer> format = generateBlurPalette(size);

    File img = saveImage(pixels, W, H, format, Integer.toString(frame.getTimeStamp()));
    return extractText(img);
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
}

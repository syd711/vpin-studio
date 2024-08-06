package de.mephisto.vpin.server.score;

import java.io.File;
import java.nio.ByteBuffer;

import javafx.scene.image.PixelFormat;

/**
 * A Processor that dump frames in a folder as png
 */
public class DMDScoreProcessorImageDump extends DMDScoreScannerBase {

  @Override
  public String onFrameReceived(Frame frame) {

    File imgFile = new File(folder, frame.getTimeStamp() + ".png");

    PixelFormat<ByteBuffer> format = generatePalette(frame.getPalette());
    saveImage(frame.getPlane(), frame.getWidth(), frame.getHeight(), format, imgFile);

    return null;
  }

  @Override
  protected String extractText(Frame frame, String name, byte[] pixels, int w, int h, int size) {
    return null;
  }

}

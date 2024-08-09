package de.mephisto.vpin.server.score;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import javafx.scene.image.PixelFormat;

/**
 * A Processor that dump frames in a folder as png
 */
public class DMDScoreProcessorImageDump extends DMDScoreProcessorBase {

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    File imgFile = new File(folder, frame.getTimeStamp() + ".png");
    PixelFormat<ByteBuffer> format = generatePalette(frame.getPalette());
    saveImage(frame.getPlane(), frame.getWidth(), frame.getHeight(), format, imgFile);
  }

}

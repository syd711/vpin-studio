package de.mephisto.vpin.ui.apng.image;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import de.mephisto.vpin.ui.apng.chunks.ApngChunkDataInputStream;
import de.mephisto.vpin.ui.apng.chunks.ApngDecoder;
import de.mephisto.vpin.ui.apng.chunks.ApngFrameControl;
import de.mephisto.vpin.ui.apng.chunks.ApngHeader;
import de.mephisto.vpin.ui.apng.chunks.ApngPalette;
import de.mephisto.vpin.ui.apng.image.ApngFrameDecoder;

public class ApngFrameDecoder implements Closeable {

  protected final ApngChunkDataInputStream stream;

  private int appFrameDecoded = 0;
  private ApngFrame prevImage = null;
  private ApngFrame currentImage = null;

  // FOR DEBUG PURPOSES: in order to understand how a specific frame is decoded, 
  // force max frame to that frame number and forceMaxLoops to 1, and the image will stop on that frame
  // override the number of frames in the PNG and stop parsing after forceMaxFrames. -1 values are for PROD use
  private int forceMaxFrames = -1;
  // override the number of loops in ACTL chunk
  private int forceMaxLoops = -1;

  // keep the current frameControl so that the disposal to be done after rendering this frame, can be applied before next image generation
  // disposeOp specifies how the output buffer should be changed at the end of the delay (before rendering the next frame).
  private ApngFrameControl currentFrameControl;


  public ApngFrameDecoder(InputStream input) throws IOException {
   this.stream = new ApngChunkDataInputStream(input);
  }

  public ApngFrame nextFrame() throws IOException {
    // interrupt the APNG sequence on a given frame
    if (forceMaxFrames > 0 && forceMaxFrames == appFrameDecoded) {
      return null;
    }

    // read the next image / frame
    byte[] frameData = stream.nextDat();
    // is null for non-animated PNG and when default image is not part of the animation
    ApngFrameControl frameControl = stream.getLastFrameControl();

    ApngDecoder decoder = new ApngDecoder(stream.getHeader(), stream.getTransparentColor());

    ApngFrame next = null;
    // case of first frame
    if (currentImage == null) {
      if (frameData == null) {
        throw new IOException("The image does not contain any frame");
      }

      ApngHeader header = stream.getHeader();
      // when frame is before first FrameControl, then the frame is the default image but not part of the animation
      if (frameControl == null) {
        // memorize the default image
        currentImage = new ApngFrame(header.getWidth(), header.getHeight(), decoder.bpp(), 0);
        
        // generate a dummy frameControl for decoding
        currentFrameControl = new ApngFrameControl(header.getWidth(), header.getHeight(), 0, 0, 
            0, 0, ApngFrameControl.DISPOSE_OP_BACKGROUND, ApngFrameControl.BLEND_OP_SOURCE);
        decoder.decode(currentImage, currentFrameControl, frameData, false);

        // dest is not set so that another frame is read
        next = nextFrame();
        // no more frame, just a default one, return it
        if (next == null) {
          appFrameDecoded++;
          return currentImage;
        }
        return next;
      }
      else {
        // default image is part of the animation
        next = new ApngFrame(header.getWidth(), header.getHeight(), decoder.bpp(), frameControl.getDelayMillis());
        // for the first frame, the two blend modes are functionally equivalent due to the clearing of the output buffer, so ignore
        decoder.decode(next, frameControl, frameData, false);
      }
    } else if (frameData != null) {
      // dispose the current image with the currentFrameControl
      next = decoder.dispose(currentImage, prevImage, currentFrameControl, frameControl.getDelayMillis());
      // secondary frames
      decoder.decode(next, frameControl, frameData, frameControl.getBlendOp() == ApngFrameControl.BLEND_OP_OVER);
    }

    // shift images
    if (next != null) {
      prevImage = currentImage;
      currentImage = next;
      currentFrameControl = frameControl;
      appFrameDecoded++;
     return next;
    }
    // else end of decoding, check integrity
    int availableFrames = getNbAvailableFrames();
      // if no ACTL chunk in the image, getNbAvailableFrames() return -1 and it is normal
    if (availableFrames > 0 && appFrameDecoded != availableFrames) {
      throw new IOException("Wrong number of frame, decoded " + appFrameDecoded + ", but available " + availableFrames);
    }
    return null;
  }

  public int getNbAvailableFrames() {
    return forceMaxFrames > 0 ? forceMaxFrames : stream.getAvailableFrames();
  }

  public int getColorType() {
    return stream.getColorType();
  }

  public boolean hasTransparency() {
    return stream.hasTransparency();
  }

  public byte[][] getPalette() {
    ApngPalette palette = stream.getPalette();
    return palette != null ? palette.asBytes() : null;
  }

  public int getAnimationNumPlays() {
    return forceMaxLoops > 0 ? forceMaxLoops : stream.getAnimationLoops();
  }

  public void close() throws IOException {
    stream.close();
  }
}
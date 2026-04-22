package de.mephisto.vpin.commons.fx.apng;

import com.sun.javafx.iio.*;
import com.sun.javafx.iio.ImageStorage.ImageType;
import com.sun.javafx.iio.common.*;

import de.mephisto.vpin.commons.fx.apng.chunks.ApngDecoder;
import de.mephisto.vpin.commons.fx.apng.image.ApngFrame;
import de.mephisto.vpin.commons.fx.apng.image.ApngFrameDecoder;

import java.io.*;
import java.nio.ByteBuffer;

public class ApngImageLoader extends ImageLoaderImpl {

  ApngFrameDecoder apngFrameDecoder = null;

  public ApngImageLoader(InputStream input) throws IOException {
    super(ApngDescriptor.getInstance());
    this.apngFrameDecoder = new ApngFrameDecoder(input);
  }

  public void dispose() {
    try {
      this.apngFrameDecoder.close();
    }
    catch(IOException ioe) {
    }
  }

  @Override
  public ImageFrame load(int imageIndex, double rWidth, double rHeight,
          boolean preserveAspectRatio, boolean smooth, float pixelScaleX, float pixelScaleY) throws IOException {

    ApngFrame frame = apngFrameDecoder.nextFrame();
    if (frame == null) {
      return null;
    }

    int[] outWH = ImageTools.computeDimensions(frame.getWidth(), frame.getHeight(), (int)rWidth, (int)rHeight, preserveAspectRatio);
    int targetWidth = outWH[0];
    int targetHeight = outWH[1];

    int delay = frame.getDelayMillis();
    int loop = apngFrameDecoder.getAnimationNumPlays();

    ImageMetadata metaData = new ImageMetadata(null, true, null, null, null, delay, loop, targetWidth, targetHeight, null, null, null);
    updateImageMetadata(metaData);

    ImageType imageType = colorTypeToImageType(apngFrameDecoder.getColorType());
    ImageFrame imgPNG = new ImageFrame(imageType, ByteBuffer.wrap(frame.getBytes()), frame.getWidth(), frame.getHeight(), frame.getStride(), pixelScaleX, metaData);

    if (frame.getWidth() != targetWidth || frame.getHeight() != targetHeight) {
      imgPNG = ImageTools.scaleImageFrame(imgPNG, targetWidth, targetHeight, smooth);
    }
    return imgPNG;
  }

  /**
   * Palette based image is not an option, as they are transformed on the fly by the decoder into PNG_COLOR_RGB or PNG_COLOR_RGB_ALPHA
   */
  private ImageType colorTypeToImageType(int colorType) {
    switch (colorType) {
      case ApngDecoder.PNG_COLOR_GRAY:
        return ImageType.GRAY;
      case ApngDecoder.PNG_COLOR_RGB:
        return ImageType.RGB;
      case ApngDecoder.PNG_COLOR_GRAY_ALPHA:
        return ImageType.GRAY_ALPHA;
      case ApngDecoder.PNG_COLOR_RGB_ALPHA:
        return ImageType.RGBA;
      default: // unreacheble
        throw new RuntimeException("not supported colorType " + colorType);
    }
  }
}

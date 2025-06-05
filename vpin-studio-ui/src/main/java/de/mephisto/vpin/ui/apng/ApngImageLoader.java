package de.mephisto.vpin.ui.apng;

import com.sun.javafx.iio.*;
import com.sun.javafx.iio.ImageStorage.ImageType;
import com.sun.javafx.iio.common.*;

import de.mephisto.vpin.ui.apng.chunks.ApngColorType;
import de.mephisto.vpin.ui.apng.chunks.ApngPalette;
import de.mephisto.vpin.ui.apng.image.ApngFrameDecoder;
import de.mephisto.vpin.ui.apng.image.ApngFrame;

import java.io.*;
import java.nio.ByteBuffer;

public class ApngImageLoader extends ImageLoaderImpl {

  ApngFrameDecoder apngFrameDecoder = null;

  public ApngImageLoader(InputStream input) throws IOException {
    super(ApngDescriptor.getInstance());
    this.apngFrameDecoder = ApngFrameDecoder.getDecoderFor(input);
  }

  public void dispose() {
    try {
      this.apngFrameDecoder.close();
    }
    catch(IOException ioe) {
    }
  }

  @Override
  public ImageFrame load(int imageIndex, int width, int height,
          boolean preserveAspectRatio, boolean smooth) throws IOException {

    ApngFrame frame = apngFrameDecoder.nextFrame();
    if (frame == null) {
      return null;
    }

    int[] outWH = ImageTools.computeDimensions(frame.getWidth(), frame.getHeight(), width, height, preserveAspectRatio);
    width = outWH[0];
    height = outWH[1];

    int delay = frame.getDelayMillis();
    int loop = apngFrameDecoder.getAnimationNumPlays();

    ImageMetadata metaData = new ImageMetadata(null, true, null, null, null, delay, loop, width, height, null, null, null);
    updateImageMetadata(metaData);

    ApngColorType colorType = apngFrameDecoder.getColorType();
    ImageType imageType = getType(colorType);

    ApngPalette pngPalette = apngFrameDecoder.getPalette();
    byte[][] palette = pngPalette != null ? pngPalette.asBytes() : null;

    ByteBuffer bb = ByteBuffer.wrap(frame.getBytes(colorType));

    ImageFrame imgPNG = new ImageFrame(imageType, bb, frame.getWidth(), frame.getHeight(), frame.getWidth() * colorType.getComponentsPerPixel(), palette, metaData);
    if (frame.getWidth() != width || frame.getHeight() != height) {
      imgPNG = ImageTools.scaleImageFrame(imgPNG, width, height, smooth);
    }
    return imgPNG;
  }

  private ImageStorage.ImageType getType(ApngColorType colorType) {
    switch (colorType) {
      case GREYSCALE:
        return ImageStorage.ImageType.GRAY;
      case TRUECOLOR:
        return ImageStorage.ImageType.RGB;
      case INDEXED:
        return ImageStorage.ImageType.PALETTE;
      case GREYSCALE_ALPHA:
        return ImageStorage.ImageType.GRAY_ALPHA;
      case TRUECOLOR_ALPHA:
        return ImageStorage.ImageType.RGBA;
      default: // unreacheble
          throw new RuntimeException();
    }
  }
}

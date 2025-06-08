package de.mephisto.vpin.ui.apng;

import com.sun.javafx.iio.*;
import com.sun.javafx.iio.ImageStorage.ImageType;
import com.sun.javafx.iio.common.*;

import de.mephisto.vpin.ui.apng.chunks.ApngDecoder;
import de.mephisto.vpin.ui.apng.image.ApngFrameDecoder;
import de.mephisto.vpin.ui.apng.image.ApngFrame;

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
  public ImageFrame load(int imageIndex, int rWidth, int rHeight,
          boolean preserveAspectRatio, boolean smooth) throws IOException {

    ApngFrame frame = apngFrameDecoder.nextFrame();
    if (frame == null) {
      return null;
    }

    int[] outWH = ImageTools.computeDimensions(frame.getWidth(), frame.getHeight(), rWidth, rHeight, preserveAspectRatio);
    rWidth = outWH[0];
    rHeight = outWH[1];

    int delay = frame.getDelayMillis();
    int loop = apngFrameDecoder.getAnimationNumPlays();

    ImageMetadata metaData = new ImageMetadata(null, true, null, null, null, delay, loop, rWidth, rHeight, null, null, null);
    updateImageMetadata(metaData);

    ImageType imageType = getType(apngFrameDecoder.getColorType(), apngFrameDecoder.hasTransparency());

    byte[][] palette = apngFrameDecoder.getPalette();

    
    ImageFrame imgPNG = imageType == ImageType.PALETTE ? decodePalette(frame.getBytes(), frame.getWidth(), frame.getHeight(), palette, metaData)
      : new ImageFrame(imageType, ByteBuffer.wrap(frame.getBytes()), frame.getWidth(), frame.getHeight(), frame.getStride(), palette, metaData);

    if (frame.getWidth() != rWidth || frame.getHeight() != rHeight) {
      imgPNG = ImageTools.scaleImageFrame(imgPNG, rWidth, rHeight, smooth);
    }
    return imgPNG;
  }

  private ImageFrame decodePalette(byte[] srcImage, int width, int height, byte[][] palette, ImageMetadata metadata) throws IOException {
    int bpp = apngFrameDecoder.hasTransparency() ? 4 : 3;
    if (width >= (Integer.MAX_VALUE / height / bpp)) {
      throw new IOException("Bad PNG image size!");
    }
    int l = width * height;
    byte newImage[] = new byte[l * bpp];

    if (apngFrameDecoder.hasTransparency()) {
      for (int i = 0, j = 0; i != l; j += 4, i++) {
        int index = 0xFF & srcImage[i];
        newImage[j + 0] = palette[0][index];
        newImage[j + 1] = palette[1][index];
        newImage[j + 2] = palette[2][index];
        newImage[j + 3] = palette[3][index];
        }
    } else {
      for (int i = 0, j = 0; i != l; j += 3, i++) {
        int index = 0xFF & srcImage[i];
        newImage[j + 0] = palette[0][index];
        newImage[j + 1] = palette[1][index];
        newImage[j + 2] = palette[2][index];
      }
    }

    ImageType type = apngFrameDecoder.hasTransparency() ? ImageType.RGBA : ImageType.RGB;
    return new ImageFrame(type, ByteBuffer.wrap(newImage), width, height, width * bpp, null, metadata);
  }

  private ImageType getType(int colorType, boolean tRNS_present) {
    switch (colorType) {
      case ApngDecoder.PNG_COLOR_GRAY:
        return tRNS_present ? ImageType.GRAY_ALPHA : ImageType.GRAY;
      case ApngDecoder.PNG_COLOR_RGB:
        return tRNS_present ? ImageType.RGBA : ImageType.RGB;
      case ApngDecoder.PNG_COLOR_PALETTE:
        return ImageType.PALETTE;
      case ApngDecoder.PNG_COLOR_GRAY_ALPHA:
        return ImageType.GRAY_ALPHA;
      case ApngDecoder.PNG_COLOR_RGB_ALPHA:
        return ImageType.RGBA;
      default: // unreacheble
        throw new RuntimeException();
    }
  }
}

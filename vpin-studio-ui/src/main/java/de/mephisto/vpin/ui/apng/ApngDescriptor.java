package de.mephisto.vpin.ui.apng;

import com.sun.javafx.iio.common.ImageDescriptor;

import de.mephisto.vpin.ui.apng.chunks.ApngChunkDataInputStream;

/**
 * APNG Image Descriptor
 */
public class ApngDescriptor extends ImageDescriptor {

  private static final String formatName = "PNG";

  private static final String[] extensions = { "png" };

  private static final Signature[] signatures = {
    new Signature(ApngChunkDataInputStream.APNG_SIGNATURE)
  };

  private static final String[] mimeSubtypes = { "png", "x-png", "apng" };

  private static ImageDescriptor theInstance = null;

  private ApngDescriptor() {
    super(formatName, extensions, signatures, mimeSubtypes);
  }

  public static synchronized ImageDescriptor getInstance() {
    if (theInstance == null) {
      theInstance = new ApngDescriptor();
    }
    return theInstance;
  }

}

package de.mephisto.vpin.ui.apng;

import java.io.IOException;
import java.io.InputStream;

import com.sun.javafx.iio.ImageFormatDescription;
import com.sun.javafx.iio.ImageLoader;
import com.sun.javafx.iio.ImageLoaderFactory;
import com.sun.javafx.iio.ImageStorage;

public class ApngImageLoaderFactory implements ImageLoaderFactory {

  private static ApngImageLoaderFactory theInstance;

  public static final ImageLoaderFactory getInstance() {
    return theInstance;
  }

  private ApngImageLoaderFactory() {}

  public ImageFormatDescription getFormatDescription() {
    return ApngDescriptor.getInstance();
  }

  public ImageLoader createImageLoader(InputStream input) throws IOException {
    return new ApngImageLoader(input);
  }

  public static void install() {
    // avoid several installation
    if (theInstance == null) {
      theInstance = new ApngImageLoaderFactory();
      ImageStorage.getInstance().addImageLoaderFactory(theInstance);
    }
  }

}

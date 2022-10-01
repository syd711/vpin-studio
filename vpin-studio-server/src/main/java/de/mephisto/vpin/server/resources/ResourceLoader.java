package de.mephisto.vpin.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Used for load images and stuff.
 */
public class ResourceLoader {
  private final static Logger LOG = LoggerFactory.getLogger(ResourceLoader.class);

  public static BufferedImage getResource(String s) {
    try {
      BufferedImage read = ImageIO.read(ResourceLoader.class.getResource(s));
      return read;
    } catch (Exception e) {
      LOG.error("Resource not found: " + s);
    }
    return null;
  }
}

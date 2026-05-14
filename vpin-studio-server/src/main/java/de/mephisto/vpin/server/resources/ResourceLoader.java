package de.mephisto.vpin.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Used for load images and stuff.
 */
public class ResourceLoader {
  private final static Logger LOG = LoggerFactory.getLogger(ResourceLoader.class);

  public static BufferedImage getResource(String s) {
    try (InputStream in = ResourceLoader.class.getResourceAsStream(s)) {
      if (in == null) {
        LOG.error("Resource not found: {}", s);
        return null;
      }
      return ImageIO.read(in);
    } catch (Exception e) {
      LOG.error("Error loading resource {}: {}", s, e.getMessage());
    }
    return null;
  }
}

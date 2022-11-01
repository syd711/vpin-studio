package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MediaUtil.class);

  public static void openDirectB2SBackground(GameRepresentation game) {
    if (game != null) {
      try {
        ByteArrayInputStream s = new VPinStudioClient().getDirectB2SImage(game);
        byte[] bytes = s.readAllBytes();
        File png = File.createTempFile("vpin-studio-directb2s-", ".png");
        png.deleteOnExit();
        IOUtils.write(bytes, new FileOutputStream(png));
        s.close();

        Desktop.getDesktop().open(png);
      } catch (IOException e) {
        LOG.error("Failed to create image temp file: " + e.getMessage(), e);
      }
    }
  }
}

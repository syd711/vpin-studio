package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.scene.Cursor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MediaUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MediaUtil.class);

  public static void openMedia(InputStream in) {
    try {
      Platform.runLater(() -> Studio.stage.getScene().setCursor(Cursor.WAIT));
      byte[] bytes = in.readAllBytes();
      File png = File.createTempFile("vpin-media-", ".png");
      png.deleteOnExit();
      FileOutputStream fileOutputStream = new FileOutputStream(png);
      IOUtils.write(bytes, fileOutputStream);
      fileOutputStream.close();
      in.close();

      Desktop.getDesktop().open(png);
    } catch (IOException e) {
      LOG.error("Failed to create image temp file: " + e.getMessage(), e);
    } finally {
      Platform.runLater(() -> Studio.stage.getScene().setCursor(Cursor.DEFAULT));
    }
  }
}

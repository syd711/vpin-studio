package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.lang.invoke.MethodHandles;

public class TrashBin {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static boolean moveTo(@Nullable File file) {
    if (file != null) {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
        if (!Desktop.getDesktop().moveToTrash(file)) {
          LOG.error("Failed moving file to trash: " + file.getAbsolutePath());
          return false;
        }

      }
      else {
        if (!file.delete()) {
          LOG.error("Failed fallback deletion of trash bin file: " + file.getAbsolutePath());
          return false;
        }
      }
    }
    return true;
  }
}

package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.util.VPXFileScanner;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.poifs.filesystem.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class VPXUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VPXFileScanner.class);

  public static String readScript(@NonNull File file) {
    byte[] content = readBytes(file);

    int startIndex = 0;
    int endIndex = 0;

    int index = 0;
    for (byte b : content) {
      if (b == 4) {
        if (startIndex == 0) {
          index++;
          startIndex = index;
          continue;
        }

        endIndex = index;
        break;
      }
      index++;
    }

    byte[] scriptData = Arrays.copyOfRange(content, startIndex, endIndex);
    return new String(scriptData);
  }

  public static byte[] readBytes(@NonNull File file) {
    byte[] content = new byte[0];
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(file, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameStg = (DirectoryEntry) root.getEntry("GameStg");
      DocumentNode gameData = (DocumentNode) gameStg.getEntry("GameData");

      POIFSDocument document = new POIFSDocument(gameData);
      DocumentInputStream documentInputStream = new DocumentInputStream(document);
      content = new byte[documentInputStream.available()];
      documentInputStream.read(content);
      documentInputStream.close();

    } catch (Exception e) {
      LOG.error("Reading script failed for " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return null;
    } finally {
      try {
        if (fs != null) {
          fs.close();
        }
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }

    return content;
  }

  public static void writeGameData(@NonNull File file, byte[] decoded) throws IOException {
    POIFSFileSystem fs = null;
    try {
      byte[] content = readBytes(file);

      byte[] headerBytes = new byte[0];
      int index = 1;
      do {
        headerBytes = Arrays.copyOfRange(content, 0, index);
        index++;
      }
      while (headerBytes[headerBytes.length - 1] != 4);

      byte[] footerBytes = content;
      while (footerBytes[0] != 4) {
        footerBytes = Arrays.copyOfRange(footerBytes, 1, footerBytes.length);
      }

      byte[] documentBytes = ArrayUtils.addAll(headerBytes, decoded);
      documentBytes = ArrayUtils.addAll(documentBytes, footerBytes);

      fs = new POIFSFileSystem(file, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameStg = (DirectoryEntry) root.getEntry("GameStg");
      DocumentNode gameData = (DocumentNode) gameStg.getEntry("GameData");
      POIFSDocument updatedDocument = new POIFSDocument(gameData);
      updatedDocument.replaceContents(new ByteArrayInputStream(documentBytes));

      fs.writeFilesystem();
    } catch (Exception e) {
      LOG.error("Writing script failed for " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      throw e;
    } finally {
      try {
        if (fs != null) {
          fs.close();
        }
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }
  }
}

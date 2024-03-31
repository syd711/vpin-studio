package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.util.MD5ChecksumUtil;
import de.mephisto.vpin.server.util.VPXFileScanner;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.poifs.filesystem.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VPXUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VPXFileScanner.class);

  public static String readScript(@NonNull File file) {
    try {
      byte[] content = readBytes(file);

      int index = 0;
      List<Integer> indexes = new ArrayList<>();
      for (byte b : content) {
        if (b == 4) {
          indexes.add(index);
        }
        index++;
      }
      byte[] scriptData = Arrays.copyOfRange(content, indexes.get(indexes.size() - 2) + 12, indexes.get(indexes.size() - 1));
      return new String(scriptData);
    } catch (Exception e) {
      return String.valueOf(e.getMessage());
    }
  }

  public static byte[] readScreenshot(@NonNull File file) throws Exception {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(file, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry tableInfo = (DirectoryEntry) root.getEntry("TableInfo");
      if(tableInfo.hasEntry("Screenshot")) {
        DocumentNode screenshot = (DocumentNode) tableInfo.getEntry("Screenshot");
        DocumentInputStream documentInputStream = new DocumentInputStream(screenshot);
        byte[] infoContent = new byte[documentInputStream.available()];
        documentInputStream.read(infoContent);
        documentInputStream.close();
        LOG.info("Extracted screenshot from file " + file.getAbsolutePath() + ", size: " + FileUtils.readableFileSize(infoContent.length));
        return infoContent;
      }
      return null;
    } catch (Exception e) {
      LOG.error("Reading table screenshot failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
      throw new Exception("Reading table screenshot failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
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

  public static Map<String, Object> readTableInfo(@NonNull File file) throws Exception {
    Map<String, Object> result = new HashMap<>();
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(file, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry tableInfo = (DirectoryEntry) root.getEntry("TableInfo");
      Set<String> infoEntries = tableInfo.getEntryNames();
      for (String infoEntry : infoEntries) {
        DocumentNode infoNode = (DocumentNode) tableInfo.getEntry(infoEntry);
        POIFSDocument document = new POIFSDocument(infoNode);
        DocumentInputStream documentInputStream = new DocumentInputStream(document);
        byte[] infoContent = new byte[documentInputStream.available()];
        documentInputStream.read(infoContent);

        List<Byte> collect = new ArrayList<>();
        for (byte b : infoContent) {
          if (b == 0) {
            continue;
          }
          collect.add(b);
        }

        Byte[] bytes = collect.toArray(new Byte[collect.size()]);
        byte[] primitive = ArrayUtils.toPrimitive(bytes);
        if (infoEntry.equals("Screenshot")) {
          result.put(infoEntry, primitive);
        }
        else {
          result.put(infoEntry, new String(primitive, StandardCharsets.UTF_8));
        }
      }
    } catch (Exception e) {
      LOG.error("Reading table info failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
      throw new Exception("Reading table info failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
    } finally {
      try {
        if (fs != null) {
          fs.close();
        }
      } catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }

    return result;
  }

  public static byte[] readBytes(@NonNull File file) throws Exception {
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
      LOG.error("Reading script failed for " + file.getAbsolutePath() + " failed: " + e.getMessage());
      throw new Exception("Reading script failed for " + file.getAbsolutePath() + " failed: " + e.getMessage());
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

  public static void importVBS(@NonNull File file, String vps) throws IOException {
    try {
      String vpxFilePath = file.getAbsolutePath();
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("vpxtools.exe", "importvbx",  vpxFilePath));
      executor.setDir(new File("./resources"));
      executor.executeCommand();
    } catch (Exception e) {
      LOG.error("Importing VBS failed for " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  public static void exportVBS(@NonNull File file, String vps) throws IOException {
    try {
      String vpxFilePath = file.getAbsolutePath();
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("vpxtools.exe", "importvbx",  vpxFilePath));
      executor.setDir(new File("./resources"));
      executor.executeCommand();
    } catch (Exception e) {
      LOG.error("Importing VBS failed for " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  public static String getChecksum(File gameFile) {
    if (gameFile.exists()) {
      String s = readScript(gameFile);
      return MD5ChecksumUtil.checksum(s);
    }
    throw new UnsupportedOperationException("No game file found");
  }
}

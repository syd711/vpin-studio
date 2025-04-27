package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.util.MD5ChecksumUtil;
import de.mephisto.vpin.server.util.VPXFileScanner;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.poifs.filesystem.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VPXUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VPXFileScanner.class);
  private final static String VPX_TOOL_EXE = "vpxtool.exe";

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

      int start = indexes.get(indexes.size() - 2) + 12;
      int end = indexes.get(indexes.size() - 1);
      if (start < end) {
        byte[] scriptData = Arrays.copyOfRange(content, start, end);
        return new String(scriptData);
      }
      return new String(content);
    }
    catch (Exception e) {
      return String.valueOf(e.getMessage());
    }
  }

  public static byte[] readScreenshot(@NonNull File file) throws Exception {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(file, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry tableInfo = (DirectoryEntry) root.getEntry("TableInfo");
      if (tableInfo.hasEntry("Screenshot")) {
        DocumentNode screenshot = (DocumentNode) tableInfo.getEntry("Screenshot");
        DocumentInputStream documentInputStream = new DocumentInputStream(screenshot);
        byte[] infoContent = new byte[documentInputStream.available()];
        documentInputStream.read(infoContent);
        documentInputStream.close();
        LOG.info("Extracted screenshot from file " + file.getAbsolutePath() + ", size: " + FileUtils.readableFileSize(infoContent.length));
        return infoContent;
      }
      return null;
    }
    catch (Exception e) {
      LOG.error("Reading table screenshot failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
      return null;
    }
    finally {
      try {
        if (fs != null) {
          fs.close();
        }
      }
      catch (Exception e) {
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
    }
    catch (Exception e) {
      LOG.error("Reading table info failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
      throw new Exception("Reading table info failed for " + file.getAbsolutePath() + ", cause: " + e.getMessage());
    }
    finally {
      try {
        if (fs != null) {
          fs.close();
        }
      }
      catch (Exception e) {
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

    }
    catch (Exception e) {
      LOG.error("Reading script failed for " + file.getAbsolutePath() + " failed: " + e.getMessage());
      throw new Exception("Reading script failed for " + file.getAbsolutePath() + " failed: " + e.getMessage());
    }
    finally {
      try {
        if (fs != null) {
          fs.close();
        }
      }
      catch (Exception e) {
        LOG.error("Failed to close vpx file stream: " + e.getMessage(), e);
      }
    }

    return content;
  }

  public static void importVBS(@NonNull File vpxFile, String vps, boolean keepVbsFile) throws IOException {
    try {
      File vbsFile = new File(vpxFile.getParentFile(), FilenameUtils.getBaseName(vpxFile.getName()) + ".vbs");
      if (vbsFile.exists()) {
        vbsFile.delete();
      }
      org.apache.commons.io.FileUtils.writeStringToFile(vbsFile, vps, Charset.defaultCharset());

      String vpxFilePath = "\"" + vpxFile.getAbsolutePath() + "\"";
      List<String> cmds = Arrays.asList(VPX_TOOL_EXE, "importvbs", vpxFilePath);
      LOG.info("VBS Import CMD: " + String.join(" ", cmds));
      SystemCommandExecutor executor = new SystemCommandExecutor(cmds);
      executor.setDir(new File("./resources"));
      executor.executeCommand();

      if (!keepVbsFile && !vbsFile.delete()) {
        LOG.error("Failed to delete VBS import file " + vbsFile.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Importing VBS failed for " + vpxFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  public static String exportVBS(@NonNull File vpxFile, boolean keepVbsFile) throws Exception {
    String error = null;
    try {
      File vbsFile = new File(vpxFile.getParentFile(), FilenameUtils.getBaseName(vpxFile.getName()) + ".vbs");
      if (vbsFile.exists()) {
        vbsFile.delete();
      }
      String vpxFilePath = "\"" + vpxFile.getAbsolutePath() + "\"";
      List<String> cmds = Arrays.asList(VPX_TOOL_EXE, "extractvbs", vpxFilePath);
      LOG.info("VBS Export CMD: " + String.join(" ", cmds));
      SystemCommandExecutor executor = new SystemCommandExecutor(cmds);
      executor.setDir(new File("./resources"));
      executor.executeCommand();

      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (standardErrorFromCommand != null) {
        error = standardErrorFromCommand.toString();
      }

      String script = org.apache.commons.io.FileUtils.readFileToString(vbsFile, Charset.defaultCharset());
      if (!keepVbsFile && !vbsFile.delete()) {
        LOG.error("Failed to delete VBS export file " + vbsFile.getAbsolutePath());
      }
      return script;
    }
    catch (Exception e) {
      LOG.error("Exporting VBS failed for {}: {} - {}", vpxFile.getAbsolutePath(), error,  e.getMessage(), e);
      throw new Exception("Exporting VBS failed for \"" + vpxFile.getAbsolutePath() + "\": " + error);
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

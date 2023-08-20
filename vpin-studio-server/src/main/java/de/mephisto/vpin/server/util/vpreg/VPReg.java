package de.mephisto.vpin.server.util.vpreg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.filesystem.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VPReg {
  private final static Logger LOG = LoggerFactory.getLogger(VPReg.class);

  public static final String ARCHIVE_FILENAME = "vpreg-stg.json";

  public static final String NAME_SUFFIX = "Name";
  public static final String HIGH_SCORE = "HighScore";

  private final File vpregFile;
  private final String rom;
  private final String tablename;

  public VPReg(File vpregFile, String rom, String tablename) {
    this.vpregFile = vpregFile;
    this.rom = rom;
    this.tablename = tablename;
  }

  public boolean containsGame() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      return getGameDirectory(root) != null;
    } catch (Exception e) {
      LOG.error("Failed to read VPReg: " + e.getMessage());
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return false;
  }

  public boolean restoreHighscore(VPRegScoreSummary summary) {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry(HIGH_SCORE + "1")) {
          return false;
        }

        LOG.info("Writing VPReg entry \"" + gameFolder.getName() + "\"");
        List<VPRegScoreEntry> scores = summary.getScores();
        for (VPRegScoreEntry score : scores) {
          DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + score.getPos());
          POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
          scoreDocument.replaceContents(new ByteArrayInputStream(new Base64Encoder().decode(score.getBase64Score())));

          DocumentNode nameEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + score.getPos() + NAME_SUFFIX);
          POIFSDocument nameDocument = new POIFSDocument(nameEntry);
          nameDocument.replaceContents(new ByteArrayInputStream(new Base64Encoder().decode(score.getBase64Name())));

          LOG.info("Written VPReg score entry: " + score);
        }

        fs.writeFilesystem();
        return true;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return false;
  }

  public boolean resetHighscores() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry(HIGH_SCORE + "1")) {
          return false;
        }
        int index = 1;
        while (gameFolder.hasEntry(HIGH_SCORE + index) && gameFolder.hasEntry(HIGH_SCORE + index + NAME_SUFFIX)) {
          DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + index);
          POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
          scoreDocument.replaceContents(new ByteArrayInputStream("0".getBytes()));

          DocumentNode nameEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + index + NAME_SUFFIX);
          POIFSDocument nameDocument = new POIFSDocument(nameEntry);
          nameDocument.replaceContents(new ByteArrayInputStream("???".getBytes()));

          index++;
        }

        fs.writeFilesystem();
        return true;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return false;
  }

  public String toJson() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, true);
      DirectoryEntry root = fs.getRoot();

      DirectoryEntry gameFolder = getGameDirectory(root);
      Map<String, String> target = new LinkedHashMap<>();
      if (gameFolder != null) {
        Set<String> entryNames = gameFolder.getEntryNames();
        for (String entryName : entryNames) {
          DocumentEntry documentEntry = (DocumentEntry) gameFolder.getEntry(entryName);

          DocumentInputStream documentInputStream = new DocumentInputStream(documentEntry);
          byte[] fieldContent = new byte[documentInputStream.available()];
          documentInputStream.read(fieldContent);
          documentInputStream.close();

          target.put(entryName, new Base64Encoder().encode(fieldContent));
        }
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      return objectMapper.writeValueAsString(target);
    } catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return null;
  }

  public void restore(String data) {
    POIFSFileSystem fs = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {
      };
      HashMap<String, String> values = objectMapper.readValue(data, typeRef);

      fs = new POIFSFileSystem(vpregFile, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getOrCreateGameDirectory(root);
      Set<String> entryNames = gameFolder.getEntryNames();

      Set<Map.Entry<String, String>> entries = values.entrySet();
      for (Map.Entry<String, String> entry : entries) {
        String name = entry.getKey();
        byte[] value = new Base64Encoder().decode(entry.getValue());
        if (entryNames.contains(name)) {
          DocumentNode documentEntry = (DocumentNode) gameFolder.getEntry(name);
          POIFSDocument scoreDocument = new POIFSDocument(documentEntry);
          scoreDocument.replaceContents(new ByteArrayInputStream(value));
        }
        else {
          gameFolder.createDocument(entry.getKey(), new ByteArrayInputStream(value));
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }

  @Nullable
  public VPRegScoreSummary readHighscores() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        if (!gameFolder.hasEntry(HIGH_SCORE + "1")) {
          return null;
        }

        VPRegScoreSummary summary = new VPRegScoreSummary();
        int index = 1;
        String prefix = HIGH_SCORE;
        String nameSuffix = "Name";
        while (gameFolder.hasEntry(prefix + index) && gameFolder.hasEntry(prefix + index + nameSuffix)) {
          DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
          DocumentEntry nameEntry = (DocumentEntry) gameFolder.getEntry(prefix + index + nameSuffix);

          DocumentInputStream scoreEntryStream = new DocumentInputStream(scoreEntry);
          byte[] scoreContent = new byte[scoreEntryStream.available()];
          scoreEntryStream.read(scoreContent);
          scoreEntryStream.close();

          DocumentInputStream nameEntryStream = new DocumentInputStream(nameEntry);
          byte[] nameContent = new byte[nameEntryStream.available()];
          nameEntryStream.read(nameContent);
          nameEntryStream.close();

          String nameString = new String(nameContent, StandardCharsets.UTF_8);
          nameString = nameString.replace("\0", "").trim();

          String scoreString = new String(scoreContent, StandardCharsets.UTF_8);
          scoreString = scoreString.replace("\0", "");

          VPRegScoreEntry score = new VPRegScoreEntry();
          score.setBase64Score(new Base64Encoder().encode(scoreContent));
          score.setBase64Name(new Base64Encoder().encode(nameContent));
          score.setInitials(nameString);
          score.setScore(StringUtils.isEmpty(scoreString) ? 0 : Long.parseLong(scoreString));
          score.setPos(index);
          summary.getScores().add(score);
          index++;
        }

        return summary;
      }
    } catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return null;
  }

  /**
   * Checks if the VPReg.stg has a subfolder that matches the game's ROM name.
   *
   * @param root the root folder of the archive
   * @throws FileNotFoundException
   */
  private DirectoryEntry getGameDirectory(DirectoryEntry root) throws FileNotFoundException {
    if (root.hasEntry(rom)) {
      return (DirectoryEntry) root.getEntry(rom);
    }

    if (tablename != null && root.hasEntry(tablename)) {
      return (DirectoryEntry) root.getEntry(tablename);
    }

    return null;
  }

  /**
   * Checks if the VPReg.stg has a subfolder that matches the game's ROM name.
   *
   * @param root the root folder of the archive
   * @throws FileNotFoundException
   */
  private DirectoryEntry getOrCreateGameDirectory(DirectoryEntry root) throws IOException {
    if (root.hasEntry(rom)) {
      return (DirectoryEntry) root.getEntry(rom);
    }

    if (tablename != null && root.hasEntry(tablename)) {
      return (DirectoryEntry) root.getEntry(tablename);
    }

    return root.createDirectory(rom);
  }
}

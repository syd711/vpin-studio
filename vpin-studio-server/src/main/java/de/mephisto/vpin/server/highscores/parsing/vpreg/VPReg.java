package de.mephisto.vpin.server.highscores.parsing.vpreg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import de.mephisto.vpin.server.highscores.parsing.vpreg.adapters.*;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.poi.poifs.filesystem.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class VPReg {
  private final static Logger LOG = LoggerFactory.getLogger(VPReg.class);

  public static final String ARCHIVE_FILENAME = "vpreg-stg.json";

  private final File vpregFile;
  private String rom;
  private String tablename;


  private static Map<String, VPRegHighscoreAdapter> adapters = new LinkedHashMap<>();

  static {
    adapters.put("numericList", new NumericListVPRegHighscoreAdapter());
    adapters.put("numericList2", new NumericList2VPRegHighscoreAdapter("HSName%s", "HSPoints%s"));
    adapters.put("numericList3", new NumericList2VPRegHighscoreAdapter("HighscoreInits(%s)", "Highscore(%s)"));
    adapters.put("numericList4", new NumericList2VPRegHighscoreAdapter("Name%s", "High%s", 1));
    adapters.put("singleAnonymousEntry1", new SingleEntryAnonymousVPRegHighscoreAdapter("madscientist", "Score"));
    adapters.put("singleAnonymousEntry2", new SingleEntryAnonymousVPRegHighscoreAdapter("RaygunRunner", "highscore"));
    adapters.put("singleAnonymousEntry3", new SingleEntryAnonymousVPRegHighscoreAdapter("Heatw", "HS"));
    adapters.put("singleAnonymousEntry4", new SingleEntryAnonymousVPRegHighscoreAdapter("SpEyes", "HS"));
    adapters.put("singleAnonymousEntry5", new SingleEntryAnonymousVPRegHighscoreAdapter("JungleQueen", "HighScore5"));
    adapters.put("multiWithLettersEntry", new MultiEntryWithLettersVPRegHighscoreAdapter(Arrays.asList("Gulfstream", "Mariner", "AirAces", "SeaRay"), 5));
    adapters.put("singleAnonymousEntry", new SingleEntryAnonymousVPRegHighscoreAdapter());
    adapters.put("singleWithLettersEntry", new SingleEntryWithLettersVPRegHighscoreAdapter());
    adapters.put("numericListAnonymous", new NumericListAnonymousVPRegHighscoreAdapter("HighScore%s"));
    adapters.put("numericListAnonymous2", new NumericListAnonymousVPRegHighscoreAdapter("sScore%sP"));
    adapters.put("numericListAnonymous3", new NumericListAnonymousVPRegHighscoreAdapter("hsa%s"));
  }


  public VPReg(File vpregFile) {
    this.vpregFile = vpregFile;
  }

  public VPReg(File vpregFile, String rom, String tablename) {
    this.vpregFile = vpregFile;
    this.rom = rom;
    this.tablename = tablename;
  }

  public List<String> getEntries() {
    List<String> result = new ArrayList<>();
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      if (root != null) {
        Iterator<Entry> entries = root.getEntries();
        while (entries.hasNext()) {
          result.add(entries.next().getName());
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read VPReg: " + e.getMessage());
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
    return result;
  }

  public void dump() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      if (root != null) {
        Iterator<Entry> entries = root.getEntries();
        while (entries.hasNext()) {
          Entry next = entries.next();
          String folder = next.getName();
          System.out.println("/" + folder);
          if (next.isDirectoryEntry()) {
            DirectoryEntry child = (DirectoryEntry) next;
            Iterator<Entry> childEntries = child.getEntries();
            while (childEntries.hasNext()) {
              Entry childNext = childEntries.next();
              String childName = childNext.getName();

              DocumentNode scoreEntry = (DocumentNode) child.getEntry(childName);
              DocumentInputStream scoreEntryStream = new DocumentInputStream(scoreEntry);
              byte[] scoreContent = new byte[scoreEntryStream.available()];
              scoreEntryStream.read(scoreContent);
              scoreEntryStream.close();

              System.out.println("/" + folder + "/" + childName + " => " + new String(scoreContent));
            }
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read VPReg: " + e.getMessage());
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
  }

  public boolean containsGame() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      return getGameDirectory(root) != null;
    }
    catch (Exception e) {
      LOG.error("Failed to read VPReg: " + e.getMessage());
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
    return false;
  }

  public boolean resetHighscores(long score) {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        for (VPRegHighscoreAdapter adapter : adapters.values()) {
          if (adapter.isApplicable(gameFolder)) {
            LOG.info("Resetting highscore using " + adapter.getClass().getSimpleName());
            return adapter.resetHighscore(fs, gameFolder, score);
          }
        }
        fs.writeFilesystem();
        return true;
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
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
    }
    catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
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

      fs = new POIFSFileSystem(vpregFile, false);
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

      fs.writeFilesystem();
    }
    catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
  }

  @Nullable
  public ScoreParsingSummary readHighscores() {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, true);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        for (VPRegHighscoreAdapter adapter : adapters.values()) {
          if (adapter.isApplicable(gameFolder)) {
            return adapter.readHighscore(gameFolder);
          }
        }
      }
    }
    catch (IOException e) {
      LOG.error("Failed to read VPReg.stg: " + e.getMessage(), e);
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
    return null;
  }

  public void deleteEntry(String amh) {
    POIFSFileSystem fs = null;
    try {
      fs = new POIFSFileSystem(vpregFile, false);
      DirectoryEntry root = fs.getRoot();
      DirectoryEntry gameFolder = getGameDirectory(root);
      if (gameFolder != null) {
        List<String> entryNames = new ArrayList<>(gameFolder.getEntryNames());
        for (String entryName : entryNames) {
          Entry next = gameFolder.getEntry(entryName);
          next.delete();
          LOG.info("Deleted {}", next.getName());
        }
        gameFolder.delete();
        LOG.info("Deleted all entries for {}", gameFolder.getName());
      }

      fs.writeFilesystem();
    }
    catch (IOException e) {
      LOG.error("Failed to deleting entry from VPReg.stg: " + e.getMessage(), e);
    }
    finally {
      if (fs != null) {
        try {
          fs.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
  }

  /**
   * Checks if the VPReg.stg has a subfolder that matches the game's ROM name.
   *
   * @param root the root folder of the archive
   * @throws FileNotFoundException
   */
  private DirectoryEntry getGameDirectory(DirectoryEntry root) throws FileNotFoundException {
    Set<String> entryNames = root.getEntryNames();
    for (String entryName : entryNames) {
      if (entryName.equalsIgnoreCase(rom)) {
        return (DirectoryEntry) root.getEntry(entryName);
      }
    }

    if (tablename != null) {
      for (String entryName : entryNames) {
        if (entryName.equalsIgnoreCase(tablename)) {
          return (DirectoryEntry) root.getEntry(entryName);
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
  private DirectoryEntry getOrCreateGameDirectory(DirectoryEntry root) throws IOException {
    if (root.hasEntry(rom)) {
      return (DirectoryEntry) root.getEntry(rom);
    }

    if (tablename != null && root.hasEntry(tablename)) {
      return (DirectoryEntry) root.getEntry(tablename);
    }

    return root.createDirectory(rom);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VPReg)) return false;

    VPReg vpReg = (VPReg) o;

    return vpregFile.equals(vpReg.vpregFile);
  }

  @Override
  public int hashCode() {
    return vpregFile.hashCode();
  }
}

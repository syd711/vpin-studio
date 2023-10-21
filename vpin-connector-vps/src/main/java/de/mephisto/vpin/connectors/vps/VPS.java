package de.mephisto.vpin.connectors.vps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class VPS {
  private final static Logger LOG = LoggerFactory.getLogger(VPS.class);

  public final static String URL = "https://fraesh.github.io/vps-db/vpsdb.json";

  private final ObjectMapper objectMapper;

  private List<VpsTable> tables;

  private static VPS instance;

  public static VPS getInstance() {
    if (instance == null) {
      instance = new VPS();
    }
    return instance;
  }

  public VPS() {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    if (!this.getVpsDbFile().exists()) {
      try {
        if (this.getVpsDbFile().getParentFile().exists()) {
          this.getVpsDbFile().getParentFile().mkdirs();
        }
        download();
      } catch (Exception e) {
        LOG.error("Failed to initialize VPS db: " + e.getMessage(), e);
      }
    }
    this.tables = loadTables();
  }

  public VpsTable getTableById(String id) {
    return this.tables.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
  }

  public List<VpsTable> getTables() {
    return tables;
  }

  public VpsTableFile findVersion(VpsTable table, String tableFileName, String tableName, String version) {
    List<VpsTableFile> tableFiles = table.getTableFiles();
    if(tableFiles.size() == 1) {
      return tableFiles.get(0);
    }

    for (VpsTableFile tableFile : tableFiles) {
      if (version != null && tableFile.toString().toLowerCase().contains(version.toLowerCase())) {
        return tableFile;
      }

      String versionString = tableFile.toString();
      if (versionMatches(versionString, tableFileName, tableName, "vpw")) {
        return tableFile;
      }
      if (versionMatches(versionString, tableFileName, tableName, "bigus")) {
        return tableFile;
      }
      if (versionMatches(versionString, tableFileName, tableName, "TastyWasps")) {
        return tableFile;
      }
      if (versionMatches(versionString, tableFileName, tableName, "Salas")) {
        return tableFile;
      }
      if (versionMatches(versionString, tableFileName, tableName, "VPinWorkshop")) {
        return tableFile;
      }
    }
    return null;
  }

  private boolean versionMatches(String version, String tableFileName, String tableName, String term) {
    if (version.toLowerCase().contains(term)) {
      if (tableFileName.toLowerCase().contains(term) || tableName.toLowerCase().contains(term)) {
        return true;
      }
    }
    return false;
  }

  public List<VpsTable> find(String searchTerm) {
    String term = searchTerm;
    term = term.replaceAll("_", " ");
    term = term.replaceAll("'", " ");
    term = term.replaceAll("-", " ");
    term = term.replaceAll("\\.", " ");
    term = term.replaceAll("The ", "");
    term = term.replaceAll(", The", "");
    if (term.contains("(")) {
      term = term.substring(0, term.indexOf("("));
    }
    term = term.toLowerCase().trim();

    List<VpsTable> results = findInternal(term);

    while (results.isEmpty()) {
      if (term.contains(" ")) {
        term = term.substring(0, term.lastIndexOf(" "));
      }
      else {
        break;
      }
      results = findInternal(term);
    }

//    if (results.isEmpty()) {
//      String[] segments = searchTerm.split(" ");
//      for (String segment : segments) {
//        if (results.isEmpty() && segment.length() >= 4) {
//          results = findInternal(term);
//        }
//      }
//    }

    return results;
  }

  private List<VpsTable> findInternal(String term) {
    List<VpsTable> results = new ArrayList<>();
    for (VpsTable table : this.tables) {
      String name = table.getName().toLowerCase();
      name = name.replaceAll("-", " ");
      name = name.replaceAll("'", " ");
      if (!name.contains(term)) {
        continue;
      }
      results.add(table);
    }

    return results;
  }

  public File getVpsDbFile() {
    File folder = new File("./resources");
    if (!folder.exists()) {
      folder = new File("../resources");
    }
    return new File(folder, "vpsdb.json");
  }

  private List<VpsTable> loadTables() {
    try {
      VpsTable[] vpsTables = objectMapper.readValue(getVpsDbFile(), VpsTable[].class);
      return Arrays.stream(vpsTables)
          .filter(t -> t.getFeatures().contains(VpsFeatures.VPX))
          .collect(Collectors.toList());
    } catch (Exception e) {
      LOG.error("Failed to load VPS json: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public void download() throws Exception {
    try {
      LOG.info("Downloading " + VPS.URL);
      java.net.URL url = new URL(VPS.URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File tmp = new File(getVpsDbFile().getParentFile(), getVpsDbFile().getName() + ".tmp");
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete existing tmp file vpsdb.json.tmp");
      }
      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();

      if (getVpsDbFile().exists() && !getVpsDbFile().delete()) {
        LOG.error("Failed to delete vpsdb.json");
      }
      if (!tmp.renameTo(getVpsDbFile())) {
        LOG.error("Failed to rename vpsdb.json");
        return;
      }

      LOG.info("Written " + getVpsDbFile().getAbsolutePath());
    } catch (IOException e) {
      LOG.error("VPS download failed: " + e.getMessage());
      throw e;
    }
  }

  public Date getChangeDate() {
    return new Date(getVpsDbFile().lastModified());
  }

  public void reload() {
    this.tables = loadTables();
  }
}

package de.mephisto.vpin.connectors.vps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableDiff;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class VPS {
  private final static Logger LOG = LoggerFactory.getLogger(VPS.class);

  public final static String URL = "https://fraesh.github.io/vps-db/vpsdb.json";

  private static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private List<VpsChangeListener> listeners = new ArrayList<>();

  private final List<String> versionIndicators = Arrays.asList("vpw", "bigus", "salas", "tasty", "thalamus", "VPinWorkshop", "Paulie");

  private List<VpsTable> tables;

  private static VPS instance;

  public void addChangeListener(VpsChangeListener listener) {
    this.listeners.add(listener);
  }

  public static VPS loadInstance(InputStream in) {
    VPS instance = new VPS();
    try {
      instance.tables = loadTables(in);
      return instance;
    } catch (Exception e) {
      LOG.error("Failed to load VPS stream: " + e.getMessage(), e);
    }
    return null;
  }

  public static String getVpsTableUrl(String tableId, String versionId) {
    String url = "https://virtual-pinball-spreadsheet.web.app/game/" + tableId;
    if (versionId != null) {
      url += "#" + versionId;
    }
    return url;
  }

  public static String getVpsTableUrl(String tableId) {
    return "https://virtual-pinball-spreadsheet.web.app/game/" + tableId;
  }

  public static VPS getInstance() {
    if (instance == null) {
      instance = new VPS();
    }
    return instance;
  }

  public VPS() {
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
    this.tables = loadTables(null);
  }

  public VpsTable getTableById(String id) {
    return this.tables.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
  }

  public List<VpsTable> getTables() {
    return tables;
  }

  public VpsTableVersion findVersion(VpsTable table, String tableFileName, String tableName, String version) {
    List<VpsTableVersion> tableFiles = table.getTableFiles();
    if (tableFiles.size() == 1) {
      return tableFiles.get(0);
    }

    for (VpsTableVersion tableFile : tableFiles) {
      if (version != null && tableFile.toString().toLowerCase().contains(version.toLowerCase())) {
        return tableFile;
      }

      String versionString = tableFile.toString();
      for (String versionIndicator : versionIndicators) {
        if (versionMatches(versionString, tableFileName, tableName, versionIndicator)) {
          return tableFile;
        }
      }
    }

    if (!tableFiles.isEmpty()) {
      return tableFiles.get(0);
    }
    return null;
  }

  private boolean versionMatches(String version, String tableFileName, String tableName, String term) {
    if (version.toLowerCase().contains(term.toLowerCase())) {
      if (tableFileName.toLowerCase().contains(term) || tableName.toLowerCase().contains(term)) {
        return true;
      }
    }
    return false;
  }

  public List<VpsTable> find(String searchTerm) {
    return find(searchTerm, null);
  }

  public List<VpsTable> find(String searchTerm, String rom) {
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

    return results;
  }

  private List<VpsTable> findInternal(String term) {
    List<VpsTable> results = new ArrayList<>();
    for (VpsTable table : this.tables) {
      String name = table.getName().toLowerCase();
      name = name.replaceAll("-", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("&", "and");
      if (!name.contains(term)) {
        continue;
      }
      results.add(table);
    }

    return results;
  }

  public static File getVpsDbFile() {
    File folder = new File("./resources");
    if (!folder.exists()) {
      folder = new File("../resources");
    }
    return new File(folder, "vpsdb.json");
  }

  private static List<VpsTable> loadTables(InputStream in) {
    try {
      if (in == null) {
        in = new FileInputStream(getVpsDbFile());
      }

      VpsTable[] vpsTables = objectMapper.readValue(in, VpsTable[].class);
      return Arrays.stream(vpsTables)
        .filter(t -> t.getFeatures().contains(VpsFeatures.VPX))
        .collect(Collectors.toList());
    } catch (Exception e) {
      LOG.error("Failed to load VPS json: " + e.getMessage(), e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        //ignore
      }
    }
    return Collections.emptyList();
  }

  public List<VpsTableDiff> download() {
    List<VpsTableDiff> diff = new ArrayList<>();
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
        return diff;
      }

      LOG.info("Written " + getVpsDbFile().getAbsolutePath());


      VPS newInstance = loadInstance(null);
      diff.addAll(newInstance.diff(this));
      LOG.info("VPS updated with " + diff.size() + " updates.");
      VPS.instance = newInstance;

      if (!diff.isEmpty()) {
        LOG.info("VPS download detected " + diff.size() + " changes, notifiying listeners...");
        for (VpsChangeListener listener : listeners) {
          listener.vpsSheetChanged(diff);
        }
      }
    } catch (IOException e) {
      LOG.error("VPS download failed: " + e.getMessage());
    }
    return diff;
  }

  public Date getChangeDate() {
    return new Date(getVpsDbFile().lastModified());
  }

  public void reload() {
    this.tables = loadTables(null);
  }

  public List<VpsTableDiff> diff(VPS old) {
    return diff(old, Collections.emptyList());
  }

  public List<VpsTableDiff> diff(VPS old, List<String> filteredTableIds) {
    List<VpsTableDiff> diff = new ArrayList<>();
    List<VpsTable> selectedTables = this.tables;
    if (!filteredTableIds.isEmpty()) {
      selectedTables = this.tables.stream().filter(t -> filteredTableIds.contains(t.getId())).collect(Collectors.toList());
    }

    for (VpsTable table : selectedTables) {
      VpsTable oldTable = old.getTableById(table.getId());
      if (oldTable != null && table.getUpdatedAt() != oldTable.getUpdatedAt()) {
        VpsTableDiff tableDiff = new VpsTableDiff(table, oldTable);
        if (!tableDiff.getDifferences().isEmpty()) {
          diff.add(tableDiff);
        }
      }
    }

    Collections.sort(diff, Comparator.comparing(VpsTableDiff::getDisplayName));
    return diff;
  }
}

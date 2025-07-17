package de.mephisto.vpin.connectors.vps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class VPS {
  private final static Logger LOG = LoggerFactory.getLogger(VPS.class);

  public final static String URL = "https://virtualpinballspreadsheet.github.io/vps-db/db/vpsdb.json";
  public final static String BASE_URL = "https://virtualpinballspreadsheet.github.io";

  private static final ObjectMapper objectMapper;

  private final static boolean skipUpdates = false;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private final List<VpsSheetChangedListener> listeners = new ArrayList<>();

  private List<VpsTable> tables = new ArrayList<>();

  public void addChangeListener(VpsSheetChangedListener listener) {
    this.listeners.add(listener);
  }

  //----------------- 

  public static String getVpsTableUrl(String tableId, String versionId) {
    String url = getVpsTableUrl(tableId);
    if (versionId != null) {
      url += "#" + versionId;
    }
    return url;
  }

  public static String getVpsTableUrl(String tableId) {
    return BASE_URL + "/?game=" + tableId + "&fileType=table";
  }

  public static boolean isVpsTableUrl(String url) {
    return url.startsWith(BASE_URL);
  }

  public static String getVpsBaseUrl() {
    return BASE_URL;
  }

  //----------------- 

  public VPS() {
    File vpsDbFile = this.getVpsDbFile();
    if (!vpsDbFile.getParentFile().exists()) {
      vpsDbFile.getParentFile().mkdirs();
    }
  }

  public VpsTable getTableById(String id) {
    if (this.tables != null) {
      return this.tables.stream().filter(t -> t.getId() != null && t.getId().equals(id)).findFirst().orElse(null);
    }
    return null;
  }

  public List<VpsTable> getTables() {
    return tables;
  }

  //----------------- FINDERS

  public List<VpsTable> find(String searchTerm) {
    return find(searchTerm, null);
  }

  /**
   * The ROM is only used for the asset search in the asset dialog.
   *
   * @param searchTerm
   * @param rom
   * @return
   */
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

    List<VpsTable> results = findInternal(term, rom);

    while (results.isEmpty()) {
      if (term.contains(" ")) {
        term = term.substring(0, term.lastIndexOf(" "));
      }
      else {
        break;
      }
      results = findInternal(term, rom);
    }

    return results;
  }

  private List<VpsTable> findInternal(String term, String rom) {
    List<VpsTable> results = new ArrayList<>();
    for (VpsTable table : this.tables) {
      List<VpsAuthoredUrls> romFiles = table.getRomFiles();
      if (romFiles != null && rom != null) {
        for (VpsAuthoredUrls romFile : romFiles) {
          if (romFile.getVersion() != null && romFile.getVersion().equalsIgnoreCase(rom)) {
            results.add(table);
            return results;
          }
        }
      }

      String name = table.getName().toLowerCase();
      name = name.replaceAll("-", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("&", "and");
      if (!name.contains(term)) {
        continue;
      }
      results.add(table);
    }

    return results;
  }

  //----------------- LOADERS

  private File getVpsDbFile() {
    File folder = new File("./resources");
    if (!folder.exists()) {
      folder = new File("../resources");
    }
    return new File(folder, "vpsdb.json");
  }

  public void loadTables(InputStream in) {
    try {
      VpsTable[] vpsTables = objectMapper.readValue(in, VpsTable[].class);
      this.tables = Arrays.asList(vpsTables);
//          .filter(t -> t.getFeatures() == null || t.getFeatures().isEmpty() || t.getFeatures().contains(VpsFeatures.VPX) || !t.getFeatures().contains(VpsFeatures.FP))
//          .collect(Collectors.toList());
      LOG.info(this.tables.size() + " Tables loaded from database");
    }
    catch (Exception e) {
      LOG.error("Failed to load VPS json: " + e.getMessage(), e);
      this.tables = Collections.emptyList();
    }
  }

  public List<VpsDiffer> update() {
    return this.update(Collections.emptyList());
  }

  public List<VpsDiffer> update(List<String> authorDenyList) {
    if (skipUpdates) {
      LOG.warn("VPS updates are skipped.");
      return Collections.emptyList();
    }

    File vpsDbFile = getVpsDbFile();

    try {
      LOG.info("Downloading " + VPS.URL);
      java.net.URL url = new URL(VPS.URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File tmp = new File(vpsDbFile.getParentFile(), vpsDbFile.getName() + ".tmp");
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

      long oldSize = 0;
      if (vpsDbFile.exists()) {
        oldSize = vpsDbFile.length();
      }

      if (vpsDbFile.exists() && !vpsDbFile.delete()) {
        LOG.error("Failed to delete vpsdb.json");
      }

      if (!tmp.renameTo(vpsDbFile)) {
        LOG.error("Failed to rename vpsdb.json");
        return Collections.emptyList();
      }

      LOG.info("Written " + vpsDbFile.getAbsolutePath() + ", (" + oldSize + " vs " + vpsDbFile.length() + " bytes)");
    }
    catch (IOException e) {
      LOG.error("VPS download failed: " + e.getMessage());
    }

    // save old tables
    List<VpsTable> oldTables = this.tables;

    if (reload() && !oldTables.isEmpty() && !this.tables.isEmpty()) {
      List<VpsDiffer> diffs = this.diff(oldTables, this.tables, authorDenyList);
      if (!diffs.isEmpty()) {
        LOG.info("VPS download detected " + diffs.size() + " changes, notifying " + listeners.size() + " listeners...");
        for (VpsSheetChangedListener listener : listeners) {
          listener.vpsSheetChanged(diffs);
          LOG.info("Notified VPS change listener \"" + listener.getClass().getName() + "\"");
        }
      }
      else {
        LOG.info("VPS had no changes, skipped update listeners.");
      }
      return diffs;
    }
    return Collections.emptyList();
  }

  public Date getChangeDate() {
    return new Date(getVpsDbFile().lastModified());
  }

  public boolean reload() {
    File vpsDbFile = getVpsDbFile();
    if (vpsDbFile.exists()) {
      try (InputStream fin = new FileInputStream(vpsDbFile)) {
        loadTables(fin);
        return true;
      }
      catch (IOException e) {
        LOG.error("Failed to reload VPS file: " + e.getMessage(), e);
      }
    }
    return false;
  }

  //----------------- DIFFS

  public List<VpsDiffer> diff(List<VpsTable> oldTables, List<VpsTable> newTables, List<String> authorDenyList) {
    if (oldTables == null || newTables == null) {
      LOG.info("Skipping VPS diff, because lists are empty.");
      return Collections.emptyList();
    }

    LOG.info("Differing " + oldTables.size() + " old tables against " + newTables.size() + " new tables.");
    List<VpsDiffer> diff = new ArrayList<>();
    for (VpsTable newTable : newTables) {
      Optional<VpsTable> oldTable = oldTables.stream().filter(t -> t.getId() != null && t.getId().equalsIgnoreCase(newTable.getId())).findFirst();
      VpsDiffer tableDiff = new VpsDiffer(newTable, oldTable.orElse(null), authorDenyList);
      VPSChanges changes = tableDiff.getChanges();
      if (!changes.isEmpty()) {
        LOG.info("Updates for \"" + newTable.getDisplayName() + "\": " + changes.getChanges().stream().map(c -> c.getDiffType().toString()).collect(Collectors.joining(", ")));
        diff.add(tableDiff);
      }
    }

    Collections.sort(diff, Comparator.comparing(VpsDiffer::getDisplayName));
    return diff;
  }
}

package de.mephisto.vpin.connectors.vps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VPS {
  private final static Logger LOG = LoggerFactory.getLogger(VPS.class);

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
    this.tables = loadTables();
  }

  public List<VpsTable> getTables() {
    return tables;
  }

  public List<VpsTable> find(String term) {
    term = term.replaceAll("_", " ");
    term = term.replaceAll("'", " ");
    term = term.replaceAll("\\.", " ");
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
      if (!name.contains(term)) {
        continue;
      }
      results.add(table);
    }

    return results;
  }


  private List<VpsTable> loadTables() {
    try {
      VpsTable[] vpsTables = objectMapper.readValue(new File("E:\\Development\\workspace\\vpin-studio\\resources\\", "vpsdb.json"), VpsTable[].class);
      return Arrays.stream(vpsTables)
          .filter(t -> t.getFeatures().contains(VpsFeatures.VPX))
          .collect(Collectors.toList());
    } catch (Exception e) {
      LOG.error("Failed to load VPS json: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }
}

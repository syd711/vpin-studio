package de.mephisto.vpin.connectors.vps.matcher;

import de.mephisto.vpin.connectors.vps.model.VpsTable;

import java.util.ArrayList;
import java.util.List;

public class LegacyTableMatcher {
  private final List<VpsTable> tables;

  public LegacyTableMatcher(List<VpsTable> tables) {
    this.tables = tables;
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
}

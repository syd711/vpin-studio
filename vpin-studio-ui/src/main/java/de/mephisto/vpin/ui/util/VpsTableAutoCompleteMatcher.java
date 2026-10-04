package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.apache.commons.lang3.Strings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matches VPS tables by display name and returns the VPS table id as the model id,
 * so that tables sharing the same display name stay individually selectable.
 */
public class VpsTableAutoCompleteMatcher implements AutoCompleteMatcher {

  private final List<AutoMatchModel> models;

  public VpsTableAutoCompleteMatcher(List<VpsTable> tables) {
    Set<String> seen = new HashSet<>();
    Set<String> duplicates = new HashSet<>();
    for (VpsTable table : tables) {
      if (!seen.add(table.getDisplayName())) {
        duplicates.add(table.getDisplayName());
      }
    }

    this.models = tables.stream().map(t -> {
      String label = t.getDisplayName();
      if (duplicates.contains(label)) {
        label = label + " [" + t.getId() + "]";
      }
      return new AutoMatchModel(label, t.getId());
    }).collect(Collectors.toList());
  }

  @Override
  public List<AutoMatchModel> match(String input) {
    return models.stream()
        .filter(m -> Strings.CI.contains(m.getDisplayName(), input))
        .collect(Collectors.toList());
  }
}

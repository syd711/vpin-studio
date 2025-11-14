package de.mephisto.vpin.ui.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultAutoCompleteMatcher implements AutoCompleteMatcher {

  private List<String> entries = new ArrayList<>();

  public DefaultAutoCompleteMatcher(List<String> entries) {
    this.entries = entries;
  }

  public List<AutoMatchModel> match(String input) {
    return entries.stream()
        .filter(e -> StringUtils.containsIgnoreCase(e, input))
        .map(e -> new AutoMatchModel(e, e))
        .collect(Collectors.toList());
  }

  public void setEntries(List<String> entries) {
    this.entries = entries;
  }
}

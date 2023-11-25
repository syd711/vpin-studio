package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.List;

public class ComponentSummary {
  private ComponentType type;
  private List<ComponentSummaryEntry> entries = new ArrayList<>();
  private String error;

  public void setType(ComponentType type) {
    this.type = type;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public void addEntry(String key, String value) {
    addEntry(key, value, true, null, null);
  }

  public void addEntry(String key, String value, boolean valid, String status) {
    addEntry(key, value, valid, status, null);
  }

  public void addEntry(String key, String value, boolean valid, String status, String description) {
    ComponentSummaryEntry entry = new ComponentSummaryEntry();
    entry.setName(key);
    entry.setStatus(status);
    entry.setValid(valid);
    entry.setValue(value);
    entry.setDescription(description);
    entries.add(entry);
  }

  public ComponentType getType() {
    return type;
  }

  public List<ComponentSummaryEntry> getEntries() {
    return entries;
  }
}

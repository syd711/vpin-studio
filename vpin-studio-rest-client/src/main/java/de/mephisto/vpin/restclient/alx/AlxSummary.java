package de.mephisto.vpin.restclient.alx;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlxSummary {
  private OffsetDateTime startDate;
  private List<TableAlxEntry> entries = new ArrayList<>();

  public OffsetDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(OffsetDateTime startDate) {
    this.startDate = startDate;
  }

  public List<TableAlxEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<TableAlxEntry> entries) {
    this.entries = entries;
  }
}

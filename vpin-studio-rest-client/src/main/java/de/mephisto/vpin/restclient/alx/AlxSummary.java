package de.mephisto.vpin.restclient.alx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlxSummary {
  private Date startDate;
  private List<TableAlxEntry> entries = new ArrayList<>();

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public List<TableAlxEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<TableAlxEntry> entries) {
    this.entries = entries;
  }
}

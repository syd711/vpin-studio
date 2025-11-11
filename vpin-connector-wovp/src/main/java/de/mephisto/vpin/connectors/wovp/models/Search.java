package de.mephisto.vpin.connectors.wovp.models;

import java.util.Arrays;
import java.util.List;

/**
 *             "cultureCode":"en",
 *             "filters":{
 *                 "statuses":[1],
 *                 "inProgress":true
 *             },
 *             "expands":["challenge.scoreboard","challenge.pinballTable.minimum"]
 */
public class Search {
  private String cultureCode = "en";
  private List<String> expands = Arrays.asList("challenge.scoreboard", "challenge.pinballTableVersion.minimum", "challenge.pinballTable.minimum");
  private Filters filters = new Filters();

  public String getCultureCode() {
    return cultureCode;
  }

  public void setCultureCode(String cultureCode) {
    this.cultureCode = cultureCode;
  }

  public List<String> getExpands() {
    return expands;
  }

  public void setExpands(List<String> expands) {
    this.expands = expands;
  }

  public Filters getFilters() {
    return filters;
  }

  public void setFilters(Filters filters) {
    this.filters = filters;
  }
}

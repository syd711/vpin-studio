package de.mephisto.vpin.restclient.representations.vpx;

import java.util.Map;

/**
 * // >    "/TableInfo/AuthorName",
 * // >    "/TableInfo/Screenshot",
 * // >    "/TableInfo/TableBlurb",
 * // >    "/TableInfo/TableRules",
 * // >    "/TableInfo/AuthorEmail",
 * // >    "/TableInfo/ReleaseDate",
 */
public class TableInfo {
  private String tableName;
  private String authorWebSite;
  private String authorName;
  private String tableBlurb;
  private String tableRules;
  private String tableVersion;
  private String authorEmail;
  private String releaseDate;
  private String tableDescription;

  public TableInfo() {

  }

  public TableInfo(Map<String, String> values) {
    this.tableName = values.get("TableName");
    this.authorWebSite = values.get("AuthorWebSite");
    this.authorName = values.get("AuthorName");
    this.tableBlurb = values.get("TableBlurb");
    this.tableRules = values.get("TableRules");
    this.tableVersion = values.get("TableVersion");
    this.authorEmail = values.get("AuthorEmail");
    this.releaseDate = values.get("ReleaseDate");
    this.tableDescription = values.get("TableDescription");
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getAuthorWebSite() {
    return authorWebSite;
  }

  public void setAuthorWebSite(String authorWebSite) {
    this.authorWebSite = authorWebSite;
  }

  public String getAuthorName() {
    return authorName;
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  public String getTableBlurb() {
    return tableBlurb;
  }

  public void setTableBlurb(String tableBlurb) {
    this.tableBlurb = tableBlurb;
  }

  public String getTableRules() {
    return tableRules;
  }

  public void setTableRules(String tableRules) {
    this.tableRules = tableRules;
  }

  public String getTableVersion() {
    return tableVersion;
  }

  public void setTableVersion(String tableVersion) {
    this.tableVersion = tableVersion;
  }

  public String getAuthorEmail() {
    return authorEmail;
  }

  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }

  public String getTableDescription() {
    return tableDescription;
  }

  public void setTableDescription(String tableDescription) {
    this.tableDescription = tableDescription;
  }
}

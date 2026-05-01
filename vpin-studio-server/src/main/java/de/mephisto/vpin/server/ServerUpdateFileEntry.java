package de.mephisto.vpin.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a single file entry in the sync manifest.
 *
 * <p>A manifest entry describes a remote file to be synchronized locally.
 * Validation is performed against either {@code length} or {@code crc32},
 * whichever is present. If both are provided, both must match.
 *
 * <p>Example JSON entry:
 * <pre>{@code
 * {
 *   "name": "config.properties",
 *   "url": "https://example.com/files/config.properties",
 *   "destination": "resources/config.properties",
 *   "length": 2048,
 *   "crc32": "1A2B3C4D"
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerUpdateFileEntry {

  /** 
   * Display name of the file 
   */
  private String name;

  /**
   * For zip file: "archiveFolder/files" are extracted as "files"
   */
  private String archiveFolder;

  /** 
   * Full URL from which to download the file. 
   */
  private String url;

  /**
   * Relative path (including filename) where the file should be installed
   * under the local base directory. Uses forward slashes.
   * Example: {@code "conf/config.properties"}
   */
  private String destination;

  /**
   * Expected file size in bytes.
   * May be {@code null} if only CRC validation is used.
   */
  private Long length;

  /**
   * Expected CRC-32 checksum as an 8-character uppercase hex string (e.g. {@code "1A2B3C4D"}).
   * May be {@code null} if only length validation is used.
   */
  private String crc32;

  /**
   * Whether the file must be deleted instead of downloaded
   */
  private boolean delete;

    /**
   * Whether the parent folder must be empty first
   */
  private boolean emptyParentFolder;

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  public ServerUpdateFileEntry() {}

  public ServerUpdateFileEntry(String name, String url, String destination, Long length, String crc32) {
      this.name = name;
      this.url = url;
      this.destination = destination;
      this.length = length;
      this.crc32 = crc32;
  }

  // -------------------------------------------------------------------------
  // Accessors
  // -------------------------------------------------------------------------

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getArchiveFolder() {
    return archiveFolder;
  }

  public void setArchiveFolder(String archiveFolder) {
    this.archiveFolder = archiveFolder;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public Long getLength() {
    return length;
  }

  public void setLength(Long length) {
    this.length = length;
  }

  public String getCrc32() {
    return crc32;
  }

  public void setCrc32(String crc32) {
    this.crc32 = (crc32 == null) ? null : crc32.toUpperCase();
  }

  public boolean isDelete() {
    return delete;
  }

  public void setDelete(boolean delete) {
    this.delete = delete;
  }

  public boolean isEmptyParentFolder() {
    return emptyParentFolder;
  }

  public void setEmptyParentFolder(boolean emptyParentFolder) {
    this.emptyParentFolder = emptyParentFolder;
  }
  
  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /** 
   * Returns {@code true} if this entry carries at least one validation criterion. 
   */
  public boolean hasValidation() {
    return length != null || crc32 != null;
  }

  @Override
  public String toString() {
    return "FileSyncEntry{name='" + name + "', destination='" + destination
          + "', length=" + length + ", crc32='" + crc32 + "'}";
  }
}
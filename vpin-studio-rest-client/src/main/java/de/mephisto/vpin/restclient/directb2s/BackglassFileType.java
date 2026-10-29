package de.mephisto.vpin.restclient.directb2s;

import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.Nullable;

/**
 * The supported backglass file formats.
 * A .B2SPro file is a superset of the .directb2s format: same XML structure (root DirectB2SData),
 * but with additional elements and attributes. Both are handled the same way.
 * The order of the constants is the lookup priority: when both files exist for a table, the .B2SPro one is used.
 */
public enum BackglassFileType {
  B2SPRO("B2SPro"),
  DIRECTB2S("directb2s");

  private final String extension;

  BackglassFileType(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  /**
   * @return the extension with the leading dot, e.g. ".directb2s"
   */
  public String getSuffix() {
    return "." + extension;
  }

  @Nullable
  public static BackglassFileType fromExtension(@Nullable String extension) {
    if (extension != null) {
      for (BackglassFileType value : values()) {
        if (value.extension.equalsIgnoreCase(extension)) {
          return value;
        }
      }
    }
    return null;
  }

  @Nullable
  public static BackglassFileType fromFileName(@Nullable String fileName) {
    return fileName != null ? fromExtension(FilenameUtils.getExtension(fileName)) : null;
  }

  public static boolean isBackglassExtension(@Nullable String extension) {
    return fromExtension(extension) != null;
  }

  public static boolean isBackglassFile(@Nullable String fileName) {
    return fromFileName(fileName) != null;
  }

  public static boolean isB2SPro(@Nullable String fileName) {
    return B2SPRO.equals(fromFileName(fileName));
  }

  /**
   * @return all backglass extensions, without leading dot
   */
  public static String[] extensions() {
    BackglassFileType[] values = values();
    String[] result = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = values[i].extension;
    }
    return result;
  }

  /**
   * @return all backglass extensions as file chooser patterns, e.g. "*.directb2s"
   */
  public static String[] patterns() {
    String[] extensions = extensions();
    for (int i = 0; i < extensions.length; i++) {
      extensions[i] = "*." + extensions[i];
    }
    return extensions;
  }
}
